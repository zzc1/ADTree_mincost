package adtool.mincost;

import javax.swing.*;
import java.awt.*;

public class SetWindow extends Frame {
	protected JFrame frame;
	protected String result;
	protected JTextArea jTextArea;

	public void initUI() {
		frame = new JFrame("Defense-Cost");
		Dimension dim = getScreenSize(0.5, 0.4);
		frame.setSize(dim.width, dim.height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(2);
		frame.setBackground(Color.WHITE);
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/icons/tree.png")));

		JScrollPane jScrollPane = new JScrollPane();
		frame.add(jScrollPane);
		jTextArea = new TextAreaMenu();
		jTextArea.setToolTipText("");
		jTextArea.setFont(new Font("����", Font.PLAIN, 30));
		jTextArea.setLineWrap(true);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane.setViewportView(jTextArea);

		jScrollPane.setPreferredSize(new Dimension(600, 500));
		;
		frame.setVisible(true);
	}

	public void setPanel(String result) {
		jTextArea.setText(result);
	}

	public Dimension getScreenSize(double scaleY, double scaleX) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		DisplayMode dm = gs[0].getDisplayMode();
		return new Dimension((int) (dm.getWidth() * scaleX), (int) (dm.getHeight() * scaleY));
	}

}