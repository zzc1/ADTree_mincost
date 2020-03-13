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

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import adtool.ui.ADTreeCanvas;
import adtool.ui.DomainCanvas;

import org.abego.treelayout.NodeExtentProvider;

/**

 */
public class ADTreeNodeExtentProvider extends ADTLocalTNExtendProvider {
	/**
	 * List of canvas that have common size of node with main canvas.
	 */
	private Set<DomainCanvas<?>> canvasSet;
	private HashMap<ADTreeNode, Point2D.Double> sizes;

	/**
	 * Constructs a new instance.
	 */
	public ADTreeNodeExtentProvider() {
		super(null);
		canvasSet = new HashSet<DomainCanvas<?>>();
		sizes = new HashMap<ADTreeNode, Point2D.Double>();
	}

	public void registerCanvas(DomainCanvas<?> canvas) {
		canvasSet.add(canvas);
	}

	public void deregisterCanvas(DomainCanvas<?> canvas) {
		canvasSet.remove(canvas);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see NodeExtentProvider#getWidth(ADTreeNode)
	 */
	@Override
	public final double getWidth(final ADTreeNode node) {
		return sizes.get(node).x;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see NodeExtentProvider#getHeight(ADTreeNode)
	 */
	@Override
	public final double getHeight(final ADTreeNode node) {
		return sizes.get(node).y;
	}

	/**
	 * Deletes all calculated sizes.
	 * 
	 * @param node
	 *
	 */
	public final void clearSizes() {
		sizes.clear();
	}

	/**
	 * Calculates new size for the node.
	 * 
	 * @param node
	 *
	 */
	public final void updateSize(final ADTreeNode node) {
		if (getOwner() != null) {
			String[] labels = getOwner().getLabelLines(node);
			Point2D.Double size = getSizeOfLabels(labels);
			for (ADTreeCanvas canvas : canvasSet) {
				labels = canvas.getLabelLines(node);
				Point2D.Double newSize = getSizeOfLabels(labels);
				size.x = Math.max(size.x, newSize.x);
				size.y = Math.max(size.y, newSize.y);
			}
			size.x = correctForOval(size.x, node);
			size.y = correctForOval(size.y, node);
			// no vertical ellipses/rectangle - use cicle/square then
			if (size.x < size.y) {
				size.x = size.y;
			}
			sizes.put(node, size);
		}
	}

	/**
	 * Gets the sizes for this instance.
	 *
	 * @return The sizes.
	 */
	public HashMap<ADTreeNode, Point2D.Double> getSizes() {
		return this.sizes;
	}

}
