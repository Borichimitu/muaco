package ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.current;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.util.Array;

public class GlobalElitistMinBoundPheromoneUpdater<Instance extends Constructable<Instance>, MutationType
			extends InstanceMutation<Instance>> extends PheromoneUpdater<Instance, MutationType> {
	
	private double minPheromone = 0.001;
	private final Array minPheromoneArray;
	
	public GlobalElitistMinBoundPheromoneUpdater(int dimensionality) {
		super(dimensionality);
		minPheromoneArray = Array.getValues(dimensionality, minPheromone);
	}
	
	@Override
	public void updatePheromone(SearchGraph<Instance, MutationType> graph, List<Path> iterationPaths, 
			Path iterationBestPath, Path bestPath, double evaporationRate, boolean useRisingPaths) {
		
		//elitism - update best pheromone for all paths 
		for (Path path : iterationPaths) {
			path.updateBestPheromone();
		}
		
		//elitism - update best pheromone for global best path
		bestPath.updateBestPheromone();
		
		//evaporate and imprint pheromone on all edges
		for (Edge e : graph.getEdges()) {
			e.setPheromone(Array.max(minPheromoneArray, e.getPheromone().multiply(1.0 - evaporationRate)).add(e.getBestPheromone()));
		}

		if (useRisingPaths) {
			List<Path> risingIterationPaths = new ArrayList<Path>();
			for (Path path : iterationPaths) {
				risingIterationPaths.add(path.getRisingPath(graph));
			}
			updatePheromone(graph, risingIterationPaths, iterationBestPath.getRisingPath(graph), 
					bestPath.getRisingPath(graph), evaporationRate, false);
		}
	}
	
	@Override
	public void initializePheromone(Edge edge) {
		edge.setPheromone(minPheromoneArray);
		edge.setBestPheromone(minPheromoneArray);
	}
	
	@Override
	public double getMinPheromoneBound() {
		return minPheromone;
	}
}
