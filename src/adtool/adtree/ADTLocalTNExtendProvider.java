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
package adtool.adtree;

import adtool.Choices;
import adtool.ui.ADTreeCanvas;
import org.abego.treelayout.NodeExtentProvider;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * A {@link NodeExtentProvider} for nodes of type {@link TextInBox}.
 * <p>
 * As one would expect this NodeExtentProvider returns the width and height as
 * specified with each TextInBox.
 * 
 * @author Piot Kordy
 */
public class ADTLocalTNExtendProvider implements NodeExtentProvider<ADTreeNode> {
	private final static int X_PADDING = 5;
	private final static int Y_PADDING = 5;
	private ADTreeCanvas owner;

	/**
	 * Constructs a new instance.
	 * 
	 * @param owner
	 *            canvas owning this instarce.
	 */
	public ADTLocalTNExtendProvider(ADTreeCanvas owner) {
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see NodeExtentProvider#getWidth(ADTreeNode)
	 */
	public double getWidth(final ADTreeNode node) {
		String[] labels = owner.getLabelLines(node);
		double result = getSizeOfLabels(labels).x;
		// no vertical ellipses
		return Math.max(correctForOval(result, node), getHeight(node));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see NodeExtentProvider#getHeight(ADTreeNode)
	 */
	public double getHeight(final ADTreeNode node) {
		String[] labels = owner.getLabelLines(node);
		double result = getSizeOfLabels(labels).y;
		return correctForOval(result, node);
	}

	/**
	 * Calculates the width and height of array of lines
	 * 
	 * @param labels
	 *            array or strings
	 */
	protected final Point2D.Double getSizeOfLabels(String[] labels) {
		@SuppressWarnings("deprecation")
		final FontMetrics m = Toolkit.getDefaultToolkit().getFontMetrics(Choices.canv_Font);
		Point2D.Double result = new Point2D.Double();
		for (String line : labels) {
			result.setLocation(Math.max(result.getX(), m.stringWidth(line)), result.getY() + m.getHeight());
		}
		return new Point2D.Double(result.getX() + X_PADDING, result.getY() + Y_PADDING);
	}

	/**
	 * Increases the size if the shape is oval.
	 *
	 */
	protected double correctForOval(double x, ADTreeNode node) {
		Choices.ShapeType shape;
		double result = x;
		if (node.getType() == Choices.canv_Defender) {
			shape = Choices.canv_ShapeDef;
		} else {
			shape = Choices.canv_ShapeAtt;
		}
		switch (shape) {
		case OVAL:
			result = (2 * x) / Math.sqrt(2);
			break;
		case ROUNDRECT:
		case RECTANGLE:
		default:
			break;
		}
		return result;
	}

	/**
	 * Sets the owner for this instance.
	 *
	 * @param owner
	 *            The owner.
	 */
	public void setOwner(ADTreeCanvas owner) {
		if (this.owner == null && owner != null) {
			this.owner = owner;
		}
	}

	/**
	 * Gets the owner for this instance.
	 *
	 * @return The owner.
	 */
	public ADTreeCanvas getOwner() {
		return this.owner;
	}
}
