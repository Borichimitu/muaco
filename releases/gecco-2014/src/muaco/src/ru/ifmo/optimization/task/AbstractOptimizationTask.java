package ru.ifmo.optimization.task;

import java.util.Comparator;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.Hashable;
import ru.ifmo.optimization.instance.InstanceMetaData;

public abstract class AbstractOptimizationTask<Instance extends Hashable> {
	protected FitnessValue desiredFitness;
	protected int numberOfFitnessEvaluations = 0;
	protected int numberOfAttemptedFitnessEvaluations = 0;
	public Comparator<FitnessValue> comparator;
	
	public static int DIMENSIONALITY;
	
	public AbstractOptimizationTask(int dim) {
		DIMENSIONALITY = dim;
	}
	
	public abstract FitInstance<Instance> getFitInstance(Instance instance, FitnessValue lastbestFitness);
	
	public abstract InstanceMetaData<Instance> getInstanceMetaData(Instance instance, FitnessValue lastbestFitness);
	
	public abstract FitnessValue correctFitness(FitnessValue fitness, Instance cachedInstance, Instance trueInstance);
	
	public abstract Comparator<FitnessValue> getComparator();
	
	public void setDesiredFitness(FitnessValue desiredFitness) {
		this.desiredFitness = desiredFitness;
	}
	
	public FitnessValue getDesiredFitness() {
		return desiredFitness;
	}
	
	public int getNumberOfFitnessEvaluations() {
		return numberOfFitnessEvaluations;
	}
	
	public int getNumberOfAttemptedFitnessEvaluations() {
		return numberOfAttemptedFitnessEvaluations;
	}
	
	public void increaseNumberOfAttemptedFitnessEvaluations(int value) {
		numberOfAttemptedFitnessEvaluations += value;
	}
	
	public abstract int getNeighborhoodSize();
	
	public void reset() {
	}
}
