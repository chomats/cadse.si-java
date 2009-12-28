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
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaParameterListValidator;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.BidiSegmentEvent;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import fede.workspace.tool.view.WSPlugin;



/**
 * A source viewer configured to display Java source. This viewer obeys the font
 * and color preferences specified in the Java UI plugin.
 */
public class JDISourceViewer extends SourceViewer implements IPropertyChangeListener {
    
    /** The font. */
    private Font fFont;
    
    /** The background color. */
    private Color fBackgroundColor;
    
    /** The foreground color. */
    private Color fForegroundColor;

    /**
	 * Instantiates a new jDI source viewer.
	 * 
	 * @param parent
	 *            the parent
	 * @param ruler
	 *            the ruler
	 * @param styles
	 *            the styles
	 */
    public JDISourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        super(parent, ruler, styles);
        StyledText text= this.getTextWidget();
        text.addBidiSegmentListener(new  BidiSegmentListener() {
            public void lineGetSegments(BidiSegmentEvent event) {
                try {
                    event.segments= getBidiLineSegments(event.lineOffset);
                } catch (BadLocationException x) {
                    // ignore
                }
            }
        });
        updateViewerFont();
        updateViewerColors();
        getPreferenceStore().addPropertyChangeListener(this);
    }
    
    /**
	 * Updates the viewer's font to match the preferences.
	 */
    private void updateViewerFont() {
        IPreferenceStore store= getPreferenceStore();
        if (store != null) {
            FontData data= null;
            if (store.contains(JFaceResources.TEXT_FONT) && !store.isDefault(JFaceResources.TEXT_FONT)) {
                data= PreferenceConverter.getFontData(store, JFaceResources.TEXT_FONT);
            } else {
                data= PreferenceConverter.getDefaultFontData(store, JFaceResources.TEXT_FONT);
            }
            if (data != null) {
                Font font= new Font(getTextWidget().getDisplay(), data);
                applyFont(font);
                if (getFont() != null) {
                    getFont().dispose();
                }
                setFont(font);
                return;
            }
        }
        // if all the preferences failed
        applyFont(JFaceResources.getTextFont());
    }
    
    /**
	 * Sets the current font.
	 * 
	 * @param font
	 *            the new font
	 */
    private void setFont(Font font) {
        fFont= font;
    }
    
    /**
	 * Returns the current font.
	 * 
	 * @return the current font
	 */
    private Font getFont() {
        return fFont;
    }
    
    /**
	 * Sets the font for the given viewer sustaining selection and scroll
	 * position.
	 * 
	 * @param font
	 *            the font
	 */
    private void applyFont(Font font) {
        IDocument doc= getDocument();
        if (doc != null && doc.getLength() > 0) {
            Point selection= getSelectedRange();
            int topIndex= getTopIndex();
            
            StyledText styledText= getTextWidget();
            styledText.setRedraw(false);
            
            styledText.setFont(font);
            setSelectedRange(selection.x , selection.y);
            setTopIndex(topIndex);
            
            styledText.setRedraw(true);
        } else {
            getTextWidget().setFont(font);
        }   
    }
    
    /**
	 * Updates the given viewer's colors to match the preferences.
	 */
    public void updateViewerColors() {
        IPreferenceStore store= getPreferenceStore();
        if (store != null) {
            StyledText styledText= getTextWidget();
            Color color= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
                ? null
                : createColor(store, AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
            styledText.setForeground(color);
            if (getForegroundColor() != null) {
                getForegroundColor().dispose();
            }
            setForegroundColor(color);
            
            color= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
                ? null
                : createColor(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
            styledText.setBackground(color);
            if (getBackgroundColor() != null) {
                getBackgroundColor().dispose();
            }
            setBackgroundColor(color);
        }
    }
    
    /**
	 * Creates a color from the information stored in the given preference
	 * store. Returns <code>null</code> if there is no such information
	 * available.
	 * 
	 * @param store
	 *            the store
	 * @param key
	 *            the key
	 * @param display
	 *            the display
	 * 
	 * @return the color
	 */
    private Color createColor(IPreferenceStore store, String key, Display display) {
        RGB rgb= null;  
        if (store.contains(key)) {
            if (store.isDefault(key)) {
                rgb= PreferenceConverter.getDefaultColor(store, key);
            } else {
                rgb= PreferenceConverter.getColor(store, key);
            }
            if (rgb != null) {
                return new Color(display, rgb);
            }
        }
        return null;
    }
    
    /**
	 * Returns the current background color.
	 * 
	 * @return the current background color
	 */
    protected Color getBackgroundColor() {
        return fBackgroundColor;
    }

    /**
	 * Sets the current background color.
	 * 
	 * @param backgroundColor
	 *            the new background color
	 */
    protected void setBackgroundColor(Color backgroundColor) {
        fBackgroundColor = backgroundColor;
    }

    /**
	 * Returns the current foreground color.
	 * 
	 * @return the current foreground color
	 */
    protected Color getForegroundColor() {
        return fForegroundColor;
    }

    /**
	 * Sets the current foreground color.
	 * 
	 * @param foregroundColor
	 *            the new foreground color
	 */
    protected void setForegroundColor(Color foregroundColor) {
        fForegroundColor = foregroundColor;
    }
    
    /**
	 * Returns the preference store used to configure this source viewer. The
	 * JDISourceViewer uses the Java UI preferences.
	 * 
	 * @return the Java UI preferences
	 */
    protected IPreferenceStore getPreferenceStore() {
        return PreferenceConstants.getPreferenceStore();
    }
    
    /**
	 * Property change.
	 * 
	 * @param event
	 *            the event
	 * 
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
    public void propertyChange(PropertyChangeEvent event) {
        IContentAssistant assistant= getContentAssistant();
        if (assistant instanceof ContentAssistant) {
            JDIContentAssistPreference.changeConfiguration((ContentAssistant) assistant, event);
        }
        String property= event.getProperty();
        
        if (JFaceResources.TEXT_FONT.equals(property)) {
            updateViewerFont();
        }
        if (AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND.equals(property) || AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property) ||
            AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND.equals(property) ||  AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)) {
            updateViewerColors();
        }
        if (affectsTextPresentation(event)) {
            invalidateTextPresentation();
        }
    }
    
    /**
	 * Affects text presentation.
	 * 
	 * @param event
	 *            the event
	 * 
	 * @return true, if affects text presentation
	 * 
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 */
    protected boolean affectsTextPresentation(PropertyChangeEvent event) {
        JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
        return textTools.affectsBehavior(event);
    }

    /**
	 * Returns the current content assistant.
	 * 
	 * @return the current content assistant
	 */
    public IContentAssistant getContentAssistant() {
        return fContentAssistant;
    }
    
    /**
	 * Returns a segmentation of the line of the given document appropriate for
	 * bidi rendering. The default implementation returns only the string
	 * literals of a Java code line as segments.
	 * 
	 * @param lineOffset
	 *            the offset of the line
	 * 
	 * @return the line's bidi segmentation
	 * 
	 * @throws BadLocationException
	 *             in case lineOffset is not valid in document
	 */
    protected int[] getBidiLineSegments(int lineOffset) throws BadLocationException {
        IDocument document= getDocument();
        if (document == null) {
            return null;
        }
        IRegion line= document.getLineInformationOfOffset(lineOffset);
        ITypedRegion[] linePartitioning= document.computePartitioning(lineOffset, line.getLength());
        
        List segmentation= new ArrayList();
        for (int i= 0; i < linePartitioning.length; i++) {
            if (IJavaPartitions.JAVA_STRING.equals(linePartitioning[i].getType()))
                segmentation.add(linePartitioning[i]);
        }
        
        
        if (segmentation.size() == 0) 
            return null;
            
        int size= segmentation.size();
        int[] segments= new int[size * 2 + 1];
        
        int j= 0;
        for (int i= 0; i < size; i++) {
            ITypedRegion segment= (ITypedRegion) segmentation.get(i);
            
            if (i == 0)
                segments[j++]= 0;
                
            int offset= segment.getOffset() - lineOffset;
            if (offset > segments[j - 1])
                segments[j++]= offset;
                
            if (offset + segment.getLength() >= line.getLength())
                break;
                
            segments[j++]= offset + segment.getLength();
        }
        
        if (j < segments.length) {
            int[] result= new int[j];
            System.arraycopy(segments, 0, result, 0, j);
            segments= result;
        }
        
        return segments;
    }
    
    /**
	 * Disposes the system resources currently in use by this viewer.
	 */
    public void dispose() {
        if (getFont() != null) {
            getFont().dispose();
            setFont(null);
        }
        if (getBackgroundColor() != null) {
            getBackgroundColor().dispose();
            setBackgroundColor(null);
        }
        if (getForegroundColor() != null) {
            getForegroundColor().dispose();
            setForegroundColor(null);
        }
        getPreferenceStore().removePropertyChangeListener(this);
    }
}

class JDIContentAssistPreference {
    
    private final static String VISIBILITY= "org.eclipse.jdt.core.codeComplete.visibilityCheck"; //$NON-NLS-1$
    private final static String ENABLED= "enabled"; //$NON-NLS-1$
    private final static String DISABLED= "disabled"; //$NON-NLS-1$
    
    private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
        RGB rgb= PreferenceConverter.getColor(store, key);
        return manager.getColor(rgb);
    }
    
    private static Color getColor(IPreferenceStore store, String key) {
        JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
        return getColor(store, key, textTools.getColorManager());
    }
    
    private static DisplayCompletionProcessor getDisplayProcessor(ContentAssistant assistant) {
        IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
        if (p instanceof DisplayCompletionProcessor)
            return  (DisplayCompletionProcessor) p;
        return null;
    }
    
    private static JavaSnippetCompletionProcessor getJavaSnippetProcessor(ContentAssistant assistant) {
        IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
        if (p instanceof JavaSnippetCompletionProcessor)
            return  (JavaSnippetCompletionProcessor) p;
        return null;
    }
    
    private static void configureDisplayProcessor(ContentAssistant assistant, IPreferenceStore store) {
        DisplayCompletionProcessor dcp= getDisplayProcessor(assistant);
        if (dcp == null) {
            return;
        }
        String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
        if (triggers != null) {
            dcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
        }
            
        boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS);
        restrictProposalsToVisibility(enabled);
        
        enabled= store.getBoolean(PreferenceConstants.CODEASSIST_CASE_SENSITIVITY);
        restrictProposalsToMatchingCases(enabled);
        
        enabled= store.getBoolean(PreferenceConstants.CODEASSIST_ORDER_PROPOSALS);
        dcp.orderProposalsAlphabetically(enabled);
    }
    
    private static void configureJavaSnippetProcessor(ContentAssistant assistant, IPreferenceStore store) {
        JavaSnippetCompletionProcessor cp= getJavaSnippetProcessor(assistant);
        if (cp == null) {
            return;
        }
            
        String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
        if (triggers != null) {
            cp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
        }
            
        boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS);
        restrictProposalsToVisibility(enabled);
        
        enabled= store.getBoolean(PreferenceConstants.CODEASSIST_CASE_SENSITIVITY);
        restrictProposalsToMatchingCases(enabled);
        
        enabled= store.getBoolean(PreferenceConstants.CODEASSIST_ORDER_PROPOSALS);
        cp.orderProposalsAlphabetically(enabled);
    }
    
    /**
     * Configure the given content assistant from the preference store.
     */
    public static void configure(ContentAssistant assistant, IColorManager manager) {
        
        IPreferenceStore store= getPreferenceStore();
        
        boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION);
        assistant.enableAutoActivation(enabled);
        
        int delay= store.getInt(PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY);
        assistant.setAutoActivationDelay(delay);
        
        Color c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, manager);
        assistant.setProposalSelectorForeground(c);
        
        c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, manager);
        assistant.setProposalSelectorBackground(c);
        
        c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND, manager);
        assistant.setContextInformationPopupForeground(c);
        assistant.setContextSelectorForeground(c);
        
        c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND, manager);
        assistant.setContextInformationPopupBackground(c);
        assistant.setContextSelectorBackground(c);
        
        enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOINSERT);
        assistant.enableAutoInsert(enabled);

        configureDisplayProcessor(assistant, store);
        configureJavaSnippetProcessor(assistant, store);
    }
    
    
    private static void changeDisplayProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
        DisplayCompletionProcessor dcp= getDisplayProcessor(assistant);
        if (dcp == null) {
            return;
        }
        if (PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
            String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
            if (triggers != null) {
                dcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
            }
        } else if (PreferenceConstants.CODEASSIST_ORDER_PROPOSALS.equals(key)) {
            boolean enable= store.getBoolean(PreferenceConstants.CODEASSIST_ORDER_PROPOSALS);
            dcp.orderProposalsAlphabetically(enable);
        }
    }
    
    private static void changeJavaSnippetProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
        JavaSnippetCompletionProcessor cp= getJavaSnippetProcessor(assistant);
        if (cp == null) {
            return;
        }
        if (PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
            String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
            if (triggers != null) {
                cp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
            }
        } else if (PreferenceConstants.CODEASSIST_ORDER_PROPOSALS.equals(key)) {
            boolean enable= store.getBoolean(PreferenceConstants.CODEASSIST_ORDER_PROPOSALS);
            cp.orderProposalsAlphabetically(enable);
        }   
    }
    
    
    /**
     * Changes the configuration of the given content assistant according to the given property
     * change event.
     */
    public static void changeConfiguration(ContentAssistant assistant, PropertyChangeEvent event) {
        
        IPreferenceStore store= getPreferenceStore();
        String p= event.getProperty();
        
        if (PreferenceConstants.CODEASSIST_AUTOACTIVATION.equals(p)) {
            boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOACTIVATION);
            assistant.enableAutoActivation(enabled);
        } else if (PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY.equals(p)) {
            int delay= store.getInt(PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY);
            assistant.setAutoActivationDelay(delay);
        } else if (PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND.equals(p)) {
            Color c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND);
            assistant.setProposalSelectorForeground(c);
        } else if (PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND.equals(p)) {
            Color c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND);
            assistant.setProposalSelectorBackground(c);
        } else if (PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND.equals(p)) {
            Color c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND);
            assistant.setContextInformationPopupForeground(c);
            assistant.setContextSelectorForeground(c);
        } else if (PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND.equals(p)) {
            Color c= getColor(store, PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND);
            assistant.setContextInformationPopupBackground(c);
            assistant.setContextSelectorBackground(c);
        } else if (PreferenceConstants.CODEASSIST_AUTOINSERT.equals(p)) {
            boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_AUTOINSERT);
            assistant.enableAutoInsert(enabled);
        }
        
        changeDisplayProcessor(assistant, store, p);
        changeJavaSnippetProcessor(assistant, store, p);
    }
    
    /**
     * Tells this processor to restrict its proposal to those element
     * visible in the actual invocation context.
     * 
     * @param restrict <code>true</code> if proposals should be restricted
     */
    private static void restrictProposalsToVisibility(boolean restrict) {
        Hashtable options= JavaCore.getOptions();
        Object value= options.get(VISIBILITY);
        if (value instanceof String) {
            String newValue= restrict ? ENABLED : DISABLED;
            if (!newValue.equals(value)) {
                options.put(VISIBILITY, newValue);
                JavaCore.setOptions(options);
            }
        }
    }
    
    /**
     * Tells this processor to restrict is proposals to those
     * starting with matching cases.
     * 
     * @param restrict <code>true</code> if proposals should be restricted
     */
    private static void restrictProposalsToMatchingCases(boolean restrict) {
        // XXX not yet supported
    }
    
    private static IPreferenceStore getPreferenceStore() {
        return PreferenceConstants.getPreferenceStore();
    }
}

