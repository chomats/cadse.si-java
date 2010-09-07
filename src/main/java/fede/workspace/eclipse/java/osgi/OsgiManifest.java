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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;

import fede.workspace.eclipse.composition.java.EclipsePluginContentManger.PDEGenerateModel;
import fede.workspace.tool.eclipse.MappingManager;

/**
 * Cette classe permet de lire et d'ecrire un manifest OSGI.
 * 
 * @author chomats
 */
public class OsgiManifest {

	/** The Constant ECLIPSE_LAZY_START. */
	public static final String	ECLIPSE_LAZY_START					= "Eclipse-LazyStart";

	/** Bundle manifest header constants from the OSGi R4 framework constants. */
	public static final String	BUNDLE_CATEGORY						= "Bundle-Category";

	/** The Constant BUNDLE_CLASSPATH. */
	public static final String	BUNDLE_CLASSPATH					= "Bundle-ClassPath";

	/** The Constant BUNDLE_COPYRIGHT. */
	public static final String	BUNDLE_COPYRIGHT					= "Bundle-Copyright";

	/** The Constant BUNDLE_DESCRIPTION. */
	public static final String	BUNDLE_DESCRIPTION					= "Bundle-Description";

	/** The Constant BUNDLE_NAME. */
	public static final String	BUNDLE_NAME							= "Bundle-Name";

	/** The Constant BUNDLE_NATIVECODE. */
	public static final String	BUNDLE_NATIVECODE					= "Bundle-NativeCode";

	/** The Constant EXPORT_PACKAGE. */
	public static final String	EXPORT_PACKAGE						= "Export-Package";

	/** The Constant EXPORT_SERVICE. */
	public static final String	EXPORT_SERVICE						= "Export-Service";

	/** The Constant IMPORT_PACKAGE. */
	public static final String	IMPORT_PACKAGE						= "Import-Package";

	/** The Constant DYNAMICIMPORT_PACKAGE. */
	public static final String	DYNAMICIMPORT_PACKAGE				= "DynamicImport-Package";

	/** The Constant IMPORT_SERVICE. */
	public static final String	IMPORT_SERVICE						= "Import-Service";

	/** The Constant BUNDLE_VENDOR. */
	public static final String	BUNDLE_VENDOR						= "Bundle-Vendor";

	/** The Constant BUNDLE_VERSION. */
	public static final String	BUNDLE_VERSION						= "Bundle-Version";

	/** The Constant BUNDLE_DOCURL. */
	public static final String	BUNDLE_DOCURL						= "Bundle-DocURL";

	/** The Constant BUNDLE_CONTACTADDRESS. */
	public static final String	BUNDLE_CONTACTADDRESS				= "Bundle-ContactAddress";

	/** The Constant BUNDLE_ACTIVATOR. */
	public static final String	BUNDLE_ACTIVATOR					= "Bundle-Activator";

	/** The Constant BUNDLE_UPDATELOCATION. */
	public static final String	BUNDLE_UPDATELOCATION				= "Bundle-UpdateLocation";

	/** The Constant BUNDLE_REQUIREDEXECUTIONENVIRONMENT. */
	public static final String	BUNDLE_REQUIREDEXECUTIONENVIRONMENT	= "Bundle-RequiredExecutionEnvironment";

	/** The Constant BUNDLE_SYMBOLICNAME. */
	public static final String	BUNDLE_SYMBOLICNAME					= "Bundle-SymbolicName";

	/** The Constant BUNDLE_LOCALIZATION. */
	public static final String	BUNDLE_LOCALIZATION					= "Bundle-Localization";

	/** The Constant REQUIRE_BUNDLE. */
	public static final String	REQUIRE_BUNDLE						= "Require-Bundle";

	/** The Constant FRAGMENT_HOST. */
	public static final String	FRAGMENT_HOST						= "Fragment-Host";

	/** The Constant BUNDLE_MANIFESTVERSION. */
	public static final String	BUNDLE_MANIFESTVERSION				= "Bundle-ManifestVersion";

	/** The Constant BUNDLE_URL. */
	public static final String	BUNDLE_URL							= "Bundle-URL";

	/** The Constant BUNDLE_SOURCE. */
	public static final String	BUNDLE_SOURCE						= "Bundle-Source";

	/** The Constant BUNDLE_DATE. */
	public static final String	BUNDLE_DATE							= "Bundle-Date";

	/** The Constant METADATA_LOCATION. */
	public static final String	METADATA_LOCATION					= "Metadata-Location";

