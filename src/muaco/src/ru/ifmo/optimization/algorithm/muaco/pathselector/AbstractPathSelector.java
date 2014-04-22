package ru.ifmo.optimization.algorithm.muaco.pathselector;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.muaco.pathselector.roulette.AbstractRouletteEdgeSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.roulette.SingleObjectiveRouletteEdgeSelector;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;

public abstract class AbstractPathSelector<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	protected int numberOfMutationsPerStep;
	protected int maxNumberOfMutations;
	protected AbstractOptimizationTask<Instance> task;
	protected List<Mutator<Instance, MutationType>> mutators = new ArrayList<Mutator<Instance, MutationType>>();
    protected AntStats antStats;
	protected AbstractRouletteEdgeSelector rouletteEdgeSelector;

	public AbstractPathSelector(AbstractOptimizationTask<Instance> task, List<Mutator<Instance, MutationType>> mutators,
                                PathSelectorConfig config, AntStats antStats) {
		this.task = task;		
		this.mutators = mutators;
        this.antStats = antStats;
		rouletteEdgeSelector = config.getRouletteEdgeSelector();
	}
	
	public abstract Pair<Edge, Instance> nextEdge(
			SearchGraph<Instance, MutationType> graph, Node<Instance> node, 
			Instance fsm, FitnessValue currentBestFitness,
			int antNumber, int numberOfAnts);
	
	public Pair<Edge, Instance> nextEdgeRhoulette(
			SearchGraph<Instance, MutationType> graph, Node<Instance> node, 
			Instance fsm, FitnessValue currentBestFitness,
			int antNumber, int numberOfAnts) {
		Edge edge = rouletteEdgeSelector.select(node.getEdges(), antNumber, numberOfAnts);
    	return new Pair<Edge, Instance>(edge, graph.getNodeInstance(edge.getDest()));
	}
	 
}
