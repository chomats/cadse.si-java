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

package fede.workspace.dependencies.eclipse.java.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import fede.workspace.tool.view.WSPlugin;


/**
 * The Class TypeUtil.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class TypeUtil {
	
	/**
	 * Find type.
	 * 
	 * @param packageFragment
	 *            the package fragment
	 * @param typeDef
	 *            the type def
	 * 
	 * @return the i type
	 */
	public static IType findType(IPackageFragment packageFragment, String typeDef) {
		{
			ICompilationUnit cu = packageFragment.getCompilationUnit(typeDef+".java");
			if (cu != null) {
				return cu.getType(typeDef);
			}
		}
		
		try {
			for (ICompilationUnit  cu : packageFragment.getCompilationUnits()) {
				IType ret =  cu.getType(typeDef);
				if (ret != null) return ret;
			}
		} catch (JavaModelException e) {
			WSPlugin.logException(e);
		}		
		return null;
	}
	
	/**
	 * Find type.
	 * 
	 * @param project
	 *            the project
	 * @param qualifiedPackage
	 *            the qualified package
	 * @param typeDef
	 *            the type def
	 * 
	 * @return the i type
	 */
	public static IType findType(IProject project, String qualifiedPackage,
			String typeDef) {
		IJavaProject jp = JavaCore.create(project);
		if (!jp.exists()) return null;
		
		try {
			IPackageFragmentRoot[] packagesRoot = jp.getPackageFragmentRoots();
			for (IPackageFragmentRoot packageFragmentRoot : packagesRoot) {
				if (packageFragmentRoot.isExternal()) continue;
				IPackageFragment packageFragment = packageFragmentRoot.getPackageFragment(qualifiedPackage);
				if (packageFragment.exists()) {
					IType ret = TypeUtil.findType(packageFragment, typeDef);
					if (ret != null) return ret;
				}
			}
		} catch (JavaModelException e) {
			WSPlugin.logException(e);
		}
		
		return null;
	}

}
