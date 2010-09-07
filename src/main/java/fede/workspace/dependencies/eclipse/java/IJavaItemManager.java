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
package fede.workspace.dependencies.eclipse.java;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;


/**
 * The Interface IJavaItemManager.
 */
public interface IJavaItemManager {

	/**
	 * Determines if a requirement link should be considered as transitive.
	 * 
	 * @param lk
	 *            the lk
	 * 
	 * @return true, if checks if is transitive link
	 */
	public boolean isTransitiveLink(Link lk);

	/**
	 * Specifies the kind of actual dependency that is abstracted by a
	 * requirement link between items.
	 */
	public enum DependencyNature{ 
 
 /** The Java compilation dependency. */
 JavaCompilationDependency }
	
	/**
	 * Adds a classpath entry to represent the requirement dependency, direct or
	 * indirect, between the specified source and target items.
	 * 
	 * This method is called on the manager of the target item, as it can
	 * determine to export its content differently based on the source item, or
	 * the nature of the dependency
	 * 
	 * @param requirementLink
	 *            the requirement link
	 * @param target
	 *            the target
	 * @param source
	 *            the source
	 * @param nature
	 *            the nature
	 * @param classpath
	 *            the classpath
	 * @param ms
	 *            the ms
	 * 
	 * @throws CoreException
	 */
	public void addDependencyClasspathEntry(Link requirementLink, Item target, Item source, DependencyNature nature, Set<IClasspathEntry> classpath, MultiStatus ms);
	
	
	/**
	 * Null if no java elements mapping at this item;.
	 * 
	 * @return the java element
	 */
	public IJavaElement[] getJavaElement(IJavaProject  ref);
	
	/**
	 * Resolve the destination. When a projet depends some imported packages and
	 * that this packages are exported by other project.
	 * 
	 * @param target
	 *            the target
	 * @param source
	 *            the source
	 * @param nature
	 *            the nature
	 * @param resolved
	 *            the resolved
	 * @param ms
	 *            the ms
	 * @param requirementLink
	 *            the requirement link
	 */
	public void resolvePackage(Link requirementLink, Item target, Item source, DependencyNature nature, Set<Item> resolved, MultiStatus ms);
	
	/**
	 * Gets the project from package.
	 * 
	 * @param packageItem
	 *            the package item
	 * 
	 * @return the project from package
	 */
	public Set<Item> getProjectFromPackage(Item packageItem);
	
	/**
	 * Return the set of items which must be resolved (ex : the imported packages which not mapped to a java element and has no java classpath entry).
	 * 
	 * 
	 * @param projectItem
	 *            the project item
	 * 
	 * @return the packages from project
	 */
	public Set<Item> getPackagesFromProject(Item projectItem);


	/**
	 * Gets the package name.
	 * 
	 * @param packageItem
	 *            the package item
	 * 
	 * @return the package name
	 */
	public String getPackageName(Item packageItem);


	/**
	 * Gets the package version.
	 * 
	 * @param packageItem
	 *            the package item
	 * 
	 * @return the package version
	 */
	public String getPackageVersion(Item packageItem);
	
	
}
