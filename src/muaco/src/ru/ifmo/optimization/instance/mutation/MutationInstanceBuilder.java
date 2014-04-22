package ru.ifmo.optimization.instance.mutation;

import java.util.List;

import ru.ifmo.optimization.instance.Constructable;

public class MutationInstanceBuilder<Instance extends Constructable<Instance>> {
	public Instance buildInstance(Instance start, List<InstanceMutation<Instance>> mutations) {
		Instance result = start.copyInstance(start);
		result.applyMutations(mutations);
		return result;
	}
}
