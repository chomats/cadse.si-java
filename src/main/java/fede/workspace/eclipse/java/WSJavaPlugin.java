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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


/**
 * The Class WSJavaPlugin.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class WSJavaPlugin extends Plugin{

	/** The PLUGI n_ id. */
	public static String PLUGIN_ID = "fr.imag.adele.cadse.si.java";
	
	/** The _default. */
	private static WSJavaPlugin _default;

	/**
	 * Instantiates a new wS java plugin.
	 */
	public WSJavaPlugin() {
		WSJavaPlugin._default = this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		JMergeUtil.init(context);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

 	/**
		 * Gets the default.
		 * 
		 * @return the default
		 */
	 public static WSJavaPlugin getDefault() {
		return _default;
	}
	
	/**
	 * Log.
	 * 
	 * @param status
	 *            the status
	 */
	public void log(IStatus status) {
		this.getLog().log(status);
	}
}
