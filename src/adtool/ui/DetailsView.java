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

import adtool.domains.rings.Ring;

import javax.swing.*;
import java.awt.*;

public class DetailsView extends JPanel {
	static final long serialVersionUID = 16648641466469654L;
	private JLabel text;

	// private DomainCanvas<Ring> canvas;

	public DetailsView() {
		super(new BorderLayout());
		// canvas = null;
		text = new JLabel("δѡ��.") {

			private static final long serialVersionUID = -6129269462785233124L;

			public Dimension getPreferredSize() {
				return new Dimension(400, 300);
			}

			public Dimension getMinimumSize() {
				return new Dimension(400, 300);
			}

			public Dimension getMaximumSize() {
				return new Dimension(400, 300);
			}
		};
		text.setVerticalAlignment(SwingConstants.TOP);
		text.setFont(new Font("Sans", Font.TRUETYPE_FONT, 13));
		text.setHorizontalAlignment(SwingConstants.LEFT);

		JScrollPane descPane = new JScrollPane(text);
		descPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
				BorderFactory.createTitledBorder("���㷽��:")));
		descPane.setAutoscrolls(true);
		add(descPane);
	}

	@SuppressWarnings("unchecked")
	public void assignCanvas(ADTreeCanvas canvas) {
		if (canvas instanceof DomainCanvas) {
			text.setText(((DomainCanvas<Ring>) canvas).getDomain().getDescription());
			// this.canvas = (DomainCanvas<Ring>)canvas;
		} else {
			text.setText("No attribute domain chosen.");
			// this.canvas=null;
		}
	}
}
