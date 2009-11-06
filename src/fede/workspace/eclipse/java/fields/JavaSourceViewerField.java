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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

import fede.workspace.model.manager.properties.impl.ui.DAbstractField;
import fede.workspace.tool.view.WSPlugin;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.IFedeFormToolkit;
import fr.imag.adele.cadse.core.ui.RunningModelController;
import fr.imag.adele.cadse.core.ui.IPageController;

/**
 * A dialog which prompts the user to enter an expression for evaluation.
 */
public class JavaSourceViewerField extends DAbstractField {

	/** The width of the vertical ruler. */
	protected final static int				VERTICAL_RULER_WIDTH	= 12;

	/** The result. */
	protected String						fResult					= null;

	// Input area composite which acts as a placeholder for
	// input widgetry that is created/disposed dynamically.
	/** The input area. */
	protected Composite						fInputArea;
	// Source viewer widgets
	/** The source viewer. */
	protected ISourceViewer					fSourceViewer;

	/** The completion processor. */
	protected DisplayCompletionProcessor	fCompletionProcessor;

	/** The document listener. */
	protected IDocumentListener				fDocumentListener;

	/** The submission. */
	protected IHandlerActivation			fSubmission;

	/** The document. */
	IDocument								document;

	/** The ic. */
	JavaSourceInteractifController			ic;

	/** The vertical ruler. */
	private IVerticalRuler					fVerticalRuler;

	/** The annotation preferences. */
	private MarkerAnnotationPreferences		fAnnotationPreferences;

	/**
	 * The overview ruler of this editor.
	 * <p>
	 * This field should not be referenced by subclasses. It is
	 * <code>protected</code> for API compatibility reasons and will be made
	 * <code>private</code> soon. Use {@link #getOverviewRuler()} instead.
	 * </p>
	 */
	protected IOverviewRuler				fOverviewRuler;

	/**
	 * Helper for accessing annotation from the perspective of this editor.
	 * <p>
	 * This field should not be referenced by subclasses. It is
	 * <code>protected</code> for API compatibility reasons and will be made
	 * <code>private</code> soon. Use {@link #getAnnotationAccess()} instead.
	 * </p>
	 */
	protected IAnnotationAccess				fAnnotationAccess;

	/** The projection support. */
	private ProjectionSupport				fProjectionSupport;

