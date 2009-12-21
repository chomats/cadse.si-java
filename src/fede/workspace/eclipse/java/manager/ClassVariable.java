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

package fede.workspace.eclipse.java.manager;

import java.util.UUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.impl.var.VariableImpl;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.Variable;

/**
 * The Class ClassVariable.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ClassVariable extends VariableImpl {

	/** The class name. */
	Variable	className;

	/**
	 * Instantiates a new class variable.
	 * 
	 * @param id
	 *            the id
	 * @param className
	 *            the class name
	 */
	public ClassVariable(UUID id, String name, Variable className) {
		super(id, name);
		this.className = className;
	}

	public ClassVariable(Variable className) {
		super();
		this.className = className;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.var.Variable#compute(fr.imag.adele.cadse.core.var.ContextVariable,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	public String compute(ContextVariable context, Item item) {
		return className.compute(context, item) + ".java";
	}

}