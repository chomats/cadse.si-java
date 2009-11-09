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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;

import fede.workspace.eclipse.java.JavaProjectManager;
import fede.workspace.tool.view.WSPlugin;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.si.workspace.uiplatform.swt.ic.IC_AbstractTreeDialogForList_Browser_Combo;

/**
 * String package-select-attribut-jar : le nom d'un attribut de l'item contenant
 * un liste de jar valid.
 * 
 * @author chomats
 */
public class JavaElementTreeController extends IC_AbstractTreeDialogForList_Browser_Combo {

	public JavaElementTreeController() {
	}
	/**
	 * Instantiates a new java element tree controller.
	 * 
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 */
	public JavaElementTreeController(String title, String message) {
		super(title, message);
	}

	/** The labelprovider. */
	JavaElementLabelProvider	labelprovider	= null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.IInteractionControllerForList#getLabelProvider()
	 */
	public ILabelProvider getLabelProvider() {
		if (labelprovider == null) {
			labelprovider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		}
		return labelprovider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.IInteractionControllerForBrowserOrCombo#getValues()
	 */
	public Object[] getValues() {
		return getInputValues();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.impl.ic.IC_AbstractTreeDialogForList_Browser_Combo#getInputValues()
	 */
	@Override
	protected Object[] getInputValues() {
		Item theCurrentItem = getItem();
		IPackageFragmentRoot[] froot = getPackageFragmentRoot();
		IJavaElement[] packages = null;
		if (froot != null) {
			packages = froot;
			if (froot.length == 1) {
				try {
					packages = froot[0].getChildren();
				} catch (JavaModelException e) {
					WSPlugin.getDefault().log(
							new Status(IStatus.ERROR, WSPlugin.PLUGIN_ID, 0, MessageFormat.format(
									"Cannot find packages from the item {0}", theCurrentItem.getName()), null));
				}
			}
		}
		if (packages == null) {
			packages = new IJavaElement[0];
		}
		return packages;
	}

	
	/**
	 * Gets the package fragment root.
	 * 
	 * @return the package fragment root
	 */
	protected IPackageFragmentRoot[] getPackageFragmentRoot() {
		Item theCurrentItem = getItem();
		IJavaProject jp;
		try {
			jp = JavaProjectManager.getJavaProject(theCurrentItem);

		} catch (CoreException e) {
			WSPlugin.getDefault().log(
					new Status(IStatus.ERROR, WSPlugin.PLUGIN_ID, 0,
							MessageFormat.format("Cannot find the java projet or fragment root from the item {0}",
									theCurrentItem.getName()), null));
			return null;
		}
		// String fragmentone = (String) fd.get("package.fragmentroot-one");
		// if (fragmentone != null) {
		// IPackageFragmentRoot fr = jp.findPackageFragmentRoot()
		// }
		try {
			IPackageFragmentRoot[] ret = jp.getPackageFragmentRoots();
			ArrayList<IPackageFragmentRoot> retSelected = new ArrayList<IPackageFragmentRoot>();
			for (IPackageFragmentRoot pfr : ret) {
				if (selectFragmentRoot(pfr)) {
					retSelected.add(pfr);
				}
			}
			return retSelected.toArray(new IPackageFragmentRoot[retSelected.size()]);

		} catch (CoreException e) {
			WSPlugin.getDefault().log(
					new Status(IStatus.ERROR, WSPlugin.PLUGIN_ID, 0,
							MessageFormat.format("Cannot find the java projet or fragment root from the item {0}",
									theCurrentItem.getName()), null));

		}
		return null;
	}

	/**
	 * Select fragment root.
	 * 
	 * @param pfr
	 *            the pfr
	 * 
	 * @return true, if successful
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	protected boolean selectFragmentRoot(IPackageFragmentRoot pfr) throws JavaModelException {
		String attrName = (String) getUIField().getLocal("package-select-attribut-jar");
		if (attrName != null) {
			Item theItem = getItem();
			Object jarListObj = theItem.getAttribute(attrName);
			if (jarListObj instanceof List) {
				List<String> jarList = (List<String>) jarListObj;
				if (jarList.contains(pfr.getElementName())) {
					return true;
				}

			}
		}

		if (pfr.getKind() == IPackageFragmentRoot.K_BINARY) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.impl.ic.IC_AbstractTreeDialogForList_Browser_Combo#getTreeContentProvider()
	 */
	@Override
	protected ITreeContentProvider getTreeContentProvider() {
		return new StandardJavaElementContentProvider() {
			@Override
			public Object[] getChildren(Object element) {
				if (element instanceof Object[]) {
					return (Object[]) element;
				}
				return super.getChildren(element);
			};
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.impl.ic.IC_AbstractTreeDialogForList_Browser_Combo#getFilter()
	 */
	@Override
	protected ViewerFilter getFilter() {
		return null;
	}

	/** The Error. */
	Status	Error	= new Status(Status.ERROR, WSPlugin.PLUGIN_ID, 0, "Select one or more package", null);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.ISelectionStatusValidator#validate(java.lang.Object[])
	 */
	public IStatus validate(Object[] selection) {
		if (selection == null || selection.length == 0) {
			return Error;
		}

		for (Object object : selection) {
			if (!(object instanceof IPackageFragment)) {
				return Error;
			}
		}

		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.IInteractionControllerForBrowserOrCombo#toString(java.lang.Object)
	 */
	public String toString(Object value) {
		return getLabelProvider().getText(value);
	}

	public ItemType getType() {
		// TODO Auto-generated method stub
		return null;
	}
}
