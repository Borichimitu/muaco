package ru.ifmo.optimization.algorithm.muaco.mutator;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public class MutatedInstanceMetaData<Instance, MutationType extends InstanceMutation<Instance>> {
	private Instance instance;
	private MutationCollection<MutationType> mutations;
	
	public MutatedInstanceMetaData(Instance instance, MutationCollection<MutationType> mutations) {
		this.instance = instance;
		this.mutations = mutations;
	}
	
	public Instance getInstance() {
		return instance;
	}
	
	public MutationCollection<MutationType> getMutations() {
		return mutations;
	}
}
