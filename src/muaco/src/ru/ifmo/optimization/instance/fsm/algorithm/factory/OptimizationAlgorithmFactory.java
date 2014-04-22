package ru.ifmo.optimization.instance.fsm.algorithm.factory;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

public abstract class OptimizationAlgorithmFactory<Instance extends Hashable> {
	
	protected OptimizationRunnerConfig runnerConfig;
	
	public OptimizationAlgorithmFactory(OptimizationRunnerConfig runnerConfig) {
		this.runnerConfig = runnerConfig;
	}
	
	public abstract AbstractOptimizationAlgorithm<Instance> createOptimizationAlgorithm(AbstractTaskFactory<Instance> taskFactory);
}
