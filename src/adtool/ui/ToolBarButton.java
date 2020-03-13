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

import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ToolBarButton extends JButton {
	private static final Insets margins = new Insets(0, 0, 0, 0);

	public ToolBarButton(JButton button) {
		super(button.getText(), button.getIcon());
		setMargin(margins);
		setVerticalTextPosition(BOTTOM);
		setHorizontalTextPosition(CENTER);
	}

	public ToolBarButton(Icon icon) {
		super(icon);
		setMargin(margins);
		setVerticalTextPosition(BOTTOM);
		setHorizontalTextPosition(CENTER);
	}

	public ToolBarButton(String imageFile) {
		this(new ImageIcon(imageFile));
	}

	public ToolBarButton(String imageFile, String text) {
		this(new ImageIcon(imageFile));
		setText(text);
	}
}
