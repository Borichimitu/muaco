package ru.ifmo.optimization.algorithm.muaco.ant.current;

import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;

public class CurrentAnt<Instance extends Constructable<Instance>,
				MutationType extends InstanceMutation<Instance>> extends AbstractAnt<Instance, MutationType> {
	public CurrentAnt(int antId, int numberOfAnts, Node<Instance> start, 
			Instance startInstance, 
			AbstractPathSelector<Instance, MutationType> pathSelector, 
			AbstractOptimizationTask<Instance> task,
			PheromoneUpdater<Instance, MutationType> pheromoneUpdater) {
		super(antId, numberOfAnts, start, startInstance, pathSelector, task, pheromoneUpdater);
	}

	@Override
	public boolean step(SearchGraph<Instance, MutationType> graph, AntStats stats) {
		if (isLimitExceeded()) {
			return false;
		}
		Pair<Edge, Instance> p = pathSelector.nextEdge(graph, node, instance, stats.getBestFitness(), antId, numberOfAnts);
		if (p == null) {
			stopNow = true;
			return false;
		}
		Edge edge = p.first;
		if (edge == null || p.second == null) {
			return false;
		}
		instance = p.second;
		node = edge.getDest();
		
		node.incrementNumberOfVisits();
		path.add(edge);
		
		FitnessValue fitness = node.getFitness();
		if (fitness.betterThanOrEqualTo(stats.getBestFitness())) {
			if (fitness.betterThan(stats.getBestFitness())) {
				System.out.println("current = " + fitness + "; best = " + fitness + "; size = " + graph.getNumberOfNodes());
//				double diff = Math.abs(task.getFitInstance(instance, null).getFitness().data[0] - fitness.data[0]);
//				if (diff > 1e-1) {
//					throw new RuntimeException("diff=" + diff);
//				}
				stats.setBest(node, instance, System.currentTimeMillis());
				stats.setLastBestFitnessColonyIterationNumber(stats.getColonyIterationNumber());
				stats.addHistory(task.getNumberOfFitnessEvaluations(), fitness);
			}
			if (fitness.betterThanOrEqualTo(task.getDesiredFitness())) {
				return true;
			}
		}
		return false;
	}
}
