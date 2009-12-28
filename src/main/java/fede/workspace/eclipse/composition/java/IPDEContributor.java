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

package fede.workspace.eclipse.composition.java;



import java.util.Set;

import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;


/**
 * The Interface IPDEContributor.
 */
public interface IPDEContributor {
	
	/**
	 * Compute imports package.
	 * 
	 * @param imports
	 *            the imports
	 */
	public void computeImportsPackage(Set<String> imports);
	
	/**
	 * Compute exports package.
	 * 
	 * @param exports
	 *            the exports
	 */
	public void computeExportsPackage(Set<String> exports);
	
	/**
	 * Compute extenstion.
	 * 
	 * @param pluginBase
	 *            the plugin base
	 * @param workspacePluginModel
	 *            the workspace plugin model
	 */
	public void computeExtenstion(IPluginBase pluginBase, WorkspacePluginModel workspacePluginModel);
}
