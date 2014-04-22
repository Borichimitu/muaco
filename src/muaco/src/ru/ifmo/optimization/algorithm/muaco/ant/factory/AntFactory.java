package ru.ifmo.optimization.algorithm.muaco.ant.factory;

import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.config.AntConfig;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public abstract class AntFactory<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	protected AntConfig antConfig;
	protected AbstractOptimizationTask<Instance> task;
	protected PheromoneUpdater<Instance, MutationType> pheromoneUpdater;
	
	public AntFactory(AntConfig antConfig, AbstractOptimizationTask<Instance> task, PheromoneUpdater<Instance, MutationType> pheromoneUpdater){
		this.antConfig = antConfig;
		this.task = task;
		this.pheromoneUpdater = pheromoneUpdater;
	}
	
	public abstract AbstractAnt<Instance, MutationType> createAnt(int antId, int numberOfAnts, Node<Instance> start, Instance startInstance);
	
	public abstract AbstractAnt<Instance, MutationType> createAnt(int antId, int numberOfAnts, Node<Instance> start, AbstractPathSelector<Instance, MutationType> pathSelector, Instance startInstance);
}
