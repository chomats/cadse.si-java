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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.codegen.jet.JETException;
import org.eclipse.emf.codegen.merge.java.JControlModel;
import org.eclipse.emf.codegen.merge.java.JMerger;
import org.eclipse.emf.codegen.merge.java.facade.FacadeHelper;
import org.eclipse.emf.codegen.merge.java.facade.JCompilationUnit;
import org.eclipse.emf.codegen.util.CodeGenUtil;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.BundleContext;

import fede.workspace.tool.eclipse.MappingManager;

/**
 * The Class JMergeUtil.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class JMergeUtil {

	/** The jmerge model file url. */
	private static String	jmergeModelFileURL;

	/**
	 * Inits the.
	 * 
	 * @param context
	 *            the context
	 */
	static public void init(BundleContext context) {
		jmergeModelFileURL = context.getBundle().getResource("schema/jmerge.xml").toString();
	}

	/**
	 * Returns a non-null progress monitor.
	 * 
	 * @param monitor
	 *            an existing progress monitor
	 * 
	 * @return a new <code>NullProgressMonitor</code> if the specified monitor
	 *         is <code>null</code>, or the existing monitor otherwise
	 */
	private static IProgressMonitor createIfNull(IProgressMonitor monitor) {
		if (monitor == null) {
			return new NullProgressMonitor();
		}
		return monitor;
	}

	/**
	 * Merges the specified emitterResult with the contents of an existing file
	 * and returns the result. The existing file is not modified.
	 * <p>
	 * The location of the file to merge with is found by finding or creating
	 * the container (folder) for the <code>Config</code>'s package in the
	 * <code>Config</code>'s target folder. The name of the file to merge
	 * with is the <code>Config</code>'s target file.
	 * 
	 * @param monitor
	 *            the progress monitor to use. May be <code>null</code>.
	 * @param emitterResult
	 *            generated content to merge with the existing content
	 * @param targetFile
	 *            the target file
	 * 
	 * @return the result of merging the specified generated contents with the
	 *         existing file
	 * 
	 * @throws CoreException
	 *             if an error occurs accessing the contents of the file
	 * @throws JETException
	 *             the JET exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void merge(IProgressMonitor monitor, IFile targetFile, String emitterResult) throws CoreException,
			JETException, IOException {
		try {
			monitor = createIfNull(monitor);
			if (!targetFile.exists()) {
				monitor.worked(1);
				setContent(targetFile, emitterResult, monitor);
				return;
			}

			FacadeHelper facadeHelper = CodeGenUtil.instantiateFacadeHelper(JMerger.DEFAULT_FACADE_HELPER_CLASS);
			JControlModel jMergeControlModel = new JControlModel();
			jMergeControlModel = new JControlModel();

			jMergeControlModel.initialize(facadeHelper, JMergeUtil.jmergeModelFileURL);

			JMerger jMerger = new JMerger(jMergeControlModel);

			JCompilationUnit targetCompilationUnit = jMerger.createCompilationUnitForInputStream(targetFile
					.getContents(true));
			if (targetCompilationUnit == null) {
				monitor.worked(1);
				setContent(targetFile, emitterResult, monitor);
				return;
			}
			String oldContents = targetCompilationUnit.getContents();
			if (oldContents == null || oldContents.length() == 0) {
				monitor.worked(1);
				setContent(targetFile, emitterResult, monitor);
				return;
			}

			jMerger.setSourceCompilationUnit(jMerger.createCompilationUnitForContents(emitterResult));
			jMerger.setTargetCompilationUnit(targetCompilationUnit);
			jMerger.merge();
			monitor.worked(1);

			String result = targetCompilationUnit.getContents();
			if (oldContents.equals(result)) {
				return;
			}

			if (!targetFile.isReadOnly()) {
				setContent(targetFile, result, monitor);
			}

			// /TODO
			// The file may be read-only because it is checked out
			// by a VCM component. Here we ask permission to change the file.
			if (targetFile.getWorkspace().validateEdit(new IFile[] { targetFile }, new SubProgressMonitor(monitor, 1))
					.isOK()) {
				jMerger.setTargetCompilationUnit(targetCompilationUnit);
				jMerger.remerge();
				setContent(targetFile, targetCompilationUnit.getContents(), monitor);
			}
			setContent(targetFile, result, monitor);
			;
		} catch (Throwable e) {
			System.err.println("Cannot merge a file : " + targetFile);
			System.err.println(emitterResult);
			e.printStackTrace();
		}
	}

	/**
	 * Merges the specified emitterResult with the contents of an existing file
	 * and returns the result. The existing file is not modified.
	 * <p>
	 * The location of the file to merge with is found by finding or creating
	 * the container (folder) for the <code>Config</code>'s package in the
	 * <code>Config</code>'s target folder. The name of the file to merge
	 * with is the <code>Config</code>'s target file.
	 * 
	 * @param monitor
	 *            the progress monitor to use. May be <code>null</code>.
	 * @param emitterResult
	 *            generated content to merge with the existing content
	 * @param targetFile
	 *            the target file
	 * 
	 * @return the result of merging the specified generated contents with the
	 *         existing file
	 * 
	 * @throws CoreException
	 *             if an error occurs accessing the contents of the file
	 * @throws JETException
	 *             the JET exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void merge(IProgressMonitor monitor, IFile targetFile, String emitterResult, Pattern[] replaces,
			String[] replaceString) throws CoreException, JETException, IOException {
		try {
			monitor = createIfNull(monitor);
			if (!targetFile.exists()) {
				monitor.worked(1);
				setContent(targetFile, emitterResult, monitor);
				return;
			}

			FacadeHelper facadeHelper = CodeGenUtil.instantiateFacadeHelper(JMerger.DEFAULT_FACADE_HELPER_CLASS);
			JControlModel jMergeControlModel = new JControlModel();
			jMergeControlModel = new JControlModel();

			jMergeControlModel.initialize(facadeHelper, JMergeUtil.jmergeModelFileURL);

			JMerger jMerger = new JMerger(jMergeControlModel);

			JCompilationUnit targetCompilationUnit = jMerger.createCompilationUnitForInputStream(targetFile
					.getContents(true));
			if (targetCompilationUnit == null) {
				monitor.worked(1);
				setContent(targetFile, emitterResult, monitor);
				return;
			}
			String oldContents = targetCompilationUnit.getContents();
			if (oldContents == null || oldContents.length() == 0) {
				monitor.worked(1);
				setContent(targetFile, emitterResult, monitor);
				return;
			}

			jMerger.setSourceCompilationUnit(jMerger.createCompilationUnitForContents(emitterResult));
			jMerger.setTargetCompilationUnit(targetCompilationUnit);
			jMerger.merge();
			monitor.worked(1);

			String result = targetCompilationUnit.getContents();
			if (replaces != null) {
				String v = result;
				for (int i = 0; i < replaces.length; i++) {
					Pattern p = replaces[i];
					String r = replaceString[i];
					v = transformeString(p, r, v);
				}
				result = v;
			}

			IJavaProject javaProject = JavaCore.create(targetFile.getProject());
			result = formatCode(javaProject, result);

			if (oldContents.equals(result)) {
				return;
			}

			if (!targetFile.isReadOnly()) {
				setContent(targetFile, result, monitor);
			}

		} catch (Throwable e) {
			System.err.println("Cannot merge a file : " + targetFile);
			System.err.println(emitterResult);
			e.printStackTrace();
		}
	}

	public static String transformeString(Pattern p, String r, String v) {
		Matcher m = p.matcher(v);
		StringBuffer sb = new StringBuffer();
		boolean find = false;
		while (m.find()) {
			m.appendReplacement(sb, r);
			find = true;
		}
		if (find) {
			m.appendTail(sb);
			v = sb.toString();
		}
		return v;
	}

	/**
	 * Sets the content.
	 * 
	 * @param file
	 *            the file
	 * @param content
	 *            the content
	 * @param monitor
	 *            the monitor
	 * 
	 * @throws CoreException
	 *             the core exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void setContent(IFile file, String content, IProgressMonitor monitor) throws CoreException,
			IOException {
		IContainer parent = file.getParent();
		if (parent.getType() == IResource.FOLDER) {
			MappingManager.createFolder((IFolder) parent, monitor);
		}

		ByteArrayInputStream stream = new ByteArrayInputStream(content.toString().getBytes(
				file.getProject().getDefaultCharset()));
		if (file.exists()) {
			file.setContents(stream, true, true, monitor);
		} else {
			file.create(stream, false, monitor);
		}
		stream.close();
	}

	public static CodeFormatter createCodeFormatter(IJavaProject javaProject) {
		return ToolFactory.createCodeFormatter(javaProject.getOptions(true));
	}

	public static String formatCode(IJavaProject javaProject, String contents) throws MalformedTreeException,
			BadLocationException {
		CodeFormatter codeFormatter = createCodeFormatter(javaProject);

		IDocument doc = new Document(contents);
		String contentsValue = doc.get();
		TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, contentsValue, 0,
				contentsValue.length(), 0, null);
		edit.apply(doc);
		return doc.get();
	}
}
