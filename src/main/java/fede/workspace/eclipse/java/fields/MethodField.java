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

package fede.workspace.eclipse.java.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import fede.workspace.tool.view.WSPlugin;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.si.workspace.uiplatform.swt.ui.DAbstractField;
/**
 * The Class MethodField.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class MethodField<IC extends IMethodInteractiveController> extends DAbstractField<IC> {

	/**
	 * Instantiates a new method field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * @param poslabel
	 *            the poslabel
	 * @param mc
	 *            the mc
	 * @param ic
	 *            the ic
	 */
	public MethodField() {
	}

	/** The JAV a_ elemen t_ labe l_ provider. */
	private final JavaElementLabelProvider	JAVA_ELEMENT_LABEL_PROVIDER	= new JavaElementLabelProvider(
																				JavaElementLabelProvider.SHOW_DEFAULT);

	/** The package table. */
	private Tree							packageTable;

	/** The method selected. */
	private IMethod							methodSelected				= null;

	/** The select button. */
	private Button							selectButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#createControl(fr.imag.adele.cadse.core.ui.IPageController,
	 *      fr.imag.adele.cadse.core.ui.IFedeFormToolkit, java.lang.Object, int)
	 */
	@Override
	public void createControl(Composite container,	int hspan) {
		GridData gd;
		packageTable = new Tree((Composite) container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 65;
		gd.verticalSpan = 1;
		gd.horizontalSpan = hspan - 1;
		packageTable.setLayoutData(gd);

		selectButton = new Button((Composite) container, SWT.PUSH);
		selectButton.setText("...");
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(@SuppressWarnings("unused")
			SelectionEvent e) {
				IMethod oldm = methodSelected;
				handleSelect();
				if (oldm != methodSelected) {
					_swtuiplatform.broadcastValueChanged(_page, _field, getVisualValue());
				}

			}
		});
		gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.verticalSpan = 1;
		gd.horizontalSpan = 1;
		selectButton.setLayoutData(gd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#thisFieldHasChanged()
	 */
	@Override
	public void thisFieldHasChanged() {
		selectButton.setEnabled(getReferencedItem() != null);
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public IMethod getMethod() {
		return methodSelected;
	}

	/**
	 * Sets the method.
	 * 
	 * @param m
	 *            the new method
	 */
	protected void setMethod(IMethod m) {
		packageTable.removeAll();

		IType t = getType(m);
		if (t == null) {
			return;
		}

		IPackageFragment p = getPackage(t);
		if (p == null) {
			return;
		}

		this.methodSelected = m;

		packageTable.removeAll();

		TreeItem pi = new TreeItem(packageTable, SWT.NONE);
		pi.setText(JAVA_ELEMENT_LABEL_PROVIDER.getText(p));
		pi.setImage(JAVA_ELEMENT_LABEL_PROVIDER.getImage(p));

		TreeItem ti = new TreeItem(pi, SWT.NONE);
		ti.setText(JAVA_ELEMENT_LABEL_PROVIDER.getText(t));
		ti.setImage(JAVA_ELEMENT_LABEL_PROVIDER.getImage(t));

		TreeItem mi = new TreeItem(ti, SWT.NONE);
		mi.setText(JAVA_ELEMENT_LABEL_PROVIDER.getText(m));
		mi.setImage(JAVA_ELEMENT_LABEL_PROVIDER.getImage(m));

		pi.setExpanded(true);
		ti.setExpanded(true);
	}

	/**
	 * Handle select.
	 */
	protected void handleSelect() {

		IMethod m = chooseMethod();
		if (m == null) {
			return;
		}

		setMethod(m);

	}

	/**
	 * Gets the package.
	 * 
	 * @param t
	 *            the t
	 * 
	 * @return the package
	 */
	private IPackageFragment getPackage(IType t) {
		IJavaElement je = t.getParent();
		while (je != null) {
			if (je instanceof IPackageFragment) {
				return (IPackageFragment) je;
			}
			je = je.getParent();
		}
		return null;
	}

	/**
	 * Gets the type.
	 * 
	 * @param m
	 *            the m
	 * 
	 * @return the type
	 */
	private IType getType(IMethod m) {
		IJavaElement je = m.getParent();
		while (je != null) {
			if (je instanceof IType) {
				return (IType) je;
			}
			je = je.getParent();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setVisualValue(java.lang.Object)
	 */
	public void setVisualValue(Object visualValue, boolean sendNotification) {
		String methodString = (String) visualValue;
		if (methodString != null) {
			IMethod m = (IMethod) JavaCore.create(methodString);
			if (m != null) {
				setMethod(m);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getVisualValue()
	 */
	@Override
	public Object getVisualValue() {
		return getMethod() == null ? null : getMethod().getHandleIdentifier();
	}

	/**
	 * Choose method.
	 * 
	 * @return the i method
	 */
	private IMethod chooseMethod() {
		IJavaElement[] packages = getPackageFragment();

		if (packages == null) {
			packages = new IJavaElement[0];
		}

		StandardJavaElementContentProvider standardJavaElementContentProvider = new StandardJavaElementContentProvider(
				true) {
			@Override
			public Object[] getChildren(Object element) {
				if (element instanceof IJavaElement[]) {
					return (Object[]) element;
				}
				if (element instanceof IPackageFragment) {
					try {
						return mygetPackageContents((IPackageFragment) element);
					} catch (JavaModelException e) {
						return new Object[0];
					}
				}

				return super.getChildren(element);
			}

			private Object[] mygetPackageContents(IPackageFragment fragment) throws JavaModelException {
				List<IType> types = new ArrayList<IType>();
				if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
					ICompilationUnit[] cu = fragment.getCompilationUnits();
					for (ICompilationUnit oneCu : cu) {
						types.addAll(Arrays.asList(oneCu.getTypes()));
					}
					return types.toArray();
					// concatenate(fragment.getCompilationUnits(),
					// fragment.getNonJavaResources());
				}
				IClassFile[] cfs = fragment.getClassFiles();
				for (IClassFile cf : cfs) {
					types.add(cf.getType());
				}
				return types.toArray();
				// return concatenate(fragment.getClassFiles(),
				// fragment.getNonJavaResources());
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof IJavaElement[]) {
					return true;
				}
				return super.hasChildren(element);
			}
		};
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(packageTable.getShell(),
				JAVA_ELEMENT_LABEL_PROVIDER, standardJavaElementContentProvider);
		// dialog.setIgnoreCase(false);
		dialog.setTitle(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_title);
		dialog.setMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_description);
		dialog.setEmptyListMessage(NewWizardMessages.NewTypeWizardPage_ChoosePackageDialog_empty);
		dialog.setAllowMultiple(false);
		dialog.setDoubleClickSelects(false);
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection != null && selection.length == 1 && selection[0] instanceof IMethod) {
					return Status.OK_STATUS;
				}
				return new Status(IStatus.ERROR, WSPlugin.PLUGIN_ID, 0, "Select a method", null);
			}
		});

		dialog.setInput(packages);

		if (methodSelected != null) {
			dialog.setInitialSelections(new Object[] { methodSelected });
		}

		if (dialog.open() == Window.OK) {
			return (IMethod) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Gets the package fragment.
	 * 
	 * @return the package fragment
	 */
	private IPackageFragment[] getPackageFragment() {
		Item _referencedItem = getReferencedItem();
		if (_referencedItem == null) {
			return null;
		}
		return _ic.getPackageFragment(_referencedItem);
	}

	/**
	 * Gets the referenced item.
	 * 
	 * @return the referenced item
	 */
	protected Item getReferencedItem() {
		return _ic.getReferencedItem();
	}

	/**
	 * Dialog has changed.
	 */
	public void dialogHasChanged() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean v) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setEditable(boolean)
	 */
	@Override
	public void setEditable(boolean v) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.impl.ui.DAbstractField#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean v) {
	}

	@Override
	public Control getMainControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getSelectedObjects() {
		// TODO Auto-generated method stub
		return null;
	}
}
