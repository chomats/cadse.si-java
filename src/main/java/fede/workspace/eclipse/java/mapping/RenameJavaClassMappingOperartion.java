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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameCompilationUnitChange;
import org.eclipse.ltk.core.refactoring.Change;

import fede.workspace.eclipse.java.manager.JavaFileContentManager;
import fede.workspace.tool.eclipse.EclipseTool;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;

public class RenameJavaClassMappingOperartion extends MappingOperation {
	String	cn;
	String	oldcn;

	public RenameJavaClassMappingOperartion(ItemDelta parent) throws CadseException {
		super(parent);
	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		JavaFileContentManager cm = (JavaFileContentManager) goodItem.getContentItem();
		ContextVariable oldcontext = new ContextVariableImpl();
		oldcontext.putValue(goodItem, CadseGCST.ITEM_at_NAME_, oldcn);

		ICompilationUnit cu = cm.getCompilationUnit(oldcontext);
		IResource f = cu.getResource();

		RenameCompilationUnitChange mcuc = new RenameCompilationUnitChange(cu, cn + ".java");
		try {
			IProgressMonitor defaultMonitor = EclipseTool.getDefaultMonitor();
			Change perform = mcuc.perform(defaultMonitor);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected String getLabel() {
		return "rename to " + cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public void setOldcn(String oldcn) {
		this.oldcn = oldcn;
	}
}
