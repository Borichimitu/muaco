package ru.ifmo.optimization.instance.mutation;

public interface InstanceMutation<Instance> {
	void apply(Instance instance);
}
