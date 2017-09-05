package ssr.intern.project;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class MyDialog extends Dialog {
	private Composite container;
	private static int val = 15;
	private static int loan = 10;
	private Spinner spin;
	private Text text;

	public MyDialog(Shell parentShell) {
		super(parentShell);
	}
	
	public int getEnteredThreshold() {
		return spin.getSelection();
	}

	public void setDefaultThreshold(int value) {
		val = value;
	}
	
	public int getDefaultThreshold() {
		return val;
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		container = (Composite) super.createDialogArea(parent);
		
		// Button button = new Button(container, SWT.PUSH);
		RowLayout rowlayout = new RowLayout();
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		rowlayout.fill = true;
		Group group1 = new Group(container,SWT.NONE);
		group1.setText("Treshold");
		group1.setLayout(rowlayout);
		Group group2 = new Group(container,SWT.NONE);
		group2.setText("Payment");
		group2.setLayout(rowlayout);
		
		Label label = new Label(group1, SWT.NONE);
		Label label2 = new Label(group2, SWT.NONE);
		//label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Please set the threshold: ");
		label2.setText("Please set hourly wage: ");
		text = new Text(group2, SWT.NONE);
		text.setText(Integer.toString(ThresholdBundle.getPayment()));
		spin = new Spinner(group1, SWT.NONE);
		//spin.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		spin.setDigits(1);
		spin.setMinimum(1);
		spin.setMaximum(99);
		spin.setSelection(val);	
		return container;
		
	}
	
	@Override
	protected void okPressed()
	{
	  val = spin.getSelection();
	  loan = Integer.parseInt(text.getText());
	  ThresholdBundle tb = new ThresholdBundle();
	  tb.setPayment(loan);
	  tb.setThreshold(val);

	  super.okPressed();
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Selection dialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

}