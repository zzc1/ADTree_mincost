package adtool.mincost;

//求最小割集、最小径集
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ComputeSet {
	private ATNode root;
	private int prime = 1;
	private Map<String, Integer> primes = new HashMap<>();

	private Set<BigInteger> closeSet = new HashSet<BigInteger>();
	private HashMap<String, ATNode> labels = new HashMap<>();


	public String getcloseSet(String s) {
		toprime();
		String[] sa1 = s.split(" ");
		for (int i = 0; i < sa1.length; i++) {
			String[] sa2 = sa1[i].split("#");
			BigInteger result = new BigInteger("1");
			Set<String> set = new HashSet<String>();
			for (int j = 0; j < sa2.length; j++) {
				if (!set.contains(sa2[j]))
					set.add(sa2[j]);
			}
			Iterator<String> it0 = set.iterator();
			while (it0.hasNext()) {
				String string = (String) it0.next();
				result = result.multiply(new BigInteger(primes.get(string).toString()));
				//System.out.print(string);
			}
			if (!closeSet.contains(result))
				closeSet.add(result);
			Iterator<BigInteger> it1 = closeSet.iterator();
			boolean flag = false;
			while (it1.hasNext()) {
				BigInteger integer = (BigInteger) it1.next();
				if (integer.mod(result).intValue()==0 && integer != result) {
					it1.remove();
				}
				if (result.mod(integer).intValue()==0 && integer != result) {
					flag = true;
				}
			}
			if (flag)
				closeSet.remove(result);
		}
		StringBuffer sb1 = new StringBuffer();
		Iterator<BigInteger> it2 = closeSet.iterator();
		while (it2.hasNext()) {
			StringBuffer sb2 = new StringBuffer();
			BigInteger integer = (BigInteger) it2.next();
			//System.out.println(integer);
			Iterator<String> it3 = primes.keySet().iterator();
			while (it3.hasNext()) {
				String key = (String) it3.next();
				//System.out.println(primes.get(key));
				if (integer.mod(new BigInteger(primes.get(key).toString())).intValue()== 0) {
					//System.out.println(key);
					sb2.append(key + ",");
				}
			}
			//System.out.println(sb2+"10000");
			if(sb2.length()>0)
			sb1.append("{" + sb2.substring(0, sb2.length() - 1) + "} ");
		}
		
		if (!sb1.toString().trim().equals(""))
			return sb1.substring(0, sb1.length() - 1);
		else
			return "";
	}

	private int getnextprime() {
		for (int i = prime + 1; i < Integer.MAX_VALUE; i++) {
			if (isprime(i)) {
				prime = i;
				break;
			}
		}
		return prime;
	}

	private boolean isprime(int i) {
		for (int j = 2; j < Math.sqrt(i) + 1; j++) {
			if (i % j == 0)
				return false;
		}
		return true;
	}

	private void toprime() {
		prime = 1;
		primes.clear();
		closeSet.clear();
		Iterator<String> it = labels.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (labels.get(key).getChildren().isEmpty()) {
				if (!primes.containsKey(key))
					primes.put(labels.get(key).getLabel(), getnextprime());
			}
		}
	}

	public ATNode getRoot() {
		return root;
	}

	public void setRoot(ATNode root) {
		this.root = root;
	}

	public HashMap<String, ATNode> getlabels() {
		return labels;
	}

	public void setlabels(HashMap<String, ATNode> labels) {
		this.labels = labels;
	}
}


//			labels.clear();
//			root=createATree();
//			computeSet.setRoot(root);
//			computeSet.setlabels(labels);
//			starttime=System.currentTimeMillis();
//			minvalues=computeSet.getcloseSet(root.getExp3().replace("+", " ").replace("*", "#"));
//			String[] sets=minvalues.split(" ");
//			Arrays.sort(sets);
//			for(String s:sets){
//			String tmp=s.substring(1,s.length()-1);
//			String[] def=tmp.split(",");
//			double result=0;
//			for(String x: def){
//			result=result+((RealG0)denfCost.get(x.toLowerCase())).getValue();
//			}
//			minCost.put(s, result);
//			}
//
//			String result="逻辑表达式："+root.getLabel()+"="+root.getLogExp()+"\n\n"
//			+"表达式展开后："+ root.getLabel()+ "=" + root.getExp()+"\n\n"
//			+"最小割集是："+ computeSet.getcloseSet(root.getExp().replace("+", " ").replace("*", "#"))+"\n\n"
//			+"最小径集是："+minvalues+"\n\n";
//			endtime=System.currentTimeMillis();
//			System.out.println(result);
//			List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(minCost.entrySet());
//		Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
////升序排序
//public int compare(Map.Entry<String, Double> o1,
//		Map.Entry<String, Double> o2) {
//		return o1.getValue().compareTo(o2.getValue());
//		}
//
//		});
//
//		String res="";
//		for(Map.Entry<String, Double> mapping:list){
//		res+=mapping.getKey()+":"+String.format("%.2f", mapping.getValue())+"\n";
//		}
//		res+="\n\n\n";
//		System.out.println(res);
