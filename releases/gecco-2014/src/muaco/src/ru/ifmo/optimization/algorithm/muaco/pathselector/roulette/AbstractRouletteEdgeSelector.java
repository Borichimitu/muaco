package ru.ifmo.optimization.algorithm.muaco.pathselector.roulette;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.Edge;

public abstract class AbstractRouletteEdgeSelector {
	protected final double alpha;
	protected final double beta;
	
	public AbstractRouletteEdgeSelector(double alpha, double beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
	
	public abstract Edge select(List<Edge> edges, int antNumber, int numberOfAnts);
}
