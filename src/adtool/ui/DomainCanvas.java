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

import java.awt.Color;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import adtool.Choices;
import adtool.adtree.ADTNode;
import adtool.adtree.ADTreeForGui;
import adtool.adtree.ADTreeNode;
import adtool.domains.Domain;
import adtool.domains.ValuationDomain;
import adtool.domains.ValueAssignement;
import adtool.domains.rings.RealG0;
import adtool.domains.rings.Ring;

/**
 * Canvas for drawing tree for specific domain.
 */
public class DomainCanvas<Type> extends ADTreeCanvas {
	static final long serialVersionUID = 745558011570251703L;
	private boolean showLabels;
	private InputDialog dialog;
	private boolean markEditable;
	private HashSet<String> markedPro = new HashSet<String>();
	private HashSet<String> markedOpp = new HashSet<String>();

	public DomainCanvas(final ADTreeForGui newTree, MainWindow mw, int newId) {
		super(newTree, mw, newId);
		this.treeChanged();
		this.setClear(1);
		dialog = null;
		markEditable = true;
	}

	/**
	 * Shows the dialog to edit a value in the tree.
	 *
	 * @param node
	 */
	public void editValue(ADTreeNode node) {
		@SuppressWarnings("unchecked")
		ValuationDomain<Type> valuationDomain = (ValuationDomain<Type>) mainWindow.getValuation(getId());
		if (node.getTerm().isEditable(valuationDomain.getDomain())) {
			Ring value = (Ring) valuationDomain.getValue(node);
			if (value instanceof RealG0) {
				dialog = new RealG0Dialog(getMainWindow());
				value = (Ring) (dialog.showInputDialog(value));
			} 
			if (value != null) {
				String key = node.getTerm().getName();
				putNewValue(node.getType() == ADTreeNode.Type.PROPONENT, key, value);
				//putNewValue(true,key,value);
				valuesUpdated();
			}
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				requestFocus();
			}
		});
	}

	/**
	 * Assigns a new value to a node.
	 *
	 * @param key
	 */
	public void putNewValue(boolean proponent, String key, Ring value) {
		mainWindow.getValuation(getId()).setValue(proponent, key, (Ring) value);
	}

	public void refreshAllValues() {
		mainWindow.getValuation(getId()).refreshAllValues(tree.getRoot(true));
	}

	public void valuesUpdated() {
		@SuppressWarnings("unchecked")
		ValuationDomain<Type> valuationDomain = (ValuationDomain<Type>) mainWindow.getValuation(getId());
		valuationDomain.valuesUpdated(tree.getRoot(true));
		ValuationView valuationView = ((ValuationView) getMainWindow().getViews()[2].getComponent());
		if (valuationView.getCanvas() == this) {
			valuationView.assignCanvas(this);
		}
		tree.updateAllSizes();
		this.repaint();
	}

	@Override
	protected void addListener() {
		listener = new DomainCanvasHandler<Type>(this);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
		this.addKeyListener(listener);
	}

	/**
	 * Recalculate the positions of the nodes on the canvas when the size of
	 * some nodes have changed.
	 *
	 */
	@Override
	public void sizeChanged() {
		recalculateLayout();
	}

	/**
	 * Recalculate the positions of the nodes on the canvas when the tree is
	 * changed.
	 *
	 */
	@Override
	public void treeChanged() {
		mainWindow.getValuation(getId()).treeChanged(tree.getRoot(true));
		ValuationView valuationView = ((ValuationView) getMainWindow().getViews()[2].getComponent());
		if (valuationView.getCanvas() != null && valuationView.getCanvas().getId() == this.getId()) {
			valuationView.assignCanvas((ADTreeCanvas) this);
		}
		tree.updateAllSizes();
	}

	/**
	 * Gets the domain for this instance.
	 *
	 * @return The domain.
	 */
	@SuppressWarnings("unchecked")
	public Domain<Type> getDomain() {
		return (Domain<Type>) mainWindow.getValuation(getId()).getDomain();
	}

	/**
	 * Sets the valueAssPro for this instance.
	 *
	 * @param newValueAssPro
	 *            new value assignement.
	 */
	@SuppressWarnings("unchecked")
	public void setValueAssPro(ValueAssignement<Type> newValueAss) {
		mainWindow.getValuation(getId()).setValueAssPro((ValueAssignement<Ring>) newValueAss, tree.getRoot(true));
	}

	/*
	 * Sets the valueAssOpp for this instance.
	 *
	 * @param newValueAssOpp new value assignement.
	 */
	@SuppressWarnings("unchecked")
	public void setValueAssOpp(ValueAssignement<Type> newValueAss) {
		mainWindow.getValuation(getId()).setValueAssOpp((ValueAssignement<Ring>) newValueAss, tree.getRoot(true));
	}

	/**
	 * Gets the valueAssPro for this instance.
	 *
	 * @return The valueAssPro.
	 */
	@SuppressWarnings("unchecked")
	public ValueAssignement<Type> getValueAssPro() {
		return (ValueAssignement<Type>) mainWindow.getValuation(getId()).getValueAssPro();
	}

	/**
	 * Gets the valueAssOpp for this instance.
	 *
	 * @return The valueAssOpp.
	 */
	@SuppressWarnings("unchecked")
	public ValueAssignement<Type> getValueAssOpp() {
		return (ValueAssignement<Type>) mainWindow.getValuation(getId()).getValueAssOpp();
	}

	/**
	 * Sets whether or not this instance is showLabels.
	 *
	 * @param showLabels
	 *            The showLabels.
	 */
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
		tree.updateAllSizes();
		repaint();
	}

	/**
	 * @return the showAllLabels
	 */
	public boolean isShowAllLabels() {
		return ((ValuationDomain<Type>) mainWindow.getValuation(getId())).isShowAllLabels();
	}

	/**
	 * Sets whether or not this instance is showAllLabels.
	 *
	 * @param showAllLabels
	 *            The showAllLabels.
	 */
	public void setShowAllLabels(boolean showAllLabels) {
		((ValuationDomain<Type>) mainWindow.getValuation(getId())).setShowAllLabels(showAllLabels);
		tree.updateAllSizes();
		repaint();
	}

	/**
	 * @return the dialog
	 */
	public InputDialog getDialog() {
		return dialog;
	}

	/**
	 * @param dialog
	 *            the dialog to set
	 */
	public void setDialog(InputDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * Determines if this instance is markEditable.
	 *
	 * @return The markEditable.
	 */
	public boolean isMarkEditable() {
		return this.markEditable;
	}

	/**
	 * Sets whether or not this instance is markEditable.
	 *
	 * @param markEditable
	 *            The markEditable.
	 */
	public void setMarkEditable(boolean markEditable) {
		this.markEditable = markEditable;
		repaint();
	}

	public boolean isMarked(ADTreeNode node) {
		// return false;//not finished
		if (node.getType() == ADTreeNode.Type.PROPONENT) {
			return markedPro.contains(node.getLabel());
		} else {
			return markedOpp.contains(node.getLabel());
		}
	}

	protected String getFillColorS(ADTreeNode node) {
		if (isMarked(node)) {
			return "markedFill";
		}
		if (markEditable) {
			if (node.getTerm().isEditable(getDomain())) {
				return "editableFill";
			}
		}
		return super.getFillColorS(node);
	}

	protected Color getFillColor(ADTreeNode node) {
		if (isMarked(node)) {
			return Choices.canv_MarkedColor;
		}
		if (markEditable) {
			if (node.getTerm().isEditable(getDomain())) {
				return Choices.canv_EditableColor;
			}
		}
		return super.getFillColor(node);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see ADTreeCanvas#getLabel(ADTreeNode)
	 */
	public String getLabel(ADTreeNode node) {
		@SuppressWarnings("unchecked")
		ValuationDomain<Type> valuationDomain = (ValuationDomain<Type>) mainWindow.getValuation(getId());
		String result;
		if (node == null) {
			return "Null node";
		}
		if (showLabels) {
			result = node.getLabel() + "\n";
		} else {
			result = "";
		}
		Object value = null;
		if (valuationDomain.hasEvaluator()&&node.getType()==ADTreeNode.Type.OPPONENT) {
			ADTNode term = node.getTerm();
			if (term != null) {
				if ((term.getType() == ADTNode.Type.CP || term.getType() == ADTNode.Type.CO)
						&& ((ADTNode) term.getChildren().elementAt(0)).getChildren().size() == 0) {
					if (isShowAllLabels()) {
						value = valuationDomain.getTermValue(node.getTerm());
						result += toUnicode((Ring) value) + "\n";
					}
					value = valuationDomain.getTermValue((ADTNode) term.getChildren().elementAt(0));
				} else {
					value = valuationDomain.getTermValue(node.getTerm());
				}
				result += toUnicode((Ring) value);
			}
		}
		return result;
	}

	public void markPro(String label) {
		markedPro.add(label);
	}

	public void unmarkPro(String label) {
		markedPro.remove(label);
	}

	public void markOpp(String label) {
		markedOpp.add(label);
	}

	public void unmarkOpp(String label) {
		markedOpp.remove(label);
	}

	public void unmarkAll() {
		markedPro.clear();
		markedOpp.clear();
	}

	/**
	 * Converts value to unicode string.
	 *
	 * @param value
	 *            value
	 * @return
	 */
	private String toUnicode(Ring value) {
		if (value == null) {
			return "null";
		} else {
			return value.toUnicode();
		}
	}

	public String toString() {
		String result = super.toString();
		result += " id=" + getId();
		return result;

	}

}
