package ru.ifmo.optimization.algorithm.muaco.pathselector;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Pair;
import ru.ifmo.util.RandomProvider;

public class HeuristicAntPathSelector<Instance extends Constructable<Instance>, 
	MutationType extends InstanceMutation<Instance>> extends AbstractPathSelector<Instance, MutationType> {
	protected double newMutationProbability;	
	
	public HeuristicAntPathSelector(AbstractOptimizationTask<Instance> task, 
			List<Mutator<Instance, MutationType>> mutators, PathSelectorConfig config, 
			AntStats antStats) {
        super(task, mutators, config, antStats);
        numberOfMutationsPerStep = config.getIntProperty("numberOfMutationsPerStep");
        maxNumberOfMutations = task.getNeighborhoodSize();
        newMutationProbability = config.getDoubleProperty("newMutationProbability");
	}
	
	@Override
	public Pair<Edge, Instance> nextEdge(
			SearchGraph<Instance, MutationType> graph, Node<Instance> node, 
			Instance instance, FitnessValue currentBestFitness,
			int antNumber, int numberOfAnts) {
		if ((RandomProvider.getInstance().nextDouble() < newMutationProbability || !node.hasChildren()) && node.getNumberOfChildren() < maxNumberOfMutations) {
    		return bestMutation(graph, node, instance, currentBestFitness);
    	}
    	Edge edge = rouletteEdgeSelector.select(node.getEdges(), antNumber, numberOfAnts);
    	return new Pair<Edge, Instance>(edge, (Instance) graph.getNodeInstance(edge.getDest()));
	}
	
	protected FitInstance<Instance> applyFitness(Instance originalInstance, FitnessValue sourceFitness, 
		MutatedInstanceMetaData<Instance, MutationType> mutatedInstanceMetaData, FitnessValue currentBestFitness) {
		task.increaseNumberOfAttemptedFitnessEvaluations(1);
		return task.getFitInstance(mutatedInstanceMetaData.getInstance(), currentBestFitness);
	}
	
	protected Pair<Edge, Instance> bestMutation(
			SearchGraph<Instance, MutationType> graph, Node<Instance> node, Instance instance, FitnessValue currentBestFitness) {
		FitnessValue localBestFitness = FitnessValue.getMinValue();
    	Edge result = null;
    	Instance resultInstance = null;
    	int numberOfMutationsToMake = Math.min(numberOfMutationsPerStep, maxNumberOfMutations - node.getChildren().size());
    	for (int i = 0; i < numberOfMutationsToMake; i++) {
    		MutatedInstanceMetaData<Instance, MutationType> mutated = mutateInstance(instance);
    		if (mutated == null) {
    			continue;
    		}
    		Node<Instance> old = graph.getNode(mutated.getInstance());
    		
    		//if this is a new node
    		if (old == null) {
    			Edge edge = dealWithNewNode(graph, mutated, instance, node, currentBestFitness, localBestFitness);
    			if (edge.getDest().getFitness().betterThan(localBestFitness)) {
    				result = edge;
    				resultInstance = mutated.getInstance();
    				
    				localBestFitness = result.getDest().getFitness();
    				//if the ant found a globally acceptable solution, return it immediately
    				if (edge.getDest().getFitness().betterThanOrEqualTo(task.getDesiredFitness())) {
    					return new Pair<Edge, Instance>(result, resultInstance);
    				}
    			}
    		} else {
    			RunStats.N_CACHE_HITS++;
    			//if this is an old node
    			Edge edge = node.getChild(mutated);
    			if (edge == null) {
    				edge = graph.addEdge(node, mutated.getMutations(), old);
    			}
    			if (old.getFitness().betterThanOrEqualTo(localBestFitness)) {
    				result = edge;
    				resultInstance = (Instance) graph.getNodeInstance(old);
    				localBestFitness = old.getFitness();
    			}
    		}
    	}
    	return new Pair<Edge, Instance>(result, resultInstance);
    }
	
	protected MutatedInstanceMetaData<Instance, MutationType> mutateInstance(Instance instance) {
		long start = System.currentTimeMillis();
		Mutator<Instance, MutationType> mutator = mutators.get(RandomProvider.getInstance().nextInt(mutators.size()));
        MutatedInstanceMetaData<Instance, MutationType> result = mutator.apply(instance);
        RunStats.TIME_MUTATING += (System.currentTimeMillis() - start) / 1000.0;
        return result;
    }
	
	protected Edge dealWithNewNode(
			SearchGraph<Instance, MutationType> graph, MutatedInstanceMetaData<Instance, MutationType> mutated,
			Instance instance, Node<Instance> node,
			FitnessValue currentBestFitness, FitnessValue localBestFitness) {
		FitInstance<Instance> metaData = applyFitness(instance, node.getFitness(), mutated, currentBestFitness);
		Edge edge = graph.addNode(node, mutated.getMutations(), metaData, task.getNumberOfFitnessEvaluations());
		return edge;
		
	}
}
