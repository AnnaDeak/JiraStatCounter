package ssr.intern.project;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class OpenHandler {

	@Inject
	private EPartService partService;

	@Execute
	public void execute(Shell shell) {
		FileDialog dialog = new FileDialog(shell);
		boolean res = false;
		dialog.setText("Open the needed excel file");
		res = dialog.open() != null;
		if (!res) {
			System.out.println("cancer pressed at fileopen");
		} else {
			String path = "";
			String filename = dialog.getFileName();
			System.out.println(filename);
			if (filename.endsWith(".xls")) {
				MessageDialog.openWarning(shell, "Warning",
						"The program does not support 2003 or older XML file format (*.xls) . \n Please convert your file to 2007 or newer XML file format (*.xlsx)!");
			} else if (filename.endsWith(".xlsx")) {
				path += dialog.getFilterPath() + "\\" + filename;
				MPart showPart = partService.showPart("ssr.intern.project.part.alma", PartState.CREATE);
				showPart.setLabel("Alma");
				((SamplePart) showPart.getObject()).populate(path, filename);
			} else {
				MessageDialog.openError(shell, "Error!",
						"The selected file is not an XML-based file for Excel 2007 or newer release.");
			}
		}

	}

}
