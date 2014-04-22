package ru.ifmo.optimization.algorithm.muaco.ant;

import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public abstract class AbstractAnt<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	protected final int antId;
	protected final int numberOfAnts;
	protected Node<Instance> start;
	protected Instance startInstance;
	protected AbstractPathSelector<Instance, MutationType> pathSelector;
	protected AbstractOptimizationTask<Instance> task; 
	protected PheromoneUpdater<Instance, MutationType> pheromoneUpdater;
	
	protected Node<Instance> node;
	protected Instance instance;
	protected Path path;
	protected boolean stopNow = false;
	
	public AbstractAnt(int antId, int numberOfAnts, Node<Instance> start, Instance startInstance, 
			AbstractPathSelector<Instance, MutationType> pathSelector, 
			AbstractOptimizationTask<Instance> task, 
			PheromoneUpdater<Instance, MutationType> pheromoneUpdater) {
		this.antId = antId;
		this.numberOfAnts = numberOfAnts;
		this.start = start;
		this.startInstance = startInstance;
		this.pathSelector = pathSelector;
		this.task = task;
		
		this.node = start;
		this.instance = startInstance;
		this.path = new Path();
	}
	
	/**
	 * 
	 * @param graph
	 * @param stats
	 * @return true if the global optimum was found
	 * @return false, otherwise
	 */
	public abstract boolean step(SearchGraph<Instance, MutationType> graph, AntStats stats);
	
	public boolean isLimitExceeded() {
		return OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations());
	}
	
	public Path getPath() {
		return path;
	}
	
	public boolean doStopNow() {
		return stopNow;
	}
}
