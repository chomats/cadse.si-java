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



import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution2;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fede.workspace.tool.view.WSPlugin;


/**
 * The Class DependencyMarkerResolution.
 * 
 * @deprecated
 * @author chomats
 */
public class DependencyMarkerResolution implements IJavaCompletionProposal {
	
	/** The qualified package. */
	String qualifiedPackage;
	
	/** The addimport. */
	boolean addimport;
	
	/** The type name. */
	String typeName;
	
	/** The item dep. */
	Item	itemDep;
	
	/** The item source. */
	Item 	itemSource;
	
	/** The lt. */
	LinkType lt;
	
	/**
	 * Instantiates a new dependency marker resolution.
	 * 
	 * @param qualifiedPackage
	 *            the qualified package
	 * @param typeName
	 *            the type name
	 * @param itemSource
	 *            the item source
	 * @param itemDep
	 *            the item dep
	 * @param lt
	 *            the lt
	 * @param addimport
	 *            the addimport
	 */
	public DependencyMarkerResolution(String qualifiedPackage, String typeName,
			Item itemSource, Item itemDep, LinkType lt, boolean addimport) {
		super();
		this.qualifiedPackage = qualifiedPackage;
		this.typeName = typeName;
		this.itemDep = itemDep;
		this.itemSource = itemSource;
		this.lt = lt;
		this.addimport = addimport;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	public int getRelevance() {
		return 50;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	public void apply(IDocument document) {
		
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Create link from ");
		sb.append(itemSource.getQualifiedName());
		sb.append(" --(");
		sb.append(lt.getName());
		sb.append(")--> ");
		sb.append(itemDep.getQualifiedName());
		sb.append("\n");
		sb.append("resolve import ").append(qualifiedPackage);
		if (typeName == null) {
			sb.append(".*");
		} else {
			sb.append(".").append(typeName);
		}
		if (addimport) {
			sb.append("\n add import in source");
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Create link from ");
		sb.append(itemSource.getQualifiedName());
		sb.append(" --(");
		sb.append(lt.getName());
		sb.append(")--> ");
		sb.append(itemDep.getQualifiedName());
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return WSPlugin.getDefault().getImageFrom(itemDep.getType(), itemDep);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(0,0);
	}

	
}
