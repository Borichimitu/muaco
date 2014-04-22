package ru.ifmo.optimization.algorithm.muaco.heuristicdist;

import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.util.Array;

public interface HeuristicDistance<Instance extends Constructable<Instance>> {
	Array getHeuristicDistance(Node<Instance> from, Node<Instance> to);
}
