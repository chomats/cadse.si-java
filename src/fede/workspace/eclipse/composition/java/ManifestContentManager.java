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

import fede.workspace.eclipse.content.FileContentManager;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.impl.var.StringVariable;


/**
 * The Class ManifestContentManager.n *
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>n
 */
public class ManifestContentManager extends FileContentManager {

	/**
	 * Instantiates a new manifest content manager.
	 * 
	 * @param parent
	 *            the parent
	 * @param item
	 *            the item
	 */
	public ManifestContentManager(CompactUUID id) {
		super(id, new StringVariable("MANIFEST.MF"), new StringVariable("META-INF/") );
	}
	
}
