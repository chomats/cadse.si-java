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

public class SetJavaClassMappingOperation extends MappingOperation {

	public SetJavaClassMappingOperation(ItemDelta parent) throws CadseException {
		super(parent);
	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		// try {
		// IType t = GeneratedInterfaceManager.getJdtType(goodItem);
		// IProgressMonitor monitor = EclipseTool.getDefaultMonitor();
		//
		// ICompilationUnit cu;
		// if (t != null) {
		// cu = t.getCompilationUnit();
		// IFile f = (IFile) cu.getResource();
		// if (!f.exists()) {
		// // error must be created by the content manager
		// cu =
		// t.getPackageFragment().createCompilationUnit(cu.getElementName(),
		// JavaElementManager.getGeneratedTextAttribute(goodItem), false,
		// monitor);
		// } else {
		// IDE.saveAllEditors(new IResource[] { f }, true);
		// try {
		// JMergeUtil.merge(monitor, f,
		// JavaElementManager.getGeneratedTextAttribute(goodItem));
		// } catch (JETException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// JavaProjectManager.createInheritedMethods(t, true, false, true,
		// monitor);
		// } else {
		// JavaFileContentManager cm = (JavaFileContentManager)
		// goodItem.getContentItem();
		// String pn =
		// GeneratedInterfaceManager.getPackage(goodItem).getShortName();
		// String cn = goodItem.getName();
		// IPackageFragment pnJdt =
		// cm.createPackageFragment(ContextVariableImpl.DEFAULT, pn, true, monitor);
		// pnJdt.createCompilationUnit(cn + ".java",
		// JavaElementManager.getGeneratedTextAttribute(goodItem), true,
		// monitor);
		// }
		// } catch (JavaModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (CoreException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	@Override
	protected String getLabel() {
		return "set source";
	}

}
