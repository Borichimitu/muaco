package ru.ifmo.optimization.algorithm.muaco.graph;

import ru.ifmo.util.Array;

public class Edge { 
	private MutationCollection mutations;
	private Node from;
	private Node to;
	private Array pheromone;        
	private Array bestPheromone;
	private Array heuristicDistance;
	
	public Edge(MutationCollection mutations, Node from, Node to, Array heuristicDistance){
		this.mutations = mutations;
		this.from = from;
		this.to = to;
		this.heuristicDistance = new Array(heuristicDistance);
		bestPheromone = Array.getValues(heuristicDistance.length(), 0);
	}

	public MutationCollection getMutations() {
		return mutations;
	}
	
	public Node getSource() {
		return from;
	}
	
	public Node getDest() {
		return to;
	}
	
	public Array getPheromone() {
		return pheromone;
	}
	
	public void setPheromone(Array pheromone) {
		this.pheromone = new Array(pheromone);
	}
	
	public void setBestPheromone(Array pheromone) {
		bestPheromone = Array.max(bestPheromone, pheromone);
	}
	
	public Array getBestPheromone() {
		return bestPheromone;
	}

	public Array getHeuristicDistance() {
		return heuristicDistance;
	}

	@Override
	public String toString() {
		return to + "[p = " + pheromone + "; d = " + heuristicDistance + "]";
	}
	
	@Override
	public boolean equals(Object o) {
		Edge other = (Edge)o;
		return mutations.equals(other.mutations) && to.equals(other.to) && from.equals(other.from);
	}
	
	@Override
	public int hashCode() {
		return from.hashCode() + to.hashCode();
	}
} 
