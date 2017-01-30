package com.github.subalakr.yasjl;

import java.util.ArrayList;
import java.util.List;

import com.github.subalakr.yasjl.JsonPointer;
import rx.subjects.Subject;

/**
 * @author Subhashni Balakrishnan
 */
public class JsonPointerTree {
	private Node root;
	private boolean isRootAPointer;

	public JsonPointerTree() {
		this.root = new Node("", null);
		this.isRootAPointer = false;
	}

	/**
	 * Add json pointer, returns true if the json pointer is valid to be inserted
	 * TODO: check validity as per rfc 6901
	 */
	public boolean addJsonPointer(JsonPointer jp) throws Exception {
		if (isRootAPointer) {
			throw new IllegalArgumentException("Root is a json pointer, other json pointers are not allowed");
		}
		List<String> jpRefTokens = jp.refTokens();
		if (jpRefTokens.size() == 1) {
			isRootAPointer = true;
			return true;
		}
		Node parent = root;
		boolean pathDoesNotExist = false;
		for(int i=1; i < jpRefTokens.size(); i++){
			Node childMatch = parent.match(jpRefTokens.get(i));
			if (childMatch == null) {
				parent = parent.addChild(jpRefTokens.get(i), jp.subject());
				pathDoesNotExist = true;
			}
		}
		return pathDoesNotExist;
	}

	public boolean isIntermediaryPath(JsonPointer jp) throws Exception {
		List<String> jpRefTokens = jp.refTokens();
		if (jpRefTokens.size() == 1) {
			return false;
		}

		Node node = root;
		for(int i=1; i < jpRefTokens.size(); i++){
			Node childMatch = node.match(jpRefTokens.get(i));
			if (childMatch == null) {
				return false;
			} else {
				node = childMatch;
			}
		}
		if (node.children == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isTerminalPath(JsonPointer jp) throws Exception {
		List<String> jpRefTokens = jp.refTokens();
		Node node = root;
		if (jpRefTokens.size() == 1) {
			if (node.children == null) {
				return false;
			}
		}
		for(int i=1; i < jpRefTokens.size(); i++){
			Node childMatch = node.match(jpRefTokens.get(i));
			if (childMatch == null) {
				return false;
			} else {
				node = childMatch;
			}
		}
		if (node != null && node.children == null) {
			jp.subject(node.subject);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Node in the JP tree contains a reference token in Json pointer
	 */
	class Node {
		private String value;
		private List<Node> children;
		private Subject subject;

		public Node(String value, Subject subject) {
			this.value = value;
			this.children = null;
			this.subject = subject;
		}
		public Node addChild(String value,  Subject subject) {
			if (children == null) {
				children = new ArrayList<Node>();
			}
			Node child = new Node(value, subject);
			children.add(child);
			return child;
		}

		private boolean isIndex(String value) {
			try {
				Integer.parseInt(value);
				return true;
			} catch(NumberFormatException ex) {
				return false;
			}
		}

		public Node match(String value) {
			if (this.children == null) {
				return null;
			}
			for (Node child:children) {
				if (child.value.equals(value)) {
					return child;
				}
				if (child.value.equals("-") && isIndex(value)) {
					return child;
				}
			}
			return null;
		}
	}
}
