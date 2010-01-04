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
/*
 * Adele/LIG/ Grenoble University, France
 * 2006-2008
 */
package fede.workspace.eclipse.composition.java;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import fede.workspace.eclipse.MelusineProjectManager;
import fede.workspace.eclipse.composer.EclipseExportedContent;
import fede.workspace.eclipse.exporter.EclipseExporter;
import fede.workspace.eclipse.java.JavaProjectManager;
import fr.imag.adele.cadse.core.content.ContentItem;

/**
 * The Class JavaProjectExporter.
 */
public class JavaProjectExporter extends EclipseExporter {

	/** The Constant JAVA_TYPE_EXPORTER. */
	public final static Class<?>	JAVA_TYPE_EXPORTER	= File.class;

	/**
	 * Instantiates a new java project exporter.
	 * 
	 * @param cm
	 *            the cm
	 */
	public JavaProjectExporter(ContentItem cm) {
		super(cm, JAVA_TYPE_EXPORTER);
	}

	/**
	 * Instantiates a new java project exporter.
	 * 
	 * @param cm
	 *            the cm
	 * @param exporterTypes
	 *            the exporter types
	 */
	public JavaProjectExporter(ContentItem cm, Class<?>... exporterTypes) {
		super(cm, exporterTypes);
	}

	/**
	 * Updates an existing packaged binary version of the content of the item in
	 * this project.
	 * 
	 * Scans all output directories of the java project and updates all modified
	 * classes in the packaged item version.
	 * 
	 * If no resource delta is specified all the binary contents are copied to
	 * the packaged version.
	 * 
	 * @param monitor
	 *            the monitor
	 * @param eclipseExportedContent
	 *            the eclipse exported content
	 * @param projectDelta
	 *            the project delta
	 * @param exporterType
	 *            the exporter type
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	@Override
	protected void exportItem(EclipseExportedContent eclipseExportedContent, IResourceDelta projectDelta,
			IProgressMonitor monitor, Class<?> exporterType) throws CoreException {

		/*
		 * skip empty notifications
		 */
		if ((projectDelta != null) && (projectDelta.getKind() == IResourceDelta.NO_CHANGE)) {
			return;
		}

		/*
		 * Verify this item is actually hosted in a Java Project
		 */
		if (!JavaProjectManager.isJavaProject(MelusineProjectManager.getProject(getItem()))) {
			return;
		}

		IJavaProject javaProject = JavaProjectManager.getJavaProject(getItem());

		/*
		 * TODO We scan all output directories, we should only scan the output
		 * directory associated with the item.
		 * 
		 * We need to handle mapping variants in which there are many composites
		 * in a single java project, this is the case for example when a
		 * composite has parts that are themselves java composites.
		 */
		Set<IPath> outputLocations = new HashSet<IPath>();
		for (IClasspathEntry entry : javaProject.getResolvedClasspath(true)) {
			if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
				continue;
			}

			IPath outputPath = entry.getOutputLocation();
			if (outputPath == null) {
				outputPath = javaProject.getOutputLocation();
			}

