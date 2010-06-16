package fr.imag.adele.fede.workspace.si.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import fede.workspace.dependencies.eclipse.java.ItemDependenciesClasspathEntry;
import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WSModelState;

/**
 * The Class JobInitMapping.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
final class JobReloadClassPathMapping extends Job {
	/**
	 * 
	 */
	private final Java javaInstance;
	

	/** The model. */
	LogicalWorkspace	model;

	/**
	 * Instantiates a new job init mapping.
	 * @param java TODO
	 * 
	 * @param model
	 *            the model
	 */
	public JobReloadClassPathMapping(Java java) {
		super("init java class path");
		javaInstance = java;
	}

	public void testworkspaceToSleep(final IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return ;
		CadseDomain cu = javaInstance.getWorkspaceCU();
		model = cu == null ? null : cu.getLogicalWorkspace();
		if (model == null || model.getState() != WSModelState.RUN) {
			new Thread() {
				@Override
				public void run() {
					while (true) {
						if (monitor.isCanceled())
							return ;
						try {
							sleep(5000);
						} catch (InterruptedException e) {

						}
						CadseDomain cu = javaInstance.getWorkspaceCU();
						if (cu == null) continue;
						model = cu.getLogicalWorkspace();
						if (model != null && model.getState() == WSModelState.RUN)
							break;
					}
					wakeUp();
				}
			}.start();
			sleep();
		}
		if (monitor.isCanceled())
			return ;
		
		// model != null && state == run
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			return Status.OK_STATUS;
		testworkspaceToSleep(monitor);
		if (monitor.isCanceled())
			return Status.OK_STATUS;
		if (model == null)
			return Status.OK_STATUS;
		Map<IJavaProject, IClasspathContainer> toset = new HashMap<IJavaProject, IClasspathContainer>();
		for (Item item : model.getItems()) {
			try {
				if (monitor.isCanceled())
					return Status.OK_STATUS;
				IJavaProject jp = item.getMainMappingContent(IJavaProject.class); // force
				// to
				// load
				// content
				if (jp == null) {
					continue;
				}

				IClasspathContainer cc = JavaCore.getClasspathContainer(
						ItemDependenciesClasspathEntry.CLASSPATH_ENTRY_PATH, jp);
				if (cc == null) {
					toset.put(jp, new ItemDependenciesClasspathEntry(jp, jp.getElementName(), item, null, null));
					continue;
				}
				if (cc instanceof ItemDependenciesClasspathEntry) {
					if (((ItemDependenciesClasspathEntry) cc).isResolved()) {
						continue;
					}
					toset.put(jp, new ItemDependenciesClasspathEntry(jp, jp.getElementName(), item, null, cc));
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (toset.size() > 0) {
			try {
				// update classpath for all affected workspace plug-ins in
				// one operation
				IJavaProject[] jProjects = toset.keySet().toArray(new IJavaProject[toset.size()]);
				IClasspathContainer[] containers = toset.values().toArray(new IClasspathContainer[toset.size()]);
				JavaCore.setClasspathContainer(ItemDependenciesClasspathEntry.CLASSPATH_ENTRY_PATH, jProjects,
						containers, null);
			} catch (JavaModelException e) {
			}
		}
		return Status.OK_STATUS;
	}
}