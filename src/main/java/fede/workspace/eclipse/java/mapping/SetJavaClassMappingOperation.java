package fede.workspace.eclipse.java.mapping;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;

public class SetJavaClassMappingOperation extends MappingOperation {

	public SetJavaClassMappingOperation(ItemDelta parent) throws CadseException {
		super(parent);
	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		// try {
		// IType t = GeneratedInterfaceManager.getJdtType(goodItem);
		// IProgressMonitor monitor = EclipseTool.getDefaultMonitor();
		//
		// ICompilationUnit cu;
		// if (t != null) {
		// cu = t.getCompilationUnit();
		// IFile f = (IFile) cu.getResource();
		// if (!f.exists()) {
		// // error must be created by the content manager
		// cu =
		// t.getPackageFragment().createCompilationUnit(cu.getElementName(),
		// JavaElementManager.getGeneratedTextAttribute(goodItem), false,
		// monitor);
		// } else {
		// IDE.saveAllEditors(new IResource[] { f }, true);
		// try {
		// JMergeUtil.merge(monitor, f,
		// JavaElementManager.getGeneratedTextAttribute(goodItem));
		// } catch (JETException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// JavaProjectManager.createInheritedMethods(t, true, false, true,
		// monitor);
		// } else {
		// JavaFileContentManager cm = (JavaFileContentManager)
		// goodItem.getContentItem();
		// String pn =
		// GeneratedInterfaceManager.getPackage(goodItem).getShortName();
		// String cn = goodItem.getName();
		// IPackageFragment pnJdt =
		// cm.createPackageFragment(ContextVariableImpl.DEFAULT, pn, true, monitor);
		// pnJdt.createCompilationUnit(cn + ".java",
		// JavaElementManager.getGeneratedTextAttribute(goodItem), true,
		// monitor);
		// }
		// } catch (JavaModelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (CoreException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	@Override
	protected String getLabel() {
		return "set source";
	}

}
