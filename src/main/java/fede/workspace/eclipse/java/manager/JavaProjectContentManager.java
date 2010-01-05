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

package fede.workspace.eclipse.java.manager;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.PlatformUI;

import fede.workspace.dependencies.eclipse.java.IJavaItemManager;
import fede.workspace.eclipse.MelusineProjectManager;
import fede.workspace.eclipse.content.ProjectContentManager;
import fede.workspace.eclipse.java.JavaProjectManager;
import fede.workspace.eclipse.java.WSJavaPlugin;
import fede.workspace.tool.eclipse.EclipseTool;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ChangeID;
import java.util.UUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.impl.var.NullVariable;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;
import fr.imag.adele.cadse.core.var.Variable;
import fr.imag.adele.fede.workspace.si.view.View;

/**
 * The Class JavaProjectContentManager.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JavaProjectContentManager extends ProjectContentManager implements IJavaItemManager {

	private final class JavaProjectWorkspaceListener extends WorkspaceListener {
		@Override
		public void workspaceChanged(ImmutableWorkspaceDelta delta) {
			ImmutableItemDelta itemDelta = delta.getItem(getOwnerItem());
			if (itemDelta != null
					&& (itemDelta.hasRemovedOutgoingLink() || itemDelta.hasUnresolvedOutgoingLink()
							|| itemDelta.hasAddedOutgoingLink() || itemDelta.hasResolvedOutgoingLink())) {
				try {
					JavaProjectManager.updateItemDependenciesClasspath(getOwnerItem(), EclipseTool.getDefaultMonitor());
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/** The sourcefolder. */
	private Variable						sourcefolder;

	/** The class folder. */
	private Variable						classFolder;

	protected JavaProjectWorkspaceListener	wsListener;

	/**
	 * Instantiates a new java project content manager.
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
	public JavaProjectContentManager(UUID id, Variable projectname, Variable sourcefolder, Variable classFolder) {
		super(id, projectname);
		assert sourcefolder != null;
		assert classFolder != null;
		assert (sourcefolder.isNull() && classFolder.isNull()) || (!sourcefolder.isNull() && !classFolder.isNull());
		assert classFolder != null;
		this.sourcefolder = sourcefolder;
		this.classFolder = classFolder;
	}

	/**
	 * Instantiates a new java project content manager.
	 * 
	 * @param item
	 *            the item
	 * @param projectname
	 *            the projectname
	 * @param sourcefolder
	 *            the sourcefolder
	 */
	public JavaProjectContentManager(UUID id, Variable projectname, Variable sourcefolder) {
		this(id, projectname, sourcefolder, sourcefolder.isNull() ? NullVariable.INSTANCE
				: JavaProjectManager.DEFAULT_OUTPUT_FOLDER_NAME);
	}

	/**
	 * Instantiates a new java project content manager.
	 * 
	 * @param item
	 *            the item
	 * @param projectname
	 *            the projectname
	 * @param hasDefaulSourceFolder
	 *            the has defaul source folder
	 */
	public JavaProjectContentManager(UUID id, Variable projectname, boolean hasDefaulSourceFolder) {
		this(id, projectname, hasDefaulSourceFolder ? JavaProjectManager.DEFAULT_SOURCES_FOLDER_NAME
				: NullVariable.INSTANCE, hasDefaulSourceFolder ? JavaProjectManager.DEFAULT_OUTPUT_FOLDER_NAME
				: NullVariable.INSTANCE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.ProjectContentManager#init()
	 */
	@Override
	public void init() throws CadseException {
		super.init();
		initUpdaterClaspathListener();
	}

	protected void initUpdaterClaspathListener() {
		wsListener = new JavaProjectWorkspaceListener();
		getOwnerItem().getLogicalWorkspace().addListener(
				wsListener,
				ChangeID.toFilter(ChangeID.CREATE_OUTGOING_LINK, ChangeID.DELETE_OUTGOING_LINK,
						ChangeID.UNRESOLVE_INCOMING_LINK));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.ProjectContentManager#create()
	 */
	@Override
	public void create() throws CadseException {

		try {
			super.create();
			IProject p = getProject();
			IProgressMonitor defaultMonitor = View.getDefaultMonitor();
			MelusineProjectManager.createAndOpenProject(p, defaultMonitor);
			if (getOwnerItem() != null) {
				View.setItemPersistenceID(p, getOwnerItem());
			}
			JavaProjectManager.createJavaProject(p, getOwnerItem(), defaultMonitor, ContextVariableImpl.DEFAULT, sourcefolder,
					classFolder);

			/*
			 * Add Melsuine nature and associated builders
			 * 
			 * WARNING: This has to be done after the Java project is created,
			 * so that the Melusine builders are added in the right builder
			 * order
			 */
			MelusineProjectManager.addMelusineProject(p, defaultMonitor);

		} catch (CoreException e) {
			System.err.println(e.getStatus().toString());
			e.printStackTrace();
			throw new CadseException(e);
		}
	}

	/**
	 * Gets the java project.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the java project
	 */
	public IJavaProject getJavaProject(ContextVariable cxt) {
		IProject project = getProject(cxt);
		return JavaCore.create(project);
	}

	/**
	 * Gets the java source element.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the java source element
	 */
	public IPackageFragmentRoot getJavaSourceElement(ContextVariable cxt) {
		if (!sourcefolder.isNull()) {

			IFolder source = getProject(cxt).getFolder(sourcefolder.compute(cxt, getOwnerItem()));
			IJavaProject jp = getJavaProject(cxt);
			if (jp != null) {
				return jp.getPackageFragmentRoot(source);
			}
		}
		return null;
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.ProjectContentManager#getMainMappingContent(fr.imag.adele.cadse.core.var.ContextVariable,
	 *      java.lang.Class)
	 */
	@Override
	public <T> T getMainMappingContent(ContextVariable cxt, Class<T> clazz) {
		if (IPackageFragmentRoot.class.isAssignableFrom(clazz)) {
			return (T) getJavaSourceElement(cxt);
		}
		if (IJavaProject.class.isAssignableFrom(clazz)) {
			return (T) getJavaProject(cxt);
		}
		return super.getMainMappingContent(cxt, clazz);
	}

	/**
	 * Creates the package.
	 * 
	 * @param cxt
	 *            the cxt
	 * @param packageName
	 *            the package name
	 * 
	 * @return the i package fragment
	 */
	public IPackageFragment createPackage(ContextVariable cxt, String packageName) {
		IPackageFragmentRoot source = getJavaSourceElement(cxt);
		if (source != null) {
			IProgressMonitor monitor = EclipseTool.getDefaultMonitor();
			try {
				return source.createPackageFragment(packageName, true, monitor);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.ProjectContentManager#delete()
	 */
	@Override
	public void delete() throws CadseException {
		getOwnerItem().getLogicalWorkspace().removeListener(wsListener);
		try {
			IProject p = (IProject) getMainResource();
			if (p.exists()) {
				p.delete(true, EclipseTool.getDefaultMonitor());
			}
		} catch (CoreException e) {
			throw new CadseException("Cannot delete java project from {0} : {1}", e, getOwnerItem().getId(), e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getJavaElement()
	 */
	public IJavaElement[] getJavaElement(IJavaProject jpRef) {
		return new IJavaElement[] { JavaCore.create((IProject) getMainResource()) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#isTransitiveLink(fr.imag.adele.cadse.core.Link)
	 */
	public boolean isTransitiveLink(Link lk) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.ProjectContentManager#getResources(java.lang.String)
	 */
	@Override
	public Object[] getResources(String kind) {
		if ("java-source".equals(kind)) {
			IPackageFragmentRoot s = getJavaSourceElement(ContextVariableImpl.DEFAULT);
			if (s != null) {
				return new Object[] { s };
			}
		}
		return super.getResources(kind);
	}

	/**
	 * Creates the java file.
	 * 
	 * @param cxt
	 *            the cxt
	 * @param packageName
	 *            the package name
	 * @param className
	 *            the class name
	 * @param contents
	 *            the contents
	 * 
	 * @return the i compilation unit
	 */
	public ICompilationUnit createJavaFile(ContextVariable cxt, String packageName, String className, String contents) {
		IProgressMonitor monitor = EclipseTool.getDefaultMonitor();
		IPackageFragment packageElement = createPackage(cxt, packageName);
		try {
			if (packageElement != null) {
				return packageElement.createCompilationUnit(className, contents, false, monitor);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.ProjectContentManager#getContentProvider()
	 */
	@Override
	public ITreeContentProvider getContentProvider() {
		return new StandardJavaElementContentProvider() {
			@Override
			public Object[] getElements(Object parent) {
				if (parent == JavaProjectContentManager.this) {
					return new Object[] { getJavaProject(ContextVariableImpl.DEFAULT) };
				}
				return super.getElements(parent);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.ProjectContentManager#getLabelProvider()
	 */
	@Override
	public ILabelProvider getLabelProvider() {
		return new DecoratingLabelProvider(new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_BASICS
				| JavaElementLabelProvider.SHOW_OVERLAY_ICONS | JavaElementLabelProvider.SHOW_SMALL_ICONS
				| JavaElementLabelProvider.SHOW_VARIABLE | JavaElementLabelProvider.SHOW_PARAMETERS), PlatformUI
				.getWorkbench().getDecoratorManager().getLabelDecorator());
	}

	/**
	 * Creates the package fragment.
	 * 
	 * @param cxt
	 *            the cxt
	 * @param name
	 *            the name
	 * @param force
	 *            the force
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i package fragment
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	public IPackageFragment createPackageFragment(ContextVariable cxt, String name, boolean force,
			IProgressMonitor monitor) throws JavaModelException {
		IPackageFragmentRoot pfr = getJavaSourceElement(cxt);
		if (pfr != null) {
			return pfr.createPackageFragment(name, force, monitor);
		}
		throw new IllegalArgumentException("Cannot create a package " + name + " in item " + getOwnerItem());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#addDependencyClasspathEntry(fr.imag.adele.cadse.core.Link,
	 *      fr.imag.adele.cadse.core.Item, fr.imag.adele.cadse.core.Item,
	 *      fede.workspace.dependencies.eclipse.java.IJavaItemManager.DependencyNature,
	 *      java.util.Set, org.eclipse.core.runtime.MultiStatus)
	 */
	public void addDependencyClasspathEntry(Link requirementLink, Item target, Item source, DependencyNature nature,
			Set<IClasspathEntry> classpath, MultiStatus ms) {
		try {
			IJavaProject targetProject = JavaProjectManager.getJavaProject(target);
			IJavaProject sourceProject = JavaProjectManager.getJavaProject(source);

			if (targetProject == null || targetProject.equals(sourceProject)) {
				WSJavaPlugin.getDefault().getLog().log(
						new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, 0, MessageFormat.format(
								"Item dependencies, classpath resolution error : "
										+ "required project is not present {0}.", target.getId()), null));
				return;
			}

			// add project dependency
			classpath.add(JavaProjectManager.newProjectEntry(targetProject.getProject().getFullPath()));
		} catch (Throwable e) {
			ms.add(new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getPackageName(fr.imag.adele.cadse.core.Item)
	 */
	public String getPackageName(Item packageItem) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getPackageVersion(fr.imag.adele.cadse.core.Item)
	 */
	public String getPackageVersion(Item packageItem) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getPackagesFromProject(fr.imag.adele.cadse.core.Item)
	 */
	public Set<Item> getPackagesFromProject(Item projectItem) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getProjectFromPackage(fr.imag.adele.cadse.core.Item)
	 */
	public Set<Item> getProjectFromPackage(Item packageItem) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#resolvePackage(fr.imag.adele.cadse.core.Link,
	 *      fr.imag.adele.cadse.core.Item, fr.imag.adele.cadse.core.Item,
	 *      fede.workspace.dependencies.eclipse.java.IJavaItemManager.DependencyNature,
	 *      java.util.Set, org.eclipse.core.runtime.MultiStatus)
	 */
	public void resolvePackage(Link requirementLink, Item target, Item source, DependencyNature nature,
			Set<Item> resolved, MultiStatus ms) {
	}
	
	
	
	
	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (type == CadseGCST.JAVA_PROJECT_CONTENT_MODEL_at_HAS_SOURCE_FOLDER_)
			return (T) new Boolean(!sourcefolder.isNull());
		return super.internalGetOwnerAttribute(type);
	}
	
}
