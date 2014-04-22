package ru.ifmo.optimization.algorithm.muaco.colony.factory;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.AntColony;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public abstract class AntColonyFactory<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	public abstract AntColony<Instance, MutationType> createAntColony(SearchGraph<Instance, MutationType> graph, List<Node<Instance>> startNodes,
			AntFactory<Instance, MutationType> antFactory, AntStats stats, AbstractOptimizationTask<Instance> task, int antStagnationParameter);
}
