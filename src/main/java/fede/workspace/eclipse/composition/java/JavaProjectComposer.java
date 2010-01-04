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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import fede.workspace.eclipse.MelusineProjectManager;
import fede.workspace.eclipse.composer.EclipseComposer;
import fede.workspace.eclipse.composition.CompositeBuildingContext;
import fede.workspace.eclipse.java.JavaProjectManager;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.build.IBuildingContext;
import fr.imag.adele.cadse.core.build.IExportedContent;
import fr.imag.adele.cadse.core.build.IExporterTarget;

/**
 * The Class JavaProjectComposer.
 */
public class JavaProjectComposer extends EclipseComposer {

	/**
	 * Instantiates a new java project composer.
	 * 
	 * @param contentManager
	 *            the content manager
	 */
	public JavaProjectComposer(ContentItem contentManager) {
		super(contentManager, JavaProjectExporter.JAVA_TYPE_EXPORTER);
	}

	/**
	 * Instantiates a new java project composer.
	 * 
	 * @param contentManager
	 *            the content manager
	 * @param exporterTypes
	 *            the exporter types
	 */
	protected JavaProjectComposer(ContentItem contentManager, Class<?>... exporterTypes) {
		super(contentManager, exporterTypes);
	}

	@Override
	protected void postCompose(IBuildingContext context, List<IExportedContent> listExportedContent,
			IExporterTarget target) {

		try {
			/*
			 * Verify this item is actually hosted in a Java Project
			 */
			if (!JavaProjectManager.isJavaProject(MelusineProjectManager.getProject(getItem()))) {
				return;
			}

			/*
			 * Add a classpath container that will resolve dependencies to the
			 * components in the repository associated with this item
			 */
			IJavaProject javaProject = JavaProjectManager.getJavaProject(getItem());
			List<IClasspathEntry> classpath = new ArrayList<IClasspathEntry>(Arrays.asList(javaProject
					.getRawClasspath()));

			IProgressMonitor monitor = ((CompositeBuildingContext) context).getMonitor();
			boolean entriesCreated = initializeClasspath(classpath, monitor);

			if (entriesCreated) {
				javaProject.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), monitor);
			}

			/*
			 * Recalculates the component classpath container entry to add the
			 * local copy of the packaged components as exported library entries
			 * of the classpath of the project.
			 * 
			 * The net effect is that all classes in the packaged components are
			 * perceived as belonging to this project.
			 */
			IClasspathContainer container = new ItemComponentsClasspathEntryClasses(javaProject, getItem(), true);
			JavaCore.setClasspathContainer(ItemComponentsClasspathEntryClasses.CLASSPATH_ENTRY_PATH,
					new IJavaProject[] { javaProject }, new IClasspathContainer[] { container }, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Initializes the classpath of a project just associated with this
	 * composer.
	 * 
	 * @param classpath
	 *            the classpath
	 * @param monitor
	 *            the monitor
	 * 
	 * @return true, if initialize classpath
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	protected boolean initializeClasspath(List<IClasspathEntry> classpath, IProgressMonitor monitor)
			throws CoreException {
		/*
		 * Add an exported entry of type ItemComponentsClasspathEntry to the
		 * classpath (for the subentry CLASSES)
		 */
		if (classpath.contains(ItemComponentsClasspathEntryClasses.CLASSPATH_ENTRY)) {
			return false;
		}

		classpath.add(ItemComponentsClasspathEntryClasses.CLASSPATH_ENTRY);
		return true;
	}

}
