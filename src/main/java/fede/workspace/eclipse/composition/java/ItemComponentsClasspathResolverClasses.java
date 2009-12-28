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

package fede.workspace.eclipse.composition.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import fede.workspace.tool.view.WSPlugin;
import fr.imag.adele.cadse.core.Item;

/**
 * The Class ItemComponentsClasspathResolverClasses.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ItemComponentsClasspathResolverClasses extends ClasspathContainerInitializer {

	/**
	 * Instantiates a new item components classpath resolver classes.
	 */
	public ItemComponentsClasspathResolverClasses() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse.core.runtime.IPath,
	 *      org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public void initialize(IPath containerPath, IJavaProject javaProject) throws CoreException {
		/*
		 * Verify if the project is associated with an item, otherwise left it
		 * unbounded
		 */
		Item item = WSPlugin.sGetItemFromResource(javaProject.getResource());
		if (item == null) {
			return;
		}

		IClasspathContainer container = new ItemComponentsClasspathEntryClasses(javaProject, item, true);
		JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { javaProject },
				new IClasspathContainer[] { container }, null);
	}

}
