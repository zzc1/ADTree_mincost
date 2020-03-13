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
package adtool.adtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import adtool.ui.ADTermView;
import adtool.ui.ADTreeCanvas;
import adtool.ui.DomainCanvas;
import adtool.ui.TreeChangeListener;

import org.abego.treelayout.util.AbstractTreeForTreeLayout;

/**
 * Provides an implementation for the org.abego.treelayout.TreeForTreeLayout
 */
public class ADTreeForGui extends AbstractTreeForTreeLayout<ADTreeNode> implements Serializable {
	static final long serialVersionUID = 98453455646646164L;
	private ADTreeNode viewRoot;
	transient protected ADTreeNodeExtentProvider extendProvider;
	transient private HashMap<Integer, TreeChangeListener> listeners;
	/**
	 * Listener that should be notified first (ADTerms).
	 */
	transient private TreeChangeListener firstListener;
	private Map<ADTreeNode, List<ADTreeNode>> childrenMap = new HashMap<ADTreeNode, List<ADTreeNode>>();
	private Map<ADTreeNode, ADTreeNode> parents = new HashMap<ADTreeNode, ADTreeNode>();
	private Vector<LinkedList<ADTreeNode>> levels = new Vector<LinkedList<ADTreeNode>>();
	private String description;
	private HashMap<ADTreeNode, String> commentsMap = new HashMap<ADTreeNode, String>();
	transient private boolean localExtendProvider;

	/**
	 * Creates a new instance with a given node as the root. Used in
	 * serialization.
	 * 
	 * @param root
	 *            root of the tree.
	 * @param newChildren
	 *            map from nodes to children
	 * @param newParents
	 *            map from nodes to parents
	 * @param newViewRoot
	 *            root after folding from above.
	 *
	 */
	public ADTreeForGui(ADTreeNode root, Map<ADTreeNode, List<ADTreeNode>> newChildren,
			Map<ADTreeNode, ADTreeNode> newParents) {
		this(root);
		childrenMap = newChildren;
		for (List<ADTreeNode> list : childrenMap.values()) {
			for (ADTreeNode node : list) {
				node.setFolded(false);
				node.setAboveFolded(false);
			}
		}
		parents = newParents;
		viewRoot = null;
		description = "";
		resetLevels(root, 0);
	}

	/**
	 * Creates a new instance with a given node as the root
	 *
	 * @param root
	 *            the node to be used as the root.
	 */
	public ADTreeForGui(ADTreeNode root) {
		super(root);
		viewRoot = null;
		root.setFolded(false);
		root.setAboveFolded(false);
		extendProvider = new ADTreeNodeExtentProvider();
		localExtendProvider = false;
		listeners = new HashMap<Integer, TreeChangeListener>();
		firstListener = null;
		resetLevels(root, 0);
	}

	/**
	 * Adds a listener for tree changes.
	 *
	 * @param listener
	 */
	public void addTreeChangeListener(TreeChangeListener listener) {
		if (listener instanceof ADTermView) {
			firstListener = listener;
		} else {
			listeners.put(getKey(listener), listener);
		}
	}

	private LinkedList<ADTreeNode> getLevelList(int i) {
		while (i >= levels.size()) {
			levels.add(new LinkedList<ADTreeNode>());
		}
		return levels.elementAt(i);
	}

	/**
	 * Removing listener for tree changes.
	 *
	 * @param listener
	 */
	public void removeTreeChangeListener(TreeChangeListener listener) {
		listeners.remove(getKey(listener));
	}

