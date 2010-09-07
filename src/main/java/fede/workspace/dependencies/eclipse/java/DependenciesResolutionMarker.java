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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import fede.workspace.eclipse.MelusineMarker;
import fede.workspace.eclipse.java.WSJavaPlugin;


/**
 * The Class DependenciesResolutionMarker.
 */
@Deprecated
public class DependenciesResolutionMarker extends MelusineMarker {

	/** The Constant MARKER_ID. */
	private final static String MARKER_ID 			= WSJavaPlugin.PLUGIN_ID+".dependencies.resolution.marker";
	
	/**
	 * Mark.
	 * 
	 * @param resource
	 *            the resource
	 * 
	 * @return the i marker
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static IMarker mark(IResource resource) throws CoreException {
		return resource.createMarker(MARKER_ID);
	}

	/**
	 * Unmark.
	 * 
	 * @param resource
	 *            the resource
	 * @param includeSubtypes
	 *            the include subtypes
	 * @param depth
	 *            the depth
	 * 
	 * @throws CoreException
	 *             the core exception
	 */
	public static void unmark(IResource resource, boolean includeSubtypes, int depth) throws CoreException {
		resource.deleteMarkers(MARKER_ID,includeSubtypes,depth);
	}

}
