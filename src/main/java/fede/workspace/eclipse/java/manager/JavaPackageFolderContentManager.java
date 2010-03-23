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

import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
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

import fede.workspace.eclipse.content.FolderContentManager;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.Variable;

/**
 * The Class JavaPackageFolderContentManager.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JavaPackageFolderContentManager extends FolderContentManager {

	/** The packagename. */
	private Variable	packagename;

	/**
	 * Instantiates a new java package folder content manager.
	 * 
	 * @param parent
	 *            the parent
	 * @param item
	 *            the item
	 * @param packagename
	 *            the packagename
	 */
	public JavaPackageFolderContentManager(UUID id, Variable packagename) {
		super(id, new PathFolderVariable(packagename));
		this.packagename = packagename;
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
		return packagename.compute(cxt, getOwnerItem());
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
				if (parent == JavaPackageFolderContentManager.this) {
					return new Object[] { JavaCore.create(getFolder()) };
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
	 * Gets the java source element.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the java source element
	 */
	public IPackageFragmentRoot getJavaSourceElement(ContextVariable cxt) {
		ContentItem parent = getPartParent();
		if (parent != null) {
			return parent.getMainMappingContent(cxt, IPackageFragmentRoot.class);
		}
		return null;
	}

	/**
	 * Gets the package fragment.
	 * 
	 * @param cxt
	 *            the cxt
	 * 
	 * @return the package fragment
	 */
	public IPackageFragment getPackageFragment(ContextVariable cxt) {
		return (IPackageFragment) JavaCore.create(getFolder(cxt));
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
		IPackageFragment pf = getPackageFragment(cxt);
		if (!pf.isDefaultPackage()) {
			name = getPackageName(cxt) + "." + name;
		}
		IPackageFragmentRoot pfr = getJavaSourceElement(cxt);
		return pfr.createPackageFragment(name, force, monitor);
	}
}
