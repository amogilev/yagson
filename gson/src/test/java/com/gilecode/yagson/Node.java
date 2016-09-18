package com.gilecode.yagson;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public class Node {
	
	int id;
	List<Node> connections = new ArrayList<Node>();
	
	public Node(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("Node ").append(id).append(" -> [");
		boolean first = true;
		for (Node n : connections) {
			if (!first) {
				sb.append(", ");
			}
			first = false;
			sb.append(n.id);
		}
		sb.append("]");
		
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		return graphEquals(other);
		
	}

	private boolean graphEquals(Node other) {
		return graphEquals(other, new IdentityHashMap<Node, Node>()) && other.graphEquals(this, new IdentityHashMap<Node, Node>());
	}

	private boolean graphEquals(Node other, IdentityHashMap<Node, Node> visited) {
		if (id != other.id) {
			return false;
		}
		if (visited.containsKey(this)) {
			// not really true, but fine with bidirectional graphEquals use
			return true;
		}
		
			
		if (connections.size() != other.connections.size()) {
			return false;
		}
		visited.put(this, this);
		for (int i = 0; i < connections.size(); i++) {
			Node n = connections.get(i);
			Node on = other.connections.get(i);
			
			if (!n.graphEquals(on, visited)) {
				return false;
			}
		}
		
		return true;
	}
}
