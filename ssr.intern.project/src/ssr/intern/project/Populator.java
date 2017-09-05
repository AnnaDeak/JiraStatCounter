package ssr.intern.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

public class Populator implements ThresholdUpdateListener {

	private final Tree tree;

	Device device = Display.getCurrent();
	// Font and Color resources
	private final Color myDarkRed = new Color(device, 120, 0, 0);
	private final Color myRed = new Color(device, 180, 20, 20);
	private final Color myLightRed = new Color(device, 255, 40, 40);
	private final Color myOrange = new Color(device, 255, 100, 35);
	private final Color myDarkYellow = new Color(device, 235, 205, 35);
	private final Color myLightYellow = new Color(device, 255, 255, 50);
	private final Color myDarkGreen = new Color(device, 100, 175, 100);
	private final Color myLightGreen = new Color(device, 100, 255, 100);
	private final Color myBlueForEpic = new Color(device, 0, 51, 153);
	private final Color myBrownForStory = new Color(device, 153, 51, 0);
	private final Color myGreyForTask = new Color(device, 65, 65, 65);
	private final Color myWhite = new Color(device, 255, 255, 255);
	private final Color myBlack = new Color(device, 0, 0, 0);

	private final Color myTreeBackgroundBlue = new Color(device, 204, 255, 204);

	private final Font myBoldFont = SWTResourceManager.getFont("@MS PGothic", 10, SWT.BOLD);
	private final Font myNormalFont = myBoldFont;// SWTResourceManager.getFont("@MS PGothic", 10, SWT.NORMAL);
	private Workbook workbook;

	public Populator(Composite parent, TabItem thisTab, final String PATH) {
		// this parent composite is the one that is linked to the TabItem (which isn't
		// composite)
		// PATH contains the absolute path for the excel file

		tree = new Tree(parent, SWT.TOP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		treeInit();
		try {
			populate(parent, thisTab, PATH);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				workbook.close();
				MessageDialog.openWarning(parent.getShell(), "Warning",
						"The program was not built for that kind of excel table. \n Please select a stat couner exported excel file!");
				parent.dispose();
				thisTab.dispose();
				return;
			} catch (IOException e1) {
				System.out.println("workbook already closed");
			}
		}
		addNeededListeners(parent, thisTab);
		parent.layout();

	}

	@SuppressWarnings({ "deprecation" })
	private void populate(Composite parent, TabItem thisTab, final String PATH)
			throws FileNotFoundException, IOException, Exception {
		int indexes[] = { 4, 5, 7, 8, 9, 10, 11 }; // these are the indexes of the needed cells from the excel table
		// [0] 4 - summary
		// [1] 5 - fix versions
		// [2] 7 - status
		// [3] 8 - team
		// [4] 9 - orig. estimate
		// [5] 10 - rem estimate
		// [6] 11 - time spent

		TreeItem lastEpic = null;
		TreeItem lastStory = null;

		FileInputStream excelFile = new FileInputStream(new File(PATH));
		workbook = new XSSFWorkbook(excelFile);
		Sheet sheet;
		int num = 1; // default case
		try {

			int sheetNum = workbook.getNumberOfSheets();
			for (int i = 0; i < sheetNum; i++) {
				if (workbook.getSheetAt(i).getSheetName().equals("RCS - Reporting Cluj")) {
					num = i;
					System.out.println(i);
					break;
				}
			}

			sheet = workbook.getSheetAt(num);
		} catch (Exception e) {
			throw e;
		}
		int rowStart = 1;
		int rowEnd = sheet.getLastRowNum();

		for (int rowNum = rowStart; rowNum < rowEnd; rowNum++) {
			Row r = sheet.getRow(rowNum);
			if (r == null) {
				// This whole row is empty
				// Handle it as needed
				continue;
			}
			Cell c = r.getCell(1, Row.RETURN_BLANK_AS_NULL);
			DataFormatter fmt = new DataFormatter();
			String contentProvider[] = getCellsFomIndex(indexes, r);
			switch (fmt.formatCellValue(c)) {
			case "Epic":
				TreeItem epicItem = new TreeItem(tree, 0);
				epicItem.setForeground(myBlueForEpic);
				contentProvider = remEstimatePlusTimeSpent(contentProvider);
				int[] neededIndexes = { 4, 8, 9 };
				populateCurrentTreeItem(contentProvider, epicItem, neededIndexes);
				lastEpic = epicItem;
				break;
			case "Story":
				TreeItem storyItem = new TreeItem(lastEpic, 0);
				storyItem.setForeground(myBrownForStory);
				storyItem.setText(getCellsFomIndex(indexes, r));
				try {
					contentProvider = remEstimatePlusTimeSpent(contentProvider);
					int[] neededIndexess = { 4, 8, 9 };
					populateCurrentTreeItem(contentProvider, storyItem, neededIndexess);
				} catch (Exception e) {
					System.out.println("Error: wrong excel input!");
				}
				lastStory = storyItem;
				break;
			case "Sub-task":
				TreeItem subtascItem = new TreeItem(lastStory, 0);
				subtascItem.setForeground(myGreyForTask);
				subtascItem.setText(getCellsFomIndex(indexes, r));
				break;
			}
		}
		workbook.close();
	}

