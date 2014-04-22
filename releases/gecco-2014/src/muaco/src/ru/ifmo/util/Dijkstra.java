package ru.ifmo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.instance.fsm.FSM;

public class Dijkstra {
	private FSM graph;
	private int start;
	private int shortestPathWeights[];
	private Set<Integer> unvisitedVertices;
	private List<Integer>[] paths;
	
	public Dijkstra(FSM graph, int start) {
		this.graph = graph;
		this.start = start; 
	}
	
	private boolean doTerminate() {
		boolean terminate = true;
		for (int i : unvisitedVertices) {
			if (shortestPathWeights[i] != Integer.MAX_VALUE) {
				terminate = false;
				break;
			}
		}
		return unvisitedVertices.size() == 0 || terminate;
	}
	
	public int[] run() {
		shortestPathWeights = new int[graph.getNumberOfStates()];
		unvisitedVertices = new HashSet<Integer>();
		Arrays.fill(shortestPathWeights, Integer.MAX_VALUE);
		shortestPathWeights[start] = 0;
		for (int i = 0; i < graph.getNumberOfStates(); i++) {
			unvisitedVertices.add(i);
		}
		
		paths = new ArrayList[graph.getNumberOfStates()];
		for (int i = 0; i < graph.getNumberOfStates(); i++) {
			paths[i] = new ArrayList<Integer>();
		}
		
		while (!doTerminate()) {
			int v = selectVertexWithMinimumDist();
			ArrayList<Integer> incidentVertices = selectEdgesFromU(v);
			for (Integer u : incidentVertices) {
				int weight = graph.hasTransitionFromUtoV(v, u) ? 1 : Integer.MAX_VALUE;
				if (shortestPathWeights[u] > shortestPathWeights[v] + weight) {
					shortestPathWeights[u] = shortestPathWeights[v] + weight;
					paths[u].addAll(paths[v]);
					paths[u].add(u);
				}
			}
			unvisitedVertices.remove(v);
		}
		
		return shortestPathWeights;
	}
	
	private int selectVertexWithMinimumDist() {
		int result = 0;
		int minDist = Integer.MAX_VALUE;
		
		for (Integer u : unvisitedVertices) {
			if (shortestPathWeights[u] < minDist) {
				minDist = shortestPathWeights[u];
				result = u;
			}
		}
		
		return result;
	}
	
	private ArrayList<Integer> selectEdgesFromU(int u) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < graph.getNumberOfStates(); i++) {
			if (graph.hasTransitionFromUtoV(u, i)) {
				result.add(i);
			}
		}
		
		return result;
	}
}
