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
 package SI.Workspace.Java;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
	@generated
*/
public class Activator extends Plugin {

	/**
		@generated
	*/
	public static String PLUGIN_ID = "SI.Workspace.Java";

	/**
		@generated
	*/
	private static Activator _default;

	/**
		@generated
	*/
	public Activator() {
		Activator._default = this;
	}
	
	/**
		@generated
	*/
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}
	
	/**
		@generated
	*/
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
		@generated
	*/
	public static Activator getDefault() {
		return _default;
	}
	
	/**
		@generated
	*/
	public void log(IStatus status) {
		this.getLog().log(status);
	}
}
