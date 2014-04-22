package ru.ifmo.optimization.algorithm.muaco.graph;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public class MutationCollection<MutationType> {
	private List<MutationType> mutations = new ArrayList<MutationType>();
	
	public MutationCollection() {
	}
	
	public MutationCollection(MutationType mutation) {
		mutations.add(mutation);
	}
	
	public void add(MutationType mutation) {
		mutations.add(mutation);
	}
	
	public void addAll(MutationCollection<MutationType> other) {
		mutations.addAll(other.mutations);
	}
	
	public List<MutationType> getMutations() {
		return mutations;
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
	
	@Override
	public String toString() {
		String s = "[";
		for (MutationType m : mutations) {
			s += m.toString() + ",";
		}
		s += "]";
		return s;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
