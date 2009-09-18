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

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import fede.workspace.dependencies.eclipse.java.IJavaItemManager;
import fede.workspace.eclipse.content.FolderContentManager;
import fede.workspace.eclipse.java.JavaProjectManager;
import fede.workspace.tool.eclipse.EclipseTool;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.delta.ImmutableItemDelta;
import fr.imag.adele.cadse.core.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.impl.var.ShortNameVariable;

/**
 * The Class JarContentManager.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JarContentManager extends FolderContentManager implements IJavaItemManager {

	/** The Constant JAR_RESOURCES_ATTRIBUTE. */
	public static final String	JAR_RESOURCES_ATTRIBUTE			= "jar-resource";

	/** The Constant JAR_SOURCE_RESOURCES_ATTRIBUTE. */
	public static final String	JAR_SOURCE_RESOURCES_ATTRIBUTE	= "jar-source-resource";

	/**
	 * Instantiates a new jar content manager.
	 * 
	 * @param parent
	 *            the parent
	 * @param item
	 *            the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public JarContentManager(ContentItem parent, Item item) throws CadseException {
		super(parent, item, new ShortNameVariable());
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
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getJavaElement()
	 */
	public IJavaElement[] getJavaElement(IJavaProject jpRef) {
		String jarpathStr = (String) getItem().getAttribute(JAR_RESOURCES_ATTRIBUTE);
		if (jarpathStr != null) {
			IFolder libFolder = getFolder();
			IFile jarFile = libFolder.getFile(new Path(jarpathStr));
			IJavaElement je = JavaCore.create(jarFile);
			if (je != null) {
				return new IJavaElement[] { je };
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#isTransitiveLink(fr.imag.adele.cadse.core.Link)
	 */
	public boolean isTransitiveLink(Link lk) {
		return false;
	}

	/**
	 * Change attribute.
	 * 
	 * @param item
	 *            the item
	 * @param wd
	 *            the wd
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public void changeAttribute(Item item, ImmutableWorkspaceDelta wd) throws CadseException {
		ImmutableItemDelta delta = wd.getItem(item);
		if (delta.isDeleted()) {
			return;
		}
		if (delta.hasSetAttributes()) {
			// TODO ERROR
			Object[] values = delta.getSetAttributes().get(JAR_RESOURCES_ATTRIBUTE);
			if (values != null) {
				try {
					JarContentManager jcm = (JarContentManager) item.getContentItem();
					IFolder libFolder = (IFolder) jcm.getMainResource();
					IPath libPath = libFolder.getFullPath();
					IJavaProject jp = JavaCore.create(libFolder.getProject());
					IProgressMonitor monitor = EclipseTool.getDefaultMonitor();
					if (values[0] == null) {
						if (values[1] != null) {
							// add
							IPath p = new Path((String) values[1]);
							p = libPath.append(p);
							IPath sourcePath = getSourcePath(item);

							JavaProjectManager.replaceProjectClasspath(JavaProjectManager.newLibraryEntry(p,
									sourcePath, true), jp, monitor);

						}
					} else {
						if (values[1] == null) {
							// remove
							IPath p = new Path((String) values[0]);
							p = libPath.append(p);
							JavaProjectManager.removeProjectClasspath(p, jp, monitor);
						} else { // replace = remove+ add
							// remove
							IPath p = new Path((String) values[0]);
							p = libPath.append(p);
							JavaProjectManager.removeProjectClasspath(p, jp, monitor);

							// add
							p = new Path((String) values[1]);
							p = libPath.append(p);
							IPath sourcePath = getSourcePath(item);

							JavaProjectManager.replaceProjectClasspath(JavaProjectManager.newLibraryEntry(p,
									sourcePath, true), jp, monitor);
						}
					}

				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			values = delta.getSetAttributes().get(JAR_SOURCE_RESOURCES_ATTRIBUTE);
			if (values != null && (values[0] != null || values[1] != null)) {
				String jarpathStr = (String) item.getAttribute(JAR_RESOURCES_ATTRIBUTE);
				if (jarpathStr != null) {
					try {
						JarContentManager jcm = (JarContentManager) item.getContentItem();
						IFolder libFolder = (IFolder) jcm.getMainResource();
						IPath libPath = libFolder.getFullPath();
						IJavaProject jp = JavaCore.create(libFolder.getProject());
						IProgressMonitor monitor = EclipseTool.getDefaultMonitor();

						// add
						IPath p = new Path(jarpathStr);
						p = libPath.append(p);
						IPath sourcePath = getSourcePath(item);

						JavaProjectManager.replaceProjectClasspath(JavaProjectManager.newLibraryEntry(p, sourcePath,
								true), jp, monitor);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Gets the source path.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @return the source path
	 * 
	 * @throws CoreException
	 *             the core exception
	 * @throws CadseException
	 *             the melusine exception
	 */
	private IPath getSourcePath(Item item) throws CoreException, CadseException {
		JarContentManager jcm = (JarContentManager) item.getContentItem();
		IFolder libFolder = (IFolder) jcm.getMainResource();
		String pathSrc = (String) item.getAttribute(JAR_SOURCE_RESOURCES_ATTRIBUTE);
		if (pathSrc == null) {
			return null;
		}
		IPath sourcePath = new Path(pathSrc);
		return libFolder.getFullPath().append(sourcePath);
	}

	/**
	 * Deleted item.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public void deletedItem(Item item) throws CadseException {
		String jarpathStr = (String) item.getAttribute(JAR_RESOURCES_ATTRIBUTE);
		if (jarpathStr != null) {
			try {
				JarContentManager jcm = (JarContentManager) item.getContentItem();
				IFolder libFolder = (IFolder) jcm.getMainResource();
				IPath libPath = libFolder.getFullPath();
				IJavaProject jp = JavaCore.create(libFolder.getProject());

				// add
				IPath p = new Path(jarpathStr);
				p = libPath.append(p);
				JavaProjectManager.removeProjectClasspath(p, jp, EclipseTool.getDefaultMonitor());

			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
}
