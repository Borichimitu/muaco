package ru.ifmo.optimization.algorithm.muaco.colony;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public abstract class AntColony<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	protected SearchGraph<Instance, MutationType> graph;
	protected List<Node<Instance>> startNodes;
	protected AntFactory<Instance, MutationType> antFactory;
	protected AntStats stats;
	protected AbstractOptimizationTask<Instance> task;
	protected int colonyStagnationParameter;
	protected List<AbstractAnt<Instance, MutationType>> ants = new ArrayList<AbstractAnt<Instance, MutationType>>();
	protected boolean foundGlobalOptimum = false;
	
	public AntColony(SearchGraph<Instance, MutationType> graph, List<Node<Instance>> startNodes, AntFactory<Instance, MutationType> antFactory, 
			AntStats stats, AbstractOptimizationTask<Instance> task) {
		this.graph = graph;
		this.startNodes = startNodes;
		this.antFactory = antFactory;
		this.stats = stats;
		this.task = task;
		
		int i = 0;
		for (Node<Instance> node: startNodes) {
			ants.add(antFactory.createAnt(i++, startNodes.size(), node, graph.getNodeInstance(node)));
		}
	}
	
	public boolean isLimitExceeded() {
		return OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations());
	}
	
	public abstract List<Path> run();
	
	public boolean hasFoundGlobalOptimum() {
		return foundGlobalOptimum;
	}
}