			outputLocations.add(getRelativePath(outputPath));
		}

		Scanner scanner = new Scanner(eclipseExportedContent);
		for (IPath outputPath : outputLocations) {
			IFolder outputRoot = getFolder(outputPath);
			IResourceDelta outputDelta = (projectDelta != null) ? projectDelta.findMember(outputRoot
					.getProjectRelativePath()) : null;

			// If no modification of the output location just skip it
			if ((projectDelta != null) && (outputDelta == null)) {
				return;
			}

			scanner.scan(outputRoot, outputDelta, monitor);
		}
	}

	/**
	 * This class is a visitor that scans a binary output directory copying all
	 * modified files to a packaged item.
	 * 
	 * @author vega
	 */
	private class Scanner implements IResourceVisitor, IResourceDeltaVisitor {

		/** The eclipse exported content. */
		private final EclipseExportedContent	eclipseExportedContent;

		/**
		 * Instantiates a new scanner.
		 * 
		 * @param eclipseExportedContent
		 *            the eclipse exported content
		 */
		public Scanner(EclipseExportedContent eclipseExportedContent) {
			this.eclipseExportedContent = eclipseExportedContent;
		}

		/** The output folder. */
		private IFolder				outputFolder;

		/** The monitor. */
		private IProgressMonitor	monitor;

		/**
		 * This methods iterates over all modifications of the output folder
		 * aplying them to the packaged item.
		 * 
		 * @param outputFolder
		 *            the output folder
		 * @param outputDelta
		 *            the output delta
		 * @param monitor
		 *            the monitor
		 * 
		 * @throws CoreException
		 *             the core exception
		 */
		public synchronized void scan(IFolder outputFolder, IResourceDelta outputDelta, IProgressMonitor monitor)
				throws CoreException {
			this.outputFolder = outputFolder;
			this.monitor = monitor;

			if (outputDelta != null) {
				outputDelta.accept(this);
			} else {
				outputFolder.accept(this);
			}
		}

		/**
		 * This callback method is called to visit and filter all the content of
		 * the output folder in the case of full copies. We consider any
		 * matching file found as an addition.
		 * 
		 * @param outputResource
		 *            the output resource
		 * 
		 * @return true, if visit
		 * 
		 * @throws CoreException
		 *             the core exception
		 */
		public boolean visit(IResource outputResource) throws CoreException {
			return added(outputResource);
		}

		/**
		 * This callback method is called to incrementally perform an update
		 * from the contents of the output folder.
		 * 
		 * @param outputDelta
		 *            the output delta
		 * 
		 * @return true, if visit
		 * 
		 * @throws CoreException
		 *             the core exception
		 */
		public boolean visit(IResourceDelta outputDelta) throws CoreException {

			switch (outputDelta.getKind()) {
				case IResourceDelta.ADDED: {
					return added(outputDelta.getResource());
				}
				case IResourceDelta.REMOVED: {
					return removed(outputDelta.getResource());
				}
				case IResourceDelta.CHANGED: {
					return changed(outputDelta.getResource());
				}
			}

			return true;
		}

		/**
		 * Removed.
		 * 
		 * @param outputResource
		 *            the output resource
		 * 
		 * @return true, if successful
		 * 
		 * @throws CoreException
		 *             the core exception
		 */
		private boolean removed(IResource outputResource) throws CoreException {
			IPath filePath = getRelativePath(outputFolder, outputResource);
			eclipseExportedContent.delete(filePath, monitor);
			return false;
		}

		/**
		 * Added.
		 * 
		 * @param outputResource
		 *            the output resource
		 * 
		 * @return true, if successful
		 * 
		 * @throws CoreException
		 *             the core exception
		 */
		private boolean added(IResource outputResource) throws CoreException {
			IPath filePath = getRelativePath(outputFolder, outputResource);
			eclipseExportedContent.add(outputResource, filePath, monitor);
			return false;
		}

		/**
		 * Changed.
		 * 
		 * @param outputResource
		 *            the output resource
		 * 
		 * @return true, if successful
		 * 
		 * @throws CoreException
		 *             the core exception
		 */
		private boolean changed(IResource outputResource) throws CoreException {
			/*
			 * We have to scan to the file level to verify wich files we have to
			 * replace
			 */
			if (outputResource.getType() == IResource.FOLDER) {
				return true;
			}

			IPath filePath = getRelativePath(outputFolder, outputResource);
			eclipseExportedContent.update(outputResource, filePath, monitor);
			return false;
		}

	}

	/**
	 * Gets a path relative to the project associated with this composer.
	 * 
	 * @param path
	 *            the path
	 * 
	 * @return the relative path
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	protected final IPath getRelativePath(IPath path) throws CoreException {
		IProject javaProject = JavaProjectManager.getProject(getItem());
		if (javaProject.getFullPath().isPrefixOf(path)) {
			path = path.removeFirstSegments(javaProject.getFullPath().segmentCount());
		}
		return path;
	}

	/**
	 * Gets the path of a resource relative to another resource, both resources
	 * must be located in the project associated with this composer.
	 * 
	 * @param container
	 *            the container
	 * @param member
	 *            the member
	 * 
	 * @return the relative path
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	protected final IPath getRelativePath(IResource container, IResource member) throws CoreException {
		IPath containerPath = getRelativePath(container.getFullPath());
		IPath memberPath = getRelativePath(member.getFullPath());

		if (containerPath.isPrefixOf(memberPath)) {
			memberPath = memberPath.removeFirstSegments(containerPath.segmentCount());
		}

		return memberPath;
	}

	/**
	 * Gets a folder in the project associated with this composer.
	 * 
	 * @param relativePath
	 *            the relative path
	 * 
	 * @return the folder
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	protected final IFolder getFolder(IPath relativePath) throws CoreException {
		return JavaProjectManager.getProject(getItem()).getFolder(relativePath);
	}

}
