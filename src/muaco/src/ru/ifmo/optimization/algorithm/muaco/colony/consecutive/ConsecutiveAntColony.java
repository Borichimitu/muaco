package ru.ifmo.optimization.algorithm.muaco.colony.consecutive;

import java.util.ArrayList;
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

/**
 * 
 * @author Danii Chivilikhin
 *
 * @param <Instance>
 * Launches the ants consecutively, one by one.
 */
public class ConsecutiveAntColony<Instance extends Constructable<Instance>, 
								    MutationType extends InstanceMutation<Instance>> extends AntColony<Instance, MutationType> {
	protected int antStangationParameter;
	
	public ConsecutiveAntColony(
			SearchGraph<Instance, MutationType> graph, List<Node<Instance>> startNodes, 
			AntFactory<Instance, MutationType> antFactory, 
			AntStats stats, AbstractOptimizationTask<Instance> task, 
			int antStagnationParameter) {
		super(graph, startNodes, antFactory, stats, task);
		this.antStangationParameter = antStagnationParameter;
	}
	
	@Override
	public List<Path> run() {
		List<Path> paths = new ArrayList<Path>();
		for (AbstractAnt<Instance, MutationType> ant : ants) {
			int step = 0;
			FitnessValue localBestFitness = FitnessValue.getMinValue();
			
			while (step < antStangationParameter && !isLimitExceeded()) {
				boolean foundOptimum = ant.step(graph, stats);
				if (ant.getPath().getCurrentFitness().betterThanOrEqualTo(localBestFitness)) {
					localBestFitness = ant.getPath().getBestFitness();
				} else {
					step++; 
				}
				
				//if the ant found the global optimum, add the path to the result
				//and return immediately
				if (foundOptimum) {
					foundGlobalOptimum = true;
					paths.add(ant.getPath());
					return paths;
				}
			}
			paths.add(ant.getPath());
		}
		
		return paths;
	}
}
