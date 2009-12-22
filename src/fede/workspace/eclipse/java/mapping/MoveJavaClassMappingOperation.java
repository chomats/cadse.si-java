package fede.workspace.eclipse.java.mapping;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;

public class MoveJavaClassMappingOperation extends MappingOperation {
	String	pn;
	String	oldpn;
	String	oldcn;

	public MoveJavaClassMappingOperation(ItemDelta parent) throws CadseException {
		super(parent);
	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		// JavaFileContentManager cm = (JavaFileContentManager)
		// goodItem.getContentItem();
		//
		// IPackageFragment newPackage;
		// try {
		// newPackage = cm.createPackageFragment(ContextVariable.DEFAULT, pn,
		// true, EclipseTool.getDefaultMonitor());
		//
		// ContextVariable oldcontext = new ContextVariable();
		// Item packageItem = GeneratedInterfaceManager.getPackage(goodItem);
		// oldcontext.putValue(packageItem, CadseGCST.ITEM_at_NAME,
		// oldpn);
		// if (oldcn != null) {
		// oldcontext.putValue(goodItem, CadseGCST.ITEM_at_NAME, oldcn);
		// }
		//
		// ICompilationUnit cu = cm.getCompilationUnit(oldcontext);
		// IResource f = cu.getResource();
		// MoveCompilationUnitChange mcuc = new MoveCompilationUnitChange(cu,
		// newPackage);
		// try {
		// mcuc.perform(EclipseTool.getDefaultMonitor());
		// } catch (CoreException e1) {
		// e1.printStackTrace();
		// }
		// Activator.unSetItemPersistenceID(f, true);
		// cu = cm.getCompilationUnit(ContextVariable.DEFAULT);
		// f = cu.getResource();
		// EclipseTool.setItemPersistenceID(f, goodItem);
		// // cm.
		// } catch (JavaModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public void setPn(String pn) {
		this.pn = pn;
	}

	public void setOldpn(String oldpn) {
		this.oldpn = oldpn;
	}

	public void setOldcn(String oldcn) {
		this.oldcn = oldcn;
	}

	@Override
	protected String getLabel() {
		return "Move Java file " + oldpn + " to " + pn;
	}

}
