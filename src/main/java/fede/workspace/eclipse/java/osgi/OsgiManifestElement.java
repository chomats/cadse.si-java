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
package fede.workspace.eclipse.java.osgi;

import java.io.IOException;
import java.util.Enumeration;

import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;

import fede.workspace.eclipse.composition.java.Messages;


/**
 * The Class OsgiManifestElement.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class OsgiManifestElement extends ManifestElement {

	/**
	 * Instantiates a new osgi manifest element.
	 * 
	 * @param values
	 *            the values
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public OsgiManifestElement(String... values) throws BundleException {
		if (values.length == 0)
			throw new BundleException(Messages.osgi_error_novalue); //$NON-NLS-1$
		
		this.valueComponents = values.clone();
		StringBuilder sb = new StringBuilder();
		for (String v : values) {
			if (v.contains(";")) throw new BundleException(Messages.bind(Messages.osgi_error_badvalue,v));  //$NON-NLS-1$//$NON-NLS-2$
			sb.append(v).append(";"); //$NON-NLS-1$
		}
		sb.setLength(sb.length()-1);
		this.value = sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.osgi.util.ManifestElement#addAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void addAttribute(String key, String value) {
		super.addAttribute(key, value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.osgi.util.ManifestElement#addDirective(java.lang.String, java.lang.String)
	 */
	@Override
	public void addDirective(String key, String value) {
		super.addDirective(key, value);
	}
	
	/**
	 * Attribute.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * 
	 * @return the osgi manifest element
	 */
	public OsgiManifestElement attribute(String key, String value) {
		super.addAttribute(key, value);
		return this;
	}
	
	/**
	 * Directive.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * 
	 * @return the osgi manifest element
	 */
	public OsgiManifestElement directive(String key, String value) {
		super.addDirective(key, value);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(this);
	}
	
	/**
	 * To string.
	 * 
	 * @param me
	 *            the me
	 * 
	 * @return the string
	 */
	public static String toString(ManifestElement me)  {
		StringBuilder sb = new StringBuilder();
		toString(sb,me);
		return sb.toString();
	}
		
	/**
	 * To string.
	 * 
	 * @param sb
	 *            the sb
	 * @param me
	 *            the me
	 */
	public static void toString(Appendable sb ,ManifestElement me)  {
		try {
			String[] vc = me.getValueComponents();
			char c = 0;
			for (String v : vc) {
				if (c != 0) sb.append(c);
				sb.append(v);
				c = ';';
			}
			c = 0;
			Enumeration dk = me.getDirectiveKeys();
			if (dk != null) {
				while (dk.hasMoreElements()) {
					String k = (String) dk.nextElement();
					String[] vs = me.getDirectives(k);
					for (String v : vs) {
						sb.append(';').append(k).append(":=").append(v); //$NON-NLS-1$
					}
					
				}
			}
			Enumeration ak = me.getKeys();
			if (ak != null) {
				while (ak.hasMoreElements()) {
					String k = (String) ak.nextElement();
					String[] vs = me.getAttributes(k);
					for (String v : vs) {
						sb.append(';').append(k).append("=\"").append(v).append("\""); //$NON-NLS-1$
					}
					
				}
			}
		} catch (IOException e) {
			// ignored
		}
		
	}
	
}
