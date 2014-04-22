package ru.ifmo.optimization.algorithm.muaco;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.AntColony;
import ru.ifmo.optimization.algorithm.muaco.colony.factory.AntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.StartNodesSelector;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.RandomProvider;

public class MuACO<Instance extends Constructable<Instance>, 
			MutationType extends InstanceMutation<Instance>> extends AbstractOptimizationAlgorithm<Instance> implements Callable<InstanceMetaData<Instance>> {
	private MuACOConfig config;
	private final int stagnationParameter;
	private final int bigStagnationParameter;
	private final int maxEvaluationsTillStagnation;
	private final int outputPeriod;
	private final int numberOfAnts;
	private final double evaporationRate;
	private final int maxNumberOfNodesInGraph;

	private SearchGraph<Instance, MutationType> graph;
	private Path bestPath = new Path();
	private StartNodesSelector<Instance, MutationType> startNodesSelector;

	private AntFactory<Instance, MutationType> antFactory;
	private AntColonyFactory<Instance, MutationType> antColonyFactory;
	private AntStats<Instance> stats;
	private PheromoneUpdater<Instance, MutationType> pheromoneUpdater;
	private InstanceGenerator instanceGenerator;
	
	private Instance globalBestSolution = null;
	private FitnessValue globalBestFitness = FitnessValue.getMinValue();
	private double globalBestSolutionGenerationTime = 0;

	public MuACO(
			MuACOConfig<Instance, MutationType> config,
			AbstractTaskFactory<Instance> taskFactory) {
		super(taskFactory);
		RandomProvider.register();
		this.config = config;
		this.stagnationParameter = config.getStagnationParameter();
		this.bigStagnationParameter = config.getBigStagnationParameter();
		this.maxEvaluationsTillStagnation = config.getMaxEvaluationsTillStagnation();
		this.numberOfAnts = config.getNumberOfAnts();
		this.outputPeriod = config.getOutputPeriod();
		this.maxNumberOfNodesInGraph = config.getMaxNumberOfNodes();
		this.evaporationRate = config.getEvaporationRate();
		
		instanceGenerator = config.getInstanceGenerator();

        stats = new AntStats<Instance>(0, FitnessValue.getMinValue(), null, 0);
		startNodesSelector = config.getStartNodesSelector();
		antFactory = config.getAntFactory(task, stats);
		antColonyFactory = config.getAntColonyFactory();
		pheromoneUpdater = config.getPheromoneUpdater(task);

		Instance startSolution = randomInstance();
		FitInstance<Instance> firstInstance = task.getFitInstance(startSolution, stats.getBestFitness());
		graph = config.getSearchGraph(pheromoneUpdater, config.getHeuristicDistance(), firstInstance);
		stats.setBest(graph.getNode(firstInstance.getInstance()), firstInstance.getInstance(), System.currentTimeMillis());
		globalBestSolution = startSolution.copyInstance(startSolution);
	}
	
	public int getNumberOfFitnessEvaluations() {
		return task.getNumberOfFitnessEvaluations();
	}
	
	public SearchGraph<Instance, MutationType> getSearchGraph() {
		return graph;
	}
	
	public void setAntFactory(AntFactory<Instance, MutationType> antFactory) {
		this.antFactory = antFactory;
	}
	
	public AbstractOptimizationTask<Instance> getTask() {
		return task;
	}
	
	public AntStats<Instance> getAntStats() {
		return stats;
	}
 
	private Instance randomInstance() {
		return (Instance) instanceGenerator.createInstance(task);
	}

	@Override
	public InstanceMetaData<Instance> runAlgorithm() {
		return runAlgorithm(randomInstance());
	}

	@Override
	public InstanceMetaData<Instance> call() throws Exception {
		RandomProvider.register();
		return runAlgorithm();
	}
	
	@Override
	public InstanceMetaData<Instance> runAlgorithm(Instance startSolution) {
		int colonyIterationNumber = 0;
		while (!stats.getBestFitness().betterThan(task.getDesiredFitness()) && !Thread.currentThread().isInterrupted()) {
			stats.setColonyIterationNumber(colonyIterationNumber);
			List<Node<Instance>> startNodes = startNodesSelector.getStartNodes(graph, numberOfAnts, null, bestPath, null);

			startNodes = startNodesSelector.getStartNodes(graph, numberOfAnts, null, bestPath, null);

			long startRunningColony = System.currentTimeMillis();
			AntColony<Instance, MutationType> antColony = antColonyFactory.createAntColony(graph, startNodes, antFactory, stats, task, stagnationParameter);
			List<Path> paths = antColony.run();
			System.out.println("    Running colony: " + (System.currentTimeMillis() - startRunningColony) / 1000.0 + " sec.");
			System.out.println("    Bundle hits = " + RunStats.GRAPH_BUNDLE_HITS);
			stats.addAntPathData(paths);

			if (paths.isEmpty()) {
				break;
			}
			Path iterationBestPath = Collections.max(paths);

			if (antColony.hasFoundGlobalOptimum()) {
				Instance bestInstance = stats.getBestInstance();

				InstanceMetaData<Instance> result = task.getInstanceMetaData(bestInstance, null);
				if (result.getFitness().data[0] < task.getDesiredFitness().data[0]) {
					System.out.println();
				}

				result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
				result.setHistory(stats.getBestFitnessHistory(), stats.getStepsHistory());
				result.setNodeVisitStats(graph.getNodeVisitStats());
				result.setInstanceGenerationTime(System.currentTimeMillis());
				return result;
			}

			if (iterationBestPath.getBestFitness().betterThanOrEqualTo(bestPath.getBestFitness())) {
				bestPath = new Path(iterationBestPath);
			}

			graph.updatePheromone(paths, iterationBestPath, bestPath, evaporationRate, config.useRisingPaths());

			writeStats(paths);

			if (doRestart()) {
				restart();
			}
			colonyIterationNumber++;

			if (OptimizationAlgorithmCutoff.getInstance().doStop(task.getNumberOfFitnessEvaluations())) {
				break;
			}
		}
		Instance bestInstance = stats.getBestFitness().betterThan(globalBestFitness) 
				? graph.getNodeInstance(stats.getBestNode()) 
						: globalBestSolution;
				double bestNodeGenerationTime = stats.getBestFitness().betterThan(globalBestFitness)
						? stats.getBestNodeGenerationTime() 
								: globalBestSolutionGenerationTime;
						InstanceMetaData<Instance> result = task.getInstanceMetaData(bestInstance, null);
						result.setNumberOfFitnessEvaluations(task.getNumberOfFitnessEvaluations());
						result.setHistory(stats.getBestFitnessHistory(), stats.getStepsHistory());
						result.setNodeVisitStats(graph.getNodeVisitStats());
						result.setInstanceGenerationTime(bestNodeGenerationTime);
						return result;
	}

	protected boolean doRestart() {
		if (stats.getColonyIterationNumber() - stats.getLastBestFitnessOccurence() > bigStagnationParameter) {
			System.out.println("Colony iteration stagnation reached");
			return true;
		}

		if (graph.getNumberOfNodes() > maxNumberOfNodesInGraph) {
			System.out.println("Reached max number of nodex in the construction graph");
			return true;
		}

		if (stats.getStepsHistory().size() > 0) {
			if (task.getNumberOfFitnessEvaluations() - stats.getStepsHistory().get(stats.getStepsHistory().size() - 1) > maxEvaluationsTillStagnation) {
				System.out.println("Stagnation by fitness evaluations reached");
				return true;
			}
		}

		return false;
	}

	protected void restart() {
		if (stats.getBestNode().getFitness().betterThan(globalBestFitness)) {
			globalBestSolution = graph.getNodeInstance(stats.getBestNode());
			globalBestFitness = stats.getBestFitness();
			globalBestSolutionGenerationTime = stats.getBestNodeGenerationTime();
		}

		long startRestart = System.currentTimeMillis();
		System.out.println("Restarting...");
		bestPath.clear();
		graph.clear();
		task.reset();
		
		System.gc();
		FitInstance<Instance> md = task.getFitInstance(randomInstance(), bestPath.getBestFitness());
		graph = config.getSearchGraph(pheromoneUpdater, config.getHeuristicDistance(), md);
		stats.setBest(graph.getNode(md.getInstance()), md.getInstance(), System.currentTimeMillis());

		stats.setLastBestFitnessColonyIterationNumber(stats.getColonyIterationNumber());
		System.out.println("Restart: "
				+ (System.currentTimeMillis() - startRestart) / 1000.0
				+ " sec.");
	}

	private void writeStats(List<Path> paths) {
		FitnessValue currentBestFitness = Collections.max(paths,
				new Comparator<Path>() {
					@Override
					public int compare(Path arg0, Path arg1) {
						return arg0.getBestFitness().compareTo(arg1.getBestFitness());
					}
				}).getBestFitness();

		FitnessValue meanFitness = new FitnessValue(paths.get(0).getBestFitness());
		if (paths.size() > 1) {
			for (int i = 1; i < paths.size(); i++) { 
				meanFitness.add(paths.get(i).getBestFitness());
			}
		}
		meanFitness.divideBy((double)paths.size());
		
		if (stats.getColonyIterationNumber() % outputPeriod == 0) {
			System.out.println(stats.getColonyIterationNumber()
					+ " : currentFitness = " + currentBestFitness
					+ "; meanFitness = " + meanFitness 
					+ "; best = " + stats.getBestFitness()); 
		}
	}
}
