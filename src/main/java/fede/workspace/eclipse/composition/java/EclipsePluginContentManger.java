/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fede.workspace.eclipse.composition.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.codegen.jet.JETException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;

import fede.workspace.eclipse.java.JMergeUtil;
import fede.workspace.eclipse.java.JavaProjectManager;
import fede.workspace.eclipse.java.WSJavaPlugin;
import fede.workspace.eclipse.java.manager.JavaProjectContentManager;
import fede.workspace.eclipse.java.osgi.OsgiManifest;
import fede.workspace.tool.eclipse.MappingManager;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import java.util.UUID;

import fr.imag.adele.cadse.core.GenContext;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;
import fr.imag.adele.cadse.core.var.Variable;
import fr.imag.adele.fede.workspace.si.view.View;

/**
 * The Class EclipsePluginContentManger.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class EclipsePluginContentManger extends JavaProjectContentManager  {

	/** The Constant SOURCES. */
	private static final String	SOURCES	= "sources";

	/**
	 * Instantiates a new eclipse plugin content manger.
	 * 
	 * @param item
	 *            the item
	 * @param projectname
	 *            the projectname
	 * @param hassourcefolder
	 *            the hassourcefolder
	 */
	public EclipsePluginContentManger(UUID id, Variable projectname, boolean hassourcefolder) {
		super(id, projectname, hassourcefolder);
	}

	/**
	 * Instantiates a new eclipse plugin content manger.
	 * 
	 * @param item
	 *            the item
	 * @param projectname
	 *            the projectname
	 * @param sourcefolder
	 *            the sourcefolder
	 * @param classFolder
	 *            the class folder
	 */
	public EclipsePluginContentManger(UUID id, Variable projectname, Variable sourcefolder, Variable classFolder) {
		super(id, projectname, sourcefolder, classFolder);
	}

	/**
	 * Instantiates a new eclipse plugin content manger.
	 * 
	 * @param item
	 *            the item
	 * @param projectname
	 *            the projectname
	 * @param sourcefolder
	 *            the sourcefolder
	 */
	public EclipsePluginContentManger(UUID id, Variable projectname, Variable sourcefolder) {
		super(id, projectname, sourcefolder);
	}

	/**
	 * The Class PDEGenerateModel.
	 */
	static public class PDEGenerateModel  {

		/** The qualified activator name. */
		public String	qualifiedActivatorName;

		/** The is lazy start. */
		public boolean	isLazyStart;

		/** The package name. */
		public String	packageName;

		/** The activator name. */
		public String	activatorName;

		/** The plugin id. */
		public String	pluginID;

		/** The source name. */
		public String	sourceName;

		/** The imports packages. */
		public String[]	importsPackages;

		/** The exports packages. */
		public String[]	exportsPackages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.java.manager.JavaProjectContentManager#create()
	 */
	@Override
	public void create() throws CadseException {
		super.create();
		try {
			IProgressMonitor monitor = View.getDefaultMonitor();

			PDEGenerateModel info = (PDEGenerateModel) getGenerateModel();

			createPDEProject(info, monitor);

		} catch (CoreException e) {
			throw new CadseException("Cannot create pde project from {0} : {1}", e, getOwnerItem().getName(), e.getMessage());
		}
	}

	public List<IPDEContributor> getPdeContributors(ContextVariable cxt) {
		List<IPDEContributor> pdeContributor = new ArrayList<IPDEContributor>();
		CadseCore.adapts(getOwnerItem(), cxt, IPDEContributor.class, pdeContributor, null);
		return pdeContributor;
	}
	/**
	 * Compute model.
	 * 
	 * @param model
	 *            the model
	 */
	protected void computeModel(PDEGenerateModel model, List<IPDEContributor> pdeContributor) {
		model.activatorName = hasActivator()? "Activator" : null;
		model.packageName = getDefaultPackage();
		model.qualifiedActivatorName = getDefaultPackage() + "." + model.activatorName;
		model.isLazyStart = false;
		model.pluginID = getOwnerItem().getQualifiedName();
		model.sourceName = SOURCES;
		model.importsPackages = computeManifestImports(pdeContributor);
		model.exportsPackages = computeManifestExports(pdeContributor);

	}

	protected boolean hasActivator() {
		return true;
	}

	/**
	 * Gets the default package.
	 * 
	 * @return the default package
	 */
	protected String getDefaultPackage() {
		return getOwnerItem().getQualifiedName();
	}

	/**
	 * Generate activator.
	 * 
	 * @param fProject
	 *            the f project
	 * @param info
	 *            the info
	 * @param content
	 *            the content
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i file
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public IFile generateActivator(IProject fProject, PDEGenerateModel info, IProgressMonitor monitor)
			throws CoreException {

		ActivatorTemplate at = new ActivatorTemplate();
		String activatorContent = at.generate(info);
		
		IPath path;
		if (info.sourceName != null) {
			path = new Path(info.sourceName).append(info.packageName.replace('.', '/')).append(
					info.activatorName + ".java");
		} else {
			path = new Path(info.packageName.replace('.', '/')).append(info.activatorName + ".java");
		}

		return generateJava(fProject.getFile(path), activatorContent, monitor);
	}

	/**
	 * Generate java.
	 * 
	 * @param fProject
	 *            the f project
	 * @param path
	 *            the path
	 * @param fileName
	 *            the file name
	 * @param content
	 *            the content
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i file
	 * 
	 * @throws CoreException
	 *             the core exception
	 * 
	 * @see #generateJava(IFile, String, IProgressMonitor)
	 */
	@Deprecated
	public static IFile generateJava(IProject fProject, IPath path, String fileName, String content,
			IProgressMonitor monitor) throws CoreException {
		IFile file = null;
		if (path == null) {
			file = fProject.getFile(fileName);
		} else {
			file = fProject.getFile(path.append(fileName));
		}
		return generateJava(file, content, monitor);
	}

	/**
	 * Generate java.
	 * 
	 * @param file
	 *            the file
	 * @param content
	 *            the content
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i file
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IFile generateJava(IFile file, String content, IProgressMonitor monitor) throws CoreException {
		MappingManager.createParentContainerFolder(file.getParent(), monitor);
		try {
			JMergeUtil.merge(monitor, file, content);
			return file;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, WSJavaPlugin.PLUGIN_ID, 0, e.getMessage(), e));
		}
	}

	/**
	 * Generate java.
	 * 
	 * @param file
	 *            the file
	 * @param content
	 *            the content
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i file
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IFile generateJava(IFile file, String content, Pattern[] replaces, String[] replaceString,
			IProgressMonitor monitor) throws CoreException {
		MappingManager.createParentContainerFolder(file.getParent(), monitor);
		try {
			JMergeUtil.merge(monitor, file, content, replaces, replaceString);
			return file;
		} catch (JETException e) {
			throw new CoreException(new Status(IStatus.ERROR, WSJavaPlugin.PLUGIN_ID, 0, e.getMessage(), e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, WSJavaPlugin.PLUGIN_ID, 0, e.getMessage(), e));
		}
	}

	/**
	 * Creates the pde project.
	 * 
	 * @param info
	 *            the info
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i project
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public IProject createPDEProject(PDEGenerateModel info, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		if (!project.exists()) {
			project.create(monitor);
			project.open(monitor);
		}
		if (!project.hasNature(PDE.PLUGIN_NATURE)) {
			MappingManager.addNatureToProject(project, PDE.PLUGIN_NATURE, monitor);
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			MappingManager.addNatureToProject(project, JavaCore.NATURE_ID, monitor);
		}

		// set classpath if project has a Java nature
		if (project.hasNature(JavaCore.NATURE_ID)) {
			IJavaProject javaProject = JavaCore.create(project);
			// 3.3 not find PDECore.CLASSPATH_CONTAINER_ID
			// public static final String CLASSPATH_CONTAINER_ID =
			// PDECore.PLUGIN_ID + ".requiredPlugins"; //$NON-NLS-1$
			String CLASSPATH_CONTAINER_ID = PDECore.PLUGIN_ID + ".requiredPlugins"; //$NON-NLS-1$
			IClasspathEntry newContainerEntry = JavaCore.newContainerEntry(new Path(CLASSPATH_CONTAINER_ID));
			JavaProjectManager.replaceProjectClasspath(newContainerEntry, javaProject, monitor);
		}

		
		if (hasActivator())
			generateActivator(project, info, monitor);

		if (hasGeneratedManifest())
			generateManifest(info, monitor);
		generatePluginXml(info, project, monitor);
		return project;
	}

	/**
	 * Compute extension.
	 * 
	 * @param item
	 *            the item
	 * @param pluginBase
	 *            the plugin base
	 * @param workspacePluginModel
	 *            the workspace plugin model
	 */
	protected void computeExtension(Item item, IPluginBase pluginBase, WorkspacePluginModel workspacePluginModel) {
		IPDEContributor[] pdeContributor = item.getType().adapts(IPDEContributor.class);
		if (pdeContributor != null)
			for (IPDEContributor c : pdeContributor) {
				c.computeExtenstion(item, pluginBase, workspacePluginModel);
			}
		
		for (Link l : item.getOutgoingLinks()) {
			if (l.getLinkType().isPart() && l.isLinkResolved()) {
				computeExtension(l.getResolvedDestination(), pluginBase, workspacePluginModel);
			}
		}
	}

	/**
	 * Generate manifest.
	 * 
	 * @param info
	 *            the info
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public void generateManifest(PDEGenerateModel info, IProgressMonitor monitor) throws CoreException {

		IFile manifest = getManifestFile();

		try {
			OsgiManifest omf = new OsgiManifest(manifest, info);
			computeManifest(omf);
			StringBuilder sb = new StringBuilder();
			omf.write(sb);
			MappingManager.generate(manifest.getProject(), new Path("META-INF"), "MANIFEST.MF", sb.toString(), monitor);
			return;

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// si exception ancienne method.
		String content;
		IPDETemplate mt = createManifestTemplate();
		content = mt.generate(info);
		MappingManager.generate(manifest.getProject(), new Path("META-INF"), "MANIFEST.MF", content, monitor);
	}

	/**
	 * Gets the manifest file.
	 * 
	 * @return the manifest file
	 */
	public IFile getManifestFile() {
		IFile manifest = getProject().getFile(new Path("META-INF/MANIFEST.MF"));
		return manifest;
	}

	/**
	 * Compute manifest.
	 * 
	 * @param omf
	 *            the omf
	 */
	public void computeManifest(OsgiManifest omf) {
	}

	/**
	 * Generate plugin xml.
	 * 
	 * @param info
	 *            the info
	 * @param project
	 *            the project
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	protected void generatePluginXml(PDEGenerateModel info, IProject project, IProgressMonitor monitor)
			throws CoreException {
		String content;
		WorkspacePluginModel pluginModel = new WorkspacePluginModel(project.getFile("plugin.xml"), false);
		if (pluginModel.getUnderlyingResource().exists()) {
			pluginModel.load();
		}
		IPluginBase pluginBase = pluginModel.getPluginBase(true);
		computeExtension(getOwnerItem(), pluginBase, pluginModel);
		content = pluginModel.getContents();
		MappingManager.generate(project, null, "plugin.xml", content, monitor);
	}

	/**
	 * Creates the manifest template.
	 * 
	 * @return the iPDE template
	 */
	protected IPDETemplate createManifestTemplate() {
		ManifestTemplate mt = new ManifestTemplate();
		return mt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IGenerateContent#generate(fr.imag.adele.cadse.core.var.ContextVariable)
	 */
	synchronized public void generate(PDEGenerateModel state, ContextVariable cxt) {
		try {
			PDEGenerateModel info = state;

			IProgressMonitor monitor = View.getDefaultMonitor();
			IProject project = getProject();
			if (!project.exists()) {
				project.create(monitor);
				project.open(monitor);
			}
			if (hasActivator()) {
				generateActivator(project, info, monitor);
			}
			if (hasGeneratedManifest())
				generateManifest(info, monitor);
			generatePluginXml(info, project, monitor);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean hasGeneratedManifest() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IGenerateContent#getGenerateModel()
	 */
	public PDEGenerateModel getGenerateModel() {
		PDEGenerateModel info = new PDEGenerateModel();
		computeModel(info, getPdeContributors(ContextVariableImpl.DEFAULT));
		return info;
	}

	/**
	 * Compute manifest imports.
	 * @param pdeContributor 
	 * 
	 * @return the string[]
	 */
	protected String[] computeManifestImports(List<IPDEContributor> pdeContributor) {
		SortedSet<String> imports = new TreeSet<String>();
		for (IPDEContributor c : pdeContributor) {
			c.computeImportsPackage(getOwnerItem(), imports);
		}
		return imports.toArray(new String[imports.size()]);
	}

	

	/**
	 * Compute manifest exports.
	 * @param pdeContributor 
	 * 
	 * @return the string[]
	 */
	protected String[] computeManifestExports(List<IPDEContributor> pdeContributor) {
		HashSet<String> exports = new HashSet<String>();
		for (IPDEContributor c : pdeContributor) {
			c.computeExportsPackage(getOwnerItem(), exports);
		}
		return exports.toArray(new String[exports.size()]);
	}

	
}
