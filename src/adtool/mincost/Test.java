package adtool.mincost;

import adtool.mincost.ATNode.RefinementType;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.parser.client.SyntaxError;
import org.matheclipse.parser.client.math.MathException;

import java.util.*;
import java.util.Map.Entry;

public class Test {
	private ATNode root;
	private ExprEvaluator util = new ExprEvaluator();

	public ATNode getRoot() {
		return root;
	}

	public void setRoot(ATNode root) {
		this.root = root;
	}

	private HashMap<String, Double> map = new HashMap<String, Double>();
	private HashMap<String, Double> atommap = new HashMap<String, Double>();
	private HashMap<String, Double> defcost = new HashMap<String, Double>();
	private HashMap<String,String>  labels=new HashMap<>();


	private Vector<HashSet<ATNode>> allnodes = new Vector<HashSet<ATNode>>();
	private Vector<Vector<ATNode>> btnodes = new Vector<Vector<ATNode>>();
	private Vector<String> ss = new Vector<String>();
	private Vector<String> bt = new Vector<String>();
	private Vector<String> at = new Vector<String>();
	private Vector<String> randomLabels=new Vector<>();
	
	private String result="";

	public double mincost(ATNode root) {
		double mincost = 0;
		Vector<ATNode> children = root.getChildren();
		Vector<ATNode> result = new Vector<ATNode>();
		if (children.isEmpty()) {
			return root.getMincost();
		} else {
			if (root.getRefinementType() == RefinementType.CONJUNCTIVE) {
				double re = Double.MAX_VALUE;
				for (ATNode child : children) {
					double temp = mincost(child);
					if (temp < re) {
						re = temp;
					}
				}
				mincost += re;
			} else {
				for (ATNode child : children) {
					mincost += mincost(child);
				}
			}
		}

		return mincost;

	}

	public void getDefCost(ATNode root) {
		if (root == null)
			return;
		defcost.put(root.getLabel().toLowerCase(), root.getMincost());
		// System.out.println(root.getLabel().toLowerCase()+defcost.get(root.getLabel().toLowerCase()));
		Vector<ATNode> children = root.getChildren();
		for (ATNode child : children) {
			getDefCost(child);
		}
	}
    
	int i=0;
	
