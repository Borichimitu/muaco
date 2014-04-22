package ru.ifmo.optimization.algorithm.muaco.startnodesselector;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.util.RandomProvider;

public class BestPathStartNodesSelector<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> extends StartNodesSelector<Instance, MutationType> {
	@Override
	public List<Node<Instance>> getStartNodes(SearchGraph<Instance, MutationType> graph, int numberOfAnts, List<Path> paths,
			Path bestPath, Path iterationBestPath) {
		List<Node<Instance>> startNodes = new ArrayList<Node<Instance>>(numberOfAnts);
    	for (int j = 0; j < numberOfAnts; j++) {
			Node<Instance> start = null;
			if (bestPath.getEdges().size() == 0 || j == 0) {
				start = graph.getRoot();
			} else {
				start = bestPath.getEdges().get(RandomProvider.getInstance().nextInt(bestPath.getEdges().size())).getDest();
			}
			startNodes.add(start);
    	}
    	return startNodes;
	}
}
