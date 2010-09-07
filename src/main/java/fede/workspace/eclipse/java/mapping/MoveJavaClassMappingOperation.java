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
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
package fede.workspace.eclipse.java.mapping;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;

public class MoveJavaClassMappingOperation extends MappingOperation {
	String	pn;
	String	oldpn;
	String	oldcn;

	public MoveJavaClassMappingOperation(ItemDelta parent) throws CadseException {
		super(parent);
	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		// JavaFileContentManager cm = (JavaFileContentManager)
		// goodItem.getContentItem();
		//
		// IPackageFragment newPackage;
		// try {
		// newPackage = cm.createPackageFragment(ContextVariableImpl.DEFAULT, pn,
		// true, EclipseTool.getDefaultMonitor());
		//
		// ContextVariable oldcontext = new ContextVariable();
		// Item packageItem = GeneratedInterfaceManager.getPackage(goodItem);
		// oldcontext.putValue(packageItem, CadseGCST.ITEM_at_NAME,
		// oldpn);
		// if (oldcn != null) {
		// oldcontext.putValue(goodItem, CadseGCST.ITEM_at_NAME, oldcn);
		// }
		//
		// ICompilationUnit cu = cm.getCompilationUnit(oldcontext);
		// IResource f = cu.getResource();
		// MoveCompilationUnitChange mcuc = new MoveCompilationUnitChange(cu,
		// newPackage);
		// try {
		// mcuc.perform(EclipseTool.getDefaultMonitor());
		// } catch (CoreException e1) {
		// e1.printStackTrace();
		// }
		// Activator.unSetItemPersistenceID(f, true);
		// cu = cm.getCompilationUnit(ContextVariableImpl.DEFAULT);
		// f = cu.getResource();
		// EclipseTool.setItemPersistenceID(f, goodItem);
		// // cm.
		// } catch (JavaModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public void setPn(String pn) {
		this.pn = pn;
	}

	public void setOldpn(String oldpn) {
		this.oldpn = oldpn;
	}

	public void setOldcn(String oldcn) {
		this.oldcn = oldcn;
	}

	@Override
	protected String getLabel() {
		return "Move Java file " + oldpn + " to " + pn;
	}

}
