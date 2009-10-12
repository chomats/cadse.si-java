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


import org.eclipse.swt.SWT;

import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.IModelController;
import fr.imag.adele.cadse.ui.field.core.FieldsCore;
import fede.workspace.eclipse.java.manager.JarContentManager;
import fede.workspace.model.manager.properties.impl.mc.StringToOneResourceModelController;
import fede.workspace.model.manager.properties.impl.mc.StringToResourceListModelController;
import fede.workspace.model.manager.properties.impl.ui.DBrowserUI;
import fede.workspace.model.manager.properties.impl.ui.DListUI;


/**
 * The Class JavaFieldsCore.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JavaFieldsCore extends FieldsCore {

	/**
	 * Creates the jar lib field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * @param title
	 *            the title
	 * @param root
	 *            the root
	 * 
	 * @return the d list ui
	 */
	static public DListUI createJarLibField( String key, String label, String title, int root) {
		return new DListUI(key, label, EPosLabel.top, 
				new StringToResourceListModelController(), 
				new IC_JarResourceForBrowser_Combo_List(title,title,root),
				true, true);
		
		
	}
	
	/**
	 * Creates the one jar lib field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * @param title
	 *            the title
	 * @param root
	 *            the root
	 * 
	 * @return the d browser ui
	 */
	static public DBrowserUI createOneJarLibField( String key, String label, String title, int root) {
		return new DBrowserUI(key, label, EPosLabel.top, 
				new StringToOneResourceModelController(), 
				new IC_JarResourceForBrowser_Combo_List(title,title,root),
				SWT.BORDER | SWT.SINGLE);
		
	}
	
	/**
	 * Creates the jar field.
	 * 
	 * @return the d browser ui
	 */
	static public DBrowserUI createJarField() {
		return createOneJarLibField(JarContentManager.JAR_RESOURCES_ATTRIBUTE, "Jar", "Select a jar", 1);
	}
	
	/**
	 * Creates the jar source field.
	 * 
	 * @return the d browser ui
	 */
	static public DBrowserUI createJarSourceField() {
		return createOneFolderOrFileField(JarContentManager.JAR_SOURCE_RESOURCES_ATTRIBUTE, "Source Jar", "Select the source of the jar.","A folder or a zip file or a jar file", 
			true,".+\\.jar|.+\\.zip",1);
	}
	
	/**
	 * Creates the jar lib field.
	 * 
	 * @param key
	 *            the key
	 * @param root
	 *            the root
	 * 
	 * @return the d list ui
	 */
	static public DListUI createJarLibField(String key, int root) {
		return createJarLibField(key, "Jars", "Select a jar",root);
		
	}
	
	
	/**
	 * Creates the java field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * @param ic
	 *            the ic
	 * 
	 * @return the d browser ui
	 */
	static public DBrowserUI createJavaField(String key, String label,
			IC_JavaClassForBrowser_Combo ic) {
		return new DBrowserUI(key, label, EPosLabel.top, 
				new MC_StringToJavaElement(), 
				ic,
				SWT.BORDER | SWT.SINGLE);
	}
	
	/**
	 * Creates the java field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * @param filter
	 *            the filter
	 * @param titleSelect
	 *            the title select
	 * @param style
	 *            the style
	 * 
	 * @return the d browser ui
	 */
	static public DBrowserUI createJavaField(String key, String label,
			String filter, String titleSelect, int style) {
		return createJavaField(key, label, new IC_JavaClassForBrowser_Combo(titleSelect, titleSelect, style, filter));
	}
	
	/**
	 * Creates the package field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * 
	 * @return the d list ui
	 */
	static public DListUI createPackageField(String key, String label) {
		return new DListUI(key, label, EPosLabel.top, 
				new StringToPackageValueController(), 
				new PackageListController("Select a package", "Select a package"),
				true, true);
		
	}
	
	/**
	 * Creates the java source field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * @param mc
	 *            the mc
	 * @param ic
	 *            the ic
	 * 
	 * @return the java source viewer field
	 */
	static public JavaSourceViewerField createJavaSourceField(
			String key, String label, IModelController mc, JavaSourceInteractifController ic) {
		return new JavaSourceViewerField(key, label, EPosLabel.top, 
				mc, 
				ic);
	}
	
	
//	public static UIField createSouceField(String string, String string2) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	
}
