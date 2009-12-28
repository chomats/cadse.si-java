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
/*
 * Adele/LIG/ Grenoble University, France
 * 2006-2008
 */
package fede.workspace.dependencies.eclipse.java.view;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import fede.workspace.dependencies.eclipse.java.ItemDependenciesClasspathEntry;
import fede.workspace.tool.view.WSPlugin;
import fr.imag.adele.cadse.core.Item;

/**
 * The Class ItemDependenciesClasspathPage.
 */
public class ItemDependenciesClasspathPage extends WizardPage implements IClasspathContainerPage,
		IClasspathContainerPageExtension {

	/** The entry. */
	private IClasspathEntry	entry;

	/** The project. */
	private IJavaProject	project;

	/**
	 * Instantiates a new item dependencies classpath page.
	 */
	public ItemDependenciesClasspathPage() {
		super("Item Dependencies", "Item Dependencies", null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension#initialize(org.eclipse.jdt.core.IJavaProject,
	 *      org.eclipse.jdt.core.IClasspathEntry[])
	 */
	public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#getSelection()
	 */
	public IClasspathEntry getSelection() {
		if (entry == null) {
			entry = ItemDependenciesClasspathEntry.CLASSPATH_ENTRY;
		}
		return entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#setSelection(org.eclipse.jdt.core.IClasspathEntry)
	 */
	public void setSelection(IClasspathEntry containerEntry) {
		entry = containerEntry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#finish()
	 */
	public boolean finish() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		try {
			label.setText(heading(WSPlugin.sGetItemFromResource(project.getResource())));
		} catch (Exception ignored) {
			label.setText("This project is not associated with a Melusine item, this container cannot be used");
		}

		setControl(label);
	}

	/**
	 * Heading.
	 * 
	 * @param item
	 *            the item
	 * 
	 * @return the string
	 */
	private String heading(Item item) {
		if (item == null) {
			return "This project is not associated with a Melusine item, this container cannot be used";
		}

		return "Item dependencies for item " + item.getName();
	}

}
