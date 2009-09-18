/**
 * 
 */
package fede.workspace.eclipse.java.manager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.changes.MoveCompilationUnitChange;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.MappingOperation;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.fede.workspace.si.view.View;

final class MovePackateMappingOperation extends MappingOperation {
	/**
	 * 
	 */
	private final JavaFileContentManager	javaFileContentManager;
	private final ContextVariable	newCxt;
	private final ContextVariable	oldCxt;
	private final String			newpackageName;
	private ICompilationUnit		_oldCompilationUnit;
	private IProgressMonitor		monitor;
	private IPackageFragment		_newPackageFragment;

	MovePackateMappingOperation(JavaFileContentManager javaFileContentManager, ItemDelta parent, ContextVariable newCxt, ContextVariable oldCxt,
			String newpackageName) throws CadseException, JavaModelException {
		super(parent);
		this.javaFileContentManager = javaFileContentManager;
		this.newCxt = newCxt;
		this.oldCxt = oldCxt;
		this.newpackageName = newpackageName;
		this.monitor = View.getDefaultMonitor();
		this._newPackageFragment = this.javaFileContentManager.createPackageFragment(newCxt, newpackageName, true, monitor);
		this._oldCompilationUnit = this.javaFileContentManager.getCompilationUnit(oldCxt);

	}

	@Override
	public void commit(LogicalWorkspace wl, Item goodItem) {
		try {
			final MoveCompilationUnitChange mcuc = new MoveCompilationUnitChange(_oldCompilationUnit,
					_newPackageFragment);
			mcuc.perform(monitor);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected String getLabel() {
		return "move class in package" + newpackageName + " : " + _newPackageFragment;
	}
}