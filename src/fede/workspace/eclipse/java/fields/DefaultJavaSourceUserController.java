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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.impl.ui.ic.IC_Abstract;
import fede.workspace.eclipse.MelusineProjectManager;


/**
 * The Class DefaultJavaSourceUserController.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
final public class DefaultJavaSourceUserController extends IC_Abstract implements JavaSourceInteractifController {
	
	/* (non-Javadoc)
	 * @see fede.workspace.eclipse.java.fields.JavaSourceInteractifController#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		Item parentItem = getParentItem();
		IProject p = MelusineProjectManager.getProject(parentItem);
	    IJavaProject jp = JavaCore.create(p);
	    return jp;
	}

	public ItemType getType() {
		// TODO Auto-generated method stub
		return null;
	}
}