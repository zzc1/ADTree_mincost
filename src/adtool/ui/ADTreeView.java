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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import adtool.adtree.ADTreeForGui;

/**
 * A view containing a graphical representation of tree.
 * 
 * @author Piot Kordy
 * @version
 * 
 */
public class ADTreeView extends JPanel
{
  static final long                serialVersionUID = 70266501192502884L;
  private ADTreeForGui tree;
  private ADTreeCanvas canvas;

  public ADTreeView(final ADTreeForGui newTree, final MainWindow mainWindow)
  {
    super(new BorderLayout());
    tree = newTree;
    canvas = new ADTreeCanvas(tree,mainWindow,-1);
    final JScrollPane scrollPane = new JScrollPane(canvas);
    scrollPane.setAutoscrolls(true);
    canvas.setScrollPane(scrollPane);
    add(scrollPane);
  }

  public final ADTreeForGui getTree()
  {
    return this.tree;
  }

  public final ADTreeCanvas getCanvas()
  {
    return canvas;
  }

}

