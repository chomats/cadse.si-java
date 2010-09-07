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
package fede.workspace.eclipse.composition.java;



import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.objectadapter.ObjectAdapter;


/**
 * The Interface IPDEContributor.
 */
public class IPDEContributor extends ObjectAdapter<IPDEContributor>{
	
	/**
	 * Compute imports package.
	 * 
	 * @param imports
	 *            the imports
	 */
	public void computeImportsPackage(Item currentItem, Set<String> imports) {
	}
	
	/**
	 * Compute exports package.
	 * 
	 * @param exports
	 *            the exports
	 */
	public void computeExportsPackage(Item currentItem, Set<String> exports) {
	}
	
	/**
	 * Compute extenstion.
	 * 
	 * @param pluginBase
	 *            the plugin base
	 * @param workspacePluginModel
	 *            the workspace plugin model
	 */
	public void computeExtenstion(Item currentItem, IPluginBase pluginBase, WorkspacePluginModel workspacePluginModel) {
	}

	@Override
	public Class<IPDEContributor> getClassAdapt() {
		return IPDEContributor.class;
	}
	
	/**
	 * Find extension.
	 * 
	 * @param pluginBase
	 *            the plugin base
	 * @param exts
	 *            the exts
	 * @param pt
	 *            the pt
	 * 
	 * @return the i plugin extension
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	protected IPluginExtension findExtension(IPluginBase pluginBase, IPluginExtension[] exts, String pt)
			throws CoreException {
		for (IPluginExtension e : exts) {
			if (e.getPoint().equals(pt)) {
				return e;
			}
		}
		return null;
	}

}
