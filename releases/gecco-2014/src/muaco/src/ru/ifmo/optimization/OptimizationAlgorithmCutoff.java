package ru.ifmo.optimization;

public class OptimizationAlgorithmCutoff {
	private int maxNumberOfFitnessEvaluations = -1;
	private double maxRunTime = -1;
	private double startTime = -1;
	private boolean terminateNow = false;
	
	public boolean doStop(int numberOfFitnessEvaluations) {
		if (terminateNow) {
			return true;
		}
		
		if (maxNumberOfFitnessEvaluations > 0 && numberOfFitnessEvaluations >= maxNumberOfFitnessEvaluations) {
			return true;
		}
		if (maxRunTime > 0 && (System.currentTimeMillis() - startTime) >  maxRunTime * 1000.0) {
			return true;
		}
		return false;
	}
	
	private OptimizationAlgorithmCutoff() {
	}
	
	public void terminateNow() {
		terminateNow = true;
	}
	
	private static OptimizationAlgorithmCutoff instance = new OptimizationAlgorithmCutoff();
	
	public static OptimizationAlgorithmCutoff getInstance() {
		return instance;
	}
	
	public void setCutoff(int maxNumberOfFitnessEvaluations, double maxRunTimeSeconds, double startTime) {
		this.maxNumberOfFitnessEvaluations = maxNumberOfFitnessEvaluations;
		this.maxRunTime = maxRunTimeSeconds;
		this.startTime = startTime;
		terminateNow = false;
	}
}
