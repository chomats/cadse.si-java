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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.changes.MoveCompilationUnitChange;

import fede.workspace.dependencies.eclipse.java.IJavaItemManager;
import fede.workspace.eclipse.MelusineProjectManager;
import fede.workspace.eclipse.content.FileContentManager;
import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.util.Assert;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.Variable;
import fr.imag.adele.fede.workspace.si.view.View;

/**
 * The Class JavaFileContentManager.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JavaFileContentManager extends FileContentManager implements IJavaItemManager {

	/** The Constant CLASS_NAME_ATTRIBUTE. */
	public static final int	CLASS_NAME_ATTRIBUTE	= 1;

	/** The Constant PACKAGE_NAME_ATTRIBUTE. */
	public static final int	PACKAGE_NAME_ATTRIBUTE	= 2;

	/** The package name. */
	Variable				packageName;

	/** The class name. */
	Variable				className;

	/**
	 * Instantiates a new java file content manager.
	 * 
	 * @param parent
	 *            the parent
	 * @param item
	 *            the item
	 * @param packageName
	 *            the package name
	 * @param className
	 *            the class name
	 */
	public JavaFileContentManager(UUID id, Variable packageName, Variable className) {
		super(id, new ClassVariable(className), new PathFolderVariable(packageName));
		this.packageName = packageName;
		this.className = className;
	}

	/**
	 * Mapping eclipse...: Plusieur mapping par item. type create
	 * FILE_EXT/FOLDER_EXT/PROJECT/ PART_OF_FILE
	 * /MULTI_PART_OF_FILE/MULTI_FILE/AUTRE Extra type JAVA|AJ|PDE|RESOURCE
	 * 
	 * name pattern paramettres : qualified-id, short-id, qualified-parent-id,
	 * short-parent-id format example message format. cas complexe extends
	 * method. Dans le cas d'un FILE_EXT ou FOLDER_EXT Recuperer le container
	 * parent, donner un chiffre 0, 1, 2 pour savoir � partir de quels item
	 * prendre le container. Il faut s'appuyer sur la relation inverse de part
	 * pour descendre la chaine des parents...
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	@Override
	public void create() throws CadseException {
		super.create();
		IJavaElement je = JavaCore.create(getFile());
	}

	/**
	 * Gets the java project from.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @return the java project from
	 */
	protected IJavaProject getJavaProjectFrom(Item item) {
		Item parentItem = item.getPartParent();
		Assert.isNotNull(parentItem, "Cannot find the parent to compute the java project");

		IResource r = MelusineProjectManager.getResource(item);
		Assert.isNotNull(r, "Cannot find the parent to compute the java project");

		IProject p = r.getProject();
		Assert.isNotNull(p, "Cannot find the parent to compute the java project");

		IJavaProject jp = JavaCore.create(p);
		Assert.isNotNull(jp, "Cannot find the parent to compute the java project");

		return jp;
	}

	/**
	 * Default content.
	 * 
	 * @return the string
	 */
	protected String defaultContent() {
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
	 * @see fede.workspace.dependencies.eclipse.java.IJavaItemManager#getJavaElement()
	 */
	public IJavaElement[] getJavaElement(IJavaProject jpRef) {
		return new IJavaElement[] { JavaCore.create(getFile()) };
	}

	/**
	 * Gets the class name.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the class name
	 */
	public String getClassName(ContextVariable cxt) {
		return className.compute(cxt, getItem());
	}

	/**
	 * Gets the package name.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the package name
	 */
	public String getPackageName(ContextVariable cxt) {
		return packageName.compute(cxt, getItem());
	}

	/**
	 * Gets the qualified class name.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the qualified class name
	 */
	public String getQualifiedClassName(ContextVariable cxt) {
		return getPackageName(cxt) + "." + getClassName(cxt);
	}

	/**
	 * Gets the sourcefolder.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the sourcefolder
	 */
	public String getSourcefolder(ContextVariable cxt) {
		IPackageFragmentRoot pfr = getJavaSourceElement(cxt);
		if (pfr != null) {
			return pfr.getElementName();
		}
		return null;
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
		ContentItem parent = getParentContentManager();
		if (parent != null) {
			return parent.getMainMappingContent(cxt, IPackageFragmentRoot.class);
		}
		return null;
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
		ContentItem parent = getParentContentManager();
		if (parent instanceof JavaSourceFolderContentManager) {
			return ((JavaSourceFolderContentManager) parent).createPackageFragment(cxt, name, force, monitor);
		}
		if (parent instanceof JavaProjectContentManager) {
			return ((JavaProjectContentManager) parent).createPackageFragment(cxt, name, force, monitor);
		}
		if (parent instanceof JavaPackageFolderContentManager) {
			return ((JavaPackageFolderContentManager) parent).createPackageFragment(cxt, name, force, monitor);
		}
		throw new IllegalArgumentException("Cannot create a package " + name + " in item " + getItem());
	}

	/**
	 * Change package.
	 * 
	 * @param newCxt
	 *            the new cxt
	 * @param oldCxt
	 *            the old cxt
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	public void changePackage(ContextVariable newCxt, ContextVariable oldCxt) throws JavaModelException {

		ICompilationUnit cu = getCompilationUnit(oldCxt);
		IProgressMonitor monitor = View.getDefaultMonitor();
		String _p = getPackageName(newCxt);

		IPackageFragment newPackage = createPackageFragment(newCxt, _p, true, monitor);
		MoveCompilationUnitChange mcuc = new MoveCompilationUnitChange(cu, newPackage);
		try {
			mcuc.perform(monitor);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Migrate package.
	 * 
	 * @param newCxt
	 *            the new cxt
	 * @param oldCxt
	 *            the old cxt
	 */
	public void migratePackage(ItemDelta ownerItem, final ContextVariable newCxt, final ContextVariable oldCxt) {
		final String newpackageName = getPackageName(newCxt);
		try {
			ownerItem.addMappingOperaion(new MovePackateMappingOperation(this, ownerItem, newCxt, oldCxt, newpackageName));
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void migrateContentItem(ItemDelta ownerItem, final ContextVariable newCxt, final ContextVariable oldCxt) {
		try {
			// IPackageFragmentRoot oldSource = getJavaSourceElement(oldCxt);
			// ICompilationUnit cu = getCompilationUnit(oldCxt);
			// IProgressMonitor monitor = View.getDefaultMonitor();
			String newpackageName = getPackageName(newCxt);
			super.migrateContentItem(ownerItem, newCxt, oldCxt); // todo
			// nothing
			// IPackageFragmentRoot newSource = getJavaSourceElement(newCxt);
			// IPackageFragment newPackage = createPackageFragment(newCxt,
			// newpackageName, true, monitor);

			ownerItem.addMappingOperaion(new MovePackateMappingOperation(this, ownerItem, newCxt, oldCxt, newpackageName));
		} catch (CoreException e1) {
			e1.printStackTrace();
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Gets the compilation unit.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the compilation unit
	 */
	public ICompilationUnit getCompilationUnit(ContextVariable cxt) {
		return (ICompilationUnit) JavaCore.create(getFile(cxt));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.eclipse.content.FileContentManager#getMainMappingContent(fr.imag.adele.cadse.core.var.ContextVariable,
	 *      java.lang.Class)
	 */
	@Override
	public <T> T getMainMappingContent(ContextVariable cxt, Class<T> clazz) {
		if (clazz == ICompilationUnit.class) {
			return (T) getCompilationUnit(cxt);
		}
		if (clazz == IType.class) {
			return (T) getJavaType(cxt);
		}

		return super.getMainMappingContent(cxt, clazz);
	}

	/**
	 * Gets the java type.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the java type
	 */
	public IType getJavaType(ContextVariable cxt) {
		IType javatype = null;
		IFile f = getFile(cxt);
		if (f != null && f.exists()) {
			ICompilationUnit cu = getCompilationUnit(cxt);
			if (cu != null) {
				javatype = cu.getType(getClassName(cxt));
			}
		}
		return javatype;
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
