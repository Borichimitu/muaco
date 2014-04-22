package ru.ifmo.optimization.instance;

import java.util.List;

import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public interface Constructable<Instance> extends Hashable {
	void applyMutations(List<InstanceMutation<Instance>> mutations);
	Instance copyInstance(Instance other);
	int getMaxNumberOfMutations();
}
