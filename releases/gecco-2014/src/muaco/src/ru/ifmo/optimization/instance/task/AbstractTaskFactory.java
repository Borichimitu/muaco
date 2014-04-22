package ru.ifmo.optimization.instance.task;

import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public abstract class AbstractTaskFactory<Instance extends Hashable> {
	protected AbstractTaskConfig config;
	
	public AbstractTaskFactory(AbstractTaskConfig config) {
		this.config = config;
	}
	
	public abstract AbstractOptimizationTask<Instance> createTask();
}
