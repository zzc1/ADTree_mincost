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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.geom.Point2D;

import javax.swing.BoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import adtool.adtree.ADTreeNode;

public abstract class AbstractCanvasHandler implements CanvasHandler {
	protected ADTreeCanvas canvas;
	private Point2D dragStart;
	private boolean dragScroll;

	public AbstractCanvasHandler(final ADTreeCanvas canvas) {
		this.canvas = canvas;
		dragStart = null;
		dragScroll = false;
	}

	public void mouseWheelMoved(final MouseWheelEvent e) {
		final JScrollPane scrollPane = canvas.getScrollPane();
		final int notches = e.getWheelRotation();
		final Rectangle pos = scrollPane.getViewport().getViewRect();
		double scale = canvas.getScale();
		JScrollBar bar = scrollPane.getHorizontalScrollBar();
		if (bar.isVisible() && e.getY() > pos.getHeight()
				&& e.getY() < pos.getHeight() + bar.getPreferredSize().height) {
			bar.setValue(bar.getValue() + (int) (scale * (notches + 4 * Math.signum(notches)) + Math.signum(notches)));
			return;
		}
		bar = scrollPane.getVerticalScrollBar();
		if (bar.isVisible() && e.getX() > pos.getWidth() && e.getX() < pos.getWidth() + bar.getPreferredSize().width) {
			bar.setValue(bar.getValue() + (int) (scale * (notches + 4 * Math.signum(notches)) + Math.signum(notches)));
			return;
		}

		if (notches < 0) {
			canvas.zoomIn();
		} else {
			canvas.zoomOut();
		}
		scale = canvas.getScale() / scale;

		int xPos = (int) (pos.getX() + ((pos.getX() + e.getX()) * (scale - 1)));
		int yPos = (int) (pos.getY() + ((pos.getY() + e.getY()) * (scale - 1)));
		int moveX = 0;
		int moveY = 0;
		final BoundedRangeModel mX = scrollPane.getHorizontalScrollBar().getModel();
		final BoundedRangeModel mY = scrollPane.getVerticalScrollBar().getModel();
		final double maxScrollX = mX.getMaximum() - mX.getExtent();
		final double maxScrollY = mY.getMaximum() - mY.getExtent();
		if (xPos > maxScrollX) {
			moveX = xPos - (int) maxScrollX;
			xPos = (int) maxScrollX;
		} else if (xPos < 0) {
			moveX = xPos;
			xPos = 0;
		}
		if (yPos > maxScrollY) {
			moveY = yPos - (int) maxScrollY;
			yPos = (int) maxScrollY;
		} else if (yPos < 0) {
			moveY = yPos;
			yPos = 0;
		}
		scrollPane.getViewport().setViewPosition(new Point(xPos, yPos));
		canvas.moveTree(-moveX / canvas.getScale(), -moveY / canvas.getScale());

	}

	public void keyPressed(KeyEvent e) {
		final ADTreeNode node = canvas.getFocused();
		if (e.isControlDown() && node != null) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_T:
				// expand node
				break;
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_ADD:
			case KeyEvent.VK_EQUALS:
				canvas.zoomIn();
				break;
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_SUBTRACT:
				canvas.zoomOut();
				break;
			case KeyEvent.VK_O:
				canvas.resetZoom();
				break;
			default:

			}
		} else {
			ADTreeNode tempNode = null;
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				tempNode = canvas.getParentNode(node);
				break;
			case KeyEvent.VK_LEFT:
				tempNode = canvas.getLeftSibling(node);
				break;
			case KeyEvent.VK_RIGHT:
				tempNode = canvas.getRightSibling(node);
				break;
			case KeyEvent.VK_DOWN:
				tempNode = canvas.getMiddleChild(node);
				break;
			}
			if (tempNode != null) {
				setFocus(tempNode);
			}
		}
	}

	public void mouseClicked(final MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		dragStart = new Point(e.getX(), e.getY());
		if (canvas.getNode(e.getX(), e.getY()) != null) {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			dragScroll = false;
		} else {
			canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			dragScroll = true;
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (dragStart != null) {
			if (dragScroll) {
				Point p = canvas.scrollTo(e.getX() - dragStart.getX(), (e.getY() - dragStart.getY()));
				dragStart = new Point(e.getX(), e.getY());
				((Point) dragStart).translate((int) -p.getX(), (int) -p.getY());
			} else {
				Point2D p = new Point(e.getX(), e.getY());
				p = canvas.transform(p);
				Point2D p2 = canvas.transform(dragStart);
				canvas.moveTree(p.getX() - p2.getX(), p.getY() - p2.getY());
				dragStart = new Point(e.getX(), e.getY());
			}
		}
	}

	public void mouseReleased(final MouseEvent e) {
		dragStart = null;
		dragScroll = true;
		canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		canvas.repaint();
	}

	public void setFocus(final ADTreeNode node) {
		canvas.setFocus(node);
	}

	public void keyTyped(final KeyEvent e) {

	}

	public void keyReleased(final KeyEvent e) {

	}

	public void mouseEntered(final MouseEvent e) {
	}

	public void mouseExited(final MouseEvent e) {
	}

	public void mouseMoved(final MouseEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		canvas.setViewPortSize(((JScrollPane) e.getComponent()).getViewport().getExtentSize());
	}

	public void componentShown(ComponentEvent e) {
	}

}