	public void getExp(String exp, HashSet<ATNode> nodes) {
		if (allnodes.contains(nodes)||at.contains(exp))
			return;
		String temp = "";
		try {
			ExprEvaluator util = new ExprEvaluator();
			IExpr expr = util.eval(exp);
			IExpr result = util.eval(F.Expand(expr));
			temp = result.toString();
		} catch (SyntaxError e) {
			// catch Symja parser errors here
			System.out.println(e.getMessage());
		} catch (MathException me) {
			// catch Symja math errors here
			System.out.println(me.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} catch (final StackOverflowError soe) {
			System.out.println(soe.getMessage());
		} catch (final OutOfMemoryError oome) {
			System.out.println(oome.getMessage());
		}
		if (bt.contains(temp))
			return;
		String[] xq = temp.split("\\+");
		for (String x : xq) {
			String[] hq = x.split("\\*");
			String ts = x.replace("*", " ,");
			for(String z:hq){
				String tmp=labels.get(z);
				ts=ts.replace(""+z,""+tmp);
			}
			ts = "{" + ts + "}";
			if (ss.contains(ts) || ts.indexOf('(') != -1) {
				continue;
			}
			ss.add(ts);
			double re = 0;
			for (String y : hq) {
				re = re + defcost.get(y.toLowerCase()).doubleValue();
				if (re == Double.POSITIVE_INFINITY)
					break;
			}
			if (re != Double.POSITIVE_INFINITY) {
				map.put(ts, re);
			}
		}
		for (ATNode node : nodes) {
			if (node.getChildren().isEmpty()) {
				continue;
			} else {
				String s1 = "";
				HashSet<ATNode> newnodes = new HashSet<ATNode>(nodes);
				newnodes.remove(node);
				if (node.getRefinementType() == RefinementType.CONJUNCTIVE) {
					for (ATNode child : node.getChildren()) {
						s1 += "+" + child.getLabel();
						newnodes.add(child);
					}
				} else {
					for (ATNode child : node.getChildren()) {
						s1 += "*" + child.getLabel();
						newnodes.add(child);
					}
				}
				s1 = s1.substring(1);
				s1 = "(" + s1 + ")";
				String newexp = exp.replaceAll(node.getLabel(), s1);
				getExp(newexp, newnodes);
				allnodes.add(newnodes);
			}
		}
		bt.add(temp);
		at.add(exp);
	}

	
	public String getLogExp(ATNode root) {
		Vector<ATNode> children=root.getChildren();
		String resultString="";
		if (!children.isEmpty()) {
			String temp="";
			if(root.getRefinementType()==RefinementType.CONJUNCTIVE) {		
				for(ATNode child:children) {
					//temp=temp+"+"+"("+getLogExp(child)+")";
					temp=temp+"+"+getLogExp(child);
				}
			}else {
				for(ATNode child:children) {
//					temp=temp+"*"+"("+getLogExp(child)+")";
					temp=temp+"*"+getLogExp(child);
				}
			}
			resultString+=temp.substring(1);
			return "("+resultString+")";
		} else {
//		System.out.println(root.getLabel());
//			if(!root.getLabel().contains("n_"))
					return root.getLabel();
//			else return "";
		}
	}
	
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String expand(String s) {
		try {
			ExprEvaluator util = new ExprEvaluator();
			IExpr expr = util.eval(s);
			IExpr result = util.eval(F.Expand(expr));
			return result.toString();

		} catch (SyntaxError e) {
			// catch Symja parser errors here
			System.out.println(e.getMessage());
		} catch (MathException me) {
			// catch Symja math errors here
			System.out.println(me.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} catch (final StackOverflowError soe) {
			System.out.println(soe.getMessage());
		} catch (final OutOfMemoryError oome) {
			System.out.println(oome.getMessage());
		}
		return "";
	}

	public void show() {
		List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			// ��������
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}

		});
		for (Entry<String, Double> mapping : list) {
			System.out.println(mapping.getKey() + mapping.getValue());
		}
		System.out.println("\n\n");
	}

	public void atomtree(String s) {
//		String exp = root.getExp2().replace(" ", "+");
		String exp = expand(s);
//		System.out.println("exp"+exp);
		String[] xq = exp.split("\\+");
		for (String x : xq) {
			String[] hq = x.split("\\*");
			String ts = x.replace("*", " ,");
			for(String z:hq){
				String tmp=labels.get(z);
				ts=ts.replace(""+z,""+tmp);
			}
			ts = "{" + ts + "}";
			double re = 0;
			for (String y : hq) {
				re = re + defcost.get(y.toLowerCase()).doubleValue();
				if (re == Double.POSITIVE_INFINITY)
					break;
			}
			if (re != Double.POSITIVE_INFINITY) {
				atommap.put(ts, re);
			}
		}

	}

	public String showatomtree() {
		List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(atommap.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}

		});
		String result = "";
		for (Entry<String, Double> mapping : list) {
			result+=mapping.getKey() +":"+mapping.getValue()+"\n";
		}
		return result;
	}


	public void showDefCost(ATNode root) {
		if (root == null)
			return;
		System.out.println(root.getLabel().toLowerCase() + ": " + root.getMincost());
		Vector<ATNode> children = root.getChildren();
		for (ATNode child : children) {
			showDefCost(child);
		}
	}

	public void equalTree(ATNode root) {
		if(root.getChildren().isEmpty()) {
			return;
		}
		Vector<ATNode> children = root.getChildren();
		Vector<ATNode> newchildren = new Vector<ATNode>();
		if (!children.isEmpty()) {
			for (ATNode child : children) {
				if (child.getMincost() != Double.POSITIVE_INFINITY) {
					ATNode newparent = new ATNode();
					newparent.setRefinementType(child.getRefinementType());
					newparent.setLabel("n_" + ATNode.getNums());
					newparent.setChildren(child.getChildren());
					equalTree(newparent);
//					equalTree(child);
					ATNode ppareAtNode = new ATNode();
					ppareAtNode.setRefinementType(RefinementType.CONJUNCTIVE);
					ppareAtNode.setLabel("n_" + ATNode.getNums());
					child.setChildren(new Vector<ATNode>());
					ppareAtNode.addChild(child);
					ppareAtNode.addChild(newparent);
					newchildren.add(ppareAtNode);

				} else {
					equalTree(child);
					newchildren.add(child);
				}
			}
			root.setChildren(newchildren);
		}

	}

	public void getEqualTree(ATNode root) {
		ATNode newRoot = new ATNode();
		newRoot.getChildren().add(root);
		equalTree(newRoot);
	}
	
	public void removeNnode(ATNode root) {
		Vector<ATNode> children=root.getChildren();
		if (!children.isEmpty()) {
			for(int i=0;i<children.size();i++) {
				ATNode child=children.get(i);
				if(child.getLabel().contains("n_")&&child.getChildren().isEmpty())
							children.remove(child);
				else
				            removeNnode(child);
			}
		}
	}

	//暂支持650个替代标签
	public void init_randomLabels(){
		String alg="abcdefghijklmnopqrstuvwxyz";
		for(int i=0;i<26;i++){
			for(int j=0;j<26;j++){
				if(i!=j)
					randomLabels.add(""+alg.charAt(i)+alg.charAt(j));
			}
		}
	}


	public void changeLabeltoCalculate(ATNode root) {
		//labels.clear();
		Queue<ATNode> queue=new LinkedList<ATNode>();
		queue.offer(root);
		int i=0;
		while (!queue.isEmpty()){
			ATNode tmpNode=queue.poll();
			String ss=randomLabels.get(i++);
			labels.put(ss,tmpNode.getLabel());
			tmpNode.setLabel(ss);
			Vector<ATNode> V=tmpNode.getChildren();
			for(ATNode node:V){
				queue.offer(node);
			}
		}
	}
}
