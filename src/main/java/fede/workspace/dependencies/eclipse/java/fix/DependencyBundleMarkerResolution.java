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
package fede.workspace.dependencies.eclipse.java.fix;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LinkType;

/**
 * The Class DependencyBundleMarkerResolution.
 * 
 * @deprecated
 * @author chomats
 */
@Deprecated
public abstract class DependencyBundleMarkerResolution implements IJavaCompletionProposal {

	/** The qualified package. */
	String				qualifiedPackage;

	/** The addimport. */
	boolean				addimport;

	/** The type name. */
	String				typeName;

	/** The item source. */
	Item				itemSource;

	/** The lt. */
	LinkType			lt;

	/** The bundle. */
	BundleDescription	bundle;

	/**
	 * Instantiates a new dependency bundle marker resolution.
	 * 
	 * @param qualifiedPackage
	 *            the qualified package
	 * @param typeName
	 *            the type name
	 * @param itemSource
	 *            the item source
	 * @param bundle
	 *            the bundle
	 * @param lt
	 *            the lt
	 * @param addimport
	 *            the addimport
	 */
	public DependencyBundleMarkerResolution(String qualifiedPackage, String typeName, Item itemSource,
			BundleDescription bundle, LinkType lt, boolean addimport) {
		super();
		this.qualifiedPackage = qualifiedPackage;
		this.typeName = typeName;
		this.itemSource = itemSource;
		this.lt = lt;
		this.bundle = bundle;
	}

	/**
	 * Sets the bundle.
	 * 
	 * @param bundle
	 *            the new bundle
	 */
	public void setBundle(BundleDescription bundle) {
		this.bundle = bundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	public int getRelevance() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	public void apply(IDocument document) {
		createLink();
	}

	/**
	 * Creates the link.
	 */
	abstract protected void createLink();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {

		StringBuilder sb = new StringBuilder();
		sb.append("Create link from ");
		sb.append(itemSource.getQualifiedName());
		sb.append(" --(");
		sb.append(lt.getName());
		sb.append(")--> ");
		sb.append(bundle.getName());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Create link from ");
		sb.append(itemSource.getQualifiedName());
		sb.append(" --(");
		sb.append(lt.getName());
		sb.append(")--> ");
		sb.append(bundle.getName());
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	abstract public Image getImage();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(0, 0);
	}

}
