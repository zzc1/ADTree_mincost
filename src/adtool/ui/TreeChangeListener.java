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

import adtool.adtree.ADTreeNode;

//The class should implement this interface if it want to be notified about changes to the atack defence tree structure.
public interface TreeChangeListener
{
  /**
   * Method called when tree has changed.
   * 
   */
  void treeChanged();
  /**
   * Method called when size of nodes have changed.
   * 
   */
  void sizeChanged();
  /**
   * Sets the focus.
   * 
   * @param node node which will have the focus.
   */
  void setFocus(ADTreeNode node);
}