	private Integer getKey(TreeChangeListener listener) {
		return ((ADTreeCanvas) listener).getId();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractTreeForTreeLayout#getParent(ADTreeNode)
	 */
	@Override
	public ADTreeNode getParent(ADTreeNode node) {
		return getParent(node, false);
	}

	/**
	 * Returns parent of the node.
	 *
	 * @param node
	 *            node
	 * @param ignoreFold
	 *            if true we return parent even if the node is folded from above
	 * @return the parent node or null
	 */
	public ADTreeNode getParent(ADTreeNode node, boolean ignoreFold) {
		if (node == null) {
			return null;
		}
		if (!ignoreFold && node.isAboveFolded()) {
			return null;
		}
		return parents.get(node);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String newDescription) {
		description = newDescription;
	}

	public HashMap<ADTreeNode, String> getComments() {
		return commentsMap;
	}

	public void setComments(HashMap<ADTreeNode, String> comments) {
		this.commentsMap = comments;
	}

	@Override
	public List<ADTreeNode> getChildrenList(ADTreeNode node) {
		return getChildrenList(node, false);
	}

	/**
	 * Gets the children list.
	 *
	 * @param node
	 * @param ignoreFold
	 * @return
	 */
	public List<ADTreeNode> getChildrenList(ADTreeNode node, boolean ignoreFold) {
		if (!ignoreFold && node.isFolded()) {
			return new ArrayList<ADTreeNode>();
		}
		List<ADTreeNode> result = childrenMap.get(node);
		if (result == null) {
			result = new ArrayList<ADTreeNode>();
			childrenMap.put(node, result);
		}
		return result;
	}

	public Integer getNumberOfNodes() {
		Set<ADTreeNode> keys = childrenMap.keySet();
		int result = 0;
		for (ADTreeNode n : keys) {
			List<ADTreeNode> list = getChildrenList(n, true);
			result += list.size();
		}
		return new Integer(result + 1);
	}

	public ADTNode getTerms() {
		return getTerms(getRoot(true), 0);
	}

	/**
	 * Returns the left sibling (with rotation) of the node.
	 *
	 * @param node
	 * @return
	 */
	public ADTreeNode getLeftSibling(final ADTreeNode node) {
		if (node != null) {
			LinkedList<ADTreeNode> level = getLevelList(node.getLevel());
			int index = level.indexOf(node);
			index = (index - 1 + level.size()) % level.size();
			return level.get(index);
		}
		return null;
	}

	/**
	 * Returns the right sibling (with rotation) of the node.
	 *
	 * @param node
	 * @return
	 */
	public ADTreeNode getRightSibling(final ADTreeNode node) {
		if (node != null) {
			LinkedList<ADTreeNode> level = getLevelList(node.getLevel());
			int index = level.indexOf(node);
			index = (index + 1) % level.size();
			return level.get(index);
		}
		return null;
	}

	/**
	 * Return a middle child.
	 *
	 * @param node
	 * @return
	 */
	public ADTreeNode getMiddleChild(final ADTreeNode node) {
		if (node != null) {
			List<ADTreeNode> list = getChildrenList(node, false);
			if (list == null) {
				return null;
			}
			if (list.size() == 0) {
				return null;
			}
			return list.get(list.size() / 2);
		}
		return null;
	}

	/**
	 * Checks if the node is
	 * 
	 * @param node
	 * @return true iff the node is in the tree
	 */
	// public boolean hasNode(ADTreeNode node)
	// {
	// return node == getRoot(true) || parents.containsKey(node);
	// }
	/**
	 * Toggle opperation on the node between disjunctive and conjunctive.
	 *
	 * @param node
	 *            node for which operation is toggled.
	 */
	public final void changeOp(final ADTreeNode node) {
		node.changeOp();
		notifyTreeChanged();
	}

	/**
	 * Addd to level list to make possible walking with arrow keys. Version of
	 * the function used when adding sibling.
	 *
	 * @param index
	 *            index at which we add new node.
	 * @param parent
	 *            parent of the node.
	 * @param node
	 *            node to be added.
	 */
	private void addToLevelList(final int index, final ADTreeNode parent, final ADTreeNode node) {
		if (parent.isFolded()) {
			return;
		}
		node.setLevel(parent.getLevel() + 1);
		final List<ADTreeNode> nodeLevelList = getLevelList(node.getLevel());
		final List<ADTreeNode> childList = getChildrenList(parent, false);
		ADTreeNode oldNode;
		if (childList == null || childList.size() <= index) {
			oldNode = childList.get(childList.size() - 1);
			nodeLevelList.add(nodeLevelList.indexOf(oldNode) + 1, node);
		} else {
			oldNode = childList.get(index);
			nodeLevelList.add(nodeLevelList.indexOf(oldNode), node);
		}
	}

	/**
	 * Addd to level list to make possible walking with arrow keys.
	 *
	 * @param parent
	 *            parent of node to be added.
	 * @param node
	 *            node to be added.
	 * @param counter
	 *            whether we add a counter node.
	 */
	private void addToLevelList(final ADTreeNode parent, final ADTreeNode node, final boolean counter) {
		if (parent.isFolded()) {
			return;
		}
		List<ADTreeNode> childList = getChildrenList(parent, false);
		LinkedList<ADTreeNode> nodeLevelList = getLevelList(node.getLevel());
		if (childList.size() == 0) {
			final List<ADTreeNode> parentLevelList = getLevelList(parent.getLevel());
			if (parentLevelList.size() > 1) {
				final ListIterator<ADTreeNode> iter = parentLevelList.listIterator(parentLevelList.indexOf(parent));
				List<ADTreeNode> list = getChildrenList(iter.next(), false);
				while ((list == null || list.size() == 0) && iter.hasPrevious()) {
					list = getChildrenList(iter.previous(), false);
				}
				if (list == null || list.size() == 0) {
					nodeLevelList.addFirst(node);

				} else {
					childList = getChildrenList(iter.next(), false);
					final ADTreeNode prevNode = childList.get(childList.size() - 1);
					nodeLevelList.add(nodeLevelList.indexOf(prevNode) + 1, node);
				}
			} else {
				nodeLevelList.add(node);
			}
		} else {
			final ADTreeNode prevNode = childList.get(childList.size() - 1);
			if (parent.isCountered() && !counter) {
				nodeLevelList.add(nodeLevelList.indexOf(prevNode), node);
			} else {
				nodeLevelList.add(nodeLevelList.indexOf(prevNode) + 1, node);
			}
		}
	}

	/**
	 * Add a counter to the node.
	 *
	 * @param parentNode
	 * @param node
	 */
	public void addCounter(ADTreeNode parentNode, ADTreeNode node) {
		// checkArg(hasNode(parentNode), "parentNode is not in the tree");
		// checkArg(!hasNode(node), "node is already in the tree");
		List<ADTreeNode> list = getChildrenList(parentNode, true);
		parentNode.setCountered(true);
		node.setLevel(parentNode.getLevel() + 1);
		if (!parentNode.isFolded()) {
			addToLevelList(parentNode, node, true);
		}
		list.add(node);
		parents.put(node, parentNode);
		if (!localExtendProvider) {
			extendProvider.updateSize(node);
		}
		notifyTreeChanged();
	}

	/**
	 * Adds a child.
	 * 
	 * @param parentNode
	 *            [hasNode(parentNode)]
	 * @param node
	 *            [!hasNode(node)]
	 */
	public void addChild(ADTreeNode parentNode, ADTreeNode node) {
		addChild(parentNode, node, true);
	}

	/**
	 * Adds a child.
	 * 
	 * @param parentNode
	 *            [hasNode(parentNode)]
	 * @param node
	 *            [!hasNode(node)]
	 * @param notify
	 *            whether to notify about tree change.
	 */
	private void addChild(ADTreeNode parentNode, ADTreeNode node, boolean notify) {
		// checkArg(hasNode(parentNode), "parentNode is not in the tree");
		// checkArg(!hasNode(node), "node is already in the tree");
		List<ADTreeNode> list = getChildrenList(parentNode, true);
		node.setLevel(parentNode.getLevel() + 1);
		if (!parentNode.isFolded()) {
			addToLevelList(parentNode, node, false);
		}
		if (parentNode.isCountered()) {
			list.add(list.size() - 1, node);
		} else {
			list.add(node);
		}
		parents.put(node, parentNode);
		if (!localExtendProvider) {
			extendProvider.updateSize(node);
		}
		if (notify) {
			notifyTreeChanged();
		}
	}

	/**
	 * @param node
	 *            [hasNode(node)]
	 * @param sibling
	 *            [!hasNode(sibling)]
	 * @param onLeft
	 */
	public void addSibling(ADTreeNode oldNode, ADTreeNode sibling, boolean onLeft) {
		ADTreeNode parentNode = getParent(oldNode, true);
		List<ADTreeNode> list = getChildrenList(parentNode, true);
		int index = list.indexOf(oldNode);
		if (!onLeft) {
			index++;
		}
		addToLevelList(index, parentNode, sibling);
		list.add(index, sibling);
		parents.put(sibling, parentNode);
		if (!localExtendProvider) {
			extendProvider.updateSize(sibling);
		}
		notifyTreeChanged();
	}

	/**
	 * Add a number of children.
	 *
	 * @param parentNode
	 * @param nodes
	 */
	public void addChildren(ADTreeNode parentNode, ADTreeNode... nodes) {
		for (ADTreeNode node : nodes) {
			addToLevelList(parentNode, node, false);
			addChild(parentNode, node, false);
		}
	}

	/**
	 * Removes a node from a tree. Note that root cannot be removed. TODO:
	 * update values of levels.
	 * 
	 * @param node
	 */
	public void removeNode(ADTreeNode node) {
		ADTreeNode parent = getParent(node, true);
		if (parent != null) {
			if (node.isAboveFolded()) {
				toggleAboveFold(node);
			}
			if (node.isFolded()) {
				toggleFold(node);
			}

			List<ADTreeNode> list1 = getChildrenList(parent, true);
			List<ADTreeNode> listChildren = getChildrenList(node, true);
			parents.remove(node);
			childrenMap.remove(node);
			for (ADTreeNode child : listChildren) {
				parents.put(child, parent);
			}
			list1.addAll(list1.indexOf(node), listChildren);
			list1.remove(node);
		}
		updateAllSizes();
	}

	/**
	 * Removes a subtree from the tree.
	 *
	 * @param node
	 *            a root of a subtree to be removed.
	 */
	public void removeTree(ADTreeNode node) {
		if (getParent(node, true) != null) {
			removeChild(getParent(node, true), node);
		}
	}

	/**
	 * Changes the node label.
	 *
	 * @param node
	 * @param label
	 */

	public void setLabel(ADTreeNode node, String label) {
		node.setLabel(label);
		updateSize(node);
	}

	/**
	 * Recalculates size for all nodes.
	 *
	 * @param node
	 */
	public void updateAllSizes() {
		if (!localExtendProvider) {
			extendProvider.clearSizes();
			updateAllSizes(getRoot(true));
		}
		notifySizeChanged();
	}

	private void updateAllSizes(ADTreeNode node) {
		extendProvider.updateSize(node);
		for (ADTreeNode child : getChildrenList(node, true)) {
			updateAllSizes(child);
		}
	}

	/**
	 * Recalculates the sizes of node
	 *
	 * @param node
	 */
	public void updateSize(ADTreeNode node) {
		if (!localExtendProvider) {
			extendProvider.updateSize(node);
		}
	}

	/**
	 * Registers a canvas to consider when calculating a node size.
	 *
	 * @param canvas
	 *            Canvas to register.
	 */
	public void setOwner(ADTreeCanvas canvas) {
		extendProvider.setOwner(canvas);
		updateAllSizes();
	}

	/**
	 * Sets the focus for all listeners to null.
	 *
	 */
	public void defocusAll() {
		if (firstListener != null) {
			firstListener.setFocus(null);
		}
		List<Integer> keys = ADTreeForGui.asSortedList(listeners.keySet());
		for (Integer integer : keys) {
			listeners.get(integer).setFocus(null);
		}
	}

	public void registerSizeCanvas(DomainCanvas<?> canvas) {
		extendProvider.registerCanvas(canvas);
		updateAllSizes();
	}

	/**
	 * Removes canvas from consideration when calculating a node size.
	 *
	 * @param canvas
	 *            Canvas to remove.
	 */
	public void deregisterSizeCanvas(DomainCanvas<?> canvas) {
		extendProvider.deregisterCanvas(canvas);
		updateAllSizes();
	}

	/**
	 * Creates tree with root only.
	 *
	 */
	public void newTree() {
		defocusAll();
		ADTreeNode treeRoot = getRoot(true);
		ADTreeNode viewRoot = getRoot(false);
		while (treeRoot != viewRoot) {
			toggleAboveFold(viewRoot);
			viewRoot = getRoot(false);
		}
		removeAllChildren(treeRoot);
		// root have already id of 1 so we set counter to 2.
		ADTreeNode.resetCounter(2);
		treeRoot.setCountered(false);
		if (treeRoot.isFolded()) {
			toggleFold(treeRoot);
		}
		treeRoot.setType(ADTreeNode.Type.PROPONENT);
		treeRoot.setLabel("Root");
		resetLevels(treeRoot, 0);
		viewRoot = null;
		notifyTreeChanged();
		updateAllSizes();
		notifySizeChanged();
	}

	/**
	 * Create new tree based on the terms.
	 *
	 * @param root
	 */
	public void createFromTerms(ADTNode termRoot) {
		ADTreeNode treeRoot = getRoot(true);
		ADTreeNode viewRoot = getRoot(false);
		while (treeRoot != viewRoot) {
			toggleAboveFold(viewRoot);
			viewRoot = getRoot(false);
		}
		removeAllChildren(getRoot(true));
		resetLevels(treeRoot, 0);
		// root have already id of 1 so we set counter to 2.
		ADTreeNode.resetCounter(2);
		treeRoot.setCountered(false);
		treeRoot.setType(ADTreeNode.Type.PROPONENT);
		switch (termRoot.getType()) {
		case LEAFP:
			treeRoot.setLabel(termRoot.getName());
			break;
		case OP:
			treeRoot.setLabel(termRoot.getName());
			treeRoot.setRefinementType(ADTreeNode.RefinementType.DISJUNCTIVE);
			for (Node termChild : termRoot.getChildren()) {
				addFromTerm((ADTNode) termChild, treeRoot, 1);
			}
			break;
		case AP:
			treeRoot.setLabel(termRoot.getName());
			treeRoot.setRefinementType(ADTreeNode.RefinementType.CONJUNCTIVE);
			for (Node termChild : termRoot.getChildren()) {
				addFromTerm((ADTNode) termChild, treeRoot, 1);
			}
			break;
		case CP:
			ADTNode subTerm = (ADTNode) termRoot.getChildren().elementAt(0);
			switch (subTerm.getType()) {
			case LEAFP:
				treeRoot.setLabel(subTerm.getName());
				break;
			case OP:
				treeRoot.setLabel(subTerm.getName());
				treeRoot.setRefinementType(ADTreeNode.RefinementType.DISJUNCTIVE);
				for (Node termChild : subTerm.getChildren()) {
					addFromTerm((ADTNode) termChild, treeRoot, 1);
				}
				break;
			case AP:
				treeRoot.setLabel(subTerm.getName());
				treeRoot.setRefinementType(ADTreeNode.RefinementType.CONJUNCTIVE);
				for (Node termChild : subTerm.getChildren()) {
					addFromTerm((ADTNode) termChild, treeRoot, 1);
				}
				break;
			case CP:
				treeRoot.setLabel(subTerm.getName());
				treeRoot.setRefinementType(ADTreeNode.RefinementType.DISJUNCTIVE);
				addFromTerm((ADTNode) subTerm, treeRoot, 1);
				break;
			case LEAFO:
			case OO:
			case AO:
			case CO:
			default:
				System.err.println("Cannot create from terms. Invalid term expression provided in counter node.");
			}
			addFromTerm((ADTNode) termRoot.getChildren().elementAt(1), treeRoot, 1);
			// it must be last !!
			treeRoot.setCountered(true);
			break;
		case LEAFO:
		case OO:
		case AO:
		case CO:
		default:
			System.err.println("Cannot create from terms. Invalid term expression provided.");
		}
		notifyTreeChanged();
		updateAllSizes();
		notifySizeChanged();
	}

	/**
	 * Returns a term representation of a subtree starting at a given node.
	 *
	 * @param node
	 *            root of a subtree.
	 * @param id
	 *            initial id for a term for current node.
	 * @return
	 */
	public ADTNode getTerms(ADTreeNode node, int id) {
		ADTNode term = new ADTNode(id);
		List<ADTreeNode> list = getChildrenList(node, true);
		boolean p = node.getType() == ADTreeNode.Type.PROPONENT;
		boolean o = node.getRefinmentType() == ADTreeNode.RefinementType.DISJUNCTIVE;
		// type leaf
		if (list.size() == 0) {
			if (id == 0) {
				term.setType(ADTNode.Type.LEAFP);
			} else {
				// if ( getParent(node,true).getType() ==
				// ADTreeNode.Type.PROPONENT) {
				term.setType(p ? ADTNode.Type.LEAFP : ADTNode.Type.LEAFO);
			}
			node.setTerm(term);
			term.setName(node.getLabel());
			return term;
		}
		// type counter
		if (node.isCountered()) {
			if (p) {
				term.setType(ADTNode.Type.CP);
				term.setName("cp");
			} else {
				term.setType(ADTNode.Type.CO);
				term.setName("co");
			}
			ADTNode subTerm = new ADTNode(id + 1);
			subTerm.setName(node.getLabel());
			// counter type of leaf
			if (list.size() == 1) {
				if (id == 0) {
					subTerm.setType(ADTNode.Type.LEAFP);
				} else {
					subTerm.setType(p ? ADTNode.Type.LEAFP : ADTNode.Type.LEAFO);
				}
			}
			// counter with children
			else {
				if (p) {
					subTerm.setType(o ? ADTNode.Type.OP : ADTNode.Type.AP);
				} else {
					subTerm.setType(o ? ADTNode.Type.OO : ADTNode.Type.AO);
				}
				for (int i = 0; i < list.size() - 1; i++) {
					subTerm.jjtAddChild(getTerms(list.get(i), id + i + 2), i);
				}
			}
			term.jjtAddChild(subTerm, 0);
			term.jjtAddChild(getTerms(list.get(list.size() - 1), id + list.size() + 1), 1);
		}
		// type operator
		else {
			if (p) {
				term.setType(o ? ADTNode.Type.OP : ADTNode.Type.AP);
			} else {
				term.setType(o ? ADTNode.Type.OO : ADTNode.Type.AO);
			}
			int noChild = 0;
			for (ADTreeNode child : list) {
				term.jjtAddChild(getTerms(child, id + noChild + 1), noChild);
				noChild++;
			}
		}
		term.setName(node.getLabel());
		node.setTerm(term);
		return term;
	}

	private void addFromTerm(ADTNode term, ADTreeNode parent, int level) {
		if (term == null) {
			return;
		}
		ADTreeNode node;
		ADTNode subTerm;
		switch (term.getType()) {
		case LEAFP:
			node = new ADTreeNode(ADTreeNode.Type.PROPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
			node.setLabel(term.getName());
			node.setLevel(level);
			addChild(parent, node, false);
			break;
		case OP:
			node = new ADTreeNode(ADTreeNode.Type.PROPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
			node.setLabel(term.getName());
			addChild(parent, node, false);
			for (Node termChild : term.getChildren()) {
				addFromTerm((ADTNode) termChild, node, level + 1);
			}
			break;
		case AP:
			node = new ADTreeNode(ADTreeNode.Type.PROPONENT, ADTreeNode.RefinementType.CONJUNCTIVE);
			node.setLabel(term.getName());
			addChild(parent, node, false);
			for (Node termChild : term.getChildren()) {
				addFromTerm((ADTNode) termChild, node, level + 1);
			}
			break;
		case CP:
			subTerm = (ADTNode) term.getChildren().elementAt(0);
			switch (subTerm.getType()) {
			case LEAFP:
				node = new ADTreeNode(ADTreeNode.Type.PROPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				break;
			case OP:
				node = new ADTreeNode(ADTreeNode.Type.PROPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				for (Node termChild : subTerm.getChildren()) {
					addFromTerm((ADTNode) termChild, node, level + 1);
				}
				break;
			case AP:
				node = new ADTreeNode(ADTreeNode.Type.PROPONENT, ADTreeNode.RefinementType.CONJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				for (Node termChild : subTerm.getChildren()) {
					addFromTerm((ADTNode) termChild, node, level + 1);
				}
				break;
			case CP:
				node = new ADTreeNode(ADTreeNode.Type.PROPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				addFromTerm((ADTNode) subTerm, node, level + 1);
				break;
			case CO:
			case LEAFO:
			case OO:
			case AO:
			default:
				System.err.println("1 Invalid term expression provided.");
				return;
			}
			addFromTerm((ADTNode) term.getChildren().elementAt(1), node, level + 1);
			node.setCountered(true);
			break;
		case LEAFO:
			node = new ADTreeNode(ADTreeNode.Type.OPPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
			node.setLabel(term.getName());
			node.setLevel(level);
			addChild(parent, node, false);
			break;
		case OO:
			node = new ADTreeNode(ADTreeNode.Type.OPPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
			node.setLabel(term.getName());
			addChild(parent, node, false);
			for (Node termChild : term.getChildren()) {
				addFromTerm((ADTNode) termChild, node, level + 1);
			}
			break;
		case AO:
			node = new ADTreeNode(ADTreeNode.Type.OPPONENT, ADTreeNode.RefinementType.CONJUNCTIVE);
			node.setLabel(term.getName());
			addChild(parent, node, false);
			for (Node termChild : term.getChildren()) {
				addFromTerm((ADTNode) termChild, node, level + 1);
			}
			break;
		case CO:
			subTerm = (ADTNode) term.getChildren().elementAt(0);
			switch (subTerm.getType()) {
			case LEAFO:
				node = new ADTreeNode(ADTreeNode.Type.OPPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				break;
			case OO:
				node = new ADTreeNode(ADTreeNode.Type.OPPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				for (Node termChild : subTerm.getChildren()) {
					addFromTerm((ADTNode) termChild, node, level + 1);
				}
				break;
			case AO:
				node = new ADTreeNode(ADTreeNode.Type.OPPONENT, ADTreeNode.RefinementType.CONJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				for (Node termChild : subTerm.getChildren()) {
					addFromTerm((ADTNode) termChild, node, level + 1);
				}
				break;
			case CO:
				node = new ADTreeNode(ADTreeNode.Type.OPPONENT, ADTreeNode.RefinementType.DISJUNCTIVE);
				node.setLabel(subTerm.getName());
				node.setLevel(level);
				addChild(parent, node, false);
				addFromTerm((ADTNode) subTerm, node, level + 1);
				break;
			case LEAFP:
			case OP:
			case AP:
			case CP:
			default:
				System.err.println("2 Invalid term expression provided.");
				return;
			}
			addFromTerm((ADTNode) term.getChildren().elementAt(1), node, level + 1);
			node.setCountered(true);
			break;
		default:
			System.err.println("3 Invalid term expression provided.");
			return;
		}
	}

	private void removeChild(ADTreeNode node, ADTreeNode child) {
		if (child.getType() != node.getType()) {
			node.setCountered(false);
		}
		removeAllChildren(child);
		List<ADTreeNode> list = getChildrenList(node, true);
		list.remove(child);
		parents.remove(child);
		childrenMap.remove(child);
		LinkedList<ADTreeNode> nodeLevelList = getLevelList(child.getLevel());
		nodeLevelList.remove(child);
	}

	public void removeAllChildren(ADTreeNode node) {
		LinkedList<ADTreeNode> nodeLevelList = getLevelList(node.getLevel() + 1);
		node.setCountered(false);
		List<ADTreeNode> list = getChildrenList(node, true);
		for (ADTreeNode child : list) {
			removeAllChildren(child);
			nodeLevelList.remove(child);
			parents.remove(child);
			childrenMap.remove(child);
		}
		list.clear();
	}

	/**
	 * Notifies registered listeners about tree change.
	 *
	 */
	public void notifyTreeChanged() {
		if (firstListener != null) {
			firstListener.treeChanged();
		}
		List<Integer> keys = ADTreeForGui.asSortedList(listeners.keySet());
		for (Integer integer : keys) {
			listeners.get(integer).treeChanged();
		}
	}

	/**
	 * Sets the extendProvider for this instance.
	 *
	 * @param extendProvider
	 *            The extendProvider.
	 */
	public void setExtendProvider(ADTreeNodeExtentProvider extendProvider) {
		this.extendProvider = extendProvider;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.abego.treelayout.TreeForTreeLayout#getRoot()
	 */
	@Override
	public ADTreeNode getRoot() {
		return getRoot(false);
	}

	/**
	 * Gets the viewRoot for this instance.
	 *
	 * @param ignoreFold
	 * @return The viewRoot.
	 */
	public ADTreeNode getRoot(final boolean ignoreFold) {
		if (!ignoreFold && viewRoot != null) {
			return viewRoot;
		} else {
			return super.getRoot();
		}
	}

	/**
	 * Folds the node from above which hides all nodes that are not descendants.
	 *
	 * @param node
	 *            node to be folded from above.
	 */
	public void toggleAboveFold(ADTreeNode node) {
		node.setAboveFolded(!node.isAboveFolded());
		viewRoot = getNewViewNode(node);
		resetLevels(getRoot(false), 0);
		notifyTreeChanged();
	}

	private ADTreeNode getNewViewNode(ADTreeNode node) {
		if (node.isAboveFolded()) {
			return node;
		} else {
			ADTreeNode parent = getParent(node, true);
			if (parent == null) {
				return null;
			} else {
				return getNewViewNode(parent);
			}
		}
	}

	/**
	 * Gets the viewRoot for this instance.
	 *
	 * @return The viewRoot.
	 */
	public ADTreeNode getViewRoot() {
		return this.viewRoot;
	}

	/**
	 * Gets the extendProvider for this instance.
	 *
	 * @return The extendProvider.
	 */
	public ADTreeNodeExtentProvider getExtendProvider() {
		return this.extendProvider;
	}

	/**
	 * Gets the childrenMap for this instance.
	 *
	 * @return The childrenMap.
	 */
	public Map<ADTreeNode, List<ADTreeNode>> getChildrenMap() {
		return this.childrenMap;
	}

	/**
	 * Gets the parents for this instance.
	 *
	 * @return The parents.
	 */
	public Map<ADTreeNode, ADTreeNode> getParents() {
		return this.parents;
	}

	/**
	 * Gets the levels for this instance.
	 *
	 * @return The levels.
	 */
	public Vector<LinkedList<ADTreeNode>> getLevels() {
		return this.levels;
	}

	/**
	 * Sets whether or not this instance is localExtendProvider.
	 *
	 * @param localExtendProvider
	 *            The localExtendProvider.
	 */
	public void setLocalExtendProvider(boolean localExtendProvider) {
		this.localExtendProvider = localExtendProvider;
	}

	public void expandAllFold() {
		expandAllFold(getRoot(true));
		resetLevels(getRoot(false), 0);
		notifyTreeChanged();
	}

	public void expandAllFold(ADTreeNode node) {
		if (node.isFolded()) {
			node.setFolded(!node.isFolded());
		}
		for (ADTreeNode child : getChildrenList(node, true)) {
			expandAllFold(child);
		}
	}

	public void toggleFold(ADTreeNode node) {
		node.setFolded(!node.isFolded());
		resetLevels(getRoot(false), 0);
		notifyTreeChanged();
	}

	/**
	 * Recreate level lists.
	 *
	 */
	public void resetLevels() {
		levels.clear();
		// levels = new Vector<LinkedList<ADTreeNode>>();
		resetLevels(getRoot(false), 0);
	}

	private void resetLevels(ADTreeNode node, int level) {
		getLevelList(level).add(node);
		List<ADTreeNode> children = getChildrenList(node, false);
		if (children != null && children.size() > 0) {
			for (ADTreeNode child : children) {
				resetLevels(child, level + 1);
			}
		}
	}

	private void notifySizeChanged() {
		List<Integer> keys = ADTreeForGui.asSortedList(listeners.keySet());
		for (Integer integer : keys) {
			listeners.get(integer).sizeChanged();
		}
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}
}
