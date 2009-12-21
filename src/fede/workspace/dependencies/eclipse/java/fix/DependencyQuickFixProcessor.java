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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import fr.imag.adele.cadse.core.Item;
import fede.workspace.tool.view.WSPlugin;


/**
 * The Class DependencyQuickFixProcessor.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class DependencyQuickFixProcessor implements IQuickFixProcessor {


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.IQuickFixProcessor#hasCorrections(org.eclipse.jdt.core.ICompilationUnit, int)
	 */
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		if (problemId == IProblem.ImportNotFound || problemId == IProblem.UndefinedType)
			return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.IQuickFixProcessor#getCorrections(org.eclipse.jdt.ui.text.java.IInvocationContext, org.eclipse.jdt.ui.text.java.IProblemLocation[])
	 */
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		List<IJavaCompletionProposal> ret = new ArrayList<IJavaCompletionProposal>();
		CompilationUnit astroot = context.getASTRoot();
		IResource r = context.getCompilationUnit().getResource();
		IProject p = r.getProject();
		IJavaProject jp = JavaCore.create(p);
		
		Item itemSource = WSPlugin.getItemFromResource(p);
		if (itemSource != null) {
			Object manager = itemSource.getType().getItemManager();
			if (manager instanceof IFixManager) {
				IFixManager fixmanager = (IFixManager) manager;
				for (IProblemLocation problem : locations) {
					ASTNode coveredNode = problem.getCoveredNode(astroot);
					String[] arguments = problem.getProblemArguments();
					
					switch (problem.getProblemId()) {
					case IProblem.IsClassPathCorrect:
						if (arguments.length == 1) {
							String qualifiedType = arguments[0];
							String packageName= Signature.getQualifier(qualifiedType);
							String typeName= Signature.getSimpleName(qualifiedType);
							fixmanager.resolve(jp, itemSource, packageName, typeName, false, ret);
						}
						continue;
					case IProblem.UndefinedType:
						if (coveredNode instanceof SimpleName) {
							fixmanager.resolve(jp, itemSource, ((SimpleName)coveredNode).getIdentifier(), null, false, ret);
						}
						break;
					default:
						break;
					}
					ASTNode selectedNode= problem.getCoveringNode(context.getASTRoot());
					if (selectedNode == null) continue;
					
					ImportDeclaration importDeclaration= (ImportDeclaration) ASTNodes.getParent(selectedNode, ASTNode.IMPORT_DECLARATION);
					if (importDeclaration == null) {
						continue;
					}
					
					String name= ASTNodes.asString(importDeclaration.getName());
					String packageName;
					String typeName= null;
					if (importDeclaration.isOnDemand()) {
						packageName= name;
					} else {
						packageName= Signature.getQualifier(name);
						typeName= Signature.getSimpleName(name);
					}
					
					fixmanager.resolve(jp, itemSource, packageName, typeName,problem.getProblemId() == IProblem.UndefinedType, ret);
					
				}
			}
		}
		return (IJavaCompletionProposal[]) ret.toArray(new IJavaCompletionProposal[ret
				.size()]);
	}

	
	

}
