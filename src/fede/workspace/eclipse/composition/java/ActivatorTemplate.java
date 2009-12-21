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



/**
 * The Class ActivatorTemplate.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ActivatorTemplate implements IPDETemplate
 {
  
  /** The nl. */
  protected static String nl;
  
  /**
	 * Creates the.
	 * 
	 * @param lineSeparator
	 *            the line separator
	 * 
	 * @return the activator template
	 */
  public static synchronized ActivatorTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    ActivatorTemplate result = new ActivatorTemplate();
    nl = null;
    return result;
  }

  /** The NL. */
  protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  
  /** The TEX t_1. */
  protected final String TEXT_1 = " package ";
  
  /** The TEX t_2. */
  protected final String TEXT_2 = ";" + NL + "" + NL + "import org.eclipse.core.runtime.IStatus;" + NL + "import org.eclipse.core.runtime.Plugin;" + NL + "import org.osgi.framework.BundleContext;" + NL + "" + NL + "/**" + NL + "\t@generated" + NL + "*/" + NL + "public class ";
  
  /** The TEX t_3. */
  protected final String TEXT_3 = " extends Plugin {" + NL + "" + NL + "\t/**" + NL + "\t\t@generated" + NL + "\t*/" + NL + "\tpublic static String PLUGIN_ID = \"";
  
  /** The TEX t_4. */
  protected final String TEXT_4 = "\";" + NL + "" + NL + "\t/**" + NL + "\t\t@generated" + NL + "\t*/" + NL + "\tprivate static Activator _default;" + NL + "" + NL + "\t/**" + NL + "\t\t@generated" + NL + "\t*/" + NL + "\tpublic Activator() {" + NL + "\t\tActivator._default = this;" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL + "\t\t@generated" + NL + "\t*/" + NL + "\t@Override" + NL + "\tpublic void start(BundleContext context) throws Exception {" + NL + "\t\tsuper.start(context);" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL + "\t\t@generated" + NL + "\t*/" + NL + "\t@Override" + NL + "\tpublic void stop(BundleContext context) throws Exception {" + NL + "\t\tsuper.stop(context);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t\t@generated" + NL + "\t*/" + NL + "\tpublic static Activator getDefault() {" + NL + "\t\treturn _default;" + NL + "\t}" + NL + "\t" + NL + "\t/**" + NL + "\t\t@generated" + NL + "\t*/" + NL + "\tpublic void log(IStatus status) {" + NL + "\t\tthis.getLog().log(status);" + NL + "\t}" + NL + "}";
  
  /** The TEX t_5. */
  protected final String TEXT_5 = NL;

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
    stringBuffer.append(info.packageName);
    stringBuffer.append(TEXT_2);
    stringBuffer.append( info.activatorName );
    stringBuffer.append(TEXT_3);
    stringBuffer.append(info.pluginID);
    stringBuffer.append(TEXT_4);
    stringBuffer.append(TEXT_5);
    return stringBuffer.toString();
  }
}