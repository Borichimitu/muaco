package ru.ifmo.optimization.instance;

import ru.ifmo.optimization.task.AbstractOptimizationTask;

public interface InstanceGenerator {
	Hashable createInstance(AbstractOptimizationTask task);
}
