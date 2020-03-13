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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PageRanges;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import adtool.Choices;
import adtool.adtree.ADTLocalTNExtendProvider;
import adtool.adtree.ADTNode;
import adtool.adtree.ADTParser;
import adtool.adtree.ADTXmlExport;
import adtool.adtree.ADTreeForGui;
import adtool.adtree.ADTreeNode;
import adtool.adtree.ParseException;
import adtool.adtree.TokenMgrError;

import net.infonode.docking.View;

import org.abego.treelayout.NodeExtentProvider;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class ADTreeCanvas extends JPanel implements Scrollable, TreeChangeListener, Printable, Pageable {
	protected ADTreeForGui tree;

	public void setTree(ADTreeForGui tree) {
		this.tree = tree;
	}

	protected boolean localExtentProvider;

	protected CanvasHandler listener;
	private final BasicStroke basicStroke = new BasicStroke(Choices.canv_LineWidth, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	private final BasicStroke selectionStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
			new float[] { 6, 10 }, 0);
	private final BasicStroke counterStroke = new BasicStroke(Choices.canv_LineWidth, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND, 0, new float[] { 0, 6 }, 0);

	private final int borderPad = 20;

	private double moveX = 0;

	private double moveY = 0;

	private final int focusPad = 10;

	private double scale = 1;

	private AffineTransform viewTransform = new AffineTransform();

	private double sizeX = 0;

	private double sizeY = 0;

	private JScrollPane scrollPane;

	protected Map<ADTreeNode, Rectangle2D.Double> bufferedLayout;

	private DefaultConfiguration<ADTreeNode> configuration;

	protected ADTreeNode focused;

	protected ADTreeNode lastFocused;

	protected PageFormat pageFormat;
	protected PrintRequestAttributeSet printAttr;

	private Dimension viewPortSize;
	protected MainWindow mainWindow;
	private int id;

	private int Clear = 0;

	public ADTreeCanvas(ADTreeForGui newTree, MainWindow mw, int newId) {
		super(new BorderLayout());
		this.id = newId;
		mainWindow = mw;
		this.setBackground(Choices.canv_BackgroundColor);
		scrollPane = null;
		printAttr = new HashPrintRequestAttributeSet();
		printAttr.add(MediaSizeName.ISO_A4);
		localExtentProvider = false;
		pageFormat = new PageFormat();
		addListener();
		tree = newTree;
		tree.setOwner(this);
		tree.addTreeChangeListener(this);
		configuration = new DefaultConfiguration<ADTreeNode>(Choices.canv_gapBetweenLevels,
				Choices.canv_gapBetweenNodes);
		// create the layout
		setFocus(null);
		lastFocused = tree.getRoot(false);
		setFocusTraversalKeysEnabled(true);
		setScale(1);
		sizeX = 0;
		sizeY = 0;
		bufferedLayout = null;
		NodeExtentProvider<ADTreeNode> extentProvider;
		if (localExtentProvider) {
			extentProvider = new ADTLocalTNExtendProvider(this);
		} else {
			extentProvider = tree.getExtendProvider();
		}
		TreeLayout<ADTreeNode> treeLayout = new TreeLayout<ADTreeNode>(tree, extentProvider, configuration);
		bufferedLayout = treeLayout.getNodeBounds();
		for (Rectangle2D.Double rect : bufferedLayout.values()) {
			sizeX = Math.max(sizeX, rect.getMaxX());
			sizeY = Math.max(sizeY, rect.getMaxY());
		}
		setScale(scale);
	}

	protected void addListener() {
		listener = new ADTreeCanvasHandler(this);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
		this.addKeyListener(listener);
	}

	public void sizeChanged() {
		recalculateLayout();
	}

	public void treeChanged() {
		recalculateLayout();
	}

	public void newTree() {
		tree.newTree();
	}

	public void moveTree(double x, double y) {
		Rectangle r = scrollPane.getViewport().getViewRect();
		setMoveX(moveX + x);
		setMoveY(moveY + y);
		updateSize();
		scrollPane.scrollRectToVisible(r);
		repaint();
	}

	private void updateSize() {
		Point2D.Double point = new Point2D.Double(sizeX + borderPad / 2, sizeY + borderPad / 2);
		viewTransform.transform(point, point);
		int x = 0;
		int y = 0;
		if (viewPortSize != null) {
			x = viewPortSize.width + x;
			y = viewPortSize.height + y;
		}
		Dimension dim = new Dimension(Math.max((int) point.getX(), x), Math.max((int) point.getY(), y));

		setPreferredSize(dim);
		setMinimumSize(dim);
		revalidate();
	}

	protected void recalculateLayout() {
		sizeX = 0;
		sizeY = 0;
		bufferedLayout = null;
		NodeExtentProvider<ADTreeNode> extentProvider;
		if (localExtentProvider) {
			extentProvider = new ADTLocalTNExtendProvider(this);
		} else {
			extentProvider = tree.getExtendProvider();
		}
		TreeLayout<ADTreeNode> treeLayout = new TreeLayout<ADTreeNode>(tree,
				(NodeExtentProvider<ADTreeNode>) extentProvider, configuration);
		bufferedLayout = treeLayout.getNodeBounds();
		for (Rectangle2D.Double rect : bufferedLayout.values()) {
			sizeX = Math.max(sizeX, rect.getMaxX());
			sizeY = Math.max(sizeY, rect.getMaxY());
		}
		setScale(scale);
		repaint();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 0;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		int maxUnitIncrement = 1;
		if (orientation == SwingConstants.HORIZONTAL)
			return visibleRect.width - maxUnitIncrement;
		else
			return visibleRect.height - maxUnitIncrement;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public void paintComponent(final Graphics2D g2, ADTreeNode startNode) {
		g2.setStroke(basicStroke);
		if (Choices.canv_DoAntialiasing) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		paintEdges(g2, startNode);

		// paint the boxes
		paintBox(g2, startNode);
		if (focused != null) {
			paintFocus(g2, focused);
		}
	}

	public Point2D transform(Point2D point) {
		try {
			return viewTransform.inverseTransform(point, null);
		} catch (NoninvertibleTransformException e) {
			return point;
		}
	}

	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Choices.canv_BackgroundColor);
		Rectangle r = g2.getClipBounds();
		if (r != null) {
			g2.fillRect(r.x, r.y, r.width, r.height);
		}
		g2.transform(viewTransform);
		if (Clear == 1) {
			paintComponent(g2, tree.getRoot(false));
		}
	}

	public PageFormat getPageFormat(int pageIndex) {
		return pageFormat;
	}

	public Printable getPrintable(int pageIndex) {
		if (pageIndex >= Choices.print_noPages) {
			throw new IndexOutOfBoundsException("No page with number " + pageIndex);
		}
		return this;
	}

	public int getNumberOfPages() {
		// return 1;
		return Choices.print_noPages;
	}

	public Point getColsRows(int noMaxPages) {
		PageFormat pf = getPageFormat(0);
		return getColsRows(pf.getImageableWidth(), pf.getImageableHeight(), noMaxPages);
	}

	public boolean showPrintDialog(boolean doPrint) {
		try {
			PrinterJob pjob = PrinterJob.getPrinterJob();
			pjob.setPageable(this);
			printAttr.add(new PageRanges(1, getNumberOfPages()));
			if (doPrint) {
				if (pjob.printDialog(printAttr)) {
					pageFormat = pjob.getPageFormat(printAttr);
					pjob.print();
					mainWindow.getStatusBar().report("Printed window with title: "
							+ ((View) this.getParent().getParent().getParent().getParent().getParent()).getTitle());
					return true;
				}
			} else {
				PageFormat page = pjob.pageDialog(getPageFormat(0));
				if (page != getPageFormat(0)) {
					setPageFormat(page);
					mainWindow.getStatusBar().report("Printed window with title: "
							+ ((View) this.getParent().getParent().getParent().getParent().getParent()).getTitle());
					return true;
				}
			}
		} catch (PrinterException exc) {
			mainWindow.getStatusBar().reportError(exc.getLocalizedMessage());
		}
		return false;
	}

	public void createXml(FileOutputStream fileStream) {
		ADTXmlExport exporter = new ADTXmlExport(this.tree, this.mainWindow.getValuations());
		exporter.exportTo(fileStream);
	}

	public void createPdf(FileOutputStream fileStream) {
		double oldScale = getScale();
		Dimension dim = getPreferredSize();
		if (dim.width > 14399 || dim.height > 14399) {
			setScale(14000.0 / Math.max(dim.width, dim.height) * oldScale);
			dim = getPreferredSize();
		}
		try {
			Document document = new Document(new com.itextpdf.text.Rectangle(dim.width, dim.height));
			PdfWriter writer = PdfWriter.getInstance(document, fileStream);
			document.open();
			PdfContentByte canv = writer.getDirectContent();
			Graphics2D g2 = new PdfGraphics2D(canv, dim.width, dim.height);
			this.paintComponent(g2);
			g2.dispose();
			document.close();
			fileStream.close();
		} catch (DocumentException e) {
			System.err.println("Error while exporting to pdf:" + e);
		} catch (IOException e) {
			System.err.println("Error while exporting to pdf:" + e);
		}
		setScale(oldScale);
	}

	public void createImage(FileOutputStream fileStream, String formatName) {
		Dimension dim = getPreferredSize();
		BufferedImage bufferedImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		this.paintComponent(g2d);
		g2d.dispose();
		try {
			ImageIO.write(bufferedImage, formatName, fileStream);
			fileStream.close();
		} catch (IOException e) {
			System.err.println("Error while exporting to image:" + e);
		}
	}

	public int print(Graphics g, PageFormat pf, int page) {
		double pW = pf.getImageableWidth();
		double pH = pf.getImageableHeight();
		Point p = getColsRows(pW, pH, getNumberOfPages());
		if (page >= (int) (p.getX() * p.getY())) {
			return NO_SUCH_PAGE;
		}
		// store focus and set it to null
		ADTreeNode tFocus = focused;
		focused = null;
		double printScaleX = (pW * p.getX()) / (sizeX + Choices.canv_LineWidth);
		double printScaleY = (pH * p.getY()) / (sizeY + Choices.canv_LineWidth);
		if (Choices.print_perserveAspectRatio) {
			if (printScaleX < printScaleY) {
				printScaleY = printScaleX;
			} else {
				printScaleX = printScaleY;
			}
		}
		int shiftX = page % (int) p.getX();
		int shiftY = page / (int) p.getX();
		// align origin
		g.translate((int) (pf.getImageableX() - (shiftX * pW)), (int) (pf.getImageableY() - (shiftY * pH)));
		((Graphics2D) g).scale(printScaleX, printScaleY);
		g.translate(Choices.canv_LineWidth, Choices.canv_LineWidth);
		paintComponent((Graphics2D) g, tree.getRoot(false));
		// restore focus
		focused = tFocus;
		return PAGE_EXISTS;
	}

	public Point scrollTo(double x, double y) {
		Rectangle r = scrollPane.getViewport().getViewRect();
		Point p = r.getLocation();
		r.translate((int) x, (int) y);
		scrollRectToVisible(r);
		r = scrollPane.getViewport().getViewRect();
		p.translate((int) (-r.getX()), (int) (-r.getY()));
		return p;
	}

	public ADTreeNode getNode(double x, double y) {
		try {
			Point2D point = viewTransform.inverseTransform(new Point2D.Double(x, y), null);
			x = point.getX();
			y = point.getY();
		} catch (NoninvertibleTransformException e) {
			System.err.println("Cannot tranlate click point!!");
		}
		if (x > sizeX || y > sizeY || x < 0 || y < 0) {
			return null;
		}
		Shape shape;
		for (ADTreeNode node : bufferedLayout.keySet()) {
			Rectangle2D rect = bufferedLayout.get(node);
			switch (Choices.canv_ShapeDef) {
			case RECTANGLE:
				shape = rect;
				break;
			case OVAL:
				shape = new Ellipse2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
				break;
			case ROUNDRECT:
			default:
				shape = new RoundRectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(),
						Choices.canv_ArcSize, Choices.canv_ArcSize);
				break;
			}
			if (shape.contains(x, y)) {
				return node;
			}

		}
		return null;
	}

	public void setLabel(ADTreeNode node, String label) {
		tree.setLabel(node, label);
		tree.notifyTreeChanged();
		if (getMainWindow().getLastFocused() != null) {
			getMainWindow().getLastFocused().requestFocus();
		}
	}

	public void removeNode(ADTreeNode node) {
		if (!node.equals(tree.getRoot(true))) {
			if (lastFocused.equals(node)) {
				lastFocused = tree.getParent(node);
				if (lastFocused == null) {
					lastFocused = tree.getRoot(true);
				}
			}
			if (focused != null) {
				if (focused.equals(node)) {
					setFocus(tree.getParent(node));
				}
			}
		}
		tree.removeNode(node);
		tree.notifyTreeChanged();
	}

	public void removeTree(ADTreeNode node) {
		if (!node.equals(tree.getRoot(true))) {
			if (lastFocused.equals(node)) {
				lastFocused = tree.getParent(node);
				if (lastFocused == null) {
					lastFocused = tree.getRoot(true);
				}
			}
			if (focused != null) {
				if (focused.equals(node)) {
					setFocus(tree.getParent(node));
				}
			}
		}
		tree.removeTree(node);
		tree.notifyTreeChanged();
	}

	public void removeChildren(ADTreeNode node) {
		if (node.isFolded()) {
			tree.toggleFold(node);
		}
		tree.removeAllChildren(node);
		tree.notifyTreeChanged();
	}

	public boolean validLabel(String s) {
		if (s == null) {
			return false;
		}
		if (s.length() == 0) {
			return false;
		}
		ADTParser.ReInit(new StringReader("aaaa" + s.trim()));
		try {
			ADTNode node = ADTParser.parse();
			return node.getType() == ADTNode.Type.LEAFP || node.getType() == ADTNode.Type.LEAFO;
		} catch (ParseException e) {
			return false;
		} catch (TokenMgrError e) {
			return false;
		}
	}

	public void addChild(ADTreeNode node) {
		ADTreeNode child = new ADTreeNode(node.getType(), Choices.tree_defRefType);
		tree.addChild(node, child);
	}

	public void addSibling(ADTreeNode node, boolean onLeft) {
		ADTreeNode parent = tree.getParent(node);
		if (parent == null)
			return;
		if (node.getType() == parent.getType()) {
			ADTreeNode sibling = new ADTreeNode(node.getType(), Choices.tree_defRefType);
			tree.addSibling(node, sibling, onLeft);
		}
	}

	public ADTreeNode getRightSibling(ADTreeNode node) {
		return tree.getRightSibling(node);
	}

	public ADTreeNode getLeftSibling(ADTreeNode node) {
		return tree.getLeftSibling(node);
	}

	public ADTreeNode getMiddleChild(ADTreeNode node) {
		return tree.getMiddleChild(node);
	}

	public void changeOp(ADTreeNode node) {
		tree.changeOp(node);
	}

	public ADTNode getTerms() {
		return tree.getTerms();
	}

	public ADTreeForGui getTree() {
		return this.tree;
	}

	public void setLocalExtentProvider(boolean localExtentProvider) {
		this.localExtentProvider = localExtentProvider;
		if (localExtentProvider) {
			tree.deregisterSizeCanvas((DomainCanvas<?>) this);
		} else {
			tree.registerSizeCanvas((DomainCanvas<?>) this);
		}
	}

	public CanvasHandler getListener() {
		return this.listener;
	}

	public void setMoveX(double moveX) {
		if ((moveX) < 0) {
			moveX = 0;
		}
		viewTransform.translate(moveX - this.moveX, 0);
		this.moveX = moveX;
	}

	public double getMoveX() {
		return this.moveX;
	}

	public void setMoveY(double moveY) {
		if ((moveY) < 0) {
			moveY = 0;
		}
		viewTransform.translate(0, moveY - this.moveY);
		this.moveY = moveY;
	}

	public double getMoveY() {
		return this.moveY;
	}

	public void fitToWindow() {
		int x = viewPortSize.width;
		int y = viewPortSize.height;
		double sX = sizeX + borderPad;
		double sY = sizeY + borderPad;
		double newScale = getScale() * Math.min(x / (sX * getScale()), y / (sY * getScale()));
		setScale(newScale);
		setMoveX((x / getScale() - (sX)) / 2.0);
		setMoveY((y / getScale() - (sY)) / 2.0);
		updateSize();
		repaint();
	}

	public void setScale(double scale) {
		this.scale = scale;
		viewTransform = new AffineTransform();
		viewTransform.scale(scale, scale);
		viewTransform.translate(moveX, moveY);
		viewTransform.translate(borderPad / 2, borderPad / 2);
		updateSize();
	}

	public void expandAllNodes() {
		ADTreeNode viewNode = tree.getRoot(false);
		while (viewNode.isAboveFolded()) {
			tree.toggleAboveFold(viewNode);
			viewNode = tree.getRoot(false);
		}
		tree.expandAllFold();
	}

	public void toggleAboveFold(ADTreeNode viewRoot) {
		if (getTree().getParent(viewRoot, true) != null) {
			tree.toggleAboveFold(viewRoot);
		}
	}

	public void zoomIn() {
		if (scale < 100) {
			setScale(scale * Choices.canv_scaleFactor);
			this.repaint();
		}
	}

	public void resetZoom() {
		setScale(1);
		this.repaint();
	}

	public void zoomOut() {
		Point2D.Double point = new Point2D.Double(sizeX + borderPad / 2, sizeY + borderPad / 2);
		viewTransform.transform(point, point);
		if (Math.max(point.getX() - moveX * getScale(), point.getY() - moveY * getScale()) > 20) {
			setScale(scale / Choices.canv_scaleFactor);
			this.repaint();
		}
	}

	public void setFocus(ADTreeNode focused) {
		if (this.focused != null) {
			lastFocused = this.focused;
		}
		if (focused != null) {
			tree.defocusAll();
		}
		this.focused = focused;
		if (focused != null) {
			Rectangle2D b2 = bufferedLayout.get(focused);
			if (b2 == null) {
				recalculateLayout();
				b2 = bufferedLayout.get(focused);
			}
			Point2D p1 = new Point2D.Double(b2.getX() - borderPad / 2, b2.getY() - borderPad / 2);
			Point2D p2 = new Point2D.Double((b2.getWidth() + borderPad) * scale, (b2.getHeight() + borderPad) * scale);
			viewTransform.transform(p1, p1);
			if (p1.getX() < 0) {
				setMoveX(getMoveX() - p1.getX() / scale);
				p1.setLocation(0, p1.getY());
			}
			if (p1.getY() < 0) {
				setMoveY(getMoveY() - p1.getY() / scale);
				p1.setLocation(p1.getX(), 0);
			}
			updateSize();
			scrollRectToVisible(new Rectangle((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY()));
		}
		this.repaint();
	}

	public ADTreeNode getParentNode(ADTreeNode node) {
		return tree.getParent(node);
	}

	public ADTreeNode getRoot(boolean ignoreFold) {
		return tree.getRoot(ignoreFold);
	}

	public double getScale() {
		return this.scale;
	}

	public double getSizeX() {
		return this.sizeX;
	}

	public double getSizeY() {
		return this.sizeY;
	}

	public JScrollPane getScrollPane() {
		return this.scrollPane;
	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		this.scrollPane.addMouseWheelListener(listener);
		this.scrollPane.addComponentListener(listener);
		viewPortSize = this.scrollPane.getViewport().getExtentSize();
		setScale(1);
	}

	public ADTreeNode getFocused() {
		return this.focused;
	}

	public ADTreeNode getLastFocused() {
		return this.lastFocused;
	}

	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}

	public void setViewPortSize(Dimension viewPortSize) {
		this.viewPortSize = viewPortSize;
		// make up for dissapearing scrollbars
		if (scrollPane.getHorizontalScrollBar().isVisible()) {
			this.viewPortSize.height += scrollPane.getHorizontalScrollBar().getPreferredSize().height;
		}
		if (scrollPane.getVerticalScrollBar().isVisible()) {
			this.viewPortSize.width += scrollPane.getVerticalScrollBar().getPreferredSize().width;
		}
		updateSize();
	}

	public MainWindow getMainWindow() {
		return this.mainWindow;
	}

	public String[] getLabelLines(ADTreeNode node) {
		return getLabel(node).split("\n");
	}

	public String getLabel(ADTreeNode node) {
		return node.getLabel();
	}

	public void treeFromTerms(ADTNode root) {
		tree.createFromTerms(root);
		tree.defocusAll();
	}

	protected void paintEdges(final Graphics2D g2, final ADTreeNode parent) {
		if (!tree.isLeaf(parent)) {
			final Rectangle2D.Double b1 = bufferedLayout.get(parent);
			final double x1 = b1.getCenterX();
			final double y1 = b1.getCenterY();
			double maxX = 0;
			double minX = x1;
			int noChildren = 0;
			g2.setColor(Choices.canv_EdgesColor);
			Rectangle2D.Double b2 = new Rectangle2D.Double(0, 0, 0, 0);
			for (ADTreeNode child : tree.getChildrenList(parent, false)) {
				b2 = bufferedLayout.get(child);
				if (child.getType() != parent.getType()) {
					g2.setStroke(counterStroke);
				} else {
					g2.setStroke(basicStroke);
				}

				if (child.getType() == parent.getType()) {
					maxX = Math.max(maxX, b2.getCenterX());
					minX = Math.min(minX, b2.getCenterX());
					noChildren++;
				}
				g2.drawLine((int) x1, (int) y1, (int) b2.getCenterX(), (int) b2.getCenterY());
			}
			g2.setStroke(basicStroke);
			if (parent.getRefinmentType() == ADTreeNode.RefinementType.CONJUNCTIVE && noChildren > 1) {
				double tangens1 = (b2.getCenterY() - y1) / (minX - x1);
				double tangens2 = (b2.getCenterY() - y1) / (maxX - x1);
				double shear = (b1.getWidth() + Choices.canv_ArcPadding) / (b1.getHeight() + Choices.canv_ArcPadding);
				int startAngle = -(int) Math.toDegrees(Math.atan(tangens2 * shear));
				if (startAngle > 0) {
					startAngle = startAngle - 180;
				}
				int endAngle = -180 - (int) Math.toDegrees(Math.atan(tangens1 * shear));
				g2.drawArc((int) (int) b1.getX() - Choices.canv_ArcPadding / 2,
						(int) b1.getY() - Choices.canv_ArcPadding / 2, (int) b1.getWidth() + Choices.canv_ArcPadding,
						(int) b1.getHeight() + Choices.canv_ArcPadding, startAngle - 1, endAngle - (startAngle - 2));
			}
			for (ADTreeNode child : tree.getChildrenList(parent, false)) {
				paintEdges(g2, child);
			}
		}
		g2.setStroke(basicStroke);
	}

	protected void paintFocus(final Graphics2D g2, final ADTreeNode node) {
		final Rectangle2D.Double box = bufferedLayout.get(node);
		if (box == null) {
			return;
		}
		int x = (int) box.x - focusPad / 2;
		int y = (int) box.y - focusPad / 2;
		g2.setColor(Choices.focusColor);
		g2.setStroke(selectionStroke);
		Choices.ShapeType shape;
		if (node.getType() == Choices.canv_Defender) {
			shape = Choices.canv_ShapeDef;
		} else {
			shape = Choices.canv_ShapeAtt;
		}
		switch (shape) {
		case RECTANGLE:
			g2.drawRect(x, y, (int) box.width + focusPad - 1, (int) box.height + focusPad - 1);
			break;
		case OVAL:
			g2.drawOval(x, y, (int) box.width + focusPad - 1, (int) box.height + focusPad - 1);
			break;
		case ROUNDRECT:
		default:
			g2.drawRoundRect(x, y, (int) box.width + focusPad - 1, (int) box.height + focusPad - 1,
					Choices.canv_ArcSize + 1, Choices.canv_ArcSize + 1);
			break;
		}
	}

	protected void paintLabels(final Graphics2D g, final ADTreeNode node, final Color textCol) {
		final Rectangle2D.Double box = bufferedLayout.get(node);
		int x = (int) box.x;
		int y = (int) box.y;
		// draw the text on top of the box (possibly multiple lines)
		final String[] lines = getLabelLines(node);
		final FontMetrics m = getFontMetrics(Choices.canv_Font);
		// center vertically
		y = (int) (y + box.height / 2 - (lines.length * m.getHeight()) / 2) - 1;
		// center horizontally
		x = x + (int) (box.width / 2);
		// draw strings
		y += m.getAscent() + m.getLeading() + 1;
		g.setColor(textCol);
		g.setFont(Choices.canv_Font);
		for (int i = 0; i < lines.length; i++) {
			g.drawString(lines[i], x - (m.stringWidth(lines[i])) / 2, y);
			y += m.getHeight();
		}
	}

	protected Color getFillColor(ADTreeNode node) {
		if (node.getType() == ADTreeNode.Type.PROPONENT) {
			return Choices.canv_FillColorDef;
		} else {
			return Choices.canv_FillColorAtt;
		}
	}

	protected String getFillColorS(ADTreeNode node) {
		if (node.getType() == ADTreeNode.Type.PROPONENT) {
			return "attFill";
		} else {
			return "defFill";
		}
	}

	protected void paintBox(final Graphics2D g, final ADTreeNode node) {
		// draw the box in the background
		Choices.ShapeType shape;
		Color fillCol;
		Color borderCol;
		Color textCol;
		fillCol = getFillColor(node);
		if (node.getType() == Choices.canv_Defender) {
			borderCol = Choices.canv_BorderColorDef;
			textCol = Choices.canv_TextColorDef;
			shape = Choices.canv_ShapeDef;

		} else {
			// ATTACKER type
			borderCol = Choices.canv_BorderColorAtt;
			textCol = Choices.canv_TextColorAtt;
			shape = Choices.canv_ShapeAtt;
		}
		// get position of node
		final Rectangle2D.Double box = bufferedLayout.get(node);
		int x = (int) box.x;
		int y = (int) box.y;
		g.setColor(Choices.canv_EdgesColor);
		if (node.isFolded()) {
			Polygon triangle = new Polygon();
			triangle.addPoint(x + (int) (box.width / 2.0), y);
			triangle.addPoint(x, y + (int) box.height + 6);
			triangle.addPoint(x + (int) box.width, y + (int) box.height + 6);
			g.fillPolygon(triangle);
		}
		if (node.isAboveFolded()) {
			Polygon triangle = new Polygon();
			triangle.addPoint(x + (int) (box.width / 2.0), y - 10);
			triangle.addPoint(x + (int) (box.width / 2.0) - 6, y);
			triangle.addPoint(x + (int) (box.width / 2.0) + 6, y);
			g.fillPolygon(triangle);
		}
		g.setColor(fillCol);
		g.setStroke(basicStroke);
		switch (shape) {
		case RECTANGLE:
			g.fillRect(x, y, (int) box.width - 1, (int) box.height - 1);
			g.setColor(borderCol);
			g.drawRect(x, y, (int) box.width - 1, (int) box.height - 1);
			break;
		case OVAL:
			g.fillOval(x, y, (int) box.width - 1, (int) box.height - 1);
			g.setColor(borderCol);
			g.drawOval(x, y, (int) box.width - 1, (int) box.height - 1);
			break;
		case ROUNDRECT:
		default:
			g.fillRoundRect(x, y, (int) box.width - 1, (int) box.height - 1, Choices.canv_ArcSize,
					Choices.canv_ArcSize);
			g.setColor(borderCol);
			g.drawRoundRect(x, y, (int) box.width - 1, (int) box.height - 1, Choices.canv_ArcSize,
					Choices.canv_ArcSize);
			break;
		}
		paintLabels(g, node, textCol);
		for (ADTreeNode child : tree.getChildrenList(node, false)) {
			paintBox(g, child);
		}
	}

	public void toggleFold(ADTreeNode node) {
		if (getTree().getChildrenList(node, true).size() > 0 && !node.isFolded()) {
			getTree().toggleFold(node);
		} else if (getTree().getChildrenList(node, false).size() == 0 && node.isFolded()) {
			getTree().toggleFold(node);
		}
	}

	private Point getColsRows(double pageHeight, double pageWidth, int noPages) {
		double ratio = (pageWidth / pageHeight) / (sizeY / sizeX);
		int rows = 1;
		int cols = 1;
		while (true) {
			if (ratio * ((double) rows / cols) > 1) {
				cols++;
				if (cols * rows > noPages) {
					cols--;
					return new Point((int) cols, (int) rows);
				}
			} else {
				rows++;
				if (cols * rows > noPages) {
					rows--;
					return new Point((int) cols, (int) rows);
				}
			}
		}
	}

	public int getId() {
		return this.id;
	}

	public int getClear() {
		return Clear;
	}

	public void setClear(int clear) {
		Clear = clear;
	}

}
