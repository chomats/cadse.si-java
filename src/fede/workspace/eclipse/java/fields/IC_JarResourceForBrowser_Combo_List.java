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

package fede.workspace.eclipse.java.fields;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import fr.imag.adele.cadse.si.workspace.uiplatform.swt.ic.IC_ResourceTreeDialogForBrowser_Combo_List;



/**
 * Attributes : string link-type; string title-select ;.
 */


public class IC_JarResourceForBrowser_Combo_List  extends IC_ResourceTreeDialogForBrowser_Combo_List {
	public IC_JarResourceForBrowser_Combo_List() {
	}
    
	
	
	/**
	 * Instantiates a new i c_ jar resource for browser_ combo_ list.
	 * 
	 * @param title
	 *            the title
	 * @param message
	 *            the message
	 * @param selectRoot
	 *            the select root
	 */
	public IC_JarResourceForBrowser_Combo_List(String title, String message, int selectRoot) {
		super(title, message, selectRoot);
	}

	/* (non-Javadoc)
	 * @see fede.workspace.model.manager.properties.impl.ic.IC_ResourceTreeDialogForBrowser_Combo_List#validate(java.lang.Object[])
	 */
	@Override
	public IStatus validate(Object[] selection) {
		return Status.OK_STATUS;
	}
	
	/* (non-Javadoc)
	 * @see fede.workspace.model.manager.properties.impl.ic.IC_ResourceTreeDialogForBrowser_Combo_List#getFilter()
	 */
	@Override
	protected ViewerFilter getFilter() {
		return new ArchiveFileFilter(null, true);
	}
	
	/**
	 * The Class ArchiveFileFilter.
	 */
	static class ArchiveFileFilter extends ViewerFilter {

		/** The Constant FILTER_EXTENSIONS. */
		public static final String[] FILTER_EXTENSIONS= new String[] {"*.jar;*.zip"}; //$NON-NLS-1$

		/** The Constant fgArchiveExtensions. */
		private static final String[] fgArchiveExtensions= { "jar", "zip" }; //$NON-NLS-1$ //$NON-NLS-2$

		/** The excludes. */
		private List<IFile> fExcludes;
		
		/** The recursive. */
		private boolean fRecursive;
		
		/**
		 * The Constructor.
		 * 
		 * @param excludedFiles
		 *            Excluded files will not pass the filter. <code>null</code>
		 *            is allowed if no files should be excluded.
		 * @param recusive
		 *            Folders are only shown if, searched recursively, contain
		 *            an archive
		 */
//		public ArchiveFileFilter(IFile[] excludedFiles, boolean recusive) {
//			if (excludedFiles != null) {
//				fExcludes= Arrays.asList(excludedFiles);
//			} else {
//				fExcludes= null;
//			}
//			fRecursive= recusive;
//		}
		
		public ArchiveFileFilter(List<IFile> excludedFiles, boolean recusive) {
			fExcludes= excludedFiles;
			fRecursive= recusive;
		}
		
		/*
		 * @see ViewerFilter#select
		 */
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			if (element instanceof IFile) {
				if (fExcludes != null && fExcludes.contains(element)) {
					return false;
				}
				return isArchivePath(((IFile)element).getFullPath());
			} else if (element instanceof IContainer) { // IProject, IFolder
				if (!fRecursive) {
					return true;
				}
				try {
					IResource[] resources= ((IContainer)element).members();
					for (int i= 0; i < resources.length; i++) {
						// recursive! Only show containers that contain an archive
						if (select(viewer, parent, resources[i])) {
							return true;
						}
					}
				} catch (CoreException e) {
					JavaPlugin.log(e.getStatus());
				}				
			}
			return false;
		}
		
		/**
		 * Checks if is archive path.
		 * 
		 * @param path
		 *            the path
		 * 
		 * @return true, if is archive path
		 */
		public static boolean isArchivePath(IPath path) {
			String ext= path.getFileExtension();
			if (ext != null && ext.length() != 0) {
				return isArchiveFileExtension(ext);
			}
			return false;
		}
		
		/**
		 * Checks if is archive file extension.
		 * 
		 * @param ext
		 *            the ext
		 * 
		 * @return true, if is archive file extension
		 */
		public static boolean isArchiveFileExtension(String ext) {
			for (int i= 0; i < fgArchiveExtensions.length; i++) {
				if (ext.equalsIgnoreCase(fgArchiveExtensions[i])) {
					return true;
				}
			}
			return false;
		}
				
	}

}
