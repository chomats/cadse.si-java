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

package fede.workspace.eclipse.java;

import java.util.Arrays;
import java.util.HashSet;



/**
 * The Class JavaIdentifier.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JavaIdentifier {

	/** The reserved words. */
	public static HashSet<String> reservedWords = new HashSet<String>(Arrays.asList(new String[] {
			"abstract", "continue", "for", "new", "switch",
			"assert", "default", "goto", "package", "synchronized",
			"boolean", "do", "if", "private", "this",
			"break", "double", "implements", "protected", "throw",
			"byte", "else", "import", "public", "throws",
			"case", "enum", "instanceof", "return", "transient",
			"catch", "extends", "int", "short", "try",
			"char", "final", "interface", "static", "void",
			"class", "finally", "long", "strictfp", "volatile",
			"const", "float", "native", "super", "while"
	}));
	
	/**
	 * UPPE r_ cst.
	 * 
	 * @param att
	 *            the att
	 * 
	 * @return the string
	 */
	static public String UPPER_CST(String att) {
   		return javaIdentifierFromStringUPPER(att, null);
   	}
   	
	/**
	 * MI n_ first.
	 * 
	 * @param str
	 *            the str
	 * 
	 * @return the string
	 */
	static public String MIN_FIRST(String str) {
		return javaIdentifierFromString(str,false, true, null);
	}

	/**
	 * UPPE r_ first.
	 * 
	 * @param str
	 *            the str
	 * 
	 * @return the string
	 */
	static public String UPPER_FIRST(String str) {
		return javaIdentifierFromString(str,true,false,null);
	}
	
	
	/**
	 * Java identifier from string.
	 * 
	 * @param str
	 *            the str
	 * @param classNameId
	 *            the class name id
	 * @param lowerfirst
	 *            the lowerfirst
	 * @param postFix
	 *            the post fix
	 * 
	 * @return the string
	 */
	static public String javaIdentifierFromString(String str, boolean classNameId, boolean lowerfirst, String postFix) {
		if (str == null || str.length()==0) {
			throw new IllegalArgumentException();
		}
				
		boolean upper_next = classNameId;
		boolean lower_first = lowerfirst;
		char[] charStr = str.toCharArray();
		StringBuilder sb = new StringBuilder(charStr.length);
		for (int i = 0; i < charStr.length; i++) {
			char c = charStr[i];
			if (c == '#') {
				sb.append("_$_");
				upper_next =true ;
				continue;
			}
			if (c == ' ' || c == '-' || c == '\'' || c =='.') {
				upper_next =true ; continue;
			}
			if (!Character.isJavaIdentifierPart(c))
				throw new IllegalArgumentException();
			
			if (sb.length() == 0 && !Character.isJavaIdentifierStart(c)) {
					sb.append('_');
			}
			
			sb.append(upper_next ? Character.toUpperCase(c):lower_first?Character.toLowerCase(c):c);
			upper_next = false;
			lower_first = false;
		}
		if (sb.length()==0) {
			throw new IllegalArgumentException();
		}
		if (postFix != null)
			sb.append(postFix);
		str = sb.toString();
		if (reservedWords.contains(str)) {
			str = "_"+str;
		}
		return str;
	}
	
	/**
	 * Java identifier from string upper.
	 * 
	 * @param str
	 *            the str
	 * @param postFix
	 *            the post fix
	 * 
	 * @return the string
	 */
	static public String javaIdentifierFromStringUPPER(String str, String postFix) {
		
		if (str == null || str.length()==0) {
			throw new IllegalArgumentException();
		}
		
		
		char[] charStr = str.toCharArray();
		StringBuilder sb = new StringBuilder(charStr.length);
		boolean pre_upper = false;
		for (int i = 0; i < charStr.length; i++) {
			char c = charStr[i];
			if (c == '#') {
				sb.append("_$_");
				continue;
			}
			if (c == ' ' || c == '-' || c == '\'') {
				sb.append("_") ; 
				continue;
			}
			if (!Character.isJavaIdentifierPart(c))
				throw new IllegalArgumentException("Not valid java identifier part "+c);
			
			if (sb.length() == 0 && !Character.isJavaIdentifierStart(c)) {
				sb.append('_');
			}
			if (i != 0 && Character.isUpperCase(c) && !pre_upper) {
				sb.append('_');
			}
			pre_upper = Character.isUpperCase(c) || c == '_';
			sb.append(Character.toUpperCase(c));
		}
		if (sb.length()==0) {
			throw new IllegalArgumentException("Empty java name");
		}
		if (postFix != null)
			sb.append(postFix);
		return sb.toString();
	}
	
	
	/**
	 * Gets the package and class name.
	 * 
	 * @param value
	 *            the value
	 * 
	 * @return the package and class name
	 */
	public static String[] getPackageAndClassName(String value) {
		int index = value.lastIndexOf('.');
		if (index == -1) return new String[] { null, value };
		return new String[] { value.substring(0, index), value.substring(index+1)};
	}
	
	/**
	 * Gets the package and class name.
	 * 
	 * @param value
	 *            the value
	 * 
	 * @return the package and class name
	 */
	public static String getPackageName(String value) {
		int index = value.lastIndexOf('.');
		if (index == -1) return "";
		return value.substring(0, index);
	}
	
	
	/**
	 * Gets the lastclass name.
	 * 
	 * @param value
	 *            the value
	 * 
	 * @return the lastclass name
	 */
	public static String getlastclassName(String value) {
		int index = value.lastIndexOf('.');
		if (index == -1) return value;
		return value.substring(index+1);
	}
	
	/**
	 * Package name.
	 * 
	 * @param value
	 *            the value
	 * 
	 * @return the string
	 */
	public static String packageName(String value) {
		int index = value.lastIndexOf('.');
		if (index == -1) return value;
		return value.substring(0, index);
	}
}
