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
package fede.workspace.dependencies.eclipse.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import fede.workspace.eclipse.MelusineProjectManager;
import fede.workspace.tool.view.WSPlugin;
import java.util.UUID;
import fr.imag.adele.cadse.core.Item;

/**
 * The Class ItemDependenciesClasspathResolver.
 */
public class ItemDependenciesClasspathResolver extends ClasspathContainerInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse.core.runtime.IPath,
	 *      org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public void initialize(IPath containerPath, IJavaProject project) throws CoreException {

		/*
		 * Verify if the project is associated with an item, otherwise left it
		 * unbounded
		 */
		Item item = null;
		try {
			item = WSPlugin.sGetItemFromResource(project.getResource());
		} catch (Throwable e) {
		}
		UUID id = null;
		if (item == null) {
			try {
				id = MelusineProjectManager.getUUIDItem(project.getResource());
			} catch (Throwable e) { // ignored
			}
		}
		IClasspathContainer previousSessionContainer = JavaCore.getClasspathContainer(containerPath, project);

		IClasspathContainer container = new ItemDependenciesClasspathEntry(project, project.getElementName(), item, id,
				previousSessionContainer);

		JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project },
				new IClasspathContainer[] { container }, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#getComparisonID(org.eclipse.core.runtime.IPath,
	 *      org.eclipse.jdt.core.IJavaProject)
	 */
	public Object getComparisonID(IPath containerPath, IJavaProject project) {
		if (containerPath == null || project == null) {
			return null;
		}

		return containerPath.segment(0) + "/" + project.getPath().segment(0); //$NON-NLS-1$
	}

}
