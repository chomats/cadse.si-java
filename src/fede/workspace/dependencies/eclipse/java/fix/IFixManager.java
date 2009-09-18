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

package fede.workspace.dependencies.eclipse.java.fix;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LinkType;


/**
 * private void resolve(IJavaProject sourceProject, Item itemSource, String
 * qualifiedPackage, String typeDef, boolean addImport, List<IJavaCompletionProposal>
 * ret) { Item[] findItems = findItemWithExportPackageWithType(qualifiedPackage,
 * typeDef); for (Item item : findItems) { LinkType lt = findLT(itemSource,
 * item); if (lt == null) continue; ret.add(createResolution(itemSource,
 * qualifiedPackage, typeDef, addImport, item, lt)); }
 * 
 * for(Item i : CadseCore.getWorkspaceLogique().getItems()) { IJavaProject jp =
 * i.getMainMappingContent(IJavaProject.class); if (jp == null) continue;
 * IPackageFragmentRoot sourceFolder =
 * i.getMainMappingContent(IPackageFragmentRoot.class); if (sourceFolder ==
 * null) continue; IType t; try { t = jp.findType(null, typeDef); if (t == null)
 * continue; LinkType lt = findLT(itemSource, i); if (lt == null) continue;
 * ret.add(new
 * DependencyMarkerResolution(t.getPackageFragment().getElementName(),typeDef,
 * itemSource, i,lt,true)); } catch (JavaModelException e) { // TODO
 * Auto-generated catch block e.printStackTrace(); } } }
 * 
 * protected DependencyMarkerResolution createResolution(Item itemSource, String
 * qualifiedPackage, String typeDef, boolean addImport, Item item, LinkType lt) {
 * return new DependencyMarkerResolution(qualifiedPackage,typeDef, itemSource,
 * item,lt,addImport); }
 * 
 * @author chomats
 */
public interface IFixManager {

	/**
	 * Find lt.
	 * 
	 * @param itemSource
	 *            the item source
	 * @param destination
	 *            the destination
	 * 
	 * @return the link type
	 */
	public LinkType findLT(Item itemSource, Item destination);

	/**
	 * Find item with export package with type.
	 * 
	 * @param qualifiedPackage
	 *            the qualified package
	 * @param typeDef
	 *            the type def
	 * 
	 * @return the item[]
	 */
	public Item[] findItemWithExportPackageWithType(String qualifiedPackage,
			String typeDef) ;
	
	/**
	 * Resolve.
	 * 
	 * @param sourceProject
	 *            the source project
	 * @param itemSource
	 *            the item source
	 * @param qualifiedPackageName
	 *            the qualified package name
	 * @param typeName
	 *            the type name
	 * @param addImport
	 *            the add import
	 * @param ret
	 *            the ret
	 */
	public void resolve(IJavaProject sourceProject, Item itemSource, String qualifiedPackageName,
			String typeName, boolean addImport, List<IJavaCompletionProposal> ret);
}
