package ru.ifmo.optimization.algorithm.muaco.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ru.ifmo.optimization.algorithm.muaco.ant.AntStats;
import ru.ifmo.optimization.algorithm.muaco.ant.config.AntConfig;
import ru.ifmo.optimization.algorithm.muaco.ant.current.CurrentAntConfig;
import ru.ifmo.optimization.algorithm.muaco.ant.current.CurrentAntFactory;
import ru.ifmo.optimization.algorithm.muaco.ant.factory.AntFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.consecutive.ConsecutiveAntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.factory.AntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.colony.stepbystep.StepByStepAntColonyFactory;
import ru.ifmo.optimization.algorithm.muaco.graph.SearchGraph;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.DestinationFitnessHeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.FitnessDifferenceHeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.HeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.heuristicdist.NoneHeuristicDistance;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.HeuristicAntPathSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.config.PathSelectorConfig;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.PheromoneUpdater;
import ru.ifmo.optimization.algorithm.muaco.pheromoneupdater.current.GlobalElitistMinBoundPheromoneUpdater;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.BestNodeStartNodesSelector;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.BestPathStartNodesSelector;
import ru.ifmo.optimization.algorithm.muaco.startnodesselector.StartNodesSelector;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.InitialFSMGenerator;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeFinalStateMutator;
import ru.ifmo.optimization.instance.fsm.mutator.ChangeOutputActionMutator;
import ru.ifmo.optimization.instance.fsm.mutator.efsm.EFSMAddOrDeleteTransitionMutator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig.InstanceType;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public class MuACOConfig<Instance extends Constructable<Instance>, MutationType extends InstanceMutation<Instance>> {
	
	private InstanceType instanceType;
	
	public static enum MutatorType {
		CHANGE_DEST,
		CHANGE_ACTIONS,
		CHANGE_EVENT,
		EFSM_ADD_DELETE_TRANSITIONS,
	}
	
	private static enum PathSelectorType {
		HEURISTIC
	}
	
	private static enum StartNodesSelectorType {
		BEST_PATH,
		BEST
	}
	
	private static enum AntType {
		CURRENT
	}
	
	private static enum PheromoneUpdaterType {
		GLOBAL_ELITIST_MIN_BOUND,
	}
	
	private static enum AntColonyFactoryType {
		CONSECUTIVE,
		STEP_BY_STEP
	}
	
	private static enum HeuristicDistanceType {
		NONE,
		ABS_DIFF,
		DEST_FITNESS
	}
	
	private static enum InstanceGeneratorType {
		PLAIN
	}
	
	private static enum ConstructionGraphType {
		PLAIN
	}
	
	private Properties properties = new Properties();
	
	public MuACOConfig(String propertiesFileName) {
		try {
			properties.load(new FileInputStream(new File(propertiesFileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public double getEvaporationRate() {
		return Double.parseDouble(properties.getProperty("evaporation-rate"));
	}
	
	public int getStagnationParameter() {
		return Integer.parseInt(properties.getProperty("stagnation-parameter"));
	}
	
	public int getBigStagnationParameter() {
		return Integer.parseInt(properties.getProperty("big-stagnation-parameter"));
	}
	
	public int getMaxEvaluationsTillStagnation() {
		return Integer.parseInt(properties.getProperty("max-evals-till-stagnation"));
	}
	
	public int getOutputPeriod() {
		return Integer.parseInt(properties.getProperty("output-period"));
	}
	
	public int getNumberOfMutationsPerStep() {
		return Integer.parseInt(properties.getProperty("number-of-mutations-per-step"));
	}
	
	public int getNumberOfAnts() {
		return Integer.parseInt(properties.getProperty("number-of-ants"));
	}
	
	public double getNewMutationProbability() {
		return Double.parseDouble(properties.getProperty("new-mutation-probability"));
	}
	
	public int getMaxNumberOfNodes() {
		return Integer.parseInt(properties.getProperty("max-number-of-nodes"));
	}
	
	public double getMultipleMutationProbability() {
		return Double.parseDouble(properties.getProperty("multiple-mutation-probability"));
	}
	
	public boolean useRisingPaths() {
		return Boolean.parseBoolean(properties.getProperty("use-rising-paths", "0"));
	}
	
	public int getMaxCanonicalCacheSize() {
		return Integer.parseInt(properties.getProperty("max-canonical-cache-size"));
	}
	
	public int getNumberOfThreads() {
		return Integer.parseInt(properties.getProperty("number-of-threads", "1"));
	}
	
	public List<MutatorType> getMutatorTypes() {
		String[] listOfMutators = properties.getProperty("mutators").split(",");
		List<MutatorType> mutators = new ArrayList<MutatorType>();
		for (String s : listOfMutators) {
			MutatorType type = MutatorType.valueOf(s);
			mutators.add(type);
		}
		return mutators;
	}
	
	public List<Mutator<FSM, FsmMutation>> getFsmMutators(AbstractOptimizationTask<Instance> t) {
		List<Mutator<FSM, FsmMutation>> mutators = new ArrayList<Mutator<FSM, FsmMutation>>();
		for (MutatorType type : getMutatorTypes()) {
			switch (type) {
			case CHANGE_ACTIONS: {
				AbstractAutomatonTask task = (AbstractAutomatonTask)t;
				mutators.add(new ChangeOutputActionMutator(task.getActions(), task.getConstraints()));
				break;
			}
			case CHANGE_DEST:
				mutators.add(new ChangeFinalStateMutator());
				break;
				
			case EFSM_ADD_DELETE_TRANSITIONS: {
				AbstractAutomatonTask task = (AbstractAutomatonTask)t;
				double addDeleteTransitionProbability = Double.parseDouble(properties.getProperty("add-delete-transition-probability"));
				mutators.add(new EFSMAddOrDeleteTransitionMutator(task.getEvents(), addDeleteTransitionProbability));
				break;
			}
			}
		}
		return mutators;
	}
	
	public AbstractPathSelector<Instance, MutationType> getPathSelector(AbstractOptimizationTask<Instance> task, AntStats antStats) {
		PathSelectorType type;
		try {
			type = PathSelectorType.valueOf(properties.getProperty("path-selector"));
		} catch (Exception e) {
			return null;
		}
		
		switch (type) {
		case HEURISTIC:
			return new HeuristicAntPathSelector(task, getFsmMutators(task),
                    new PathSelectorConfig("heuristic-path-selector.properties"), antStats);
		}
		return null;
	}
	
	public StartNodesSelector<Instance, MutationType> getStartNodesSelector() {
		StartNodesSelectorType type;
		try {
			type = StartNodesSelectorType.valueOf(properties.getProperty("start-nodes-selector"));
		} catch (Exception e) {
			return null;
		}
		
		switch (type) {
		case BEST_PATH:
			return new BestPathStartNodesSelector();
		case BEST:
			return new BestNodeStartNodesSelector();
		}
		
		return null;
	}
	
	public AntFactory<Instance, MutationType> getAntFactory(AbstractOptimizationTask<Instance> task, AntStats antStats) {
		return getAntFactory(task, antStats, getPathSelector(task, antStats));
	}
	
	public AntFactory<Instance, MutationType> getAntFactory(AbstractOptimizationTask<Instance> task, AntStats antStats, AbstractPathSelector<Instance, MutationType> pathSelector) {
		AntType type;
		try {
			type = AntType.valueOf(properties.getProperty("ant-type"));
		} catch (Exception e) {
			return null;
		}
		
		AntConfig antConfig;
		switch (type) {
		case CURRENT:
			antConfig = new CurrentAntConfig(pathSelector, null);
			return new CurrentAntFactory<Instance, MutationType>(antConfig, task, getPheromoneUpdater(task));
		}
		
		return null;
	}
	
	public AntColonyFactory getAntColonyFactory() {
		AntColonyFactoryType type;
		try {
			type = AntColonyFactoryType.valueOf(properties.getProperty("ant-colony-type"));
		} catch (Exception e) {
			return null;
		}
		
		switch (type) {
		case CONSECUTIVE:
			return new ConsecutiveAntColonyFactory();
		case STEP_BY_STEP:
			return new StepByStepAntColonyFactory();
		}
		
		return null;
	}
	
	public PheromoneUpdater<Instance, MutationType> getPheromoneUpdater(AbstractOptimizationTask<Instance> task) {
		PheromoneUpdaterType type;
		try {
			type = PheromoneUpdaterType.valueOf(properties.getProperty("pheromone-updater"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		switch (type) {
		case GLOBAL_ELITIST_MIN_BOUND:
			return new GlobalElitistMinBoundPheromoneUpdater<Instance, MutationType>(AbstractOptimizationTask.DIMENSIONALITY);
		}
		
		return null;
	}
	
	public HeuristicDistance<Instance> getHeuristicDistance() {
		HeuristicDistanceType type;
		try {
			type = HeuristicDistanceType.valueOf(properties.getProperty("heuristic-distance"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		switch (type) {
		case ABS_DIFF:
			return new FitnessDifferenceHeuristicDistance<Instance>();
		case DEST_FITNESS:
			return new DestinationFitnessHeuristicDistance<Instance>();
		case NONE:
			return new NoneHeuristicDistance<Instance>();
		}
		
		return null;
	}

	public InstanceGenerator getInstanceGenerator() {
		InstanceGeneratorType type;
		try {
			type = InstanceGeneratorType.valueOf(properties.getProperty("instance-generator"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		switch (type) {
		case PLAIN:
			return new InitialFSMGenerator();
	
		}
		return null;
	}
	
	public SearchGraph<Instance, MutationType> getSearchGraph(PheromoneUpdater<Instance, MutationType> pheromoneUpdater, 
            HeuristicDistance<Instance> heuristicDistance, FitInstance<Instance> metaData) {
		ConstructionGraphType type;
		try {
			type = ConstructionGraphType.valueOf(properties.getProperty("construction-graph-type"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		switch (type) {
		case PLAIN:
			return new SearchGraph<Instance, MutationType>(pheromoneUpdater, heuristicDistance, metaData);
		}
		return null;
	}
 }
