package ru.ifmo.optimization.algorithm.muaco.ant.current;

import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.config.AntConfig;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class CurrentAntFactory<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> 
			extends AntFactory<Instance, MutationType> {

	public CurrentAntFactory(AntConfig antConfig, AbstractOptimizationTask<Instance> task, 
			PheromoneUpdater<Instance, MutationType> pheromoneUpdater) {
		super(antConfig, task, pheromoneUpdater);
	}

	@Override
	public AbstractAnt<Instance, MutationType> createAnt(int antId, int numberOfAnts, Node<Instance> start, Instance startInstance) {
		return new CurrentAnt<Instance, MutationType>(antId, numberOfAnts, start, startInstance, antConfig.getPathSelector(), task, pheromoneUpdater);
	}
	
	@Override
	public AbstractAnt<Instance, MutationType> createAnt(int antId, int numberOfAnts, Node<Instance> start, AbstractPathSelector<Instance, MutationType> pathSelector, Instance startInstance) {
		return new CurrentAnt<Instance, MutationType>(antId, numberOfAnts, start, startInstance, pathSelector, task, pheromoneUpdater);
	}
}
