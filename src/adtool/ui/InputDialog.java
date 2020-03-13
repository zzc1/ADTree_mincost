
package adtool.ui;

import adtool.domains.rings.Ring;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

// Dialog to edit values for the attribute domains. It varies depending on the type of attribute domain.

public abstract class InputDialog extends JDialog implements ActionListener, PropertyChangeListener, KeyListener {
	protected JLabel errorMsg;
	protected Ring value;
	protected JButton setButton;
	protected Container contentPane;
	protected JFormattedTextField valueField;
	protected boolean setPressed;

	public InputDialog(Frame frame, String title) {
		super(frame, title, true);
		setPressed = false;
		this.setLocationRelativeTo(frame);
		setAlwaysOnTop(true);
		setLocation(160, 160);
		setSize(800, 600);
		createCommonLayout();
	}
	// Handle clicks on the Set and Cancel buttons.

	public void actionPerformed(ActionEvent e) {
		if ("ȡ��".equals(e.getActionCommand())) {
			escPressed();
		} else if ("ȷ��".equals(e.getActionCommand())) {
			enterPressed();
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		// Ring source = e.getSource();
		if (!sync()) {
		}
	}

	public final Ring showInputDialog(final Ring defaultValue) {
		return showInputDialog(defaultValue, true);
	}

	public final Ring showInputDialog(final Ring defaultValue, boolean showDefault) {
		value = defaultValue;
		createLayout(showDefault);
		valueField.requestFocusInWindow();
		this.setVisible(true);
		if (setPressed) {
			return value;
		} else
			return null;
	}

	abstract protected void createLayout(final boolean showDefault);

	abstract protected boolean isValid(final double d);

	abstract protected void setValue(final double d);

	protected boolean sync() {
		final Number num = (Number) valueField.getValue();
		if (num == null) {
			return false;
		} else {
			final double d = num.doubleValue();
			if (!isValid(d)) {
				return false;
			} else {
				setValue(d);
			}
			return true;
		}
	}

	private void createCommonLayout() {
		errorMsg = new JLabel("");
		JButton cancelButton = new JButton("ȡ��");
		cancelButton.addActionListener(this);
		setButton = new JButton("ȷ��");
		setButton.setActionCommand("ȷ��");
		setButton.addActionListener(this);
		getRootPane().setDefaultButton(setButton);
		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		// buttonPane.add(errorMsg);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(setButton);
		contentPane = getContentPane();
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
	}

	protected JRootPane createRootPane() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				escPressed();
			}
		};
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// rootPane.registerKeyboardAction(new EnterListener(), strokeEnter,
		// JComponent.WHEN_IN_FOCUSED_WINDOW);

		return rootPane;
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 10) {
			enterPressed();
		}
		if (e.getKeyCode() == 27) {
			escPressed();
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void popup() {
		JOptionPane.showMessageDialog(this, errorMsg.getText(), "Wrong number format", JOptionPane.ERROR_MESSAGE);
	}

	public void escPressed() {
		value = null;
		setVisible(false);
	}

	public void enterPressed() {
		try {
			valueField.commitEdit();
			if (sync()) {
				setPressed = true;
				setVisible(false);
			} else {
				setPressed = false;
				popup();
			}
		} catch (ParseException e) {
			popup();
		}
	}
}
