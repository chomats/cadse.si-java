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
package fede.workspace.eclipse.java.manager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.changes.MoveCompilationUnitChange;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.fede.workspace.si.view.View;

final class MovePackateMappingOperation extends MappingOperation {
	/**
	 * 
	 */
	private final JavaFileContentManager	javaFileContentManager;
	private final ContextVariable	newCxt;
	private final ContextVariable	oldCxt;
	private final String			newpackageName;
	private ICompilationUnit		_oldCompilationUnit;
	private IProgressMonitor		monitor;
	private IPackageFragment		_newPackageFragment;

	MovePackateMappingOperation(JavaFileContentManager javaFileContentManager, ItemDelta parent, ContextVariable newCxt, ContextVariable oldCxt,
			String newpackageName) throws CadseException, JavaModelException {
		super(parent);
		this.javaFileContentManager = javaFileContentManager;
		this.newCxt = newCxt;
		this.oldCxt = oldCxt;
		this.newpackageName = newpackageName;
		this.monitor = View.getDefaultMonitor();
		this._newPackageFragment = this.javaFileContentManager.createPackageFragment(newCxt, newpackageName, true, monitor);
		this._oldCompilationUnit = this.javaFileContentManager.getCompilationUnit(oldCxt);

	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		try {
			final MoveCompilationUnitChange mcuc = new MoveCompilationUnitChange(_oldCompilationUnit,
					_newPackageFragment);
			mcuc.perform(monitor);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected String getLabel() {
		return "move class in package" + newpackageName + " : " + _newPackageFragment;
	}
}
