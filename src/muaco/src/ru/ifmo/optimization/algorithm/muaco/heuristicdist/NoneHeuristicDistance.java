package ru.ifmo.optimization.algorithm.muaco.heuristicdist;

import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Array;

public class NoneHeuristicDistance<Instance extends Constructable<Instance>> implements HeuristicDistance<Instance> {

	@Override
	public Array getHeuristicDistance(Node<Instance> from, Node<Instance> to) {
		return Array.getValues(AbstractOptimizationTask.DIMENSIONALITY, 1.0);
	}
}
