package de.tudresden.inf.rn.mobilis.server.services.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class ContextNode {

	private String name;
	private long validity;
	private List<ContextNode> subNodes = null;
	private List<ContextItem> entries = null;
	
	public ContextNode(String name) {
		this.name = name;
		this.entries = new ArrayList<ContextItem>();
		this.subNodes = new ArrayList<ContextNode>();
	}
	public ContextNode(String name, long validity) {
		this.name = name;
		this.validity = validity;
		this.entries = new ArrayList<ContextItem>();
		this.subNodes = new ArrayList<ContextNode>();
	}
	
	// ContextItem-related
	
	public List<ContextItem> getEntries() {
		return entries;
	}
	
	public boolean addOrUpdateEntry (String key, String value, int type) {
		if (key==null || value==null) return false;
		
		ContextItem ci = null;
		for (ContextItem entry : entries)
			if (entry.getKey().equals(key)) {
				ci = entry;
				break;
			}
		
		if (ci == null) {
			ci = new ContextItem(key, value, type);
			return entries.add(ci);
		} else {
			if (ci.getValue().equals(value) && ci.getType()==type) {				
				return false; //No update neccessary
			} else {
				//Update value and type:
				ci.setValue(value);
				ci.setType(type);				
				return true; //Update successful
			}			
		}		
	}
	
	public boolean addEntry (ContextItem ci) {
		return entries.add(ci);
	}
	public boolean removeEntry (ContextItem ci) {
		return entries.remove(ci);
	}	
	
	public boolean isLeafNode() {
		if (subNodes == null || subNodes.size() == 0)
			return true;
		return false;
	}
	
	// SubNode-related
	
	public List<ContextNode> getSubNodes() {
		return subNodes;
	}
	public boolean addSubNode (ContextNode node) {
		return subNodes.add(node);
	}
	public boolean removeSubNode (ContextNode node) {
		return subNodes.remove(node);
	}
	public ContextNode getDirectSubNode(String name) {
		if (name==null) return null;
		
		for (ContextNode sub : subNodes) {
			if (sub.getName().equals(name))
				return sub;
		}
		return null;
	}
	
	//Getter & Setter
	
	public String getName() {
		return name;
	}
	public long getValidity() {
		return validity;
	}
	
	public boolean hasSubNode(String node) {
//		System.out.println("hasSubNode("+node+")");
		if (node==null || node.equals("")) return false;
				
		for (ContextNode cn : subNodes) {
			if (node.equals(cn.name)) return true;
			StringTokenizer st = new StringTokenizer(node, "/");
			if (st.hasMoreTokens()) {
				if (st.nextToken().equals(cn.name)) {
					return cn.hasSubNode(node.substring(node.indexOf("/")+1));
				}			
			}	
		}
		return false;
	}
	
	
	
	public String toString() {
		String result = name+" ";
		if (validity>0) result+="("+validity+") ";	
		result+="[ ";
		for (ContextItem ci : entries)
			result+=ci.toString()+"; ";
		result+="]\n";
		result+="";
	return result;
}
	
	/**
	 * Creates a tree representation of the node
	 * @param node The node, which may not be null
	 * @return A string containing the formatted tree
	 */
	public static String toStringTree(ContextNode node) {
	  final StringBuilder buffer = new StringBuilder();
	  return toStringTreeHelper(node, buffer, new LinkedList<Iterator<ContextNode>>()).toString();
	}
	 
	private static String toStringTreeDrawLines(List<Iterator<ContextNode>> parentIterators, boolean amLast) {
	  StringBuilder result = new StringBuilder();
	  Iterator<Iterator<ContextNode>> it = parentIterators.iterator();
	  while (it.hasNext()) {
	    Iterator<ContextNode> anIt = it.next();
	    if (anIt.hasNext() || (!it.hasNext() && amLast)) {
	      result.append("  ");
	    }
	    else {
	      result.append("  ");
	    }
	  }
	  return result.toString();
	}
	 
	private static StringBuilder toStringTreeHelper(ContextNode node, StringBuilder buffer, List<Iterator<ContextNode>>
	    parentIterators) {
	  if (!parentIterators.isEmpty()) {
	    boolean amLast = !parentIterators.get(parentIterators.size() - 1).hasNext();
//	    buffer.append("\n");
	    String lines = toStringTreeDrawLines(parentIterators, amLast);
//	    buffer.append(lines);
//	    buffer.append("\n");
	    buffer.append(lines);
	    buffer.append("+ ");
	  }
	  buffer.append(node.toString());
	  if (node.subNodes!=null && !node.subNodes.isEmpty()) {
	    Iterator<ContextNode> it = node.subNodes.iterator();
	    parentIterators.add(it);
	    while (it.hasNext()) {
	    	ContextNode child = it.next();
	      toStringTreeHelper(child, buffer, parentIterators);
	    }
	    parentIterators.remove(it);
	  }
	  return buffer;
	}

	
	
}
