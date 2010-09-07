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
package fede.workspace.eclipse.composition.java;



/**
 * The Class ManifestTemplate.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ManifestTemplate implements IPDETemplate
 {
  
  /** The nl. */
  protected static String nl;
  
  /**
	 * Creates the.
	 * 
	 * @param lineSeparator
	 *            the line separator
	 * 
	 * @return the manifest template
	 */
  public static synchronized ManifestTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    ManifestTemplate result = new ManifestTemplate();
    nl = null;
    return result;
  }

  /** The NL. */
  protected final String NL = nl == null ? ("\n") : nl;
  
  /** The TEX t_1. */
  protected final String TEXT_1 = "Manifest-Version: 1.0" + NL + "Bundle-ManifestVersion: 2" + NL + "Bundle-Name: ";
  
  /** The TEX t_2. */
  protected final String TEXT_2 = NL + "Bundle-SymbolicName: ";
  
  /** The TEX t_3. */
  protected final String TEXT_3 = ";singleton:=true" + NL + "Bundle-Version: 1.0.0" + NL + "Bundle-Localization: plugin";
  
  /** The TEX t_4. */
  protected final String TEXT_4 = NL + "Bundle-Activator: ";
  
  /** The TEX t_5. */
  protected final String TEXT_5 = NL + "Import-Package: org.eclipse.core.runtime," + NL + " org.osgi.framework";
  
  /** The TEX t_6. */
  protected final String TEXT_6 = ",";
  
  /** The TEX t_7. */
  protected final String TEXT_7 = NL + " ";
  
  /** The TEX t_8. */
  protected final String TEXT_8 = NL + "Export-Package: ";
  
  /** The TEX t_9. */
  protected final String TEXT_9 = ",";
  
  /** The TEX t_10. */
  protected final String TEXT_10 = NL + " ";
  
  /** The TEX t_11. */
  protected final String TEXT_11 = NL + "Bundle-ClassPath: ." + NL + "Eclipse-LazyStart: ";
  
  /** The TEX t_12. */
  protected final String TEXT_12 = NL;

/* (non-javadoc)
    * @see IGenerator#generate(Object)
    */
	/* (non-Javadoc)
 * @see fede.workspace.eclipse.composition.java.IPDETemplate#generate(fede.workspace.eclipse.composition.java.EclipsePluginContentManger.PDEGenerateModel)
 */
public String generate(EclipsePluginContentManger.PDEGenerateModel info)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    stringBuffer.append(info.pluginID );
    stringBuffer.append(TEXT_2);
    stringBuffer.append(info.pluginID );
    stringBuffer.append(TEXT_3);
    if (info.qualifiedActivatorName!= null) { 
    stringBuffer.append(TEXT_4);
    stringBuffer.append(info.qualifiedActivatorName);
    }
    stringBuffer.append(TEXT_5);
    for(String imp : info.importsPackages) {
    stringBuffer.append(TEXT_6);
    stringBuffer.append(TEXT_7);
    stringBuffer.append(imp);
    }
    if (info.exportsPackages.length > 0) { int len = info.exportsPackages.length;
    stringBuffer.append(TEXT_8);
    stringBuffer.append(info.exportsPackages[0]);
    for(int i = 1; i< len; i++) {
    stringBuffer.append(TEXT_9);
    stringBuffer.append(TEXT_10);
    stringBuffer.append(info.exportsPackages[i]);
    }}
    stringBuffer.append(TEXT_11);
    stringBuffer.append(Boolean.toString(info.isLazyStart) );
    stringBuffer.append(TEXT_12);
    return stringBuffer.toString();
  }
}
