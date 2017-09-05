package ssr.intern.project;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

public class TresholdHandler {
	@Execute
	public void execute(Shell shell) {	
		MyDialog dialog = new MyDialog(shell);
		dialog.setDefaultThreshold(ThresholdBundle.getThreshold());
		dialog.open();
		
	}
}
