package ssr.intern.project;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;




public class SamplePart {

	public TabFolder tabFolder;
	public Composite parent;

	@Inject
	private MDirtyable dirty;
	

	@PostConstruct
	public void createComposite(Composite parent) {
		this.parent = parent;
		tabFolder = new TabFolder(parent, SWT.NONE);
	}
	
	public void populate(final String PATH,final String FILENAME) {				
		TabItem tbtmTab = new TabItem(tabFolder, SWT.NONE);
		tbtmTab.setText(FILENAME);
		Composite c = new Composite(tabFolder, SWT.NONE);
		c.setLayout(new GridLayout(1,true));
		tbtmTab.setControl(c);
		@SuppressWarnings("unused")
		Populator first = new Populator(c,tbtmTab,PATH);		
	}
	
	@Focus
	public void setFocus() {
		tabFolder.setFocus();
	}

	@Persist
	public void save() {
		dirty.setDirty(false);
	}
	
	public TabFolder getTabFolder() {
		return tabFolder;
	}
}