	public void treeInit() {

		// Sets the font , header , and column separator for the tree
		tree.setFont(myNormalFont);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		tree.setFocus();
		createTreeColumns(tree);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@SuppressWarnings({ "deprecation" })
	private String[] getCellsFomIndex(int indexes[], Row r) {

		// Get the cells value from the excel table's r (parameter) row , each needed
		// element's index is found in the indexes array;

		String response[] = { "", "", "", "", "", "", "", "", "", "" };
		int stringIterator = 0;
		for (int i : indexes) {
			Cell c = r.getCell(i, Row.RETURN_BLANK_AS_NULL);
			DataFormatter fmt = new DataFormatter();
			if (c == null) {
				response[stringIterator] = " ";
				stringIterator++;
				continue;
			} else {
				response[stringIterator] = fmt.formatCellValue(c);
				stringIterator++;
			}
		}
		return response;
	}

	private String[] bossFactor(String[] stirng, int labelIndex, int stringIndex, int index) {
		// Calculates the boss factor , adds it to the string array that contains all
		// the displaying information
		// index contains the divident fields index
		// stringIndex contains the position where the result should be placed in the
		// answer string array
		// labelIndex contains the index of the divider field's

		String[] response = stirng; // passing the original string which contains already the needed fields for boss
									// factor calculation
		try {
			String[] originalEstimate = response[index].split(":");
			String[] forFactor = response[labelIndex].split(":");
			double divider = Double.parseDouble(originalEstimate[0]);
			if (!originalEstimate[1].equals("00")) {
				divider++;
			}

			double divident = Double.parseDouble(forFactor[0]);
			if (!forFactor[1].equals("00")) {
				divident++;
			}
			double res = divident / divider;
			res = BigDecimal.valueOf(res).setScale(2, RoundingMode.HALF_UP).doubleValue();

			response[stringIndex] = Double.toString(res);
		} catch (NumberFormatException e) {
			// empty excel cell
		}
		return response;
	}

	private String[] remEstimatePlusTimeSpent(String[] stirng) {
		// gets a string array as argument which already contains the remaining time and
		// the estimated time fields , calculates their sum

		String[] response = stirng;
		String[] remEstimate = response[5].split(":");
		String[] timeSpent = response[6].split(":");
		int hours = Integer.parseInt(remEstimate[0]) + Integer.parseInt(timeSpent[0]);
		int minutes = Integer.parseInt(remEstimate[1]) + Integer.parseInt(timeSpent[1]);
		hours += minutes / 60;
		minutes = minutes % 60;
		String mins = Integer.toString(minutes);
		if (mins.length() == 1) {
			mins = "0" + mins;
		}
		response[7] = Integer.toString(hours) + ":" + mins;
		return response;
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

	private String formatUserInput(String userInput) {
		// the userInput string is the ACN estimate field , which has to be checked to
		// match the expected format
		// expected formats: hh:mm ; hh , where h - means hour and m - min (both in
		// numeric format)
		// no minimum or maximum digit number for the minute or hour!
		String res = "";
		try {
			if (userInput.contains(":")) { // checks matches the standard requirement
				String[] tokenizer = userInput.split(":");
				if (tokenizer.length > 2) {
					return res; // wrong user input , return empty string
				}
				if (tokenizer.length == 1) { // no minutes digit , adds manually to get to the standard format
					try {
						int hours = Integer.parseInt(tokenizer[0]);
						res = Integer.toString(hours) + ":00";
					} catch (Exception e) {
						return res;
					}
				} else { // there are both hours and minute sections
					try {
						if (tokenizer[1].length() == 1) { // only one minute digit , so it adds one more for to get the
															// standard format
							tokenizer[1] = "0" + tokenizer[1];
						} else if (tokenizer[1].length() == 0) { // no minutes digit , adds manually to match standard
																	// format
							tokenizer[1] += "00";
						} else if (tokenizer[1].length() > 2) { // there was more then 2 minute digits , so cuts down
																// the excessive ones
							tokenizer[1] = tokenizer[1].substring(0, 2);
						}
						int hours = Integer.parseInt(tokenizer[0]); // converts from string to integer to check if input
																	// is numerical
						int mins = Integer.parseInt(tokenizer[1]); // if it isn't then it will throw exception.
						String minutes = Integer.toString(mins); // converts back to string
						if (minutes.length() == 1) { // check if no Zero digits were lost during conversion
							minutes = "0" + minutes;
						} else if (minutes.length() == 0) {
							minutes += "00";
						} else if (minutes.length() > 2) {
							minutes = minutes.substring(0, 2);
						}

						res = Integer.toString(hours) + ":" + minutes; // concatenates to get to standard format
					} catch (Exception e) {
						return res; // wrong user input , returns empty string
					}
				}

			} else { // the case when there were no minute digits given
				try {
					int hours = Integer.parseInt(userInput); // convert to integer to check if the input is numerical
					res = Integer.toString(hours) + ":00"; // manually adds minute digits , to match the standard format
				} catch (Exception e) {
					// wrong user input!
					return res;
				}

			}
		} catch (Exception e) {
			return res;
		}
		return res;
	}

	private void createTreeColumns(Tree parent) {
		// Creates the needed columns , sets their name , and size

		TreeColumn column1 = new TreeColumn(parent, SWT.CENTER);
		column1.setText("Summary"); // E
		column1.setWidth(392);
		TreeColumn column2 = new TreeColumn(parent, SWT.CENTER);
		column2.setWidth(100);
		column2.setText("Fix \n Version/s"); // F
		TreeColumn column3 = new TreeColumn(parent, SWT.CENTER);
		column3.setText("Status"); // H
		column3.setWidth(85);
		TreeColumn column4 = new TreeColumn(parent, SWT.CENTER);
		column4.setText("Team"); // I
		column4.setWidth(85);
		TreeColumn column5 = new TreeColumn(parent, SWT.RIGHT);
		column5.setText("Original Estimate"); // J
		column5.setWidth(120);
		TreeColumn column6 = new TreeColumn(parent, SWT.RIGHT);
		column6.setText("Remaining Estimate"); // K
		column6.setWidth(133);
		TreeColumn column7 = new TreeColumn(parent, SWT.RIGHT);
		column7.setText("Time Spent"); // L
		column7.setWidth(80);
		TreeColumn column8 = new TreeColumn(parent, SWT.RIGHT);
		column8.setText("Rem estimate + \nTime spent");
		column8.setWidth(187);
		TreeColumn column9 = new TreeColumn(parent, SWT.RIGHT);
		column9.setText("Current Boss Factor");
		column9.setWidth(145);
		TreeColumn column10 = new TreeColumn(parent, SWT.RIGHT);
		column10.setText("EAC Boss Factor");
		column10.setWidth(140);
		TreeColumn column11 = new TreeColumn(parent, SWT.RIGHT);
		column11.setText("Original Estimate ACN");
		column11.setWidth(150);
		TreeColumn column12 = new TreeColumn(parent, SWT.RIGHT);
		column12.setText("Current Boss Factor");
		column12.setWidth(145);
		TreeColumn column13 = new TreeColumn(parent, SWT.RIGHT);
		column13.setText("EAC Boss Factor");
		column13.setWidth(140);

		parent.setHeaderBackground(myBlueForEpic);
		parent.setHeaderForeground(myTreeBackgroundBlue);
		parent.setBackground(myTreeBackgroundBlue);
		parent.getParent().setBackground(myTreeBackgroundBlue);
	}

	private void populateCurrentTreeItem(String[] content, TreeItem currentItem, int[] indx) {
		final int TIME_SPENT_INDX = 6; // Index of Time Spent field from the tree
		final int REM_ESTIMATE_PLUS_TIMES_PENT_INDX = 7; // Index of Remaining Estimate + Time Spent field from the tree
		// The indx[] array should contain 5 elements , each of them is the index of a
		// needed field for the calculation
		// of the needed empty fields value
		// [0] -> Estimate field (original estimate or ACN estimate)
		// [1] -> Current Boss factor field Index
		// [2] -> EAC Boss factor field Index
		content = bossFactor(content, TIME_SPENT_INDX, indx[1], indx[0]);
		content = bossFactor(content, REM_ESTIMATE_PLUS_TIMES_PENT_INDX, indx[2], indx[0]);
		Color cellColorCBF = getBackgroundColor(content[indx[1]]); // for current Boss Factor Field
		Color cellColorEBF = getBackgroundColor(content[indx[2]]); // for EAC Boss Factor Field
		Color cellForeGroundCBF = getForegroundColor(cellColorCBF);
		Color cellForeGroundEBF = getForegroundColor(cellColorEBF);
		currentItem.setBackground(indx[1], cellColorCBF);
		currentItem.setBackground(indx[2], cellColorEBF);
		currentItem.setForeground(indx[1], cellForeGroundCBF);
		currentItem.setForeground(indx[2], cellForeGroundEBF);
		currentItem.setFont(indx[1], myBoldFont);
		currentItem.setFont(indx[2], myBoldFont);
		currentItem.setText(content);
	}

	private void recolorCellsAfterThresholdUpdate(TreeItem thisItem) {
		thisItem.setBackground(8, getBackgroundColor(thisItem.getText(8)));
		thisItem.setForeground(8, getForegroundColor(getBackgroundColor(thisItem.getText(8))));
		thisItem.setBackground(9, getBackgroundColor(thisItem.getText(9)));
		thisItem.setForeground(9, getForegroundColor(getBackgroundColor(thisItem.getText(9))));
		thisItem.setBackground(11, getBackgroundColor(thisItem.getText(11)));
		thisItem.setForeground(11, getForegroundColor(getBackgroundColor(thisItem.getText(11))));
		thisItem.setBackground(12, getBackgroundColor(thisItem.getText(12)));
		thisItem.setForeground(12, getForegroundColor(getBackgroundColor(thisItem.getText(12))));
	}

	private Color getForegroundColor(Color backgroundColor) {
		// If the background of the boss factor cells are dark , then returns white for
		// font color

		Color res = myBlack; // default black
		if (backgroundColor == myRed || backgroundColor == myOrange || backgroundColor == myLightRed
				|| backgroundColor == myDarkRed) {
			res = myWhite; // dark cell background , needs white font , otherwise it's not visible
		}
		return res;
	}

	public void addNeededListeners(Composite parent, TabItem tab) {
		final TreeEditor editor = new TreeEditor(tree);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.setColumn(10);

		tree.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.ESC) {
					tree.deselectAll();
				}
			}
		});

		tree.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == 'q' && event.stateMask == SWT.ALT) {

					tab.dispose();
					parent.layout();
					parent.dispose();
					return;
				}
			}
		});

		tree.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.stateMask == SWT.ALT && event.keyCode == 'e' && tree.getSelectionCount() == 1) {
					final TreeItem item = tree.getSelection()[0];
					final Text text = new Text(tree, SWT.NONE);
					text.setText(item.getText(10));
					text.selectAll();
					text.setFocus();
					text.addFocusListener(new FocusAdapter() {
						public void focusLost(FocusEvent event) {
							item.setText(10, text.getText());
							text.dispose();
						}
					});

					text.addKeyListener(new KeyAdapter() {
						public void keyPressed(KeyEvent event) {
							switch (event.keyCode) {
							case SWT.CR:
								String txt = text.getText();
								txt = formatUserInput(txt);
								item.setText(10, txt);
								String[] content = { "", "", "", "", "", "", "", "", "", "", "", "", "" };
								for (int i = 0; i < 10; i++) {
									content[i] = item.getText(i);
								}
								content[10] = txt;
								int[] neededIndexes = { 10, 11, 12 };
								populateCurrentTreeItem(content, item, neededIndexes);
								tree.deselectAll();
							case SWT.ESC:
								text.dispose();
								break;
							}
						}
					});
					editor.setEditor(text, item);
				}
			}
		});

		tree.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.stateMask == SWT.ALT && event.keyCode == 's' && tree.getSelectionCount() != 0) {
					try {
					final int n = tree.getSelectionCount();
					final TreeItem[] items = new TreeItem[n];
					int pos = 0;
					for (TreeItem i : tree.getSelection()) {
						items[pos] = i;
						pos++;
					}
					MyStatsDialog dialog = new MyStatsDialog(tree.getShell(), items);
					dialog.open();
					}catch(Exception e) {
						MessageDialog.openError(tree.getShell(), "Error", "One or more selected rows don't have Original Estimate. "
								+ "\nPlease select row(s), that have Original Estimated time , in order to calculate the potential loss.");
					}

				}
			}
		});

		tree.addMouseListener(new MouseAdapter() {

			public void mouseDown(MouseEvent event) {
				if (event.button == 3 && tree.getSelectionCount() != 0) {
					try {
						
					
					final int n = tree.getSelectionCount();
					final TreeItem[] items = new TreeItem[n];
					int pos = 0;
					for (TreeItem i : tree.getSelection()) {
						items[pos] = i;
						pos++;
					}
					MyStatsDialog dialog = new MyStatsDialog(tree.getShell(), items);
					dialog.open();
					}catch(Exception e) {
						MessageDialog.openError(tree.getShell(), "Error", "One or more selected rows don't have Original Estimate. "
								+ "\nPlease select row(s), that have Original Estimated time , in order to calculate the potential loss.");
					}
				}
			}
		});
		
		ThresholdBundle tb = new ThresholdBundle();
		tb.addListener(this);
	}

	@Override
	public void thresholdUpdated() {
		System.out.println("Threshold updated!");
		for (TreeItem epic : tree.getItems()) {
			recolorCellsAfterThresholdUpdate(epic);

			for (TreeItem story : epic.getItems()) {
				recolorCellsAfterThresholdUpdate(story);
			}
		}

	}

}
