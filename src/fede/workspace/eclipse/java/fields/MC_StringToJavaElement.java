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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_AttributesItem;
import fr.imag.adele.cadse.core.ui.UIField;

/**
 * The Class MC_StringToJavaElement.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class MC_StringToJavaElement extends MC_AttributesItem {

	public MC_StringToJavaElement(Item id) {
		super(id);
	}

	public MC_StringToJavaElement() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.MC_AttributesItem#getValue()
	 */
	@Override
	public Object getValue() {
		Object ret = super.getValue();
		return JavaCore.create((String) ret);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.MC_AttributesItem#notifieValueChanged(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	@Override
	public void notifieValueChanged(UIField field, Object value) {
		super.notifieValueChanged(field, convertToModelValue(value));
	}

	@Override
	public Object convertToModelValue(Object visualValue) {
		if (visualValue == null) {
			return null;
		}
		return ((IJavaElement) visualValue).getHandleIdentifier();
	}

	@Override
	public void notifieValueDeleted(UIField field, Object oldvalue) {
		super.notifieValueChanged(field, null);
	}

	@Override
	public Object defaultValue() {
		Object ret = super.defaultValue();
		if ("".equals(ret)) {
			return null;
		}

		try {
			return JavaCore.create((String) ret);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
