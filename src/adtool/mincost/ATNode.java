package adtool.mincost;

import adtool.adtree.ADTreeNode;

import java.util.Iterator;
import java.util.Vector;

public class ATNode {
	private static int nums = 0;
	// ���ӽڵ�
	protected Vector<ATNode> children;
	// ���ڵ�
	protected ATNode parent;
	// �ڵ��ǩ
	private String label;
	// ���ӷ�����
	private RefinementType refinementType;

	private double mincost;

	public double getMincost() {
		return mincost;
	}

	public void setMincost(double mincost) {
		this.mincost = mincost;
	}

	public enum RefinementType {
		DISJUNCTIVE, CONJUNCTIVE
	}

	/**
	 * Constructor.
	 * 
	 * @param id the id
	 */
	public ATNode() {
		nums++;
		this.mincost=Double.POSITIVE_INFINITY;
		children = new Vector<ATNode>();
	}

	public static int getNums() {
		return nums;
	}

	public static void setNums(int nums) {
		ATNode.nums = nums;
	}

	public ATNode getParent() {
		return parent;
	}

	public void setParent(ATNode parent) {
		this.parent = parent;
	}

	/**
	 * Gets the children for this instance.
	 *
	 * @return The children.
	 */
	public Vector<ATNode> getChildren() {
		return children;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public RefinementType getRefinementType() {
		return refinementType;
	}

	public void setRefinementType(RefinementType refinementType) {
		this.refinementType = refinementType;
	}

	public void setChildren(Vector<ATNode> children) {
		this.children = children;
	}

	public void addChild(ATNode n) {
		if (children == null) {
			children = new Vector<ATNode>();
		}
		children.add(n);
		n.setParent(this);
	}

	public final ATNode getChild(int i) {
		return children.elementAt(i);
	}

	public int getNumChildren() {
		return (children == null) ? 0 : children.size();
	}

	// ����չ������߼����ʽ
	public String getExp() {
		return getExp1().replace(" ", "+");

	}

	public String getExp1() {

		if (!children.isEmpty()) {
			if (refinementType == RefinementType.CONJUNCTIVE) {
				Iterator<ATNode> it = children.iterator();
				ATNode node1 = null;
				ATNode node2 = null;
				String s1 = null;
				String s2 = null;
				String temp = null;
				while (it.hasNext()) {
					StringBuffer sb = new StringBuffer();
					if (node1 == null) {
						node1 = (ATNode) it.next();
						s1 = node1.getExp1();
						if (it.hasNext()) {
							node2 = (ATNode) it.next();
							s2 = node2.getExp1();
						}
					} else {
						node1 = node2;
						s1 = temp;
						node2 = (ATNode) it.next();
						s2 = node2.getExp1();
					}
					String[] sa1 = s1.split(" ");
					if (!(s2 == null)) {
						String[] sa2 = s2.split(" ");
						for (int i = 0; i < sa1.length; i++) {
							for (int j = 0; j < sa2.length; j++) {
								sb.append(sa1[i] + "*" + sa2[j] + " ");
							}
						}
						temp = sb.substring(0, sb.length() - 1);
					} else
						temp = s1;
				}
				// System.out.println(temp);
				return temp;
			} else {
				Iterator<ATNode> it = children.iterator();
				StringBuffer sb = new StringBuffer();
				while (it.hasNext()) {
					ATNode node = (ATNode) it.next();
					sb.append(node.getExp1() + " ");
				}
				// System.out.println(sb.substring(0, sb.length() - 1));
				return sb.substring(0, sb.length() - 1);
			}
		}
		// System.out.println(this.label);
		return this.label;
	}

	// ������Ҷ�ӽ����ɵ��߼����ʽ
	public String getLogExp() {
		StringBuffer sb = new StringBuffer();
		if (!this.children.isEmpty()) {
			Iterator<ATNode> it = children.iterator();
			sb.append("(");
			while (it.hasNext()) {
				ATNode node = (ATNode) it.next();
				sb.append(node.getLogExp());
				if (refinementType == RefinementType.CONJUNCTIVE) {
					sb.append("*");
				} else
					sb.append("+");
			}
			String s = sb.substring(0, sb.length() - 1) + ")";
			// System.out.println(s);
			return s;
		} else {
			// System.out.println(this.label);
			return this.label;
		}
	}

	public String getExp3() {
		return getExp2().replace(" ", "+");

	}

	public String getExp2() {

		if (!children.isEmpty()) {
			if (refinementType == RefinementType.DISJUNCTIVE) {
				Iterator<ATNode> it = children.iterator();
				ATNode node1 = null;
				ATNode node2 = null;
				String s1 = null;
				String s2 = null;
				String temp = null;
				while (it.hasNext()) {
					StringBuffer sb = new StringBuffer();
					if (node1 == null) {
						node1 = (ATNode) it.next();
						s1 = node1.getExp2();
						if (it.hasNext()) {
							node2 = (ATNode) it.next();
							s2 = node2.getExp2();
						}
					} else {
						node1 = node2;
						s1 = temp;
						node2 = (ATNode) it.next();
						s2 = node2.getExp2();
					}
					String[] sa1 = s1.split(" ");
					if (!(s2 == null)) {
						String[] sa2 = s2.split(" ");
						for (int i = 0; i < sa1.length; i++) {
							for (int j = 0; j < sa2.length; j++) {
								sb.append(sa1[i] + "*" + sa2[j] + " ");
							}
						}
						temp = sb.substring(0, sb.length() - 1);
					} else
						temp = s1;
				}
				return temp;
			} else {
				Iterator<ATNode> it = children.iterator();
				StringBuffer sb = new StringBuffer();
				while (it.hasNext()) {
					ATNode node = (ATNode) it.next();
					sb.append(node.getExp2() + " ");
				}
				return sb.substring(0, sb.length() - 1);
			}
		}
		return this.label;
	}

	public void setADTreeNode(ADTreeNode root) {
		this.setLabel(root.getLabel());
		this.setRefinementType(
				root.getRefinmentType() == ADTreeNode.RefinementType.CONJUNCTIVE ? RefinementType.CONJUNCTIVE
						: RefinementType.DISJUNCTIVE);

	}
}
