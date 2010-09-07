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
package fede.workspace.eclipse.java;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedConstructorsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.AddUnimplementedMethodsOperation;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.text.edits.TextEdit;

import adele.util.io.FileUtil;
import adele.util.io.JarUtil;
import fede.workspace.dependencies.eclipse.java.IJavaItemManager;
import fede.workspace.dependencies.eclipse.java.ItemDependenciesClasspathEntry;
import fede.workspace.dependencies.eclipse.java.IJavaItemManager.DependencyNature;
import fede.workspace.eclipse.MelusineProjectManager;
import fede.workspace.tool.view.WSPlugin;
import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.var.StringVariable;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;
import fr.imag.adele.cadse.core.var.Variable;
import fr.imag.adele.fede.workspace.si.view.View;

/**
 * The Class JavaProjectManager.
 */
public class JavaProjectManager extends MelusineProjectManager {

	/** The Constant DEFAULT_SOURCES_FOLDER_NAME. */
	public static final Variable				DEFAULT_SOURCES_FOLDER_NAME	= new StringVariable("sources");

	/** The Constant DEFAULT_OUTPUT_FOLDER_NAME. */
	public static final Variable				DEFAULT_OUTPUT_FOLDER_NAME	= new StringVariable("classes");

	/** The Constant DEFAULT_ACCESS_RULE. */
	public final static IAccessRule[]			DEFAULT_ACCESS_RULE			= new IAccessRule[] {
			JavaCore.newAccessRule(new Path("**/internal/**/*"), IAccessRule.K_DISCOURAGED),
			JavaCore.newAccessRule(new Path("**/*"), IAccessRule.K_ACCESSIBLE) };

	/** The Constant DEFAULT_PATH_ATTRIBUTES. */
	public final static IClasspathAttribute[]	DEFAULT_PATH_ATTRIBUTES		= new IClasspathAttribute[0];

	/**
	 * Determines if this is a Java Project.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return true, if checks if is java project
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static boolean isJavaProject(IProject project) throws CoreException {
		return project != null && project.hasNature(JavaCore.NATURE_ID);
	}

	/**
	 * Determines if a source file is a Java source.
	 * 
	 * @param sourceFile
	 *            the source file
	 * 
	 * @return true, if checks if is java file
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static boolean isJavaFile(IFile sourceFile) throws CoreException {
		return isJavaFileName(sourceFile.getName());
	}

	/**
	 * Determines if a file path corresponds to a Java source file name.
	 * 
	 * @param filePath
	 *            the file path
	 * 
	 * @return true, if checks if is java file path
	 */
	public static boolean isJavaFilePath(IPath filePath) {
		return isJavaFileName(filePath.lastSegment());
	}