	/** The Constant SERVICE_COMPONENT. */
	public static final String	SERVICE_COMPONENT					= "Service-Component";

	/** The Constant MANIFEST_VERSION. */
	public static final String	MANIFEST_VERSION					= "Manifest-Version";

	/** The mf. */
	protected Headers			mf;

	/** The write. */
	protected Appendable		write;

	/**
	 * Instantiates a new osgi manifest.
	 * 
	 * @param p
	 *            the p
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public OsgiManifest(IProject p) throws BundleException {
		IFile manifest = p.getFile(new Path("META-INF/MANIFEST.MF"));
		mf = null;
		if (manifest.exists()) {
			try {
				mf = Headers.parseManifest(new FileInputStream(manifest.getLocation().toFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}

		/*
		 * 
		 */
		if (mf == null) {
			mf = new Headers(20);
		} else {
			mf = new Headers(mf); // because mf is read only, il faut le
			// dupliquer
		}
	}

	/**
	 * It's readOnly.
	 * 
	 * @param manifest
	 *            the manifest
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public OsgiManifest(InputStream manifest) throws BundleException {
		mf = Headers.parseManifest(manifest);
	}

	/**
	 * Instantiates a new osgi manifest.
	 * 
	 * @param manifest
	 *            the manifest
	 * @param info
	 *            the info
	 */
	public OsgiManifest(IFile manifest, PDEGenerateModel info) {
		mf = null;
		if (manifest.exists()) {
			try {
				mf = Headers.parseManifest(new FileInputStream(manifest.getLocation().toFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}

		/*
		 * 
		 */
		if (mf == null) {
			mf = new Headers(20);
		} else {
			mf = new Headers(mf); // because mf is read only, il faut le
			// dupliquer
		}
		put(MANIFEST_VERSION, "1.0");
		put(BUNDLE_MANIFESTVERSION, "2");
		put(BUNDLE_NAME, info.pluginID);
		put(BUNDLE_SYMBOLICNAME, info.pluginID + ";singleton:=true");
		if (info.qualifiedActivatorName != null) {
			put(BUNDLE_ACTIVATOR, info.qualifiedActivatorName);
		}
		mput(BUNDLE_VERSION, "1.0.0");
		mput(BUNDLE_LOCALIZATION, "plugin");
		mput(BUNDLE_CLASSPATH, ".");
		mput(ECLIPSE_LAZY_START, Boolean.toString(info.isLazyStart));
		putArray(EXPORT_PACKAGE, true, false, info.exportsPackages);
		putArray(IMPORT_PACKAGE, true, false, info.importsPackages);
		putArray(IMPORT_PACKAGE, true, false, "org.eclipse.core.runtime", "org.osgi.framework");
	}

	/**
	 * Construct a manifest object from <code>manifest's file</code>. set
	 * attribute : MANIFEST_VERSION, BUNDLE_MANIFESTVERSION, BUNDLE_NAME,
	 * BUNDLE_SYMBOLICNAME, BUNDLE_ACTIVATOR BUNDLE_VERSION,
	 * BUNDLE_LOCALIZATION, BUNDLE_CLASSPATH, ECLIPSE_LAZY_START
	 * 
	 * @param manifest
	 *            A file can be exist or not where read old manifest value
	 * @param pluginID
	 *            the plugin id
	 * @param qualifiedActivatorName
	 *            the activator qualified class name, can be null if no
	 *            activator
	 * @param isLazyStart
	 *            true if the bundle is lazy start.
	 */
	public OsgiManifest(IFile manifest, String pluginID, String qualifiedActivatorName, boolean isLazyStart) {
		mf = null;
		if (manifest.exists()) {
			try {
				mf = Headers.parseManifest(new FileInputStream(manifest.getLocation().toFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}

		/*
		 * 
		 */
		if (mf == null) {
			mf = new Headers(20);
		} else {
			mf = new Headers(mf); // because mf is read only, il faut le
			// dupliquer
		}
		put(MANIFEST_VERSION, "1.0");
		put(BUNDLE_MANIFESTVERSION, "2");
		put(BUNDLE_NAME, pluginID);
		put(BUNDLE_SYMBOLICNAME, pluginID + ";singleton:=true");
		if (qualifiedActivatorName != null) {
			put(BUNDLE_ACTIVATOR, qualifiedActivatorName);
		}
		mput(BUNDLE_VERSION, "1.0.0");
		mput(BUNDLE_LOCALIZATION, "plugin");
		mput(BUNDLE_CLASSPATH, ".");
		mput(ECLIPSE_LAZY_START, Boolean.toString(isLazyStart));
	}

	/**
	 * Construct a default manifest.
	 */
	public OsgiManifest() {
		mf = new Headers(20);
	}

	/**
	 * Instantiates a new osgi manifest.
	 * 
	 * @param m
	 *            the m
	 */
	public OsgiManifest(OsgiManifest m) {
		mf = new Headers(m.mf);
	}

	/**
	 * Get the manifest value, null if no value.
	 * 
	 * @param key
	 *            the manifest key
	 * 
	 * @return the manifest value
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public ManifestElement getAttribute(String key) throws BundleException {
		String v = (String) mf.get(key);
		if (v == null || v.length() == 0) {
			return null;
		}
		ManifestElement[] e = ManifestElement.parseHeader(key, v);
		if (e != null && e.length == 1) {
			return e[0];
		}
		return null;
	}

	/**
	 * Gets the attribute string.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @return the attribute string
	 * 
	 * @throws BundleException
	 *             the bundle exception
	 */
	public String getAttributeString(String key) throws BundleException {
		String v = (String) mf.get(key);
		if (v == null || v.length() == 0) {
			return null;
		}
		ManifestElement[] e = ManifestElement.parseHeader(key, v);
		if (e != null && e.length == 1) {
			return e[0].getValue();
		}
		return null;
	}

	/**
	 * Return an array of manifest element for an entry 'key'. Return null if
	 * the value is null or empty
	 * 
	 * @param key
	 *            the property
	 * 
	 * @return the value for key or null
	 * 
	 * @throws BundleException
	 *             if the header value is invalid
	 */
	public ManifestElement[] getAttributes(String key) throws BundleException {
		String v = (String) mf.get(key);
		if (v == null || v.length() == 0) {
			return null;
		}
		ManifestElement[] e = ManifestElement.parseHeader(key, v);
		return e;
	}

	/**
	 * Change a array property with merge or not, overwrite or not.
	 * 
	 * @param key
	 *            a property key
	 * @param merge
	 *            merge with old values or not
	 * @param overwrite
	 *            overwrite old value
	 * @param values
	 *            an array of String to add in list.
	 */
	public void putArray(String key, boolean merge, boolean overwrite, String... values) {
		try {
			ManifestElement[] newvalues = new ManifestElement[values.length];

			for (int i = 0; i < newvalues.length; i++) {
				newvalues[i] = new OsgiManifestElement(values[i]);
			}
			putArray(key, merge, overwrite, newvalues);
		} catch (BundleException e) {
			// to nothing
		}
	}

	/**
	 * Change a array property with merge or not, overwrite or not.
	 * 
	 * @param key
	 *            a property key
	 * @param merge
	 *            merge with old values or not
	 * @param overwrite
	 *            overwrite old value
	 * @param values
	 *            an array of String to add in list.
	 */
	public void putArray(String key, boolean merge, boolean overwrite, ManifestElement... values) {
		TreeMap<String, ManifestElement> newvalues = new TreeMap<String, ManifestElement>();
		String oldHeaderValue = (String) mf.get(key);
		if (merge && overwrite && oldHeaderValue != null && oldHeaderValue.length() != 0) {
			try {
				ManifestElement[] oldValues = ManifestElement.parseHeader(key, oldHeaderValue);
				for (ManifestElement oe : oldValues) {
					newvalues.put(oe.getValue(), oe);
				}
			} catch (BundleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (ManifestElement ne : values) {
			newvalues.put(ne.getValue(), ne);
		}

		if (merge && !overwrite && oldHeaderValue != null && oldHeaderValue.length() != 0) {
			try {
				ManifestElement[] oldValues = ManifestElement.parseHeader(key, oldHeaderValue);
				if (oldValues != null) {
					for (ManifestElement oe : oldValues) {
						newvalues.put(oe.getValue(), oe);
					}
				}
			} catch (BundleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		StringBuilder sb = new StringBuilder();
		for (ManifestElement v : newvalues.values()) {
			OsgiManifestElement.toString(sb, v);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		put(key, sb.toString());
	}

	/**
	 * Put a key/value in manifest, overwrite always.
	 * 
	 * @param key
	 *            a key
	 * @param value
	 *            a value
	 */
	public void put(String key, String value) {
		mf.set(key, value, true);
	}

	/**
	 * Put a key/value in manifest if old value not exits.
	 * 
	 * @param key
	 *            a key
	 * @param value
	 *            a value
	 */
	public void mput(String key, String value) {
		String oldvalue = (String) mf.get(key);
		if (oldvalue != null) {
			return;
		}
		put(key, value);
	}

	public void save(IProject p, IProgressMonitor monitor) throws IOException, BundleException, CoreException {
		StringBuilder sb = new StringBuilder();
		write(sb);
		MappingManager.generate(p, new Path("META-INF"), "MANIFEST.MF", sb.toString(), monitor);
	}

	/**
	 * Write a manifest.
	 * 
	 * @param a
	 *            the a
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws BundleException
	 *             the bundle exception
	 */
	public void write(Appendable a) throws IOException, BundleException {
		this.write = a;
		w(MANIFEST_VERSION);
		w(BUNDLE_MANIFESTVERSION);
		w(BUNDLE_NAME);
		w(BUNDLE_SYMBOLICNAME);
		w(BUNDLE_VENDOR);
		w(BUNDLE_ACTIVATOR);
		w(BUNDLE_VERSION);
		w(BUNDLE_LOCALIZATION);
		w(BUNDLE_CLASSPATH);
		w(ECLIPSE_LAZY_START);
		mw(EXPORT_PACKAGE);
		mw(IMPORT_PACKAGE);
		mw(REQUIRE_BUNDLE);
		wo(MANIFEST_VERSION, BUNDLE_MANIFESTVERSION, BUNDLE_MANIFESTVERSION, BUNDLE_NAME, BUNDLE_SYMBOLICNAME,
				BUNDLE_ACTIVATOR, BUNDLE_VERSION, BUNDLE_LOCALIZATION, BUNDLE_CLASSPATH, ECLIPSE_LAZY_START,
				EXPORT_PACKAGE, IMPORT_PACKAGE, REQUIRE_BUNDLE, BUNDLE_VENDOR);
		write.append("\n");
	}

	/**
	 * Write a key if need.
	 * 
	 * @param key
	 *            a key to write in manifest
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void w(String key) throws IOException {
		String value = (String) mf.get(key);
		if (value == null || value.length() == 0) {
			return;
		}
		write.append(key).append(": ").append(value).append("\n");
	}

	/**
	 * Write a key of type list if need.
	 * 
	 * @param key
	 *            the key
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws BundleException
	 *             the bundle exception
	 */
	public void mw(String key) throws IOException, BundleException {
		String value = (String) mf.get(key);
		if (value == null || value.length() == 0) {
			return;
		}
		ManifestElement[] elements = ManifestElement.parseHeader(key, value);
		Arrays.sort(elements, new Comparator<ManifestElement>() {

			public int compare(ManifestElement me1, ManifestElement me2) {
				return me1.getValue().compareTo(me2.getValue());
			}
		});
		write.append(key).append(":");
		for (int i = 0; i < elements.length; i++) {
			if (i != 0) {
				write.append(",\n");
			}

			write.append(" ");
			OsgiManifestElement.toString(write, elements[i]);
		}
		write.append("\n");
	}

	/**
	 * Write others key.
	 * 
	 * @param keys
	 *            the keys
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void wo(String... keys) throws IOException {
		HashSet<String> hkeys = new HashSet<String>(Arrays.asList(keys));
		Enumeration<String> mfkeys = mf.keys();
		while (mfkeys.hasMoreElements()) {
			String k = mfkeys.nextElement();
			if (hkeys.contains(k)) {
				continue;
			}
			String value = (String) mf.get(k);
			if (value == null | value.length() == 0) {
				continue;
			}
			write.append(k).append(": ");
			writeBlock(k, value);
			write.append("\n");
		}
	}

	private void writeBlock(String key, String value) throws IOException {
		int line = 70 - 1 - key.length();
		int pos = 0;
		while (pos < value.length()) {
			int l = Math.min(line, value.length() - pos);

			if (pos != 0) {
				write.append("\n ");
			} else {
				line = 70;
			}

			write.append(value.substring(pos, pos + l));
			pos = pos + l;
		}
	}

	/**
	 * Removes the entry.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void removeEntry(String key, String value) {
		TreeMap<String, ManifestElement> newvalues = new TreeMap<String, ManifestElement>();
		String oldHeaderValue = (String) mf.get(key);
		try {
			ManifestElement[] oldValues = ManifestElement.parseHeader(key, oldHeaderValue);
			for (ManifestElement oe : oldValues) {
				if (oe.getValue().equals(value)) {
					continue;
				}
				newvalues.put(oe.getValue(), oe);
			}
		} catch (BundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		for (ManifestElement v : newvalues.values()) {
			OsgiManifestElement.toString(sb, v);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		put(key, sb.toString());
	}

}
