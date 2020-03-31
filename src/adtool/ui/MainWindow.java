package adtool.ui;

import adtool.Choices;
import adtool.adtree.ADTXmlImport;
import adtool.adtree.ADTreeForGui;
import adtool.adtree.ADTreeNode;
import adtool.domains.Domain;
import adtool.domains.DomainFactory;
import adtool.domains.ValuationDomain;
import adtool.domains.predefined.MinCost;
import adtool.domains.rings.RealG0;
import adtool.domains.rings.Ring;
import adtool.mincost.ATNode;
import adtool.mincost.SetWindow;
import adtool.mincost.Test;
import adtool.ui.texts.ButtonTexts;
import net.infonode.docking.*;
import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.LookAndFeelDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class MainWindow extends Frame {
	private MouseHandler mouseHandler;
	private static ADAction fileExportToPdf;
	private static ADAction fileExportToPng;
	private static ADAction fileExportToXml;
	private static ADAction fileImportFromXml;
	private static ADAction fileExit;
	private StatusLine status;
	private HashMap<Integer, ValuationDomain<Ring>> valuations = new HashMap<Integer, ValuationDomain<Ring>>();
	private FileHandler fh;
	private RootWindow rootWindow;
	private JFrame frame = new JFrame("ADTree Defense-cost Calculate Tool");
	private View[] views = new View[4];
	
	private HashMap<String, ATNode> labels = new HashMap<>();
	private HashMap<String,Ring>  denfCost=new HashMap<>();
	private HashMap<String,Double>  minCost=new HashMap<>();
	private String minvalues;
	/**
	 * Contains the dynamic views that has been added to the root window.
	 */
	private HashMap<Integer, DynamicView> dynamicViews = new HashMap<Integer, DynamicView>();

	/**
	 * Contains all the static views.
	 */
	private ViewMap viewMap = new ViewMap();

	/**
	 * The windows menu items.
	 */
	private JMenuItem[] windowsItems = new JMenuItem[views.length];
	/**
	 * The Attribute Domains menu.
	 */
	private JMenu attrDomainsMenu;
	/**
	 * The currently applied docking windows theme
	 */
	private DockingWindowsTheme currentTheme = new LookAndFeelDockingTheme();
	private ADTreeCanvas lastFocused;
	/**
	 * In this properties object the modified property values for close buttons
	 * etc. are stored. This object is cleared when the theme is changed.
	 */
	private RootWindowProperties properties = new RootWindowProperties();

	public ValuationDomain<Ring> getValuation(int id) {
		return valuations.get(new Integer(id));
	}

	/**
	 * Return list of DomainValuations used in main window
	 *
	 */
	public HashMap<Integer, ValuationDomain<Ring>> getValuations() {
		return valuations;
	}

	/**
	 * Sets the lastFocused for this instance.
	 *
	 * @param lastFocused
	 *            The lastFocused.
	 */
	public void setLastFocused(ADTreeCanvas lastFocused) {
		this.lastFocused = lastFocused;
		if (lastFocused == null || lastFocused instanceof DomainCanvas) {
			((ValuationView) views[2].getComponent()).assignCanvas(lastFocused);
			((DetailsView) views[3].getComponent()).assignCanvas(lastFocused);
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeDomain(int i) {
		DynamicView view = dynamicViews.get(new Integer(i));
		Component c = view.getComponent();
		if (c instanceof DomainView) {
			if (lastFocused instanceof DomainCanvas && ((DomainCanvas) lastFocused).getId() == i) {
				setLastFocused(null);
			}
			((DomainView<?>) c).onClose();
			view.close();
			getRootWindow().removeView(view);
		}
		valuations.remove(new Integer(i));
		dynamicViews.remove(new Integer(i));
		createAttrDomainMenu();
	}

	public void removeDomains() {
		if (lastFocused instanceof DomainCanvas) {
			setLastFocused(null);
		}
		Collection<DynamicView> dynViews = dynamicViews.values();
		Vector<DynamicView> set = new Vector<DynamicView>(dynViews);
		for (DynamicView view : set) {
			Component c = view.getComponent();
			if (c instanceof DomainView) {
				((DomainView<?>) c).onClose();
				view.close();
				getRootWindow().removeView(view);
			}
		}
		valuations.clear();
		dynamicViews.clear();
		createAttrDomainMenu();
	}

	/**
	 * Gets the views for this instance.
	 *
	 * @return The views.
	 */
	public View[] getViews() {
		return this.views;
	}

	/**
	 * Gets the root window for this instance.
	 *
	 * @return The rootWindow
	 */
	public RootWindow getRootWindow() {
		return this.rootWindow;
	}

	/**
	 * @return JLabel that holds the text of a status bar.
	 */
	public StatusLine getStatusBar() {
		return status;
	}

	/**
	 * Gets the views for this instance.
	 *
	 * @param index
	 *            The index to get.
	 * @return The views.
	 */
	public View getViews(int index) {
		return this.views[index];
	}

	/**
	 * Gets the lastFocused for this instance.
	 *
	 * @return The lastFocused.
	 */
	public ADTreeCanvas getLastFocused() {
		return this.lastFocused;
	}

	/**
	 * A dynamically created view containing an id.
	 */
	public static class DynamicView extends View {
		static final long serialVersionUID = 4127190623311867764L;
		private int id;

		DynamicView(String title, Icon icon, Component component, int id) {
			super(title, icon, component);
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
	}

	@SuppressWarnings("rawtypes")
	public void updateDynamicViewTitles() {
		Collection<DynamicView> dynViews = dynamicViews.values();
		Vector<DynamicView> set = new Vector<DynamicView>(dynViews);
		int i = 0;
		for (DynamicView view : set) {
			i++;
			DomainView dv = (DomainView) view.getComponent();
			view.getViewProperties().setTitle(i + ". " + (dv.getCanvas().getDomain().getName()));
		}
	}

	public MainWindow(final String args[]) {
		createActions();
		createRootWindow();
		restoreDefaultLayout();
		showFrame();
	}

	private StatusLine createStatusBar() {
		StatusLine newStatus = new StatusLine();
		newStatus.setBorder(BorderFactory.createEtchedBorder());
		return newStatus;
	}

	private void updateFloatingWindow(FloatingWindow fw) {

	}

	private int getDynamicViewId() {
		int id = 0;

		while (dynamicViews.containsKey(new Integer(id)))
			id++;

		return id;
	}

	private void createRootWindow() {
		ADTreeForGui tree = new ADTreeForGui(new ADTreeNode());
		ADTreeView treeView = new ADTreeView(tree, this);
		views[0] = new View("查看攻击树", new ImageIcon(this.getClass().getResource("/icons/trees.png")), treeView);
		treeView.getCanvas().setClear(0);
		ADTreeCanvas canvas = ((ADTreeView) views[0].getComponent()).getCanvas();
		views[1] = new View("ADTerm Edit", new ImageIcon(this.getClass().getResource("/icons/viewTree_16x16.png")),
				new ADTermView(canvas));
		views[2] = new View("查看详细值", new ImageIcon(this.getClass().getResource("/icons/tables.png")),
				new ValuationView());
		views[3] = new View("查看计算方法", new ImageIcon(this.getClass().getResource("/icons/eyes.png")), new DetailsView());
		for (int i = 0; i < views.length; i++) {
			viewMap.addView(i, views[i]);
		}
		MixedViewHandler handler = new MixedViewHandler(viewMap, new ViewSerializer() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void writeView(View view, ObjectOutputStream out) throws IOException {
				int id = ((DynamicView) view).getId();
				out.writeInt(id);
				if (view.getComponent() instanceof DomainView) {
					Domain d = ((DomainCanvas) ((DomainView) view.getComponent()).getCanvas()).getDomain();
					out.writeObject(DomainFactory.updateDomainName(DomainFactory.getClassName(d)));
				}
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public View readView(ObjectInputStream in) throws IOException {
				int id = in.readInt();
				try {
					if (Choices.currentSaveVer == 1) {
						String domainName = DomainFactory.updateDomainName((String) in.readObject());
						ValuationDomain<Ring> vd = new ValuationDomain(DomainFactory.createFromString(domainName));
						valuations.put(new Integer(id), vd);
						View view = getDynamicView(id);
						return view;
					} else {
						if (Choices.currentSaveVer == -1) {
							if (valuations.get(new Integer(id)) != null) {
								View view = getDynamicView(id);
								return view;
							}
						}
					}
				} catch (ClassNotFoundException e) {
					return null;
				}
				return null;
			}
		});
		rootWindow = DockingUtil.createRootWindow(viewMap, handler, true);
		properties.addSuperObject(currentTheme.getRootWindowProperties());
		rootWindow.getRootWindowProperties().addSuperObject(properties);
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		rootWindow.addListener(new DockingWindowAdapter() {
			public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
				if (addedWindow instanceof FloatingWindow) {
					updateFloatingWindow((FloatingWindow) addedWindow);
				} else {
					updateWindowsMenu();
				}
			}

			public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
				updateWindowsMenu();
			}

			public void windowClosing(DockingWindow window) throws OperationAbortedException {

			}

			public void windowUndocking(DockingWindow window) throws OperationAbortedException {

			}

			@SuppressWarnings("rawtypes")
			public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
				if (focusedView != null && focusedView.getComponent() != null) {
					Component c = focusedView.getComponent();
					if (c instanceof ADTreeView) {
						setLastFocused(((ADTreeView) c).getCanvas());
					} else if (c instanceof DomainView) {
						setLastFocused(((DomainView) c).getCanvas());
					} else if (!(c instanceof ValuationView || c instanceof DetailsView)) {
						setLastFocused(null);
					}
				} else if (focusedView != null) {
					setLastFocused(null);
				}
			}
		});
		rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
	}

	private void createActions() {
		fileExportToPdf = new ADAction("PDF") {
			private static final long serialVersionUID = 4325025687838671271L;

			public void actionPerformed(final ActionEvent e) {
				exportTo("pdf");
			}
		};
		fileExportToPdf.setMnemonic(KeyEvent.VK_P);
		fileExportToPdf.setSmallIcon(new ImageIcon(getClass().getResource("/icons/pdf.png")));
		fileExportToPng = new ADAction("PNG image") {
			public void actionPerformed(final ActionEvent e) {
				exportTo("png");
			}
		};
		fileExportToPng.setSmallIcon(new ImageIcon(getClass().getResource("/icons/png.png")));
		fileExportToPng.setMnemonic(KeyEvent.VK_N);

		fileExportToXml = new ADAction("XML") {
			private static final long serialVersionUID = 2724287904022882599L;

			public void actionPerformed(final ActionEvent e) {
				exportTo("xml");
			}
		};
		fileExportToXml.setSmallIcon(new ImageIcon(getClass().getResource("/icons/xml.png")));
		fileExportToXml.setMnemonic(KeyEvent.VK_X);

		fileImportFromXml = new ADAction("XML") {
			private static final long serialVersionUID = -3605440604743377670L;

			public void actionPerformed(final ActionEvent e) {
				importFrom("xml");
				((ADTreeView) views[0].getComponent()).getCanvas().setClear(1);
			}
		};
		fileImportFromXml.setSmallIcon(new ImageIcon(getClass().getResource("/icons/xml.png")));
		fileImportFromXml.setMnemonic(KeyEvent.VK_X);
		fileExit = new ADAction(ButtonTexts.EXIT) {
			private static final long serialVersionUID = -6586817922511469697L;

			public void actionPerformed(final ActionEvent e) {
				WindowEvent windowClosing = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
				frame.dispatchEvent(windowClosing);
			}
		};
		fileExit.setMnemonic(KeyEvent.VK_X);
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		fileExit.setSmallIcon(new ImageIcon(getClass().getResource("/icons/exit.png")));
	}

	private void importFrom(String type) {
		FileInputStream in = fh.getImportTreeStream(type);
		if (in != null) {
			ADTXmlImport importer = new ADTXmlImport(this);
			importer.importFrom(in);
		}
	}

	private void exportTo(String type) {
		ADTreeCanvas canvas = ((ADTreeView) views[0].getComponent()).getCanvas();
		if (lastFocused != null) {
			canvas = lastFocused;
		}
		ADTreeNode tempFocus = canvas.getFocused();
		canvas.setFocus(null);
		FileOutputStream out = fh.getExportTreeStream(type);
		if (out != null) {
			if (type.equals("pdf")) {
				canvas.createPdf(out);
			} else if (type.equals("png")) {
				canvas.createImage(out, type);
			} else if (type.equals("xml")) {
				canvas.createXml(out);
			}
		}
		canvas.setFocus(tempFocus);
	}

	private void restoreDefaultLayout() {
		TabWindow tabWindow = new TabWindow(new DockingWindow[] { views[0] });
		tabWindow.add(views[0]);
		rootWindow.setWindow(tabWindow);
		for (View v : dynamicViews.values()) {
			addDomainWindow(v);
		}
	}

	private void showFrame() {
		status = createStatusBar();
		if (isRunningJavaWebStart()) {
			fh = new JWSFileHandler(status, this);
		} else {
			fh = new FileHandler(status, this);
		}
		mouseHandler = new MouseHandler();
		frame.getContentPane().add(getRootWindow(), BorderLayout.CENTER);
		frame.getContentPane().add(status, BorderLayout.SOUTH);
		frame.setJMenuBar(createMenuBar());
		Dimension dim = getScreenSize(1, 1);
		frame.setSize(dim.width, dim.height);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/icons/tree.png")));
		frame.addWindowListener(new WindowAdapter() {
			public final void windowClosing(final WindowEvent e) {
				final JFrame localFrame = (JFrame) e.getSource();

				final int result = JOptionPane.showConfirmDialog(localFrame, "确定要退出应用吗?", "退出应用",
						JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
			}
		});
		frame.setVisible(true);
	}

	// Creates the frame menu bar.
	private JMenuBar createMenuBar() {
		final JMenuBar menu = new JMenuBar();
		menu.add(createFileMenu());
		attrDomainsMenu = new JMenu("DefenseCost");
		attrDomainsMenu.setMnemonic(KeyEvent.VK_D);
		menu.add(attrDomainsMenu);
		createAttrDomainMenu();
		return menu;
	}

	// Create Attribute Domains menu
	public void createAttrDomainMenu() {
		JMenu menu = attrDomainsMenu;
		menu.removeAll();
		JMenuItem menuItem = new JMenuItem("Add defense-cost attribute");
		menuItem.setIcon(new ImageIcon(getClass().getResource("/icons/attr.png")));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//chooseAttributeDomain();
				Domain<?> domain=new MinCost();
				 addAttributeDomain(domain);
			}
		});
		menuItem.setMnemonic(KeyEvent.VK_A);
		menu.add(menuItem);
		if (valuations.size() > 0) {
			menu.addSeparator();
			for (Integer i : valuations.keySet()) {
				final int j = i.intValue();
				final View view = getDynamicView(j);
				menuItem = new JMenuItem(view.getTitle(), view.getIcon());
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (view.getRootWindow() != null) {
							view.restoreFocus();
						} else {
							addDomainWindow(view);
						}
					}
				});
				menu.add(menuItem);
			}
		}
		JMenuItem cutset=new JMenuItem("Minimal defense-cost");
		cutset.setIcon(new ImageIcon(getClass().getResource("/icons/compute.png")));
		cutset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				ATNode root=createATree();
				Test test=new Test();
				test.setRoot(root);
				test.init_randomLabels();
				test.changeLabeltoCalculate(root);

				long starttime=System.currentTimeMillis();
				long endtime=System.currentTimeMillis();

				starttime=System.currentTimeMillis();
				//正常树求解
				test.getDefCost(root);
				HashSet<ATNode> vector=new HashSet<ATNode>();
				vector.add(root);
    			test.getExp(root.getLabel(), vector);
				test.show();
    			endtime=System.currentTimeMillis();
				System.out.println("正常树："+(endtime-starttime)+"ms");

				starttime=System.currentTimeMillis();


				//原子树求解
				ATNode newRoot=new ATNode();
				newRoot.setLabel("n_"+ATNode.getNums());
				newRoot.getChildren().add(root);
				test.equalTree(newRoot);
				test.getDefCost(newRoot);

				test.removeNnode(newRoot);

				test.atomtree(test.getLogExp(newRoot));
				endtime=System.currentTimeMillis();
				String rString=test.showatomtree();
				System.out.println("原子树求割集："+(endtime-starttime)+"ms");
		    	SetWindow setwindow=new SetWindow();
		    	setwindow.initUI();
		    	setwindow.setPanel(rString);
			}
		});
		menu.add(cutset);
	}

	public void addDomainWindow(DockingWindow window) {
		TabWindow tabWindow = new TabWindow(new DockingWindow[] { window });
		tabWindow.add(window);
		rootWindow.setWindow(tabWindow);
	}


	public HashMap<Integer, DynamicView> getDynamicViews() {
		return dynamicViews;
	}

	
	public Dimension getScreenSize(double scaleY, double scaleX) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		DisplayMode dm = gs[0].getDisplayMode();
		return new Dimension((int) (dm.getWidth() * scaleX), (int) (dm.getHeight() * scaleY));
	}

	/**
	 * Adds new attribute domain.
	 *
	 * @param d
	 *            attribute domain to be added.
	 */
	@SuppressWarnings("rawtypes")
	public View addAttributeDomain(Domain<?> d) {
		@SuppressWarnings("unchecked")
		ValuationDomain<Ring> vd = new ValuationDomain(d);
		int id = getDynamicViewId();
		valuations.put(new Integer(id), vd);
		View view = getDynamicView(id);
		addDomainWindow(view);
		setLastFocused(((DomainView) view.getComponent()).getCanvas());
		status.report("Added a new domain: " + d.getName());
		return view;
	}

	public void closeDynamicViews() {
		for (View v : dynamicViews.values()) {
			v.close();
		}
	}

	@SuppressWarnings("rawtypes")
	public View getDynamicView(int id) {
		View view = dynamicViews.get(new Integer(id));
		if (view == null) {
			if (getValuation(id) != null) {
				ADTreeForGui tree = ((ADTreeView) views[0].getComponent()).getTree();
				DomainCanvas canvas = new DomainCanvas(tree, this, id);
				DomainView dv = new DomainView(this, canvas, id);
				view = new DynamicView((1 + id) + ". " + canvas.getDomain().getName(), dv.getIcon(), dv, id);
				dynamicViews.put(new Integer(id), (DynamicView) view);
			} else {
				status.reportError("Dynamic View with id " + id + " has no associated valuations");
				return null;
			}
		} else {
		}
		return view;
	}

	private void updateWindowsMenu() {
		for (int i = 0; i < windowsItems.length; i++) {
			if (windowsItems[i] != null) {
				windowsItems[i].setEnabled(views[i].getRootWindow() == null);
			}
		}
	}

	private JMenu createFileMenu() {
		JMenuItem menuItem;
		final JMenu fileMenu = new JMenu();
		fileMenu.setText(ButtonTexts.FILE);
		fileMenu.setMnemonic('F');

		JMenu importFrom = new JMenu("Open");
		importFrom.setIcon(new ImageIcon(getClass().getResource("/icons/open.png")));
		importFrom.setMnemonic(KeyEvent.VK_I);
		menuItem = importFrom.add(fileImportFromXml);
		menuItem.addMouseListener(mouseHandler);
		importFrom.add(menuItem);
		fileMenu.add(importFrom);

		JMenu exportTo = new JMenu("Save");
		exportTo.setIcon(new ImageIcon(getClass().getResource("/icons/save.png")));
		exportTo.setMnemonic(KeyEvent.VK_E);
		menuItem = exportTo.add(fileExportToPdf);
		menuItem.addMouseListener(mouseHandler);
		exportTo.add(menuItem);
		menuItem = exportTo.add(fileExportToPng);
		menuItem.addMouseListener(mouseHandler);
		exportTo.add(menuItem);
		menuItem = exportTo.add(fileExportToXml);
		menuItem.addMouseListener(mouseHandler);
		exportTo.add(menuItem);
		fileMenu.add(exportTo);

		menuItem = fileMenu.add(fileExit);
		menuItem.addMouseListener(mouseHandler);
		fileMenu.add(menuItem);
		return fileMenu;
	}

	/**
	 * ADAction represents an action that is used in application.
	 */
	public abstract class ADAction extends AbstractAction {

		private static final long serialVersionUID = 8109471079193338016L;

		public ADAction(final String text) {
			super(text);
		}

		public final void setAccelerator(final KeyStroke accelerator) {
			putValue(ACCELERATOR_KEY, accelerator);
		}

		public final void setSmallIcon(final Icon icon) {
			putValue(SMALL_ICON, icon);
		}

		public final void setToolTip(final String text) {
			putValue(SHORT_DESCRIPTION, text);
		}

		public final void setDescription(final String text) {
			putValue(LONG_DESCRIPTION, text);
		}

		public final void setMnemonic(final Integer mnemonic) {
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public abstract void actionPerformed(final ActionEvent e);
	}

	public JFrame getFrame() {
		return frame;
	}

	/**
	 * This adapter is constructed to handle mouse over component events.
	 */
	private class MouseHandler extends MouseAdapter {
		public MouseHandler() {
		}

		public void mouseEntered(MouseEvent evt) {
		}
	}

	private boolean isRunningJavaWebStart() {
		return System.getProperty("javawebstart.version", null) != null;
	}
	
	
	
	public ATNode reverseToATree(ADTreeForGui tree,ADTreeNode root,ATNode newroot){	
        newroot.setADTreeNode(root);
        String key = UUID.randomUUID().toString();
		labels.put(key, newroot);
        List<ADTreeNode> children=tree.getChildrenList(root);
    	boolean flag=false;
    	if(children.isEmpty()) {
    		newroot.setMincost(Double.POSITIVE_INFINITY);
    		denfCost.put(newroot.getLabel().toLowerCase(),new RealG0());
    	}
        for(ADTreeNode child:children){
        	if(!flag) {
        		newroot.setMincost(Double.POSITIVE_INFINITY);
        		denfCost.put(newroot.getLabel().toLowerCase(),new RealG0());
        	}
        	if(child.getType()==ADTreeNode.Type.OPPONENT){
        		flag=true;
        		newroot.setMincost(((RealG0)this.getValuation(0).getValue(child)).getValue());
        		denfCost.put(newroot.getLabel().toLowerCase(),this.getValuation(0).getValue(child));
        		//System.out.println(newroot.getLabel()+" "+denfCost.get(newroot));
    			 continue;
    		}
        	ATNode newchild=new ATNode();
        	newchild.setADTreeNode(child);
        	newroot.addChild(newchild);
        	newchild.setParent(newroot);
        	reverseToATree(tree,child,newchild);
        }
        return newroot;
    }
	
	public ATNode createATree(){
		ADTreeCanvas canvas = ((ADTreeView) views[0].getComponent()).getCanvas();
		ADTreeForGui tree=canvas.getTree();
        ATNode newroot=new ATNode();
		return reverseToATree(tree,tree.getRoot(),newroot);
	}
}
