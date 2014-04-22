package ru.ifmo.optimization.algorithm.muaco.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ifmo.optimization.algorithm.muaco.heuristicdist.HeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;

public class Node<Instance extends Constructable<Instance>> implements Comparable<Node<Instance>> {
	private final Long hash;
	private final FitnessValue fitness;
	private final Node<Instance> parent;
	private final MutationCollection<Instance> mutations;
    private final int fitnessEvaluationCount;
	private int numberOfVisits = 0;
	private final int maxNumberOfMutations;
	private Map<MutationCollection<Instance>, Edge> children = new HashMap<MutationCollection<Instance>, Edge>();
	
	public Node(Node<Instance> parent, MutationCollection<Instance> mutations, 
			 FitInstance<Instance> metaData, int fitnessEvaluationCount) {
		this.hash = metaData.getInstance().computeStringHash();
		this.fitness = metaData.getFitness();
		this.parent = parent;
		this.mutations = mutations;
		
        this.fitnessEvaluationCount = fitnessEvaluationCount;
		this.maxNumberOfMutations = metaData.getInstance().getMaxNumberOfMutations();
	}
	
	public FitnessValue getFitness() {
		return fitness;
	}
	
	public int getNumberOfChildren() {
		return children.size();
	}
	
	public Node<Instance> getParent() {
		return parent;
	}
	
	public MutationCollection getMutations() {
		return mutations;
	}
	
	public void clear() {
		children.clear();
	}
	
	public Long getHash() {
		return hash;
	}
	
	public Edge addChild(MutationCollection mutations, FitInstance<Instance> metaData, 
			HeuristicDistance<Instance> heuristicDistance, int fitnessEvaluationCount) {
		Node<Instance> child = new Node<Instance>(this, mutations, metaData, fitnessEvaluationCount);
		Edge edge = new Edge(mutations, this, child, heuristicDistance.getHeuristicDistance(this, child));
		children.put(mutations, edge);
		return edge;
	}
	
	public Edge addChild(MutationCollection mutations, Node<Instance> node, HeuristicDistance<Instance> heuristicDistance) {
		Edge edge = new Edge(mutations, this, node, heuristicDistance.getHeuristicDistance(this, node));
		children.put(mutations, edge);
		return edge;
	}
	
	public Edge getChild(MutatedInstanceMetaData mutated) {
		return children.get(mutated.getMutations());
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public boolean hasChild(MutatedInstanceMetaData mutated) {
		return children.containsKey(mutated.getMutations());
	}
	
	public void clearChildren() {
		children.clear();
	}
	
	public List<Edge> getEdges() {
		List<Edge> result = new ArrayList<Edge>();
		result.addAll(children.values());
		return result;
	}
	
	public List<Node<Instance>> getChildren() {
		List<Node<Instance>> result = new ArrayList<Node<Instance>>(children.size());
		for (Edge child : children.values()) {
			result.add(child.getDest());
		}
		return result;
	}
	
	public Edge getBestChild() {
		return Collections.max(children.values(), new Comparator<Edge>() {
			@Override
			public int compare(Edge arg0, Edge arg1) {
				return arg0.getDest().getFitness().betterThan(arg1.getDest().getFitness()) ? 1 : -1;
			}
		});
	}
	
	@Override
	public int compareTo(Node<Instance> other) {
		return hash.compareTo(other.hash);
	}
	
	@Override
	public boolean equals(Object other) {
		return hash == ((Node<Instance>)other).hash;
	}
	
	@Override
	public String toString() {
		return "f = " + getFitness() + "; [" + hash + "]";
	}
	
	public int getNumberOfVisits() {
		return numberOfVisits;
	}
	
	public void incrementNumberOfVisits() {
		numberOfVisits++;
	}

    public int getFitnessEvaluationCount() {
        return fitnessEvaluationCount;
    }
}
