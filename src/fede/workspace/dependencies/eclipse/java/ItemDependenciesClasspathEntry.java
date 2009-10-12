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

package fede.workspace.dependencies.eclipse.java;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.RequiredPluginsClasspathContainer;

import fede.workspace.eclipse.java.JavaProjectManager;
import fede.workspace.eclipse.java.WSJavaPlugin;
import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.impl.CadseCore;

/**
 * This class represents the calculated classpath of the project associated with
 * an item in the workspace.
 * 
 * The classpath is calculated in the constructor of this class and is never
 * recalculated. In case the dependencies of an item changes, a new instance
 * must be instantiated and associated with the corresponding project.
 */
public class ItemDependenciesClasspathEntry implements IClasspathContainer {

	/** The Constant CLASSPATH_ENTRY_ID. */
	public final static String					CLASSPATH_ENTRY_ID		= "fede.workspace.eclipse.java.item.dependencies";

	/** The Constant CLASSPATH_ENTRY_PATH. */
	public final static Path					CLASSPATH_ENTRY_PATH	= new Path(CLASSPATH_ENTRY_ID);

	/** The Constant CLASSPATH_ENTRY. */
	public final static IClasspathEntry			CLASSPATH_ENTRY			= JavaCore
																				.newContainerEntry(CLASSPATH_ENTRY_PATH);

	/** The dependencies. */
	private IClasspathEntry[]					_dependencies			= null;

	/** The dependencies. */
	private IClasspathEntry[]					_previousdependencies	= null;

	/** The item. */
	private Item								item;

	/** The id. */
	private CompactUUID							id;

	private String								label;

	private RequiredPluginsClasspathContainer	_pdeClasspath;

	private boolean								_merge_with_pde			= false;

	/**
	 * Instantiates a new item dependencies classpath entry.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public ItemDependenciesClasspathEntry(IJavaProject jp, String label, Item item, CompactUUID id,
			IClasspathContainer previousSessionContainer) throws CoreException {
		this.item = item;
		this.id = item == null ? id : item.getId();
		this.label = label;
		_previousdependencies = previousSessionContainer == null ? new IClasspathEntry[0] : previousSessionContainer
				.getClasspathEntries();
		_pdeClasspath = getPDE(jp);
	}

	public static boolean isPluginProject(IProject project) {
		if (project.isOpen()) {
			return project.exists(ICoreConstants.MANIFEST_PATH);
		}
		return false;
	}

	public RequiredPluginsClasspathContainer getPDE(IJavaProject jp) {
		IProject project = jp.getProject();
		if (!isPluginProject(project)) {
			return null;
		}
		IPluginModelBase model = PluginRegistry.findModel(project);
		if (model == null) {
			return null;
		}
		return new RequiredPluginsClasspathContainer(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {

		if (id == null && _pdeClasspath != null) {
			return _pdeClasspath.getClasspathEntries();
		}

		CadseDomain wd = CadseCore.getCadseDomain();
		if (wd == null) {
			return _previousdependencies;
		}
		LogicalWorkspace logicalWorkspace = wd.getLogicalWorkspace();
		if (id == null && item == null) {
			WSJavaPlugin.getDefault().log(
					new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, 0, label
							+ " : cannot find item from a null id for the item dependencies classpath.", null));
			return _previousdependencies;
		}
		if (logicalWorkspace == null) {
			return _previousdependencies;
		}
		if (logicalWorkspace.getState() != WSModelState.RUN) {
			return _previousdependencies;
		}
		;
		if (item == null || !item.isResolved()) {
			item = logicalWorkspace.getItem(id);
		}
		if (item == null || !item.isResolved()) {
			WSJavaPlugin.getDefault().log(
					new Status(Status.ERROR, WSJavaPlugin.PLUGIN_ID, 0, label + " : cannot find item " + id
							+ " for the item dependencies classpath ", null));
			return _previousdependencies;
		}
		if (_dependencies == null) {
			MultiStatus ms = new MultiStatus(WSJavaPlugin.PLUGIN_ID, 0, label
					+ " : cannot compute the item dependencies classpath ", null);

			try {
				final Set<IClasspathEntry> calculateDependencies = JavaProjectManager.calculateDependencies(item, ms);
				_dependencies = calculateDependencies.toArray(new IClasspathEntry[calculateDependencies.size()]);
				Arrays.sort(_dependencies, new Comparator<IClasspathEntry>() {
					public int compare(IClasspathEntry o1, IClasspathEntry o2) {
						return o1.getPath().toPortableString().compareTo(o2.getPath().toPortableString());
					}
				});
			} catch (CoreException e) {
				MultiStatus ms2 = new MultiStatus(WSJavaPlugin.PLUGIN_ID, 0,
						"Cannot compute the item dependencies classpath ", e);
				ms2.add(e.getStatus());
				ms2.addAll(ms);
				ms = ms2;
				if (!ms.isOK()) {
					WSJavaPlugin.getDefault().log(ms);
				}
				return _previousdependencies;
			}
			if (!ms.isOK()) {
				WSJavaPlugin.getDefault().log(ms);
			}
		}
		return _dependencies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		return "Item Dependencies";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
	 */
	public int getKind() {
		return K_APPLICATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return new Path(CLASSPATH_ENTRY_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getPath().toString();
	}

	public boolean isResolved() {
		return _dependencies != null;
	}

}
