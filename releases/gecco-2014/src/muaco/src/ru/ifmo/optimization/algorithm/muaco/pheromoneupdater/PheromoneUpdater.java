package ru.ifmo.optimization.algorithm.muaco.pheromoneupdater;

import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.graph.Edge;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public abstract class PheromoneUpdater<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	private int dimensionality;

	public PheromoneUpdater(int dimensionality) {
		this.dimensionality = dimensionality;
	}
	public abstract void updatePheromone(SearchGraph<Instance, MutationType> graph, List<Path> iterationPaths, 
			Path iterationBestPath, Path bestPath, double evaporationRate, boolean useRisingPaths);
	
	public abstract void initializePheromone(Edge edge);
	
	public abstract double getMinPheromoneBound();
}
