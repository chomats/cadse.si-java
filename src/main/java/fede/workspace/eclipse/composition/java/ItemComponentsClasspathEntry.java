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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import fede.workspace.eclipse.composer.EclipseExportedContent;
import fede.workspace.eclipse.java.JavaProjectManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.build.IExportedContent;




/**
 * This class represents the calculated classpath of the project associated with
 * the components of an item in the workspace.
 * 
 * The classpath is calculated in the constructor of this class and is never
 * recalculated. In case the components of an item change, a new instance must
 * be instantiated and associated with the corresponding project.
 */
public abstract class ItemComponentsClasspathEntry implements IClasspathContainer {

	/** The export type. */
	private final String exportType;
	
	/** The export entries. */
	private final boolean exportEntries;

	/** The dependencies. */
	private final List<IClasspathEntry> dependencies;
	
	/** The attribute. */
	private IClasspathAttribute attribute;

	/**
	 * Instantiates a new item components classpath entry.
	 * 
	 * @param javaProject
	 *            the java project
	 * @param item
	 *            the item
	 * @param exportType
	 *            the export type
	 * @param exportEntries
	 *            the export entries
	 * @param attribute
	 *            the attribute
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public ItemComponentsClasspathEntry(IJavaProject javaProject, Item item, String exportType, boolean exportEntries, IClasspathAttribute attribute) throws CoreException {
		this.exportType	= exportType;
		this.exportEntries	= exportEntries;
		this.attribute = attribute;
		this.dependencies 	= new ArrayList<IClasspathEntry>();
		calculateDependencies(javaProject,item);
	}

	/**
	 * Create an updated list of components and refresh classpath accordingly.
	 * 
	 * @param javaProject
	 *            the java project
	 * @param item
	 *            the item
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	private void calculateDependencies(IJavaProject javaProject,Item item) throws CoreException {
		

		/*
		 * TODO Right now we have a single component repository for every java project.
		 * 
		 * We need to handle mapping variants in which there are many composites in a single java project,
		 * this is the case for example when a composite has parts that are themselves java composites.
		 */
		if (item == null)
			return;
		
		if (javaProject == null)
			return;
		
		IContainer repository = javaProject.getProject();
		
		for (IExportedContent component : EclipseExportedContent.getPackagedItems(repository,exportType,null)) {
			if (component.getItemIdentification().equals(item.getId())) continue;

			EclipseExportedContent pi = (EclipseExportedContent) component;
			
			
			IResource componentContent = pi.getItemFolder();
			if (componentContent == null || !componentContent.exists()) continue;
			
			// verify if the packaged item is actually in the workspace and get the source path
			Item workspaceComponent = item.getLogicalWorkspace().getItem(component.getItemIdentification());
			IFolder sourceFolder	= workspaceComponent != null ? JavaProjectManager.getDefaultSourceFolder(workspaceComponent): null;
			
			IClasspathEntry entry 	= JavaCore.newLibraryEntry(componentContent.getFullPath(),
					sourceFolder != null? sourceFolder.getFullPath(): null,null,
					JavaProjectManager.DEFAULT_ACCESS_RULE, new IClasspathAttribute[]{ this.attribute },
					exportEntries);
			//JavaProjectManager.newLibraryEntry(componentContent.getFullPath(),sourceFolder != null? sourceFolder.getFullPath(): null,exportEntries);
			
			// test for duplicates
			if (dependencies.contains(entry))
				return;
			
			// add dependency
			dependencies.add(entry);
		}
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {
		return dependencies.toArray(new IClasspathEntry[dependencies.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		return "Item Components ["+exportType+"]";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
	 */
	public int getKind() {
		return K_APPLICATION;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDescription();
	}

}
