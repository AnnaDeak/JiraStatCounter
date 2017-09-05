package ssr.intern.project;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class MyStatsDialog extends Dialog {
	private int tOEH = 0;  //total original estimate hours
	private int tOEM = 0;  //total original estimate minutes
	private int tTSH = 0;  //total time spent hours
	private int tTSM = 0;  //total time spent minutes
	private double totalLoss = 0;
	private Composite container;
	private String content[][];
	private final Font myFont = SWTResourceManager.getFont("@MS PGothic", 10, SWT.BOLD);
	Device device = Display.getCurrent();
	private final Color myDarkRed = new Color(device, 120, 0, 0);
	private final Color myRed = new Color(device, 180, 20, 20);
	private final Color myLightRed = new Color(device, 255, 40, 40);
	private final Color myOrange = new Color(device, 255, 100, 35);
	private final Color myDarkYellow = new Color(device, 235, 205, 35);
	private final Color myLightYellow = new Color(device, 255, 255, 50);
	private final Color myDarkGreen = new Color(device, 100, 175, 100);
	private final Color myLightGreen = new Color(device, 100, 255, 100);
	private final Color myBlueForEpic = new Color(device, 0, 51, 153);
	private final Color myTreeBackgroundBlue = new Color(device, 204, 255, 204);
	
	private int money = ThresholdBundle.getPayment();  //10 by default

	public MyStatsDialog(Shell parentShell , TreeItem items[]) {
		super(parentShell);
		content = new String[items.length][5];
		int pos = 0;
		for (TreeItem i:items) {
			content[pos] = new String[] {i.getText(0),i.getText(4),i.getText(6),i.getText(8),""};
			content[pos] = calculateLoss(content[pos]);
			pos++;
		}

		
	}
	

		
	
	@Override
	protected Control createDialogArea(Composite parent) {
		container = (Composite) super.createDialogArea(parent);
		// Button button = new Button(container, SWT.PUSH);
		container.setBackground(myTreeBackgroundBlue);
		Tree tree = new Tree(container, SWT.TOP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setFont(myFont);
		tree.setHeaderBackground(myBlueForEpic);
		tree.setHeaderForeground(myTreeBackgroundBlue);
		tree.setForeground(myBlueForEpic);
		tree.setBackground(myTreeBackgroundBlue);
		TreeColumn col1 = new TreeColumn(tree, SWT.CENTER);
		col1.setText("Summary");
		col1.setWidth(420);
		TreeColumn col2 = new TreeColumn(tree, SWT.CENTER);
		col2.setText("Original Estimate");
		col2.setWidth(130);
		TreeColumn col3 = new TreeColumn(tree, SWT.CENTER);
		col3.setText("Time Spent");
		col3.setWidth(130);
		TreeColumn col4 = new TreeColumn(tree, SWT.CENTER);
		col4.setText("Factor");
		col4.setWidth(130);
		TreeColumn col5 = new TreeColumn(tree, SWT.CENTER);
		col5.setText("Loss:");
		col5.setWidth(130);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		String actualFactor = Integer.toString(ThresholdBundle.getThreshold());
		money = ThresholdBundle.getPayment();
		actualFactor = actualFactor.substring(0,actualFactor.length()-1) + "." + actualFactor.substring(actualFactor.length()-1 , actualFactor.length());
		actualFactor += ")";
		actualFactor = "(Treshold=" + actualFactor;
		
		TreeItem root = new TreeItem(tree,0);
		
		root.setText(new String[] {"TOTAL",totalHours(tOEH, tOEM),totalHours(tTSH, tTSM),actualFactor,Double.toString(totalLoss)});
		for (int i = 0 ; i< content.length ; i++) {
			TreeItem item = new TreeItem(root,0);
			item.setText(calculateLoss(content[i]));
			item.setBackground(3, getBackgroundColor(content[i][3]));
			item.setForeground(3,getForegroundColor(getBackgroundColor(content[i][3])));
		}
		
		//tree.setS
		return container;
		
	}
	
	public String totalHours(int hours , int minutes) {
		String res = Integer.toString(minutes);
		if (res.length() == 1) {
			res += "0";
		}
		res = ":"+res;
		res = Integer.toString(hours)+res;
		return res;
	}
	
	public String[] calculateLoss(String[] currentEpic) {
		String[] res = currentEpic;
		double loss = 0;
		double threshold = (double) ThresholdBundle.getThreshold() / 10;
		String[] estimate = currentEpic[1].split(":");
		String[] spent = currentEpic[2].split(":");
		double eHours = Integer.parseInt(estimate[0]);
		double sHours = Integer.parseInt(spent[0]);
		tOEH+= eHours;
		tTSH+= sHours;
		tOEM += Integer.parseInt(estimate[1]);
		tTSM += Integer.parseInt(spent[1]);
		while (tOEM >= 60) {
			tOEH++;
			tOEM-=60;
		}
		while (tTSM >= 60) {
			tTSH++;
			tTSM-=60;
		}
		double maxSpent = eHours * threshold;  // 100%
		double plus60 = maxSpent *1.6;		
		double plus80 = maxSpent *1.8;
		double plus100 = maxSpent *2.0;
		plus60 = Math.round(plus60);
		plus80 = Math.round(plus80);
		plus100 = Math.round(plus100);
		maxSpent = Math.round(maxSpent);
		if(sHours > plus100) {
			// surpassed with more then 100%
			loss += (sHours-plus100) * 0.75 * money;
			sHours = plus100;
		}
		if(sHours > plus80) {
			loss += (sHours-plus80) * 0.5 * money;
			sHours = plus80;
			// surpassed with somewhere between 80% - 100%
		}
		if(sHours > plus60) { 
			loss += (sHours-plus60) * 0.25 * money;
			sHours = plus60;
			// surpassed with somewhere between 60% - 80%
		}
		if (sHours <= plus60){
			loss += 0; // no loss
			//didn't surpass with more then 60%
		}
		
		res[4] = Double.toString(loss);
		totalLoss+=loss;
		
		return res;
	}
	
	private Color getForegroundColor(Color backgroundColor) {
		// If the background of the boss factor cells are dark , then returns white for
		// font color

		Color res = myBlueForEpic; // default black
		if (backgroundColor == myRed || backgroundColor == myOrange || backgroundColor == myLightRed
				|| backgroundColor == myDarkRed) {
			res = myTreeBackgroundBlue; // dark cell background , needs white font , otherwise it's not visible
		}
		return res;
	}
	
	private Color getBackgroundColor(String factor) {
		// calculates the background color of a cell depending of its value , used for
		// factor fields background color calculation
		// the factor argument contains the boss/EAC factor which determines the
		// background color

		Color res = myTreeBackgroundBlue; // default in case of exception
		try {
			if (Double.parseDouble(factor) >= (double) ThresholdBundle.getThreshold() * 0.133) {
				res = myDarkRed;
			} else if (Double.parseDouble(factor) >= (double) ThresholdBundle.getThreshold() * 0.116) {
				res = myRed;
			} else if (Double.parseDouble(factor) >= (double) ThresholdBundle.getThreshold() * 0.1) {
				res = myLightRed;
			} else if (Double.parseDouble(factor) >= (double) ThresholdBundle.getThreshold() * 0.09) {
				res = myOrange;
			} else if (Double.parseDouble(factor) >= (double) ThresholdBundle.getThreshold() * 0.08) {
				res = myDarkYellow;
			} else if (Double.parseDouble(factor) >= (double) ThresholdBundle.getThreshold() * 0.066) {
				res = myLightYellow;
			} else if (Double.parseDouble(factor) >= (double) ThresholdBundle.getThreshold() * 0.05) {
				res = myDarkGreen;
			} else {
				res = myLightGreen;
			}
		} catch (Exception e) {
			// My man
		}
		return res;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Selection dialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(968, 500);
	}

}