package ru.ifmo.optimization.instance;

import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public interface Mutator<Instance, MutationType extends InstanceMutation<Instance>> {
	MutatedInstanceMetaData<Instance, MutationType> apply(Instance individual);
	Instance applySimple(Instance individual);
}
