package ru.ifmo.optimization;

import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.task.AbstractOptimizationTask;

public abstract class AbstractOptimizationAlgorithm<Instance extends Hashable> {
	protected AbstractOptimizationTask<Instance> task;
	
	public AbstractOptimizationAlgorithm(AbstractTaskFactory<Instance> taskFactory) {
		this.task = taskFactory.createTask();
	}
	
	public AbstractOptimizationAlgorithm(AbstractOptimizationTask<Instance> task) {
		this.task = task;
	}
	
	public void setTask(AbstractOptimizationTask<Instance> task) {
		this.task = task;
	}
	
	public abstract InstanceMetaData<Instance> runAlgorithm();
	
	public InstanceMetaData<Instance> runAlgorithm(Instance startSolution) {
		return null;
	}
	
}

