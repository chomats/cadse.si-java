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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import fede.workspace.eclipse.java.JavaProjectManager;
import fede.workspace.eclipse.java.WSJavaPlugin;
import fede.workspace.tool.view.WSPlugin;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_AttributesItem;
import fr.imag.adele.cadse.core.ui.UIField;

/**
 * The Class StringToPackageValueController.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class StringToPackageValueController extends MC_AttributesItem {

	/**
	 * Instantiates a new string to package value controller.
	 */
	public StringToPackageValueController() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.MC_AttributesItem#getValue()
	 */
	@Override
	public Object getValue() {
		return abstractToVisualValue(super.getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.MC_AttributesItem#notifieValueChanged(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	@Override
	public void notifieValueChanged(UIField field, Object value) {
		super.notifieValueChanged(field, visualToAbstractValue(value));
	}

	/**
	 * Abstract to visual value.
	 * 
	 * @param value
	 *            the value
	 * 
	 * @return the object
	 */
	public Object abstractToVisualValue(Object value) {
		List<String> packagesString = (List<String>) value;

		Item theCurrentItem = getItem();
		IJavaProject jp;
		try {
			jp = JavaProjectManager.getJavaProject(theCurrentItem);
		} catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, WSJavaPlugin.PLUGIN_ID, 0, MessageFormat.format(
					"Cannot find the java projet from the item {0}", theCurrentItem.getName()), null);
			WSJavaPlugin.getDefault().log(status);
			return new ArrayList<IPackageFragment>();
		}
		if (jp == null) {
			return new ArrayList<IPackageFragment>();
		}
		IPackageFragmentRoot[] rootPackages;
		try {
			rootPackages = jp.getPackageFragmentRoots();
		} catch (JavaModelException e) {
			WSJavaPlugin.getDefault()
					.log(
							new Status(IStatus.ERROR, WSPlugin.PLUGIN_ID, 0, MessageFormat
									.format("Cannot find the package fragment root from the item {0}", theCurrentItem
											.getName()), null));
			return new ArrayList<IPackageFragment>();
		}
		if (rootPackages == null || rootPackages.length == 0) {
			return new ArrayList<IPackageFragment>();
		}
		if (packagesString == null) {
			return new ArrayList<IPackageFragment>();
		}

		IPackageFragmentRoot packageSrc = null;
		for (IPackageFragmentRoot fr : rootPackages) {
			try {
				if (fr.getKind() == IPackageFragmentRoot.K_SOURCE) {
					packageSrc = fr;
					break;
				}
			} catch (JavaModelException e) {
				WSJavaPlugin.getDefault().log(
						new Status(IStatus.ERROR, WSPlugin.PLUGIN_ID, 0, MessageFormat.format(
								"Cannot find the type of the package fragment root {1} from the item {0}",
								theCurrentItem.getId(), fr.getElementName()), null));
			}
		}
		List<IPackageFragment> ret = new ArrayList<IPackageFragment>();
		for (String pString : packagesString) {
			IPackageFragment findPackage = null;
			for (IPackageFragmentRoot fr : rootPackages) {
				findPackage = fr.getPackageFragment(pString);
				if (findPackage.exists()) {
					break;
				}
			}
			if (packageSrc != null && (findPackage == null || !findPackage.exists())) {
				findPackage = packageSrc.getPackageFragment(pString);
			}
			if (findPackage != null) {
				ret.add(findPackage);
			}
		}
		return ret;
	}

	/**
	 * Visual to abstract value.
	 * 
	 * @param value
	 *            the value
	 * 
	 * @return the object
	 */
	public Object visualToAbstractValue(Object value) {
		List<String> packagesString = new ArrayList<String>();
		List<IPackageFragment> ret = (List<IPackageFragment>) value;
		for (IPackageFragment packageFragment : ret) {
			packagesString.add(packageFragment.getElementName());
		}
		return packagesString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.AbstractModelController#defaultValue()
	 */
	@Override
	public Object defaultValue() {
		return new ArrayList<IPackageFragment>();
	}

}
