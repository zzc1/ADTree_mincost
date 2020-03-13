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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;
// import java.awt.AlphaComposite;

public class StatusLine extends JLabel implements ActionListener {

	private static final long serialVersionUID = 4285019474424274296L;
	private static final Timer timer = new Timer(100, null);

	public StatusLine() {
		super("Ready");
		timer.setInitialDelay(10000);
		timer.addActionListener(this);
		timer.start();
	}

	public void report(final String s) {
		setText(s);
		timer.restart();
	}

	public void reportError(String message) {
		report("<html><font color='red'> Error: </font>" + message + "<html>");
	}

	public void reportWarning(String message) {
		report("<html><font color='orange'> Warning: </font>" + message + "<html>");
	}

	public void actionPerformed(ActionEvent e) {
		setText("");
	}
}
