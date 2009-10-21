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
package fr.imag.adele.fede.workspace.si.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import fede.workspace.dependencies.eclipse.java.ItemDependenciesClasspathEntry;
import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.fede.workspace.as.platformeclipse.IPlatformEclipse;

/**
 * @generated
 */
public class Java {

	/**
	 * @generated
	 */
	CadseDomain			workspaceCU;

	/**
	 * @generated
	 */
	IPlatformEclipse	platformEclipse;

	protected JobReloadClassPathMapping j;

	/**
	 * The Class JobInitMapping.
	 * 
	 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
	 */
	private final class JobReloadClassPathMapping extends Job {
		class UpdateClasspathLaterL extends WorkspaceListener {
			@Override
			public void workspaceChanged(ImmutableWorkspaceDelta delta) {
				if (delta.getModelstate() == WSModelState.RUN) {
					model.removeListener(this);
					wakeUp();
				}
			}
		}

		/** The model. */
		LogicalWorkspace	model;

		/**
		 * Instantiates a new job init mapping.
		 * 
		 * @param model
		 *            the model
		 */
		public JobReloadClassPathMapping() {
			super("init java class path");
		}

		public void testworkspaceToSleep(final IProgressMonitor monitor) {
			if (monitor.isCanceled())
				return ;
			model = getWorkspaceCU().getLogicalWorkspace();
			if (model == null) {
				new Thread() {
					@Override
					public void run() {
						while (model == null) {
							if (monitor.isCanceled())
								return ;
							try {
								sleep(5000);
							} catch (InterruptedException e) {

							}
							model = getWorkspaceCU().getLogicalWorkspace();
						}
						wakeUp();
					}
				}.start();
				sleep();
			}
			if (monitor.isCanceled())
				return ;
			
			if (model.getState() != WSModelState.RUN) {
				model.addListener(new UpdateClasspathLaterL(), ChangeID.toFilter(ChangeID.MODEL_STATE));
				sleep();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled())
				return Status.OK_STATUS;
			testworkspaceToSleep(monitor);
			if (monitor.isCanceled())
				return Status.OK_STATUS;
			Map<IJavaProject, IClasspathContainer> toset = new HashMap<IJavaProject, IClasspathContainer>();
			for (Item item : model.getItems()) {
				try {
					if (monitor.isCanceled())
						return Status.OK_STATUS;
					IJavaProject jp = item.getMainMappingContent(IJavaProject.class); // force
					// to
					// load
					// content
					if (jp == null) {
						continue;
					}

					IClasspathContainer cc = JavaCore.getClasspathContainer(
							ItemDependenciesClasspathEntry.CLASSPATH_ENTRY_PATH, jp);
					if (cc == null) {
						toset.put(jp, new ItemDependenciesClasspathEntry(jp, jp.getElementName(), item, null, null));
						continue;
					}
					if (cc instanceof ItemDependenciesClasspathEntry) {
						if (((ItemDependenciesClasspathEntry) cc).isResolved()) {
							continue;
						}
						toset.put(jp, new ItemDependenciesClasspathEntry(jp, jp.getElementName(), item, null, cc));
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			if (toset.size() > 0) {
				try {
					// update classpath for all affected workspace plug-ins in
					// one operation
					IJavaProject[] jProjects = toset.keySet().toArray(new IJavaProject[toset.size()]);
					IClasspathContainer[] containers = toset.values().toArray(new IClasspathContainer[toset.size()]);
					JavaCore.setClasspathContainer(ItemDependenciesClasspathEntry.CLASSPATH_ENTRY_PATH, jProjects,
							containers, null);
				} catch (JavaModelException e) {
				}
			}
			return Status.OK_STATUS;
		}
	}

	public CadseDomain getWorkspaceCU() {
		return workspaceCU;
	}

	public void start() {
		Runnable r = new Runnable() {
			public void run() {

				getPlatformEclipse().getLocation(true);

				j = new JobReloadClassPathMapping();
				j.schedule(5000);
			}
		};
		new Thread(r, "Start java").start();
	}
	
	public void stop() {
		if (j != null)
			j.cancel();
		
	}

	public IPlatformEclipse getPlatformEclipse() {
		return platformEclipse;
	}
}