	/**
	 * Instantiates a new java source viewer field.
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
	public JavaSourceViewerField(String key, String label, EPosLabel poslabel, RunningModelController mc,
			JavaSourceInteractifController ic) {
		super(key, label, poslabel, mc, ic);
		this.ic = ic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#createControl(fr.imag.adele.cadse.core.ui.IPageController,
	 *      fr.imag.adele.cadse.core.ui.IFedeFormToolkit, java.lang.Object, int)
	 */
	@Override
	public Composite createControl(IPageController globalUIController, IFedeFormToolkit toolkit, Object parent,
			int hspan) {

		fInputArea = new Composite((Composite) parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = hspan;

		fInputArea.setLayoutData(gridData);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fInputArea.setLayout(layout);
		Dialog.applyDialogFont(fInputArea);

		fVerticalRuler = createVerticalRuler();

		int styles = SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;
		fSourceViewer = createSourceViewer(fInputArea, fVerticalRuler, styles);

		configureSourceViewer(globalUIController);

		((JavaSourceViewer) fSourceViewer).doOperation(ITextOperationTarget.SELECT_ALL);

		return fInputArea;
	}

	/*
	 * @see AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler,
	 *      int)
	 */
	/**
	 * Creates the source viewer.
	 * 
	 * @param parent
	 *            the parent
	 * @param verticalRuler
	 *            the vertical ruler
	 * @param styles
	 *            the styles
	 * 
	 * @return the i source viewer
	 */
	protected final ISourceViewer createSourceViewer(Composite parent, IVerticalRuler verticalRuler, int styles) {

		IPreferenceStore store = JFacePreferences.getPreferenceStore();
		ISourceViewer viewer = createJavaSourceViewer(parent, verticalRuler, getOverviewRuler(),
				isOverviewRulerVisible(), styles, store);

		/*
		 * This is a performance optimization to reduce the computation of the
		 * text presentation triggered by {@link #setVisibleDocument(IDocument)}
		 */
		ProjectionViewer projectionViewer = (ProjectionViewer) viewer;
		fProjectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.setHoverControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell shell) {
				return null; // TODO new
				// SourceViewerInformationControl(shell,
				// SWT.TOOL | SWT.NO_TRIM, SWT.NONE);
			}
		});
		fProjectionSupport.install();

		// ensure source viewer decoration support has been created and
		// configured
		// getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	/**
	 * Creates the vertical ruler to be used by this editor. Subclasses may
	 * re-implement this method.
	 * 
	 * @return the vertical ruler
	 */
	protected IVerticalRuler createVerticalRuler() {
		return new VerticalRuler(VERTICAL_RULER_WIDTH);
	}

	/**
	 * Returns the annotation access.
	 * 
	 * @return the annotation access
	 */
	protected IAnnotationAccess getAnnotationAccess() {
		if (fAnnotationAccess == null) {
			fAnnotationAccess = createAnnotationAccess();
		}
		return fAnnotationAccess;
	}

	/**
	 * Creates the annotation access for this editor.
	 * 
	 * @return the created annotation access
	 */
	protected IAnnotationAccess createAnnotationAccess() {
		return new DefaultMarkerAnnotationAccess();
	}

	/*
	 * @see AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler,
	 *      int)
	 */
	/**
	 * Creates the java source viewer.
	 * 
	 * @param parent
	 *            the parent
	 * @param verticalRuler
	 *            the vertical ruler
	 * @param overviewRuler
	 *            the overview ruler
	 * @param isOverviewRulerVisible
	 *            the is overview ruler visible
	 * @param styles
	 *            the styles
	 * @param store
	 *            the store
	 * 
	 * @return the i source viewer
	 */
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler,
			IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		return new JavaSourceViewer(parent, verticalRuler, getOverviewRuler(), isOverviewRulerVisible(), styles, store);
	}

	/**
	 * Checks if is overview ruler visible.
	 * 
	 * @return true, if is overview ruler visible
	 */
	private boolean isOverviewRulerVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Creates the overview ruler.
	 * 
	 * @param sharedColors
	 *            the shared colors
	 * 
	 * @return the i overview ruler
	 */
	protected IOverviewRuler createOverviewRuler(ISharedTextColors sharedColors) {
		IOverviewRuler ruler = new OverviewRuler(getAnnotationAccess(), VERTICAL_RULER_WIDTH, sharedColors);
		Iterator e = fAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference preference = (AnnotationPreference) e.next();
			if (preference.contributesToHeader()) {
				ruler.addHeaderAnnotationType(preference.getAnnotationType());
			}
		}
		return ruler;
	}

	/**
	 * Returns the overview ruler.
	 * 
	 * @return the overview ruler
	 */
	protected IOverviewRuler getOverviewRuler() {
		if (fOverviewRuler == null) {
			fOverviewRuler = createOverviewRuler(getSharedColors());
		}
		return fOverviewRuler;
	}

	/**
	 * Gets the shared colors.
	 * 
	 * @return the shared colors
	 */
	protected ISharedTextColors getSharedColors() {
		ISharedTextColors sharedColors = EditorsPlugin.getDefault().getSharedTextColors();
		return sharedColors;
	}

	/**
	 * Initializes the source viewer. This method is based on code in
	 * BreakpointConditionEditor.
	 * 
	 * @param globalUIController
	 *            the global ui controller
	 */
	private void configureSourceViewer(final IPageController globalUIController) {

		// if (fConfiguration == null)
		// fConfiguration= new SourceViewerConfiguration();
		// fSourceViewer.configure(fConfiguration);

		JavaTextTools tools = JavaPlugin.getDefault().getJavaTextTools();
		document = new Document();
		IDocumentPartitioner partitioner = tools.createDocumentPartitioner();
		document.setDocumentPartitioner(partitioner);
		partitioner.connect(document);
		fSourceViewer.configure(new DisplayViewerConfiguration(getJavaProject(), getJavaType()) {
			@Override
			public IContentAssistProcessor getContentAssistantProcessor() {
				return getCompletionProcessor();
			}
		});
		fSourceViewer.setEditable(true);
		fSourceViewer.setDocument(document);
		final IUndoManager undoManager = new TextViewerUndoManager(10);
		fSourceViewer.setUndoManager(undoManager);
		undoManager.connect(fSourceViewer);

		fSourceViewer.getTextWidget().setFont(JFaceResources.getTextFont());
		fSourceViewer.getTextWidget().setData(CADSE_MODEL_KEY, this);

		Control control = ((SourceViewer) fSourceViewer).getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		gd.widthHint = 400;
		control.setLayoutData(gd);

		// gd= (GridData)fSourceViewer.getControl().getLayoutData();
		// gd.heightHint= 100;
		// gd.widthHint= 400;

		fDocumentListener = new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}

			public void documentChanged(DocumentEvent event) {
				globalUIController.broadcastValueChanged(JavaSourceViewerField.this, getVisualValue());
			}
		};
		fSourceViewer.getDocument().addDocumentListener(fDocumentListener);

		IHandler handler = new AbstractHandler() {

			public Object execute(ExecutionEvent event) throws ExecutionException {
				((JavaSourceViewer) fSourceViewer).doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
				return null;
			}
		};
		// fSubmission = new HandlerSubmission(null,
		// fSourceViewer.getControl().getShell(), null,
		// ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler,
		// ISources.LEGACY_MEDIUM); //$NON-NLS-1$
		IWorkbench workbench = PlatformUI.getWorkbench();
		IHandlerService commandSupport = (IHandlerService) workbench.getAdapter(IHandlerService.class);
		fSubmission = commandSupport.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler);

		// commandSupport.addHandlerSubmission(fSubmission);
	}

	// /**
	// * Returns the text that should be shown in the source viewer upon
	// * initialization. The text should be presented in such a way that
	// * it can be used as an evaluation expression which will return the
	// * current value.
	// * @param variable the variable
	// * @return the initial text to display in the source viewer or
	// <code>null</code>
	// * if none.
	// */
	// protected String getInitialText() {
	// return initialText;
	// }

	/**
	 * Gets the java project.
	 * 
	 * @return the java project
	 */
	protected IJavaProject getJavaProject() {
		return ic.getJavaProject();
	}

	/**
	 * Gets the java type.
	 * 
	 * @return the java type
	 */
	protected IType getJavaType() {
		return null;
	}

	/**
	 * Return the completion processor associated with this viewer.
	 * 
	 * @return DisplayConditionCompletionProcessor
	 */
	protected DisplayCompletionProcessor getCompletionProcessor() {
		if (fCompletionProcessor == null) {
			fCompletionProcessor = new DisplayCompletionProcessor(getJavaProject(), getJavaType());
		}
		return fCompletionProcessor;
	}

	// @Override
	// public String fieldDialogChanged() {
	// String errorMessage= null;
	// String text= fSourceViewer.getDocument().get();
	// boolean valid= text != null && text.trim().length() > 0;
	// if (!valid) {
	// errorMessage= "Enter a declaration field";
	// return(errorMessage);
	//
	// }
	// ASTParser parser = ASTParser.newParser(AST.JLS3);
	// parser.setProject(getJavaProject());
	// parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
	// parser.setSource(text.toCharArray());
	// ASTNode astNode = parser.createAST(null);
	// //System.out.println(astNode);
	// if (astNode instanceof TypeDeclaration) {
	// TypeDeclaration td = (TypeDeclaration) astNode;
	// int nb_size = td.bodyDeclarations().size();
	// for (Object field : td.bodyDeclarations()) {
	// if (field instanceof FieldDeclaration) {
	// FieldDeclaration fd = (FieldDeclaration) field;
	// System.out.println(fd);
	// System.out.println("type:"+fd.getType());
	// if (fd.fragments().size() == 1 && fd.fragments().get(0) instanceof
	// VariableDeclarationFragment) {
	// VariableDeclarationFragment vdf = (VariableDeclarationFragment)
	// fd.fragments().get(0);
	// System.out.println("name:"+vdf.getName());
	// System.out.println("dim:"+vdf.getExtraDimensions());
	// System.out.println("value:"+vdf.getInitializer());
	//
	// }
	// } else if (field instanceof MethodDeclaration) {
	// MethodDeclaration md = (MethodDeclaration) field;
	// System.out.println(md);
	//
	// }
	// }
	// }
	// if (astNode instanceof CompilationUnit) {
	// CompilationUnit cu = (CompilationUnit) astNode;
	// IProblem[] problems = cu.getProblems();
	// StringBuilder sb = new StringBuilder();
	// if (problems.length > 0) {
	// for (IProblem p : problems) {
	// sb.append(p.toString());
	// }
	// }
	// return sb.toString();
	// }
	//
	// return null;
	// }

	/**
	 * Returns the text that is currently displayed in the source viewer.
	 * 
	 * @return the text that is currently displayed in the source viewer
	 */
	protected String getText() {
		return fSourceViewer.getDocument().get();
	}

	/**
	 * Disposes the source viewer. This method is intended to be overridden by
	 * subclasses.
	 */
	@Override
	public void dispose() {
		super.dispose();
		disposeSourceViewer();
	}

	/**
	 * Disposes the source viewer and all associated widgetry.
	 */
	protected void disposeSourceViewer() {
		if (fSubmission != null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IHandlerService commandSupport = (IHandlerService) workbench.getAdapter(IHandlerService.class);
			commandSupport.deactivateHandler(fSubmission);
			fSubmission = null;
		}
		if (fSourceViewer != null) {
			fSourceViewer.getDocument().removeDocumentListener(fDocumentListener);
			fSourceViewer.getTextWidget().dispose();
			((JavaSourceViewer) fSourceViewer).unconfigure();
			fSourceViewer = null;
		}
		document = null;
		fDocumentListener = null;
		fCompletionProcessor = null;
	}

	/**
	 * Returns the text entered by the user or <code>null</code> if the user
	 * cancelled.
	 * 
	 * @return the text entered by the user or <code>null</code> if the user
	 *         cancelled
	 */
	public String getResult() {
		return fResult;
	}

	/**
	 * Gets the dialog settings section name.
	 * 
	 * @return the dialog settings section name
	 */
	protected String getDialogSettingsSectionName() {
		return "EXPRESSION_INPUT_DIALOG"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getVisualValue()
	 */
	@Override
	public Object getVisualValue() {
		return fSourceViewer.getDocument().get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setVisualValue(java.lang.Object)
	 */
	public void setVisualValue(Object visualValue, boolean sendNotification) {
		fSourceViewer.getDocument().set((String) visualValue);
		// fSourceViewer.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getHSpan()
	 */
	@Override
	public int getHSpan() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean v) {
		((SourceViewer) fSourceViewer).getControl().setEnabled(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setEditable(boolean)
	 */
	@Override
	public void internalSetEditable(boolean v) {
		fSourceViewer.setEditable(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fede.workspace.model.manager.properties.impl.ui.DAbstractField#setVisible(boolean)
	 */
	@Override
	public void internalSetVisible(boolean v) {
		((SourceViewer) fSourceViewer).getControl().setVisible(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getUIObject(int)
	 */
	@Override
	public Object getUIObject(int index) {
		return fSourceViewer;
	}

	public ItemType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Control getMainControl() {
		return fSourceViewer.getTextWidget();
	}

	@Override
	public Object[] getSelectedObjects() {
		// TODO Auto-generated method stub
		return null;
	}
}

final class DisplayCompletionProcessor implements IContentAssistProcessor {

	private CompletionProposalCollector		fCollector;
	private IContextInformationValidator	fValidator;
	private TemplateEngine					fTemplateEngine;
	private String							fErrorMessage	= null;
	private IJavaProject					javaProject;
	private IJavaElement					receivingType;

	private char[]							fProposalAutoActivationSet;
	private CompletionProposalComparator	fComparator;

	public DisplayCompletionProcessor(IJavaProject javaProject, IJavaElement receivingType) {
		TemplateContextType contextType = JavaPlugin.getDefault().getTemplateContextRegistry().getContextType("java"); //$NON-NLS-1$
		if (contextType != null) {
			fTemplateEngine = new TemplateEngine(contextType);
		}
		fComparator = new CompletionProposalComparator();
		this.javaProject = javaProject;
		this.receivingType = receivingType;
	}

	/**
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (fErrorMessage != null) {
			return fErrorMessage;
		}
		if (fCollector != null) {
			return fCollector.getErrorMessage();
		}
		return null;
	}

	/**
	 * Sets the error message for why completions could not be resolved. Clients
	 * should clear this before computing completions.
	 * 
	 * @param string
	 *            message
	 */
	protected void setErrorMessage(String string) {
		if (string != null && string.length() == 0) {
			string = null;
		}
		fErrorMessage = string;
	}

	/**
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		if (fValidator == null) {
			fValidator = new JavaParameterListValidator();
		}
		return fValidator;
	}

	/**
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/**
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	/**
	 * @see IContentAssistProcessor#computeProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		try {
			setErrorMessage(null);
			return computeCompletionProposals2(viewer, documentOffset);
		} finally {
			releaseCollector();
		}
	}

	protected ICompletionProposal[] computeCompletionProposals2(ITextViewer viewer, int documentOffset) {
		setErrorMessage(null);
		try {
			IJavaProject project = receivingType.getJavaProject();

			ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
			configureResultCollector(project, selection);

			IMethod m;
			// int insertionPosition = computeInsertionPosition();

			// receivingType.codeComplete(viewer.getDocument().get().toCharArray(),
			// insertionPosition, documentOffset,
			// new char[0][0], new char[0][0],
			// new int[0], isStatic(), fCollector);

			IJavaCompletionProposal[] results = fCollector.getJavaCompletionProposals();

			if (fTemplateEngine != null) {
				fTemplateEngine.reset();
				fTemplateEngine.complete(viewer, documentOffset, null);
				TemplateProposal[] templateResults = fTemplateEngine.getResults();

				// concatenate arrays
				IJavaCompletionProposal[] total = new IJavaCompletionProposal[results.length + templateResults.length];
				System.arraycopy(templateResults, 0, total, 0, templateResults.length);
				System.arraycopy(results, 0, total, templateResults.length, results.length);
				results = total;
			}
			// Order here and not in result collector to make sure that the
			// order
			// applies to all proposals and not just those of the compilation
			// unit.
			return order(results);
		} catch (Throwable x) {
			handle(viewer, x);
		}

		return null;
	}

	protected int computeInsertionPosition() throws JavaModelException {
		int insertion = -1;

		return insertion;
	}

	/**
	 * Returns the compliation unit associated with this Java stack frame.
	 * Returns <code>null</code> for a binary stack frame.
	 */
	protected ICompilationUnit getCompilationUnit() {
		if (receivingType instanceof IType) {
			return ((IType) receivingType).getCompilationUnit();
		}
		return null;
	}

	protected void handle(ITextViewer viewer, Throwable x) {
		// Shell shell= viewer.getTextWidget().getShell();
		// ErrorDialog.openError(shell,
		// DisplayMessages.DisplayCompletionProcessor_Problems_during_completion_1,
		// //$NON-NLS-1$
		// DisplayMessages.DisplayCompletionProcessor_An_exception_occurred_during_code_completion_2,
		// //$NON-NLS-1$
		// x.getStatus());
		// JDIDebugUIPlugin.log(x);
		x.printStackTrace();
	}

	/**
	 * Returns the Java project associated with the given stack frame, or
	 * <code>null</code> if none.
	 */
	protected IJavaProject getJavaProject() {

		return javaProject;
	}

	/**
	 * Order the given proposals.
	 */
	protected IJavaCompletionProposal[] order(IJavaCompletionProposal[] proposals) {
		Arrays.sort(proposals, fComparator);
		return proposals;
	}

	/**
	 * Configures the display result collection for the current code assist
	 * session
	 */
	protected void configureResultCollector(IJavaProject project, ITextSelection selection) {
		fCollector = new CompletionProposalCollector(project);
		if (selection.getLength() != 0) {
			fCollector.setReplacementLength(selection.getLength());
		}
	}

	/**
	 * Returns an array of simple type names that are part of the given type's
	 * qualified name. For example, if the given name is <code>x.y.A$B</code>,
	 * an array with <code>["A", "B"]</code> is returned.
	 * 
	 * @param typeName
	 *            fully qualified type name
	 * @return array of nested type names
	 */
	protected String[] getNestedTypeNames(String typeName) {
		int index = typeName.lastIndexOf('.');
		if (index >= 0) {
			typeName = typeName.substring(index + 1);
		}
		index = typeName.indexOf('$');
		List<String> list = new ArrayList<String>(1);
		while (index >= 0) {
			list.add(typeName.substring(0, index));
			typeName = typeName.substring(index + 1);
			index = typeName.indexOf('$');
		}
		list.add(typeName);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns a copy of the type name with '$' replaced by '.', or returns
	 * <code>null</code> if the given type name refers to an anonymous inner
	 * class.
	 * 
	 * @param typeName
	 *            a fully qualified type name
	 * @return a copy of the type name with '$' replaced by '.', or returns
	 *         <code>null</code> if the given type name refers to an anonymous
	 *         inner class.
	 */
	protected String getTranslatedTypeName(String typeName) {
		int index = typeName.lastIndexOf('$');
		if (index == -1) {
			return typeName;
		}
		if (index + 1 > typeName.length()) {
			// invalid name
			return typeName;
		}
		String last = typeName.substring(index + 1);
		try {
			Integer.parseInt(last);
			return null;
		} catch (NumberFormatException e) {
			return typeName.replace('$', '.');
		}
	}

	// /**
	// * Returns a file name for the receiving type associated with the given
	// * stack frame.
	// *
	// * @return file name for the receiving type associated with the given
	// * stack frame
	// * @exception DebugException if:<ul>
	// * <li>A failure occurs while accessing attributes of
	// * the stack frame</li>
	// * </ul>
	// */
	// protected String getReceivingSourcePath() {
	// String typeName= frame.getReceivingTypeName();
	// String sourceName= frame.getSourceName();
	// if (sourceName == null || !typeName.equals(frame.getDeclaringTypeName()))
	// {
	// // if there is no debug attribute or the declaring type is not the
	// // same as the receiving type, we must guess at the receiver's source
	// // file
	// int dollarIndex= typeName.indexOf('$');
	// if (dollarIndex >= 0) {
	// typeName= typeName.substring(0, dollarIndex);
	// }
	// typeName = typeName.replace('.', IPath.SEPARATOR);
	// typeName+= ".java"; //$NON-NLS-1$
	// } else {
	// int index = typeName.lastIndexOf('.');
	// if (index >= 0) {
	// typeName = typeName.substring(0, index + 1);
	// typeName = typeName.replace('.', IPath.SEPARATOR);
	// } else {
	// typeName = ""; //$NON-NLS-1$
	// }
	// typeName+=sourceName;
	// }
	// return typeName;
	// }
	//
	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order
	 *            <code>true</code> if proposals should be ordered.
	 */
	public void orderProposalsAlphabetically(boolean order) {
		fComparator.setOrderAlphabetically(order);
	}

	/**
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return fProposalAutoActivationSet;
	}

	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 * 
	 * @param activationSet
	 *            the activation set
	 */
	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		fProposalAutoActivationSet = activationSet;
	}

	protected CompletionProposalCollector getCollector() {
		return fCollector;
	}

	/**
	 * Clears reference to result proposal collector.
	 */
	protected void releaseCollector() {
		if (fCollector != null && fCollector.getErrorMessage().length() > 0 && fErrorMessage != null) {
			setErrorMessage(fCollector.getErrorMessage());
		}
		fCollector = null;
	}

	protected void setCollector(CompletionProposalCollector collector) {
		fCollector = collector;
	}

	// protected IType getType(IJavaProject project, String originalTypeName,
	// String typeName) {
	//
	// int dollarIndex= typeName.indexOf('$');
	// if (dollarIndex > 0) {
	// typeName= typeName.substring(0, dollarIndex);
	// }
	// IPath sourcePath = new Path(typeName);
	// IType type = null;
	// try {
	// IJavaElement result= project.findElement(sourcePath);
	// String[] typeNames = getNestedTypeNames(originalTypeName);
	// if (result != null) {
	// if (result instanceof IClassFile) {
	// type = ((IClassFile)result).getType();
	// } else if (result instanceof ICompilationUnit) {
	// type = ((ICompilationUnit)result).getType(typeNames[0]);
	// } else if (result instanceof IType) {
	// type = (IType)result;
	// }
	// }
	// for (int i = 1; i < typeNames.length; i++) {
	// String innerTypeName= typeNames[i];
	// try {
	// Integer.parseInt(innerTypeName);
	// return type;
	// } catch (NumberFormatException e) {
	// }
	// type = type.getType(innerTypeName);
	// }
	// } catch (JavaModelException e) {
	// handle(this., e);
	// }
	//
	// return type;
	// }
	/**
	 * Returns the templateEngine.
	 * 
	 * @return TemplateEngine
	 */
	public TemplateEngine getTemplateEngine() {
		return fTemplateEngine;
	}

	// /**
	// * Returns the type associated with the given type name and source name
	// * from the given launch, or <code>null</code> if none.
	// *
	// * @param launch the launch in which to resolve a type
	// * @param typeName fully qualified receiving type name (may include inner
	// types)
	// * @param sourceName fully qualified name source file name containing the
	// type
	// * @return associated Java model type or <code>null</code>
	// * @throws DebugException
	// */
	// protected IType resolveType(ILaunch launch, String typeName, String
	// sourceName) throws DebugException {
	// ISourceLocator sourceLocator = launch.getSourceLocator();
	// if (sourceLocator != null) {
	// if (sourceLocator instanceof ISourceLookupDirector) {
	// ISourceLookupDirector director = (ISourceLookupDirector) sourceLocator;
	// try {
	// Object[] objects = director.findSourceElements(sourceName);
	// if (objects.length > 0) {
	// Object element = objects[0];
	// if (element instanceof IAdaptable) {
	// IAdaptable adaptable = (IAdaptable) element;
	// IJavaElement javaElement = (IJavaElement)
	// adaptable.getAdapter(IJavaElement.class);
	// if (javaElement != null) {
	// IType type = null;
	// String[] typeNames = getNestedTypeNames(typeName);
	// if (javaElement instanceof IClassFile) {
	// type = ((IClassFile)javaElement).getType();
	// } else if (javaElement instanceof ICompilationUnit) {
	// type = ((ICompilationUnit)javaElement).getType(typeNames[0]);
	// } else if (javaElement instanceof IType) {
	// type = (IType)javaElement;
	// }
	// if (type != null) {
	// for (int i = 1; i < typeNames.length; i++) {
	// String innerTypeName= typeNames[i];
	// try {
	// Integer.parseInt(innerTypeName);
	// return type;
	// } catch (NumberFormatException e) {
	// }
	// type = type.getType(innerTypeName);
	// }
	// }
	// return type;
	// }
	// }
	// }
	// } catch (CoreException e) {
	// throw new DebugException(e.getStatus());
	// }
	// }
	// }
	// return null;
	// }
}

class DisplayViewerConfiguration extends JavaSourceViewerConfiguration {
	IJavaProject	javaProject;
	IType			receivingType;

	public DisplayViewerConfiguration(IJavaProject javaProject, IType receivingType) {
		super(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), PreferenceConstants.getPreferenceStore(), // new
				// ChainedPreferenceStore(new
				// IPreferenceStore[]
				// {PreferenceConstants.getPreferenceStore(),
				// EditorsUI.getPreferenceStore()})
				null, null);
		this.javaProject = javaProject;
		this.receivingType = receivingType;
	}

	public IContentAssistProcessor getContentAssistantProcessor() {
		return new DisplayCompletionProcessor(javaProject, receivingType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(getContentAssistantProcessor(), IDocument.DEFAULT_CONTENT_TYPE);

		JDIContentAssistPreference.configure(assistant, getColorManager());

		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDoubleClickStrategy(org.eclipse.jface.text.source.ISourceViewer,
	 *      java.lang.String)
	 */
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		ITextDoubleClickStrategy clickStrat = new ITextDoubleClickStrategy() {
			// Highlight the whole line when double clicked. See Bug#45481
			public void doubleClicked(ITextViewer viewer) {
				try {
					IDocument doc = viewer.getDocument();
					int caretOffset = viewer.getSelectedRange().x;
					int lineNum = doc.getLineOfOffset(caretOffset);
					int start = doc.getLineOffset(lineNum);
					int length = doc.getLineLength(lineNum);
					viewer.setSelectedRange(start, length);
				} catch (BadLocationException e) {
					WSPlugin.getDefault().log(new Status(Status.ERROR, WSPlugin.PLUGIN_ID, 0, e.getMessage(), e));

				}
			}
		};
		return clickStrat;
	}

}