class JavaSnippetCompletionProcessor implements IContentAssistProcessor {
    
    private CompletionProposalCollector fCollector;
   
    private AbstractDecoratedTextEditor fEditor;
    private IContextInformationValidator fValidator;
    private TemplateEngine fTemplateEngine;
    private CompletionProposalComparator fComparator;
    private String fErrorMessage;
    
    private char[] fProposalAutoActivationSet;
            
    public JavaSnippetCompletionProcessor(AbstractDecoratedTextEditor editor) {
        fEditor= editor;
        TemplateContextType contextType= JavaPlugin.getDefault().getTemplateContextRegistry().getContextType("java"); //$NON-NLS-1$
        if (contextType != null) {
            fTemplateEngine= new TemplateEngine(contextType);
        }
        
        fComparator= new CompletionProposalComparator();
    }
    
    /**
     * @see IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage() {
        return fErrorMessage;
    }
    
    protected void setErrorMessage(String message) {
        if (message != null && message.length() == 0) {
            message = null;
        }
        fErrorMessage = message;
    }

    /**
     * @see IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() {
        if (fValidator == null) {
            fValidator= new JavaParameterListValidator();
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
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int position) {
        try {
            setErrorMessage(null);
            try {
                fCollector = new CompletionProposalCollector(getJavaProject(fEditor));
                codeComplete(fEditor,fCollector);
            } catch (JavaModelException x) {
                Shell shell= viewer.getTextWidget().getShell();
                ErrorDialog.openError(shell, "CompletionProcessor.errorTitle", "CompletionProcessor.errorMessage", x.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
                WSPlugin.getDefault().log(new Status(Status.ERROR,WSPlugin.PLUGIN_ID,0,x.getMessage(),x));
            }
            
            IJavaCompletionProposal[] results= fCollector.getJavaCompletionProposals();
            
            if (fTemplateEngine != null) {
                fTemplateEngine.reset();
                fTemplateEngine.complete(viewer, position, null);           
            
                TemplateProposal[] templateResults= fTemplateEngine.getResults();
    
                // concatenate arrays
                IJavaCompletionProposal[] total= new IJavaCompletionProposal[results.length + templateResults.length];
                System.arraycopy(templateResults, 0, total, 0, templateResults.length);
                System.arraycopy(results, 0, total, templateResults.length, results.length);
                results= total;
            }
            return order(results);
        } finally {
            setErrorMessage(fCollector.getErrorMessage());
            fCollector = null;
        }
    }
    
    private void codeComplete(AbstractDecoratedTextEditor editor, CompletionProposalCollector collector) throws JavaModelException {
        // TODO Auto-generated method stub
        
    }

    private ICompilationUnit getJavaProject(AbstractDecoratedTextEditor editor) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Order the given proposals.
     */
    private ICompletionProposal[] order(IJavaCompletionProposal[] proposals) {
        Arrays.sort(proposals, fComparator);
        return proposals;   
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
     * @param activationSet the activation set
     */
    public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
        fProposalAutoActivationSet= activationSet;
    }
    
    /**
     * Tells this processor to order the proposals alphabetically.
     * 
     * @param order <code>true</code> if proposals should be ordered.
     */
    public void orderProposalsAlphabetically(boolean order) {
        fComparator.setOrderAlphabetically(order);
    }
}

