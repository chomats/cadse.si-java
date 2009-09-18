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

package fede.workspace.eclipse.java.mc;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;

import fr.imag.adele.cadse.core.impl.ui.MC_AttributesItem;
import fr.imag.adele.cadse.core.ui.IModelController;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.UIField;


/**
 * The Class PackageModelController.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public final class PackageModelController extends MC_AttributesItem implements
		IModelController {

	/** The Constant java_version. */
	private static final String java_version = "1.3";

	/**
	 * Instantiates a new package model controller.
	 */
	public PackageModelController() {
		super();
	}

	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ui.AbstractModelController#validValueChanged(fr.imag.adele.cadse.core.ui.UIField, java.lang.Object)
	 */
	@Override
	public boolean validValueChanged(UIField field, Object value) {
		String p = (String) value;
		IStatus error = JavaConventions.validatePackageName(p,java_version,java_version);
		if (error.getSeverity() == IStatus.ERROR) {
			setMessageError(error.getMessage());
			return true;
		}
		if (error.getSeverity() == IStatus.WARNING) {
			getUIField().getPageController().setMessage(error.getMessage(),IPageController.WARNING);
			
		}
		return super.validValueChanged(field, value);
	}
	

}