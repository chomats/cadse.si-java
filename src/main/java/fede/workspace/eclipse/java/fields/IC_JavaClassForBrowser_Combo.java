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

package fede.workspace.eclipse.java.fields;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.si.workspace.uiplatform.swt.ic.ICRunningField;
import fr.imag.adele.cadse.si.workspace.uiplatform.swt.ic.IC_ForBrowserOrCombo;
import fr.imag.adele.cadse.si.workspace.uiplatform.swt.ic.IC_WithDialogAction;
import fede.workspace.eclipse.MelusineProjectManager;

/**
 * String java-style-filter, default "" int java-style-search, default
 * IJavaElementSearchConstants.CONSIDER_ALL_TYPES the only valid values are
 * <code>IJavaElementSearchConstants.CONSIDER_CLASSES</code>,
 * <code>IJavaElementSearchConstants.CONSIDER_INTERFACES</code>,
 * <code>IJavaElementSearchConstants.CONSIDER_ANNOTATION_TYPES</code>,
 * <code>IJavaElementSearchConstants.CONSIDER_ENUMS</code>,
 * <code>IJavaElementSearchConstants.CONSIDER_ALL_TYPES</code>,
 * <code>IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES</code>
 * <code>IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS</code>.
 * Please note that the bitwise OR combination of the elementary constants is
 * not supported. String title-select, default "??".
 */

public class IC_JavaClassForBrowser_Combo extends IC_WithDialogAction implements IC_ForBrowserOrCombo {

	/** The Constant style_values. */
	public static final String[]	style_values				= { "CLASSES", "INTERFACES", "ANNOTATIONS", "ENUMS",
			"ALL TYPES", "CLASSES AND INTERFACES", "CLASSES AND ENUMS" };
	
	public static final int[]	style_values_cst				= { 
		IJavaElementSearchConstants.CONSIDER_CLASSES,
		IJavaElementSearchConstants.CONSIDER_INTERFACES, 
		IJavaElementSearchConstants.CONSIDER_ANNOTATION_TYPES, 
		IJavaElementSearchConstants.CONSIDER_ENUMS,
		IJavaElementSearchConstants.CONSIDER_ALL_TYPES,
		IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
		IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS };

	/** The style. */
	private int		_style;

	/** The filter. */
	private String	_filter;

	public IC_JavaClassForBrowser_Combo() {
	}
	
	/**
	 * The Constructor.
	 * 
	 * @param style
	 *            see {@link JavaUI#createTypeDialog} : flags defining the style
	 *            of the dialog; the only valid values are
	 *            <code>IJavaElementSearchConstants.CONSIDER_CLASSES</code>,
	 *            <code>IJavaElementSearchConstants.CONSIDER_INTERFACES</code>,
	 *            <code>IJavaElementSearchConstants.CONSIDER_ANNOTATION_TYPES</code>,
	 *            <code>IJavaElementSearchConstants.CONSIDER_ENUMS</code>,
	 *            <code>IJavaElementSearchConstants.CONSIDER_ALL_TYPES</code>,
	 *            <code>IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES</code>
	 * <code>IJavaElementSearchConstants.CONSIDER_CLASSES_AND_ENUMS</code>.
	 *            Please note that the bitwise OR combination of the elementary
	 *            constants is not supported.
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @param filter
	 *            the filter
	 */
	public IC_JavaClassForBrowser_Combo(String title, String message, int style, String filter) {
		this._title = title;
		this._message = message;
		this._style = style;
		this._filter = filter;
	}

	@Override
	public void init() throws CadseException {
		super.init();
		if (_ic != null) {
			String style = _ic.getAttribute(CadseGCST.IC_JAVA_CLASS_FOR_BROWSER_COMBO_at_STYLE_);
			_style = IJavaElementSearchConstants.CONSIDER_ALL_TYPES;
			if (style != null) {
				for (int i = 0; i < style_values.length; i++) {
					if (style.equals(style_values[i])) {
						_style = style_values_cst[i];
						break;
					}
				}
			}
			_filter = _ic.getAttribute(CadseGCST.IC_JAVA_CLASS_FOR_BROWSER_COMBO_at_FILTER_);
			if (_filter == null)
				_filter = "";
		}
	}
	/**
	 * Gets the active workbench window.
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Gets the active workbench shell.
	 * 
	 * @return the active workbench shell
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.IInteractionControllerForBrowserOrCombo#selectOrCreateValue(org.eclipse.swt.widgets.Shell)
	 */
	public Object selectOrCreateValue(Shell parentShell) {
		try {

			SelectionDialog dialog = createJavaDialog();
			if (dialog.open() == SelectionDialog.OK) {
				Object[] result = dialog.getResult();
				if (result.length == 0) {
					return null;
				}
				IType type = (IType) result[0];
				return type;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected SelectionDialog createJavaDialog() throws JavaModelException {
		SelectionDialog dialog = JavaUI.createTypeDialog(getActiveWorkbenchShell(), PlatformUI.getWorkbench()
				.getProgressService(), getSearchScope(), _style, false, _filter);

		dialog.setTitle(_title);
		dialog.setMessage(_message);
		return dialog;
	}

	/**
	 * Gets the search scope.
	 * 
	 * @return the search scope
	 */
	protected IJavaSearchScope getSearchScope() {
		return SearchEngine.createJavaSearchScope(getPackageFragmentRoots());
	}

	/**
	 * Gets the package fragment roots.
	 * 
	 * @return the package fragment roots
	 */
	protected IJavaElement[] getPackageFragmentRoots() {
		ArrayList<IPackageFragmentRoot> result = new ArrayList<IPackageFragmentRoot>();
		try {
			IProject p = getProject();
			addProject(result, p);
			IProject[] otherProjects = p.getReferencedProjects();
			if (otherProjects != null) {
				for (IProject op : otherProjects) {
					addProject(result, op);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return result.toArray(new IPackageFragmentRoot[result.size()]);
	}

	private void addProject(ArrayList<IPackageFragmentRoot> result, IProject p) throws JavaModelException {
		IJavaProject jp = JavaCore.create(p);
		if (jp == null) {
			return;
		}

		IPackageFragmentRoot[] roots = jp.getPackageFragmentRoots();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE || (roots[i].isArchive())) {
				result.add(roots[i]);
			}
		}

	}

	/**
	 * Gets the project.
	 * 
	 * @return the project
	 */
	protected IProject getProject() {
		Item currentItem = _uiPlatform.getItem(getUIField());
		return MelusineProjectManager.getProject(currentItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.IInteractionControllerForBrowserOrCombo#toString(java.lang.Object)
	 */
	public String toString(Object value) {
		if (value instanceof IType) {
			return ((IType) value).getFullyQualifiedName('$');
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.IInteractionControllerForBrowserOrCombo#getValues()
	 */
	public Object[] getValues() {
		return new Object[0];
	}

	public Object fromString(String value) {
		return null;
	}

	public boolean hasDeleteFunction() {
		return true;
	}

	public ItemType getType() {
		return CadseGCST.IC_JAVA_CLASS_FOR_BROWSER_COMBO;
	}

}