package ru.ifmo.optimization.algorithm.muaco.pathselector.roulette;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.util.RandomProvider;

public class SingleObjectiveRouletteEdgeSelector extends AbstractRouletteEdgeSelector {
	
	public SingleObjectiveRouletteEdgeSelector(double alpha, double beta) {
		super(alpha, beta);
	}
	
	@Override
	public Edge select(List<Edge> edges, int antNumber, int numberOfAnts) {
		Collections.sort(edges, new Comparator<Edge>() {
			@Override
			public int compare(Edge arg0, Edge arg1) {
				return Double.compare(Math.pow(arg0.getPheromone().data[0], alpha) * Math.pow(arg0.getHeuristicDistance().data[0], beta), 
						Math.pow(arg1.getPheromone().data[0], alpha) * Math.pow(arg1.getHeuristicDistance().data[0], beta));
			}
		});
		
		int size = edges.size();
		double weight[] = new double[size];
		weight[0] = Math.pow(edges.get(0).getPheromone().data[0], alpha) * Math.pow(edges.get(0).getHeuristicDistance().data[0], beta);

		for (int i = 1; i < size; i++) {
			weight[i] = weight[i - 1] + 
					Math.pow(edges.get(i).getPheromone().data[0], alpha) * Math.pow(edges.get(i).getHeuristicDistance().data[0], beta);
		}
		double p = weight[size - 1] * RandomProvider.getInstance().nextDouble();
		int j = 0;

		while (p > weight[j]) {
			j++;
		}
		return edges.get(j);
	}
}
