package ru.ifmo.optimization.algorithm.muaco.heuristicdist;

import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Array;

public class FitnessDifferenceHeuristicDistance<Instance extends Constructable<Instance>> implements HeuristicDistance<Instance>{

	@Override
	public Array getHeuristicDistance(Node<Instance> from, Node<Instance> to) {
		double[] result = new double[AbstractOptimizationTask.DIMENSIONALITY];
		for (int i = 0; i < result.length; i++) {
			result[i] = Math.max(0.001, to.getFitness().data[i] - from.getFitness().data[i]);
		}
		return new Array(result);
	}
}
