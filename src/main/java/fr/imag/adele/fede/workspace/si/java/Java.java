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
package fr.imag.adele.fede.workspace.si.java;



import fr.imag.adele.cadse.as.platformide.IPlatformIDE;
import fr.imag.adele.cadse.core.CadseDomain;

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
	IPlatformIDE    	platformEclipse;

	protected JobReloadClassPathMapping j;

	public CadseDomain getWorkspaceCU() {
		return workspaceCU;
	}

	public void start() {
		Runnable r = new Runnable() {
			public void run() {

				getPlatformEclipse().getLocation(true);

				j = new JobReloadClassPathMapping(Java.this);
				j.schedule(5000);
			}
		};
		new Thread(r, "Start java").start();
	}
	
	public void stop() {
		if (j != null)
			j.cancel();
		
	}

	public IPlatformIDE getPlatformEclipse() {
		return platformEclipse;
	}
}
