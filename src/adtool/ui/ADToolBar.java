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

import java.awt.Component;
import java.awt.Insets;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;

public class ADToolBar extends JToolBar {
	private static final long serialVersionUID = 883897992310721L;

	public ADToolBar() {
		super();
	}

	ToolBarButton add(AbstractAction a) {
		final Insets margins = new Insets(0, 0, 0, 0);
		ToolBarButton button = new ToolBarButton(super.add(a));
		button.setMargin(margins);
		return button;
	}

	public final void setTextLabels(final boolean labelsAreEnabled) {
		Component c;
		int i = 0;
		while ((c = getComponentAtIndex(i++)) != null) {
			ToolBarButton button;
			try {
				button = (ToolBarButton) c;
			} catch (ClassCastException e) {
				continue;
			}
			if (labelsAreEnabled) {
				button.setText(button.getToolTipText());
			} else {
				button.setText(null);
			}
		}
	}
}
