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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
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
import fede.workspace.eclipse.content.FolderContentManager;
import fede.workspace.eclipse.java.JavaProjectManager;
import fede.workspace.tool.eclipse.EclipseTool;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.impl.var.NullVariable;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.Variable;

/**
 * The Class JavaSourceFolderContentManager.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JavaSourceFolderContentManager extends FolderContentManager implements IJavaItemManager {

	/** The output. */
	Variable	output;

	/**
	 * Instantiates a new java source folder content manager.
	 * 
	 * @param parent
	 *            the parent
	 * @param item
	 *            the item
	 * @param path
	 *            the path
	 * @param output
	 *            the output
	 */
	public JavaSourceFolderContentManager(CompactUUID id, Variable path, Variable output) {
		super(id, path);
		this.output = output;

	}

	/**
	 * Instantiates a new java source folder content manager.
	 * 
	 * @param parent
	 *            the parent
	 * @param item
	 *            the item
	 */
	public JavaSourceFolderContentManager(CompactUUID id) {
		super(id, JavaProjectManager.DEFAULT_SOURCES_FOLDER_NAME);
		this.output = NullVariable.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.FolderContentManager#delete()
	 */
	@Override
	public void delete() throws CadseException {
		super.delete();
		try {
			JavaProjectManager.deleteJavaSourceFolder(getItem(), getFolder(), EclipseTool.getDefaultMonitor());
		} catch (CoreException e) {
			throw new CadseException("Cannot delete java source folder from {0} : {1}", e, getItem().getName(), e
					.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.FolderContentManager#create()
	 */
	@Override
	public void create() throws CadseException {
		super.create();
		try {
			JavaProjectManager.createJavaSourceFolder(getItem(), getFolder(),
					getSpecificOutputFolder(ContextVariable.DEFAULT), EclipseTool.getDefaultMonitor());
		} catch (CoreException e) {
			throw new CadseException("Cannot create java source folder from {0} : {1}", e, getItem().getName(), e
					.getMessage());
		}
	}

	/**
	 * Gets the specific output folder.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the specific output folder
	 */
	protected IFolder getSpecificOutputFolder(ContextVariable cxt) {
		return getParentContainer(cxt).getFolder(new Path(output.compute(cxt, getItem())));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getJavaElement()
	 */
	public IJavaElement[] getJavaElement(IJavaProject jpRef) {
		IJavaElement je = JavaCore.create(getFolder());
		if (je != null) {
			return new IJavaElement[] { je };
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.FolderContentManager#getResources(java.lang.String)
	 */
	@Override
	public Object[] getResources(String kind) {
		if ("java-source".equals(kind)) {
			return new Object[] { JavaCore.create(getFolder()) };
		}
		return super.getResources(kind);
	}

	/**
	 * Gets the java source element.
	 * 
	 * @return the java source element
	 */
	public IPackageFragmentRoot getJavaSourceElement() {
		return (IPackageFragmentRoot) JavaCore.create(getFolder());
	}

	/**
	 * Gets the java source element.
	 * 
	 * @return the java source element
	 */
	public IPackageFragmentRoot getJavaSourceElement(ContextVariable cxt) {
		return (IPackageFragmentRoot) JavaCore.create(getFolder(cxt));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.FolderContentManager#getContentProvider()
	 */
	@Override
	public ITreeContentProvider getContentProvider() {
		return new StandardJavaElementContentProvider() {
			@Override
			public Object[] getElements(Object parent) {
				if (parent == JavaSourceFolderContentManager.this) {
					return new Object[] { getJavaSourceElement() };
				}
				return super.getElements(parent);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.FolderContentManager#getLabelProvider()
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
	public IPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor)
			throws JavaModelException {
		IPackageFragmentRoot pfr = getJavaSourceElement();
		return pfr.createPackageFragment(name, force, monitor);
	}

	/**
	 * Creates the package fragment.
	 * 
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
		return pfr.createPackageFragment(name, force, monitor);
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
