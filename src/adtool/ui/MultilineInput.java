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

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class MultilineInput extends JOptionPane {

    private static final long serialVersionUID = -3749502278496747805L;

    public static String showInputDialog(final String message, final String title, final String content) {
        String data = null;
        class GetData extends JDialog implements ActionListener {
            JTextArea ta = new JTextArea(5, 10);
            JButton btnOK = new JButton("   OK   ");
            JButton btnCancel = new JButton("Cancel");
            String str = null;

            public GetData() {
                ta.setText(content);
                ta.addKeyListener(new KeyListener() {
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_ENTER:
                                if (e.isControlDown()) {
                                    btnOK.doClick();
                                }
                                break;
                            case KeyEvent.VK_ESCAPE:
                                btnCancel.doClick();
                                break;
                            case KeyEvent.VK_O:
                                if (e.isAltDown()) {
                                    btnOK.doClick();
                                }
                            case KeyEvent.VK_C:
                                if (e.isAltDown()) {
                                    btnCancel.doClick();
                                }
                            default:
                        }
                    }

                    public void keyTyped(KeyEvent e) {
                    }

                    public void keyReleased(KeyEvent e) {
                    }
                });
                setTitle(title);
                setModal(true);
                getContentPane().setLayout(new BorderLayout());
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                setLocation(200, 200);

                JPanel mainPane = new JPanel();
                Border paneEdge = BorderFactory.createEmptyBorder(10, 10, 10, 10);
                mainPane.setBorder(paneEdge);
                mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
                JLabel name = new JLabel(message);
                name.setAlignmentX(0.5f);
                name.setAlignmentY(0f);
                mainPane.add(name);
                mainPane.add(Box.createRigidArea(new Dimension(0, 10)));

                JScrollPane scroller = new JScrollPane(ta);
                scroller.setPreferredSize(new Dimension(200, 100));
                mainPane.add(scroller);
                mainPane.add(Box.createRigidArea(new Dimension(0, 10)));

                JPanel jp = new JPanel();
                btnOK.addActionListener(this);
                btnCancel.addActionListener(this);
                jp.add(btnOK);
                jp.add(btnCancel);
                mainPane.add(jp);
                getContentPane().add(mainPane, BorderLayout.CENTER);
                pack();
                setVisible(true);
            }

            public void actionPerformed(ActionEvent ae) {
                if (ae.getSource() == btnOK)
                    str = ta.getText();
                dispose();
            }

            public String getData() {
                return str;
            }
        }
        data = new GetData().getData();
        return data;
    }
}