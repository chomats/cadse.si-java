package fede.workspace.eclipse.java.mapping;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;

public class OpenFileMappingOperation extends MappingOperation {

	public OpenFileMappingOperation(ItemDelta parent) throws CadseException {
		super(parent);
	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		openJavaFile(goodItem);
	}

	public static void openJavaFile(Item generatedClass) {
		IFile jf = generatedClass.getContentItem().getMainMappingContent(IFile.class);
		if (jf != null) {
			try {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
				IDE.openEditor(activePage, jf, true);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String getLabel() {
		return "open file";
	}

}
