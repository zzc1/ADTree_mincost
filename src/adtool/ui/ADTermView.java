/**
 * Author: Piotr Kordy (piotr.kordy@uni.lu <mailto:piotr.kordy@uni.lu>)
 * Date:   06/06/2013
 * Copyright (c) 2013,2012 University of Luxembourg -- Faculty of Science,
 *     Technology and Communication FSTC
 * All rights reserved.
 * Licensed under GNU Affero General Public License 3.0;
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package adtool.ui;

import adtool.adtconverter.EulerTree;
import adtool.adtree.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;

// import adtool.adtconverter.EditPath;
// import adtool.adtconverter.RTED_InfoTree;


public class ADTermView extends JPanel implements TreeChangeListener {
	static final long serialVersionUID = 17266535905153654L;
	public static ADTParser parser = null;
	private ADTNode terms;
	private ADTreeCanvas canvas;
	private JTextArea errorOutput;
	private JSplitPane splitPane;
	private JTextArea editTerms;
	private JButton validate;
	private JButton revert;

	/**
	 * Constructor.
	 * 
	 * @param canvas
	 */
	public ADTermView(ADTreeCanvas canvas) {
		super(new BorderLayout());
		terms = canvas.getTerms();
		initLayout(terms.toString());
		this.canvas = canvas;
		canvas.getTree().addTreeChangeListener(this);
	}

	/**
	 * Recalculate the terms based on new tree. changed.
	 * 
	 */
	public void treeChanged() {
		setTerms(canvas.getTerms());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see TreeChangeListener#sizeChanged()
	 */
	public void sizeChanged() {
	}

	/**
	 * Checks if a string is a valid label
	 * 
	 * @param s
	 * @return
	 */
	public boolean validLabel(String s) {
		if (s == null) {
			return false;
		}
		if (s.length() == 0) {
			return false;
		}
		ADTParser.ReInit(new StringReader(s.trim()));
		try {
			ADTParser.parse();
		} catch (ParseException e) {
			return false;
		} catch (TokenMgrError e) {
			return false;
		}

		return true;

	}

	/**
	 * Sets the terms for this instance.
	 *
	 * @param terms
	 *            The terms.
	 */
	public void setTerms(ADTNode terms) {
		this.terms = terms;
		editTerms.setText(terms.toString());
	}

	/**
	 * Initialize the layout of a panel.
	 * 
	 */
	private void initLayout(String termString) {
		editTerms = new JTextArea(termString);
		editTerms.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				editTerms.getHighlighter().removeAllHighlights();
			}

			public void removeUpdate(DocumentEvent e) {
				editTerms.getHighlighter().removeAllHighlights();
			}

			public void insertUpdate(DocumentEvent e) {
				editTerms.getHighlighter().removeAllHighlights();
			}
		});
		if (parser == null) {
			parser = new ADTParser(new StringReader(editTerms.getText()));
		} else {
			ADTParser.ReInit(new StringReader(editTerms.getText()));
		}
		errorOutput = new JTextArea();
		errorOutput.setEditable(false);
		JScrollPane errorScroll = new JScrollPane(errorOutput);
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, errorScroll, new JScrollPane(editTerms));
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0);
		splitPane.setDividerLocation(0.0);
		add(splitPane);
		add(createButtonPane(), BorderLayout.PAGE_END);
	}

	/**
	 * Initialise the button panel.
	 * 
	 * @return created button panel.
	 */
	private JPanel createButtonPane() {
		JPanel buttonPane = new JPanel();
		validate = new JButton("Validate");
		revert = new JButton("Revert");
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(validate);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(revert);
		revert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				revert();
			}
		});
		validate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				parse();
			}
		});
		revert.setEnabled(false);
		return buttonPane;
	}

	private void revert() {
		if (terms != null) {
			editTerms.setText(terms.toString());
			revert.setEnabled(false);
			splitPane.setDividerLocation(0.0);
			errorOutput.setText("");
		}
	}

	public void setFocus(ADTreeNode n) {
		// to be implemented.
	}

	public void parse() {
		if (editTerms.getText().length() > 0) {
			ADTNode oldTerms = terms;
			ADTParser.ReInit(new StringReader(editTerms.getText()));
			try {
				terms = ADTParser.parse();
				editTerms.setText(terms.toString());
				revert.setEnabled(false);
				splitPane.setDividerLocation(0.0);
				errorOutput.setText("");
				EulerTree et = new EulerTree();
				et.transferLabels(terms, oldTerms);
				canvas.treeFromTerms(terms);
				canvas.getMainWindow().getStatusBar().report("Validation of terms was successful");
			} catch (ParseException e) {
				handleError(e.getMessage(), e.currentToken.endLine, e.currentToken.endColumn,
						e.currentToken.next.endLine, e.currentToken.next.endColumn);
			} catch (TokenMgrError e) {
				String m = e.getMessage();
				int line = Integer.parseInt(m.subSequence(22, m.indexOf(',', 22)).toString());
				int column = Integer.parseInt(m.subSequence(m.indexOf("n ", 23) + 2, m.indexOf('.', 23)).toString());
				handleError(m, line, column, line, column);
			}
		}
	}

	private void handleError(String m, int startLine, int startColumn, int endLine, int endColumn) {
		errorOutput.setText(m);
		canvas.getMainWindow().getStatusBar().reportError("Validation of terms was not possible: " + m);
		splitPane.setDividerLocation(
				Math.max(1 / 2, (errorOutput.getPreferredSize().getHeight() + 5) / splitPane.getSize().height));
		try {
			int offset = editTerms.getLineStartOffset(startLine - 1) + startColumn - 1;
			int endOffset = editTerms.getLineStartOffset(endLine - 1) + endColumn - 1;
			editTerms.scrollRectToVisible(editTerms.modelToView(offset));
			editTerms.setCaretPosition(endOffset);
			highlight(offset, endOffset);
			editTerms.requestFocus();
		} catch (BadLocationException err) {
			canvas.getMainWindow().getStatusBar().reportError(err.getLocalizedMessage());
		}
		revert.setEnabled(true);
	}

	private void highlight(int startPos, int endPos) {
		if (startPos == endPos) {
			if (startPos > 0)
				startPos--;
		}
		DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
				Color.YELLOW);
		try {
			editTerms.getHighlighter().addHighlight(startPos, endPos, highlightPainter);
		} catch (BadLocationException err) {
			canvas.getMainWindow().getStatusBar().reportError(err.getLocalizedMessage());
		}
	}

	/**
	 * Gets the terms for this instance.
	 *
	 * @return The terms.
	 */
	public ADTNode getTerms() {
		return this.terms;
	}
}
