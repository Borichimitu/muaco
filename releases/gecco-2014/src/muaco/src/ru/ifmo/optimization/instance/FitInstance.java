package ru.ifmo.optimization.instance;

public class FitInstance<Instance extends Hashable> implements Comparable<FitInstance<Instance>> {
	protected Instance instance;
	protected FitnessValue fitness;
	
	public FitInstance(Instance instance, FitnessValue fitness) {
		this.instance = instance;
		this.fitness = fitness;
	}
	
	public FitInstance(FitInstance<Instance> other) {
		instance = other.instance;
		fitness = other.fitness;
	}
	
	public void setFitness(FitnessValue fitness) {
		this.fitness = fitness;
	}
	
	public FitnessValue getFitness() {
		return fitness;
	}
	
	public Instance getInstance() {
		return instance;
	}
	
	@Override
	public int compareTo(FitInstance<Instance> arg0) {
		return fitness.compareTo(arg0.fitness);
	}
}
