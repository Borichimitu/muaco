package ru.ifmo.optimization.algorithm.muaco.colony.stepbystep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.ant.AbstractAnt;
import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.AntColony;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class StepByStepAntColony<Instance extends Constructable<Instance>, 
								  MutationType extends InstanceMutation<Instance>> extends AntColony<Instance, MutationType> {
	protected int antStagnationParameter;
	
	public StepByStepAntColony(SearchGraph<Instance, MutationType> graph, List<Node<Instance>> startNodes,
			AntFactory<Instance, MutationType> antFactory, AntStats stats, AbstractOptimizationTask<Instance> task, 
			int antStagnationParameter) {
		super(graph, startNodes, antFactory, stats, task);
		this.antStagnationParameter = antStagnationParameter;
	}

	@Override
	public List<Path> run() {
		List<Path> paths = new ArrayList<Path>();
		int[] antSteps = new int[ants.size()];
		FitnessValue[] antBestFitness = new FitnessValue[ants.size()];
		Arrays.fill(antSteps, 0);
		Arrays.fill(antBestFitness, FitnessValue.getMinValue());
		
		main: while (true) {
			for (int i = 0; i < ants.size(); i++) {
				if (antSteps[i] >= antStagnationParameter) {
					continue;
				}
				if (isLimitExceeded()) {
					break main;
				}
				
				AbstractAnt<Instance, MutationType> ant = ants.get(i);

				boolean foundOptimum = ant.step(graph, stats);
//				if (ant.getPath().getCurrentFitness().betterThanOrEqualTo(antBestFitness[i])) {
				if (ant.getPath().getCurrentFitness().betterThan(antBestFitness[i])) {
					antBestFitness[i] = ant.getPath().getBestFitness();
				} else {
					antSteps[i]++;
				}
				if (ant.doStopNow()) {
					antSteps[i] = antStagnationParameter;
				}

				// if the ant found the global optimum, add the path to
				// result
				// and return immediately
				if (foundOptimum) {
					foundGlobalOptimum = true;
					paths.add(ant.getPath());
					return paths;
				}
			}
			
			for (int i = 0; i < ants.size(); i++) {
				if (antSteps[i] < antStagnationParameter) {
					continue main;
				}
			}
			break;
		}
		
		for (int i = 0; i < ants.size(); i++) {
			if (ants.get(i).getPath().getEdges().size() > 0) {
				paths.add(ants.get(i).getPath());
			}
		}
		
		return paths;
	}
	
}
