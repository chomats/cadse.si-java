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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import fr.imag.adele.cadse.core.Item;

/**
 * The Class ItemComponentsClasspathEntryClasses.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ItemComponentsClasspathEntryClasses extends ItemComponentsClasspathEntry {

	/** The Constant CLASSPATH_ENTRY_ID. */
	public final static String				CLASSPATH_ENTRY_ID		= "fede.workspace.eclipse.java.item.components.classes";

	/** The Constant CLASSPATH_ENTRY_PATH. */
	public final static Path				CLASSPATH_ENTRY_PATH	= new Path(CLASSPATH_ENTRY_ID);

	/** The Constant CLASSPATH_ENTRY. */
	public final static IClasspathEntry		CLASSPATH_ENTRY			= JavaCore.newContainerEntry(CLASSPATH_ENTRY_PATH,
																			true);

	/** The Constant INPATH_ATTRIBUTE_NAME. */
	private static final String				INPATH_ATTRIBUTE_NAME	= "org.eclipse.ajdt.inpath";

	/** The Constant INPATH_ATTRIBUTE. */
	public static final IClasspathAttribute	INPATH_ATTRIBUTE		= JavaCore.newClasspathAttribute(
																			INPATH_ATTRIBUTE_NAME, "true");					//$NON-NLS-1$

	/**
	 * Instantiates a new item components classpath entry classes.
	 * 
	 * @param javaProject
	 *            the java project
	 * @param item
	 *            the item
	 * @param exportEntries
	 *            the export entries
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public ItemComponentsClasspathEntryClasses(IJavaProject javaProject, Item item, boolean exportEntries)
			throws CoreException {
		super(javaProject, item, JavaProjectExporter.JAVA_TYPE_EXPORTER, exportEntries, INPATH_ATTRIBUTE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return CLASSPATH_ENTRY_PATH;
	}
}