	/**
	 * Determines if a file name corresponds to a Java source file name.
	 * 
	 * @param fileName
	 *            the file name
	 * 
	 * @return true, if checks if is java file name
	 */
	public static boolean isJavaFileName(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf("."));
		for (String javaExtension : JavaCore.getJavaLikeExtensions()) {
			if (javaExtension.equals(extension)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines if a file is a Java binary.
	 * 
	 * @param binaryFile
	 *            the binary file
	 * 
	 * @return true, if checks if is class file
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static boolean isClassFile(IFile binaryFile) throws CoreException {
		return isClassFileName(binaryFile.getName());
	}

	/**
	 * Determines if a file path corresponds to a Java source file name.
	 * 
	 * @param filePath
	 *            the file path
	 * 
	 * @return true, if checks if is class file path
	 */
	public static boolean isClassFilePath(IPath filePath) {
		return isClassFileName(filePath.lastSegment());
	}

	/**
	 * Determines if a file name corresponds to a Java binary file name.
	 * 
	 * @param fileName
	 *            the file name
	 * 
	 * @return true, if checks if is class file name
	 */
	public static boolean isClassFileName(String fileName) {
		return fileName.endsWith(".class");
	}

	/**
	 * Returns the relative path of a source file corresponding to the relative
	 * class file path specified.
	 * 
	 * @param classFilePath
	 *            the class file path
	 * 
	 * @return the source file path
	 */
	public static IPath getSourceFilePath(IPath classFilePath) {
		if (!isClassFileName(classFilePath.lastSegment())) {
			return null;
		}

		String segments[] = classFilePath.removeFileExtension().segments();
		String className = segments[segments.length - 1].split(Pattern.quote("$"))[0];
		String sourceName = className.concat(".java");
		segments[segments.length - 1] = sourceName;

		StringBuffer path = new StringBuffer();
		for (String segment : segments) {
			path.append(segment);
			if (!segment.equals(sourceName)) {
				path.append(IPath.SEPARATOR);
			}
		}

		return new Path(path.toString());
	}

	/**
	 * Gets the Java Project associated with an item.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @return the java project
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IJavaProject getJavaProject(Item item) throws CoreException {
		IProject project = getProject(item);
		return (project != null) ? JavaCore.create(project) : null;
	}

	/**
	 * Creates an empty Java project associated with the eclipse project
	 * corresponding to the item.
	 * 
	 * Automatically creates a source directory and default ouptut location.
	 * 
	 * @param item
	 *            the item
	 * @param monitor
	 *            the monitor
	 * @param project
	 *            the project
	 * @param cxt
	 *            the cxt
	 * @param defaultSourceFolder
	 *            the default source folder
	 * @param defaultClassFolder
	 *            the default class folder
	 * 
	 * @return the created java project
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IJavaProject createJavaProject(IProject project, Item item, IProgressMonitor monitor,
			ContextVariable cxt, Variable defaultSourceFolder, Variable defaultClassFolder) throws CoreException {

		/*
		 * Create empty project if needed
		 */

		if (project == null) {
			return null;
		}

		if (!project.exists()) {
			project.create(monitor);
		}
		project.open(monitor);

		IJavaProject javaProject = JavaCore.create(project);

		/*
		 * Add Java nature and initialize classpath
		 */
		IProjectDescription description = project.getDescription();
		List<String> natures = new ArrayList<String>(Arrays.asList(description.getNatureIds()));

		if (!description.hasNature(JavaCore.NATURE_ID)) {
			IClasspathEntry[] classpath = null;
			String sourceFolder = defaultSourceFolder.compute(cxt, item);
			String classFolder = defaultClassFolder.compute(cxt, item);
			if (sourceFolder != null && classFolder != null) {
				IFolder sources = project.getFolder(sourceFolder);
				if (!sources.exists()) {
					sources.create(false, true, monitor);
				}
				IFolder output = project.getFolder(classFolder);
				if (!output.exists()) {
					output.create(false, true, monitor);
				}
			}

			natures.add(JavaCore.NATURE_ID);
			description.setNatureIds(natures.toArray(new String[natures.size()]));
			project.setDescription(description, monitor);

			if (sourceFolder != null && classFolder != null) {
				IFolder sources = project.getFolder(sourceFolder);
				IFolder output = project.getFolder(classFolder);

				classpath = new IClasspathEntry[] { JavaCore.newSourceEntry(sources.getFullPath()),
						JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER), false),
						ItemDependenciesClasspathEntry.CLASSPATH_ENTRY };
				javaProject.setRawClasspath(classpath, output.getFullPath(), monitor);
			} else {
				classpath = new IClasspathEntry[] {
						JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER), false),
						ItemDependenciesClasspathEntry.CLASSPATH_ENTRY };
				javaProject.setRawClasspath(classpath, monitor);
			}

		}

		return javaProject;
	}

	/**
	 * Gets the Java source folder associated with the specified item.
	 * 
	 * If the resource associated with item is an eclipse project the default
	 * source folder is returned, otherwise it must me a folder in a Java
	 * project.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @return the source folder.
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IFolder getDefaultSourceFolder(Item item) throws CoreException {
		IResource eclipseResource = getResource(item);

		if (eclipseResource == null) {
			throw new CoreException(new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, 0,
					"Cannot find the project or resource.", null));
		}

		/*
		 * If the associated resource is a folder, just return it
		 * 
		 * TODO: Verify that the folder is actually a folder in a Java project
		 */
		if (eclipseResource.getType() == IResource.FOLDER) {
			return (IFolder) eclipseResource;
		}

		/*
		 * TODO: We should verify that the project is actually a Java project
		 */
		IProject project = getProject(item);
		return project.getFolder(DEFAULT_SOURCES_FOLDER_NAME.compute(ContextVariableImpl.DEFAULT, item));

		/*
		 * TODO: We should consider items associated with files, or parts of a
		 * file
		 */
	}

	/**
	 * Gets the Java package fragment root associated with the eclipse resource
	 * corresponding to the item.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @return the package fragment root
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IPackageFragmentRoot getPackageFragmentRoot(Item item) throws CoreException {
		return (IPackageFragmentRoot) JavaCore.create(getDefaultSourceFolder(item));
	}

	/**
	 * Adds the source folder associated with the item to the source entries in
	 * the classpath of the java project.
	 * 
	 * @param item
	 *            the item
	 * @param monitor
	 *            the monitor
	 * @param sourceFolder
	 *            the source folder
	 * @param specificOutputFolder
	 *            the specific output folder
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static void createJavaSourceFolder(Item item, IFolder sourceFolder, IFolder specificOutputFolder,
			IProgressMonitor monitor) throws CoreException {

		if (sourceFolder == null) {
			return;
		}

		IProject project = sourceFolder.getProject();

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return;
		}

		if (!sourceFolder.exists()) {
			sourceFolder.create(false, true, monitor);
		}

		IFolder defaultOutputFolder = project.getFolder(DEFAULT_OUTPUT_FOLDER_NAME.compute(ContextVariableImpl.DEFAULT,
				item));
		if (!defaultOutputFolder.exists()) {
			defaultOutputFolder.create(false, true, monitor);
		}

		IPath specificOutputPath = null;
		if (specificOutputFolder != null) {
			specificOutputPath = specificOutputFolder.getFullPath();
			if (!specificOutputFolder.exists()) {
				specificOutputFolder.create(false, true, monitor);
			}
		}

		List<IClasspathEntry> classpath = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		IClasspathEntry sourceEntry = JavaCore.newSourceEntry(sourceFolder.getFullPath(), ClasspathEntry.INCLUDE_ALL,
				ClasspathEntry.EXCLUDE_NONE, specificOutputPath);

		if (!classpath.contains(sourceEntry)) {
			classpath.add(sourceEntry);
			javaProject.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), defaultOutputFolder
					.getFullPath(), monitor);
		}

	}

	/**
	 * Removes the java source folder.
	 * 
	 * @param item
	 *            the item
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static void removeJavaSourceFolder(Item item, IProgressMonitor monitor) throws CoreException {
		IFolder sourceFolder = getDefaultSourceFolder(item);
		removeJavaSourceFolder(item, sourceFolder, monitor);
	}

	/**
	 * Removes the java source folder.
	 * 
	 * @param item
	 *            the item
	 * @param sourceFolder
	 *            the source folder
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static void removeJavaSourceFolder(Item item, IFolder sourceFolder, IProgressMonitor monitor)
			throws CoreException {
		if (sourceFolder == null) {
			return;
		}

		IProject project = sourceFolder.getProject();

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return;
		}

		IClasspathEntry ce = removeProjectClasspath(sourceFolder.getFullPath(), javaProject, monitor);
		if (ce != null) {
			IPath p = ce.getOutputLocation();
			p = p.removeFirstSegments(1).makeRelative();
			IFolder f = project.getFolder(p);
			if (f.exists()) {
				f.delete(true, monitor);
			}
		}

		if (!useDefaultClasses(javaProject)) {
			IFolder output = project.getFolder(DEFAULT_OUTPUT_FOLDER_NAME.compute(ContextVariableImpl.DEFAULT, item));
			if (output.exists()) {
				output.delete(false, monitor);
			}
		}
	}

	/**
	 * Use default classes.
	 * 
	 * @param project
	 *            the project
	 * 
	 * @return true, if successful
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	static boolean useDefaultClasses(IJavaProject project) throws JavaModelException {
		IClasspathEntry[] classpath = project.getRawClasspath();
		int cpLength = classpath.length;
		for (int j = 0; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getOutputLocation() == null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Replace project classpath.
	 * 
	 * @param ce
	 *            the ce
	 * @param project
	 *            the project
	 * @param progressMonitor
	 *            the progress monitor
	 * 
	 * @return the i classpath entry
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	static public IClasspathEntry replaceProjectClasspath(IClasspathEntry ce, IJavaProject project,
			IProgressMonitor progressMonitor) throws JavaModelException {
		if (project == null) {
			return null;
		}

		IClasspathEntry[] classpath = project.getRawClasspath();
		boolean find = false;
		IPath rootPath = ce.getPath();

		int cpLength = classpath.length;
		int newCPIndex = -1;
		IClasspathEntry findentry = null;
		for (int j = 0; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];
			if (rootPath.equals(entry.getPath())) {
				if (!find) {
					find = true;
					newCPIndex = j;
					classpath[newCPIndex++] = ce;
					findentry = entry;
				}
			} else if (find) {
				classpath[newCPIndex++] = entry;
			}
		}

		if (find) {
			if (newCPIndex == cpLength) {
				project.setRawClasspath(classpath, progressMonitor);
			} else {
				IClasspathEntry[] newClasspath = new IClasspathEntry[newCPIndex];
				System.arraycopy(classpath, 0, newClasspath, 0, newCPIndex);
				project.setRawClasspath(newClasspath, progressMonitor);
			}
		} else {
			IClasspathEntry[] newClasspath = new IClasspathEntry[cpLength + 1];
			System.arraycopy(classpath, 0, newClasspath, 0, cpLength);
			newClasspath[cpLength] = ce;
			project.setRawClasspath(newClasspath, progressMonitor);
		}
		return findentry;
	}

	/**
	 * Removes the project classpath.
	 * 
	 * @param rootPath
	 *            the root path
	 * @param project
	 *            the project
	 * @param progressMonitor
	 *            the progress monitor
	 * 
	 * @return the i classpath entry
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	static public IClasspathEntry removeProjectClasspath(IPath rootPath, IJavaProject project,
			IProgressMonitor progressMonitor) throws JavaModelException {
		if (project == null) {
			return null;
		}

		IClasspathEntry[] classpath = project.getRawClasspath();
		boolean find = false;
		int cpLength = classpath.length;
		int newCPIndex = -1;
		IClasspathEntry findentry = null;
		for (int j = 0; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];
			if (rootPath.equals(entry.getPath())) {
				if (!find) {
					find = true;
					newCPIndex = j;
					findentry = entry;
				}
			} else if (find) {
				classpath[newCPIndex++] = entry;
			}
		}
		if (find) {
			IClasspathEntry[] newClasspath = new IClasspathEntry[newCPIndex];
			System.arraycopy(classpath, 0, newClasspath, 0, newCPIndex);
			project.setRawClasspath(newClasspath, progressMonitor);
		}
		return findentry;
	}

	/**
	 * Deletes the Java source folder associated with the eclipse from the
	 * corresponding Java Project.
	 * 
	 * @param item
	 *            the item
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 */

	public static void deleteJavaSourceFolder(Item item, IProgressMonitor monitor) throws CoreException {
		deleteJavaSourceFolder(item, getDefaultSourceFolder(item), monitor);
	}

	/**
	 * Delete java source folder.
	 * 
	 * @param item
	 *            the item
	 * @param sourceFolder
	 *            the source folder
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static void deleteJavaSourceFolder(Item item, IFolder sourceFolder, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = getJavaProject(item);
		if (javaProject == null) {
			return;
		}

		if (sourceFolder == null) {
			return;
		}

		List<IClasspathEntry> classpath = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject.getRawClasspath()));
		classpath.remove(JavaCore.newSourceEntry(sourceFolder.getFullPath()));
		javaProject.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), monitor);
	}

	/**
	 * Updates the classpath of the Java project associated with an item based
	 * on changes in its dependencies.
	 * 
	 * @param item
	 *            the item
	 * @param monitor
	 *            the monitor
	 * 
	 * @return true, if update item dependencies classpath
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static boolean updateItemDependenciesClasspath(Item item, IProgressMonitor monitor) throws CoreException {

		LogicalWorkspace model = CadseCore.getCadseDomain().getLogicalWorkspace();
		if (model == null || model.getState() != WSModelState.RUN) {
			return false;
		}
		if (ResourcesPlugin.getWorkspace().isTreeLocked()) {
			return false;
		}

		IJavaProject javaProject = getJavaProject(item);
		if (javaProject == null || !javaProject.exists()) {
			return false;
		}

		IClasspathContainer oldContainer = JavaCore.getClasspathContainer(
				ItemDependenciesClasspathEntry.CLASSPATH_ENTRY_PATH, javaProject);
		IClasspathContainer newContainer = new ItemDependenciesClasspathEntry(javaProject,
				javaProject.getElementName(), item, null, null);

		/*
		 * Verify if the resolved dependencies actually changed
		 */
		if (oldContainer != null) {
			if (Arrays.asList(oldContainer.getClasspathEntries()).equals(
					Arrays.asList(newContainer.getClasspathEntries()))) {
				return false;
			}
		}

		/*
		 * Set the item dependencies container, this will trigger the Java
		 * builder if needed.
		 */
		JavaCore.setClasspathContainer(ItemDependenciesClasspathEntry.CLASSPATH_ENTRY_PATH,
				new IJavaProject[] { javaProject }, new IClasspathContainer[] { newContainer }, monitor);
		return true;
	}

	/**
	 * Creates a new package in the source folder corresponding to an item.
	 * 
	 * @param item
	 *            the item
	 * @param packageName
	 *            the package name
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i package fragment
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IPackageFragment createPackage(Item item, String packageName, IProgressMonitor monitor)
			throws CoreException {

		IResource resource = getResource(item);
		IJavaElement element = JavaCore.create(resource);

		/*
		 * The resource associated with the item is already a package
		 */
		if (element instanceof IPackageFragment) {
			if (packageName == null || element.getElementName().equals(packageName)) {
				return (IPackageFragment) element;
			}
		}

		if (packageName == null) {
			throw new CoreException(new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, 0,
					"Package name is null in createPackage !!!.", null));
		}

		/*
		 * The resource is a source folder
		 */
		if (element instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot root = (IPackageFragmentRoot) element;
			return root.createPackageFragment(packageName, true, monitor);
		}

		/*
		 * The resource is a project or a non source folder, get the associated
		 * source folder
		 */
		IPackageFragmentRoot sources = getPackageFragmentRoot(item);
		if (sources == null) {
			throw new CoreException(new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, 0,
					"Cannot find the sources folder.", null));
		}

		IProject project = getProject(item);
		if (!project.exists()) {
			project.create(monitor);
		}

		if (!sources.exists()) {
			getDefaultSourceFolder(item).create(true, true, monitor);
		}

		return sources.createPackageFragment(packageName, false, monitor);
	}

	/**
	 * Gets the package fragment corresponding to the spepcified package in the
	 * source folder associated with the specified item.
	 * 
	 * @param item
	 *            the item
	 * @param packageName
	 *            the package name
	 * 
	 * @return the package fragment
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IPackageFragment getPackageFragment(Item item, String packageName) throws CoreException {
		IPackageFragmentRoot source = getPackageFragmentRoot(item);

		if (source == null) {
			return null;
		}

		return source.getPackageFragment(packageName);
	}

	/**
	 * Creates an java class in the specified package, with the specified
	 * initial content.
	 * 
	 * @param javaPackage
	 *            the java package
	 * @param javaName
	 *            the java name
	 * @param monitor
	 *            the monitor
	 * @param contentBody
	 *            the content body
	 * 
	 * @return the i compilation unit
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static ICompilationUnit createJavaClass(IPackageFragment javaPackage, String javaName, String contentBody,
			IProgressMonitor monitor) throws CoreException {

		/*
		 * Create an empty aspect file
		 */

		ICompilationUnit classUnit = javaPackage.createCompilationUnit(javaName + ".java", "", true, monitor)
				.getWorkingCopy(monitor);

		/*
		 * Create a template aspect file from the code JDT generation facilities
		 */
		String lineSeparator = System.getProperty("line.separator", "\n");
		String fileComment = CodeGeneration.getFileComment(classUnit, lineSeparator);
		String typeComment = CodeGeneration.getTypeComment(classUnit, javaName, lineSeparator);
		String typeContents = "public class " + javaName + " {" + lineSeparator
				+ (contentBody != null ? contentBody : "") + "}" + lineSeparator;
		String fileContents = CodeGeneration.getCompilationUnitContent(classUnit, fileComment, typeComment,
				typeContents, lineSeparator);
		classUnit.getBuffer().setContents(fileContents);

		classUnit.commitWorkingCopy(true, monitor);
		classUnit.discardWorkingCopy();

		return classUnit;

	}

	/**
	 * Creates an empty Java class.
	 * 
	 * @param javaPackage
	 *            the java package
	 * @param javaName
	 *            the java name
	 * @param monitor
	 *            the monitor
	 * 
	 * @return the i compilation unit
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static ICompilationUnit createJavaClass(IPackageFragment javaPackage, String javaName,
			IProgressMonitor monitor) throws CoreException {
		return createJavaClass(javaPackage, javaName, null, monitor);
	}

	/**
	 * Creates a Java class in the source folder of the specified item.
	 * 
	 * @param item
	 *            the item
	 * @param packageName
	 *            the package name
	 * @param javaName
	 *            the java name
	 * @param monitor
	 *            the monitor
	 * @param contentBody
	 *            the content body
	 * 
	 * @return the i compilation unit
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static ICompilationUnit createJavaClass(Item item, String packageName, String javaName, String contentBody,
			IProgressMonitor monitor) throws CoreException {
		return createJavaClass(createPackage(item, packageName, monitor), javaName, contentBody, monitor);
	}

	/**
	 * Calculates the classpath dependencies of the specified item.
	 * 
	 * The dependencies are calculated based on the required links of the
	 * specified root item and of all its descendants through the containment
	 * relationship.
	 * 
	 * The Manager of the source item of a requirement link can determine that
	 * the link is transitive. In that case, the classpath dependencies of the
	 * target of the link are added to the dependencies of the source item.
	 * 
	 * @param item
	 *            the item
	 * @param ms
	 *            the ms
	 * 
	 * @return the set< i classpath entry>
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	static public Set<IClasspathEntry> calculateDependencies(final Item item, MultiStatus ms) throws CoreException {
		/*
		 * Remove any previously identified item dependencies resolution problem
		 */
		// Error :The resource tree is locked for modifications.
		if (ResourcesPlugin.getWorkspace().isTreeLocked()) {
			// return calculateDependeciesLater(item.getLogicalWorkspace(),
			// item.getId(), item);

			return Collections.emptySet();
		}

		return getClasspathManager(item).calculateDependencies(item, ms);
	}

	// public static Set<IClasspathEntry> calculateDependeciesLater(final
	// LogicalWorkspace model, final UUID id,
	// final Item item) {
	// try {
	// if (item == null && id == null) {
	// return Collections.emptySet();
	// }
	// String label = "update classpath";
	// if (item != null) {
	// label += " " + item.getUniqueName() + " " + item.getId();
	// } else if (id != null) {
	// label += " " + id;
	// }
	//
	// UpdateClasspathLater j = new UpdateClasspathLater(label, id, item,
	// model);
	//
	// j.schedule(10);
	// j.testworkspaceToSleep();
	// } catch (Throwable e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return Collections.emptySet();
	// }

	private static IClasspathManager getClasspathManager(Item item) {
		try {
			ContentItem cm = item.getContentItem();
			if (cm instanceof IClasspathManager) {
				IClasspathManager classpathManager = (IClasspathManager) cm;
				return classpathManager;
			}
			IItemManager im = item.getType().getItemManager();
			if (im instanceof IClasspathManager) {
				return ((IClasspathManager) im);
			}
		} catch (Throwable e) {
			WSPlugin.logException(e);
		}
		return DEFAULT_EClasspathManager;
	}

	public final static IClasspathManager	DEFAULT_EClasspathManager	= new DefaultClasspathManager();

	/**
	 * Algorithme par défaut ...
	 * 
	 * @param classpathManager
	 * @param item
	 * @param ms
	 * @return
	 */
	public static Set<IClasspathEntry> defaultCalulateDependencies(IClasspathManager classpathManager, Item item,
			MultiStatus ms) {
		Set<IClasspathEntry> dependencies = new HashSet<IClasspathEntry>();
		Set<Item> visitedItems = new HashSet<Item>();
		// accumule les item à resoudre
		Set<Item> resolvedPackageItems = new HashSet<Item>();
		addDependencies(classpathManager, item, item, dependencies, visitedItems, resolvedPackageItems, ms);
		ContentItem cm = item.getContentItem();
		if (cm != null && (cm instanceof IJavaItemManager)) {
			IJavaItemManager javaManager = (IJavaItemManager) cm;
			addDependenciesPackages(classpathManager, javaManager, item, dependencies, visitedItems,
					resolvedPackageItems, ms);
		}
		return dependencies;
	}

	/**
	 * Adds the dependencies packages.
	 * 
	 * @param classpathManager
	 * 
	 * @param resolver
	 *            the resolver
	 * @param source
	 *            the source
	 * @param dependencies
	 *            the dependencies
	 * @param visitedItems
	 *            the visited items
	 * @param resolvedPackageItems
	 *            the resolved package items
	 * @param ms
	 *            the ms
	 */
	private static void addDependenciesPackages(IClasspathManager classpathManager, IJavaItemManager resolver,
			Item source, Set<IClasspathEntry> dependencies, Set<Item> visitedItems, Set<Item> resolvedPackageItems,
			MultiStatus ms) {
		Set<Item> projectItem = new HashSet<Item>();
		HashSet<String> packagesDef = new HashSet<String>();
		for (Item packageItem : resolvedPackageItems) {
			Set<Item> projectsFromPackage = resolver.getProjectFromPackage(packageItem);
			if (projectsFromPackage != null) {
				projectItem.addAll(projectsFromPackage);
			}

			packagesDef.add(resolver.getPackageName(packageItem));
		}
		for (Item p : projectItem) {
			ContentItem cm = p.getContentItem();
			if (cm != null && (cm instanceof IJavaItemManager)) {
				IJavaItemManager javaManager = (IJavaItemManager) cm;
				javaManager.addDependencyClasspathEntry(null, p, source,
						IJavaItemManager.DependencyNature.JavaCompilationDependency, dependencies, ms);
			}
		}
	}

	/**
	 * Compute used package.
	 * 
	 * @param resolver
	 *            the resolver
	 * @param packagesDef
	 *            all packages used
	 * @param packageList
	 *            some package
	 * 
	 * @return the pacakges with a version.
	 */
	private static Map<String, String> computeUsedPackage(IJavaItemManager resolver, HashSet<String> packagesDef,
			Set<Item> packageList) {
		Map<String, String> return_used = new HashMap<String, String>();
		for (Item item2 : packageList) {
			String n = resolver.getPackageName(item2);
			if (packagesDef.contains(n)) {
				return_used.put(n, resolver.getPackageVersion(item2));
			}
		}
		return return_used;
	}

	/**
	 * Iterate over all required links of the specified item adding a dependency
	 * to the classpath of the root item.
	 * 
	 * Include recursively the dependencies of the parts of the specified source
	 * item.
	 * 
	 * The source item can be a contained item of the specified root item, or an
	 * item related by transitive requirement links to the containment tree of
	 * the root.
	 * 
	 * @param classpathManager
	 * 
	 * @param root
	 *            the root
	 * @param sourceItem
	 *            the source item
	 * @param dependencies
	 *            the dependencies
	 * @param visitedItems
	 *            the visited items
	 * @param ms
	 *            the ms
	 * @param resolvedPackageItems
	 *            the resolved package items
	 * 
	 * @throws CoreException
	 */
	static private void addDependencies(IClasspathManager classpathManager, Item root, Item sourceItem,
			Set<IClasspathEntry> dependencies, Set<Item> visitedItems, Set<Item> resolvedPackageItems, MultiStatus ms) {

		if (sourceItem == null) {
			return;
		}

		/*
		 * containment links form an acyclic graph. However, in the case of
		 * transitive links this may create a cycle between two different
		 * containment trees.
		 */
		if (!visitedItems.add(sourceItem)) {
			return;
		}

		for (Link link : sourceItem.getOutgoingLinks()) {
			// add dependency to required links
			if (classpathManager.isJavaDependency(link)) {
				addDependency(classpathManager, root, link, dependencies, visitedItems, resolvedPackageItems, ms);
			}

			// recurse into containment items
			if (link.isLinkResolved() && classpathManager.isJavaPartTransitive(link)) {
				addDependencies(classpathManager, root, link.getResolvedDestination(), dependencies, visitedItems,
						resolvedPackageItems, ms);
			}

		}
	}

	/**
	 * Adds a new dependency from the the root item to the the destination item
	 * of the specified link.
	 * 
	 * @param ms
	 *            the ms
	 * @param root
	 *            the root
	 * @param requirementLink
	 *            the requirement link
	 * @param dependencies
	 *            the dependencies
	 * @param visitedItems
	 *            the visited items
	 * @param resolvedPackageItems
	 *            the resolved package items
	 */
	static private void addDependency(IClasspathManager classpathManager, Item root, Link requirementLink,
			Set<IClasspathEntry> dependencies, Set<Item> visitedItems, Set<Item> resolvedPackageItems, MultiStatus ms) {

		UUID requiredItemId = requirementLink.getDestinationId();
		Item requiredItem = requirementLink.getResolvedDestination();

		Item targetItem = requiredItem;

		if (requiredItem == null) {

			// /*
			// * If the required item is not in the workspace but it is in the
			// * composition tree of the root object we just suppose that the
			// * requirement can be ignored, as it's somehow subsumed by the
			// * composition relationship
			// */
			// if (root.isComposite() && root.isClosed() &&
			// root.containsComponent(requiredItemId)) {
			// return;
			// }
			//
			// /*
			// * Otherwise, when the required item is not directly present in
			// the
			// * workspace, we try to resolve the dependency by looking for a
			// * closed composite that contains it.
			// */
			// IWorkspaceLogique m =
			// requirementLink.getSource().getWorkspaceLogique();
			// int min = Integer.MAX_VALUE;
			// for (Item composite : m.getItems()) {
			// if (composite.isComposite() && composite.isClosed() &&
			// composite.getComponents().size() < min
			// && composite.containsComponent(requiredItemId)) {
			// targetItem = composite;
			// min = composite.getComponents().size();
			// }
			// }
		}

		/*
		 * Mark an error on the root item, in cases we are not able to resolve
		 * the dependency
		 */
		if (targetItem == null || !targetItem.isResolved()) {

			ms.add(new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, 0, MessageFormat.format(
					"Item dependencies of {1}, classpath resolution error : "
							+ "required item not present {0}, link : {2}", requiredItemId, root.getName(),
					requirementLink), null));
			return;
		}
		//

		ContentItem cm = targetItem.getContentItem();
		if (cm != null && (cm instanceof IJavaItemManager)) {
			IJavaItemManager javaManager = (IJavaItemManager) cm;
			javaManager.addDependencyClasspathEntry(requirementLink, targetItem, root,
					IJavaItemManager.DependencyNature.JavaCompilationDependency, dependencies, ms);
			javaManager.resolvePackage(requirementLink, targetItem, root, DependencyNature.JavaCompilationDependency,
					resolvedPackageItems, ms);

			/*
			 * If the link is considered to be transitive, add the dependencies
			 * of the target item
			 */
			if (requiredItem != null && classpathManager.isJavaTransitive(javaManager, requirementLink)) {//
				addDependencies(classpathManager, root, targetItem, dependencies, visitedItems, resolvedPackageItems,
						ms);
			}
		}

	}

	/**
	 * Creates a new non exported Project classpath entry.
	 * 
	 * @param fullpath
	 *            the fullpath
	 * 
	 * @return the i classpath entry
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IClasspathEntry newProjectEntry(IPath fullpath) throws CoreException {
		return JavaCore.newProjectEntry(fullpath, DEFAULT_ACCESS_RULE, true, DEFAULT_PATH_ATTRIBUTES, false);
	}

	/**
	 * Creates a new Library classpath entry.
	 * 
	 * @param fullpath
	 *            the fullpath
	 * @param sourcePath
	 *            the source path
	 * @param exported
	 *            the exported
	 * 
	 * @return the i classpath entry
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IClasspathEntry newLibraryEntry(IPath fullpath, IPath sourcePath, boolean exported)
			throws CoreException {
		return JavaCore.newLibraryEntry(fullpath, sourcePath, null, DEFAULT_ACCESS_RULE, DEFAULT_PATH_ATTRIBUTES,
				exported);
	}

	/**
	 * Creates a new Library classpath entry.
	 * 
	 * @param fullpath
	 *            the fullpath
	 * @param exported
	 *            the exported
	 * 
	 * @return the i classpath entry
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IClasspathEntry newLibraryEntry(IPath fullpath, boolean exported) throws CoreException {
		return JavaCore.newLibraryEntry(fullpath, null, null, DEFAULT_ACCESS_RULE, DEFAULT_PATH_ATTRIBUTES, exported);
	}

	private static final class DefaultClasspathManager implements IClasspathManager {
		public boolean isJavaDependency(Link link) {
			return link.isRequire();
		}

		public Set<IClasspathEntry> calculateDependencies(Item item, MultiStatus ms) throws CoreException {
			return defaultCalulateDependencies(this, item, ms);
		}

		public boolean isJavaPartTransitive(Link link) {
			return link.getLinkType().isPart();
		}

		public boolean isJavaTransitive(IJavaItemManager javaManager, Link requirementLink) {
			return javaManager.isTransitiveLink(requirementLink);
		}
	}

	/*
	 * Un job pour mettre a jour en asynchrone le classpath. Il attend de le
	 * workspace logique est dans l'etat run. Il retourne une erreur si l'item
	 * associer n'exite pas.
	 */
	// private static final class UpdateClasspathLater extends Job {
	//
	// class UpdateClasspathLaterL extends WorkspaceListener {
	// @Override
	// public void workspaceChanged(ImmutableWorkspaceDelta delta) {
	// if (delta.getModelstate() == WSModelState.RUN) {
	// model.removeListener(this);
	// wakeUp();
	// }
	// }
	// }
	//
	// private final UUID id;
	// private final Item item;
	// private final LogicalWorkspace model;
	//
	// private UpdateClasspathLater(String name, UUID id, Item item,
	// LogicalWorkspace model) {
	// super(name);
	// this.id = id;
	// this.item = item;
	// this.model = model;
	// }
	//
	// protected boolean doUpdateClasspath(IProgressMonitor monitor, Item
	// currentItem) throws CoreException {
	// if (currentItem == null) {
	// currentItem = model.getItem(id);
	// }
	// if (currentItem == null) {
	// throw new CoreException(new Status(Status.ERROR, WSPlugin.PLUGIN_ID,
	// "Cannot found item for id " + id));
	// }
	// updateItemDependenciesClasspath(currentItem, monitor);
	//
	// return false;
	// }
	//
	// class UpdateClasspathWorkspaceRunnable implements IWorkspaceRunnable {
	// boolean fCanceled = false;
	//
	// public void run(IProgressMonitor monitor) throws CoreException {
	// fCanceled = doUpdateClasspath(monitor, item);
	// }
	//
	// public boolean isCanceled() {
	// return fCanceled;
	// }
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	// */
	// protected IStatus run(IProgressMonitor monitor) {
	// try {
	// UpdateClasspathWorkspaceRunnable runnable = new
	// UpdateClasspathWorkspaceRunnable();
	// ResourcesPlugin.getWorkspace().run(runnable, monitor);
	// if (runnable.isCanceled()) {
	// return new Status(IStatus.CANCEL, WSPlugin.PLUGIN_ID, IStatus.CANCEL, "",
	// null); //$NON-NLS-1$
	// }
	//
	// } catch (CoreException e) {
	// String message = e.getMessage();
	// return new Status(IStatus.ERROR, WSPlugin.PLUGIN_ID, IStatus.OK, message,
	// e);
	// }
	// return new Status(IStatus.OK, WSPlugin.PLUGIN_ID, IStatus.OK, "", null);
	// //$NON-NLS-1$
	// }
	//
	// public void testworkspaceToSleep() {
	// if (model.getState() != WSModelState.RUN) {
	// model.addListener(new UpdateClasspathLaterL(),
	// ChangeID.toFilter(ChangeID.MODEL_STATE));
	// sleep();
	// }
	// }
	//
	// }
	/**
	 * This interface represents the set of source folders of a Java project.
	 * 
	 * It allows to quickly test if a source file is present in the project.
	 * 
	 * @author vega
	 */
	public static interface SourceRepository {

		/**
		 * Tests if source file is present in the project.
		 * 
		 * @param sourceFile
		 *            the source file
		 * 
		 * @return true, if exists
		 */
		public boolean exists(IPath sourceFile);

		/**
		 * Gets a source file resource from the project.
		 * 
		 * @param sourceFile
		 *            the source file
		 * 
		 * @return the i resource
		 */
		public IResource findMember(IPath sourceFile);
	}

	/**
	 * Returns an object representing all the source folders of a Java project.
	 * 
	 * @param javaProject
	 *            the java project
	 * 
	 * @return the source repository
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static SourceRepository getSourceRepository(IJavaProject javaProject) throws CoreException {
		return new JavaSourceRepository(javaProject);
	}

	/**
	 * A class that represents all the source folders of a Java project.
	 * 
	 * @author vega
	 */
	private static class JavaSourceRepository implements SourceRepository {

		/** The project. */
		private final IProject		project;

		/** The source roots. */
		private final List<IFolder>	sourceRoots;

		/**
		 * Instantiates a new java source repository.
		 * 
		 * @param javaProject
		 *            the java project
		 * 
		 * @throws CoreException
		 *             the core exception
		 */
		public JavaSourceRepository(IJavaProject javaProject) throws CoreException {

			this.project = javaProject.getProject();
			this.sourceRoots = new ArrayList<IFolder>();
			for (IClasspathEntry entry : javaProject.getRawClasspath()) {
				if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
					continue;
				}

				sourceRoots.add(project.getFolder(getProjectRelativePath(entry.getPath())));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fede.workspace.eclipse.java.JavaProjectManager.SourceRepository#exists(org.eclipse.core.runtime.IPath)
		 */
		public boolean exists(IPath sourceFile) {
			if (sourceFile == null) {
				return false;
			}

			for (IFolder sourceRoot : sourceRoots) {
				if (sourceRoot.exists(sourceFile)) {
					return true;
				}
			}

			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fede.workspace.eclipse.java.JavaProjectManager.SourceRepository#findMember(org.eclipse.core.runtime.IPath)
		 */
		public IResource findMember(IPath sourceFile) {
			if (sourceFile == null) {
				return null;
			}

			for (IFolder sourceRoot : sourceRoots) {
				IResource resource = sourceRoot.findMember(sourceFile);
				if (resource != null) {
					return resource;
				}
			}

			return null;
		}

		/**
		 * Gets the project relative path.
		 * 
		 * @param sourcePath
		 *            the source path
		 * 
		 * @return the project relative path
		 */
		private IPath getProjectRelativePath(IPath sourcePath) {
			return getRelativePath(project.getFullPath(), sourcePath);
		}

		/**
		 * Gets the relative path.
		 * 
		 * @param rootFullPath
		 *            the root full path
		 * @param memberFullPath
		 *            the member full path
		 * 
		 * @return the relative path
		 */
		private static IPath getRelativePath(IPath rootFullPath, IPath memberFullPath) {
			if (rootFullPath.isPrefixOf(memberFullPath)) {
				memberFullPath = memberFullPath.removeFirstSegments(rootFullPath.segmentCount());
			}
			return memberFullPath;

		}
	}

	/**
	 * Generate composite jar before close.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	static public void generateCompositeJarBeforeClose(Item item) throws CadseException {
		IContainer p = item.getMainMappingContent(IContainer.class);
		IFolder destFolder = p.getFolder(new Path("dest"));
		if (!destFolder.exists()) {
			try {
				destFolder.create(false, true, View.getDefaultMonitor());
			} catch (CoreException e1) {
				throw new CadseException("Cannot create folder {0} : {1}.", e1, destFolder.getFullPath()
						.toPortableString(), e1.getMessage());
			}
		}
		IPath localPath = p.getLocation();

		for (Link l : item.getOutgoingLinks()) {
			if (l.isComposition()) {
				Item dest = l.getResolvedDestination();
				if (dest != null) {
					IContainer destContainer = dest.getMainMappingContent(IContainer.class);
					if (destContainer != null && destContainer.exists()) {
						IFolder sourceDestContainer = destContainer.getFolder(new Path("sources"));
						if (sourceDestContainer.exists()) {
							IPath jarPath = localPath.append("dest").append(l.getDestinationId() + "-sources.jar");
							try {
								JarUtil.jar(sourceDestContainer.getLocation().toOSString(), jarPath.toOSString(),
										sourceDestContainer.getLocation().toFile());
							} catch (FileNotFoundException e) {
								throw new CadseException("Cannot create jar file {0} : {1}.", e, jarPath
										.toPortableString(), e.getMessage());

							} catch (IOException e) {
								throw new CadseException("Cannot create jar file {0} : {1}.", e, jarPath
										.toPortableString(), e.getMessage());
							}
						}
						IFolder destDestFolder = destContainer.getFolder(new Path("dest"));
						if (destDestFolder.exists()) {
							try {
								FileUtil.copy(destDestFolder.getLocation().toFile(), destFolder.getLocation().toFile(),
										false);
							} catch (IOException e) {
								throw new CadseException("Cannot copy folder {0} : {1}.", e, destDestFolder
										.getFullPath().toPortableString(), e.getMessage());
							}
						}
					}
				}
			}
		}
		for (Link l : item.getOutgoingLinks()) {
			if (l.isComposition()) {
				IFolder f = p.getFolder(new Path("components").append(l.getDestinationId().toString()));
				if (!f.exists()) {
					continue;
				}
				IFolder faj = f.getFolder("aspects");
				if (faj.exists()) {
					try {
						JarUtil.jar(faj.getLocation().toOSString(), localPath.append("dest").append(
								l.getDestinationId() + "-aspects.jar").toOSString(), faj.getLocation().toFile());
					} catch (FileNotFoundException e) {
						throw new CadseException("Cannot create folder {0} : {1}.", e, destFolder.getFullPath()
								.toPortableString(), e.getMessage());
					} catch (IOException e) {
						throw new CadseException("Cannot create folder {0} : {1}.", e, destFolder.getFullPath()
								.toPortableString(), e.getMessage());
					}
				}
				IFolder fja = f.getFolder("classes");
				if (fja.exists()) {
					try {
						JarUtil.jar(fja.getLocation().toOSString(), localPath.append("dest").append(
								l.getDestinationId() + "-classes.jar").toOSString(), fja.getLocation().toFile());
					} catch (FileNotFoundException e) {
						throw new CadseException("Cannot create folder {0} : {1}.", e, destFolder.getFullPath()
								.toPortableString(), e.getMessage());
					} catch (IOException e) {
						throw new CadseException("Cannot create folder {0} : {1}.", e, destFolder.getFullPath()
								.toPortableString(), e.getMessage());
					}
				}
			}
		}
		try {
			destFolder.refreshLocal(IResource.DEPTH_ONE, View.getDefaultMonitor());
		} catch (CoreException e) {
			throw new CadseException("Cannot create folder {0} : {1}.", e, destFolder.getFullPath().toPortableString(),
					e.getMessage());
		}
	}

	@Deprecated
	static public void addProjectClasspath(IJavaProject project, IClasspathEntry ce, IProgressMonitor progressMonitor)
			throws JavaModelException {
		addProjectClasspath(project, ce, progressMonitor, true);
	}

	/**
	 * Adds the project classpath.
	 * 
	 * @param project
	 *            the project
	 * @param ce
	 *            the ce
	 * @param progressMonitor
	 *            the progress monitor
	 * @param overwrite
	 *            true si force le classpath
	 * 
	 * @throws JavaModelException
	 *             the java model exception
	 */
	static public void addProjectClasspath(IJavaProject project, IClasspathEntry ce, IProgressMonitor progressMonitor,
			boolean overwrite) throws JavaModelException {
		if (project == null) {
			return;
		}

		IClasspathEntry[] classpath = project.getRawClasspath();
		int cpLength = classpath.length;
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getPath().equals(ce.getPath())) {
				if (overwrite) {
					classpath[i] = ce;
					project.setRawClasspath(classpath, progressMonitor);
				}
				return;
			}
		}

		IClasspathEntry[] newClasspath = new IClasspathEntry[cpLength + 1];
		;
		System.arraycopy(classpath, 0, newClasspath, 0, cpLength);
		newClasspath[cpLength] = ce;
		project.setRawClasspath(newClasspath, progressMonitor);
	}

	/**
	 * Class used in stub creation routines to add needed imports to a
	 * compilation unit.
	 */
	public static class ImportsManager {

		/** The imports rewrite. */
		private ImportRewrite	fImportsRewrite;

		/* package *//**
						 * Instantiates a new imports manager.
						 * 
						 * @param astRoot
						 *            the ast root
						 * 
						 * @throws CoreException
						 *             the core exception
						 */
		ImportsManager(CompilationUnit astRoot) throws CoreException {
			fImportsRewrite = CodeStyleConfiguration.createImportRewrite(astRoot, true);
		}

		/* package *//**
						 * Gets the compilation unit.
						 * 
						 * @return the compilation unit
						 */
		ICompilationUnit getCompilationUnit() {
			return fImportsRewrite.getCompilationUnit();
		}

		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an
		 * import of an other type with the same simple name, the import is not
		 * added.
		 * 
		 * @param qualifiedTypeName
		 *            The fully qualified name of the type to import (dot
		 *            separated).
		 * 
		 * @return Returns the simple type name that can be used in the code or
		 *         the fully qualified type name if an import conflict prevented
		 *         the import.
		 */
		public String addImport(String qualifiedTypeName) {
			return fImportsRewrite.addImport(qualifiedTypeName);
		}

		/**
		 * Adds a new import declaration that is sorted in the existing imports.
		 * If an import already exists or the import would conflict with an
		 * import of an other type with the same simple name, the import is not
		 * added.
		 * 
		 * @param typeBinding
		 *            the binding of the type to import
		 * 
		 * @return Returns the simple type name that can be used in the code or
		 *         the fully qualified type name if an import conflict prevented
		 *         the import.
		 */
		public String addImport(ITypeBinding typeBinding) {
			return fImportsRewrite.addImport(typeBinding);
		}

		/**
		 * Adds a new import declaration for a static type that is sorted in the
		 * existing imports. If an import already exists or the import would
		 * conflict with an import of an other static import with the same
		 * simple name, the import is not added.
		 * 
		 * @param declaringTypeName
		 *            The qualified name of the static's member declaring type
		 * @param simpleName
		 *            the simple name of the member; either a field or a method
		 *            name.
		 * @param isField
		 *            <code>true</code> specifies that the member is a field,
		 *            <code>false</code> if it is a method.
		 * 
		 * @return returns either the simple member name if the import was
		 *         successful or else the qualified name if an import conflict
		 *         prevented the import.
		 * 
		 * @since 3.2
		 */
		public String addStaticImport(String declaringTypeName, String simpleName, boolean isField) {
			return fImportsRewrite.addStaticImport(declaringTypeName, simpleName, isField);
		}

		/* package *//**
						 * Creates the.
						 * 
						 * @param needsSave
						 *            the needs save
						 * @param monitor
						 *            the monitor
						 * 
						 * @throws CoreException
						 *             the core exception
						 */
		void create(boolean needsSave, IProgressMonitor monitor) throws CoreException {
			TextEdit edit = fImportsRewrite.rewriteImports(monitor);
			JavaModelUtil.applyEdit(fImportsRewrite.getCompilationUnit(), edit, needsSave, null);
		}

		/* package *//**
						 * Removes the import.
						 * 
						 * @param qualifiedName
						 *            the qualified name
						 */
		void removeImport(String qualifiedName) {
			fImportsRewrite.removeImport(qualifiedName);
		}

		/* package *//**
						 * Removes the static import.
						 * 
						 * @param qualifiedName
						 *            the qualified name
						 */
		void removeStaticImport(String qualifiedName) {
			fImportsRewrite.removeStaticImport(qualifiedName);
		}
	}

	/**
	 * Imported by org.eclipse.jdt.ui.wizards.NewTypeWizardPage Creates the
	 * bodies of all unimplemented methods and constructors and adds them to the
	 * type. Method is typically called by implementers of
	 * <code>NewTypeWizardPage</code> to add needed method and constructors.
	 * 
	 * @param type
	 *            the type for which the new methods and constructor are to be
	 *            created
	 * @param doConstructors
	 *            if <code>true</code> unimplemented constructors are created
	 * @param doUnimplementedMethods
	 *            if <code>true</code> unimplemented methods are created
	 * @param monitor
	 *            a progress monitor to report progress
	 * @param addComments
	 *            the add comments
	 * 
	 * @return the created methods.
	 * 
	 * @throws CoreException
	 *             thrown when the creation fails.
	 */
	static public IMethod[] createInheritedMethods(IType type, boolean addComments, boolean doConstructors,
			boolean doUnimplementedMethods, IProgressMonitor monitor) throws CoreException {
		final ICompilationUnit cu = type.getCompilationUnit();
		JavaModelUtil.reconcile(cu);

		// set up again
		IMethod[] typeMethods = type.getMethods();
		Set<String> handleIds = new HashSet<String>(typeMethods.length);
		for (int index = 0; index < typeMethods.length; index++) {
			handleIds.add(typeMethods[index].getHandleIdentifier());
		}

		ArrayList<IMethod> newMethods = new ArrayList<IMethod>();
		// CodeGenerationSettings settings=
		// JavaPreferencesSettings.getCodeGenerationSettings(type.getJavaProject());
		// settings.createComments= addComments;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setSource(cu);
		CompilationUnit unit = (CompilationUnit) parser.createAST(new SubProgressMonitor(monitor, 1));
		final ITypeBinding binding = ASTNodes.getTypeBinding(unit, type);
		if (binding != null) {
			if (doUnimplementedMethods) {
				AddUnimplementedMethodsOperation operation = new AddUnimplementedMethodsOperation(unit, binding, null,
						-1, true, true, true);
				operation.setCreateComments(addComments);
				operation.run(monitor);
			}
			if (doConstructors) {
				AddUnimplementedConstructorsOperation operation = new AddUnimplementedConstructorsOperation(unit,
						binding, null, -1, true, true, true);
				operation.setOmitSuper(true);
				operation.setCreateComments(addComments);
				operation.run(monitor);
			}
		}
		JavaModelUtil.reconcile(cu);
		typeMethods = type.getMethods();
		for (int index = 0; index < typeMethods.length; index++) {
			if (!handleIds.contains(typeMethods[index].getHandleIdentifier())) {
				newMethods.add(typeMethods[index]);
			}
		}
		IMethod[] methods = new IMethod[newMethods.size()];
		newMethods.toArray(methods);
		return methods;
	}

}
