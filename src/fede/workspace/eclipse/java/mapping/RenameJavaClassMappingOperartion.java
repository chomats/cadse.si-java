package fede.workspace.eclipse.java.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameCompilationUnitChange;
import org.eclipse.ltk.core.refactoring.Change;

import fede.workspace.eclipse.java.manager.JavaFileContentManager;
import fede.workspace.tool.eclipse.EclipseTool;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.MappingOperation;
import fr.imag.adele.cadse.core.var.ContextVariable;

public class RenameJavaClassMappingOperartion extends MappingOperation {
	String	cn;
	String	oldcn;

	public RenameJavaClassMappingOperartion(ItemDelta parent) throws CadseException {
		super(parent);
	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		JavaFileContentManager cm = (JavaFileContentManager) goodItem.getContentItem();
		ContextVariable oldcontext = new ContextVariable();
		oldcontext.putValue(goodItem, CadseGCST.ITEM_at_NAME, oldcn);

		ICompilationUnit cu = cm.getCompilationUnit(oldcontext);
		IResource f = cu.getResource();

		RenameCompilationUnitChange mcuc = new RenameCompilationUnitChange(cu, cn + ".java");
		try {
			IProgressMonitor defaultMonitor = EclipseTool.getDefaultMonitor();
			Change perform = mcuc.perform(defaultMonitor);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected String getLabel() {
		return "rename to " + cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public void setOldcn(String oldcn) {
		this.oldcn = oldcn;
	}
}
