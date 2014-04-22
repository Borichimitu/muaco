package ru.ifmo.optimization.instance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public abstract class InstanceMetaData<Instance> {
	protected Instance instance;
	protected List<FitnessValue> bestFitnessHistory;
	protected List<Integer> stepsHistory;
	protected int numberOfSteps;
	protected double time;                        //run time of the algorithm
	protected double instanceGenerationTime;      //time it took to generate this particular instance 
	protected FitnessValue fitness;
	protected double fitnessEvaluationTime;
	protected double canonizationTime;
	public double canonicalDistance;
	protected Map<Integer, Integer> nodeVisitStats;
	protected int numberOfCacheHits;
	protected int numberOfCanonicalCacheHits;
	protected int numberOfLazySavedFitnessEvals;
	protected int sharedBundleHits;
	
	public InstanceMetaData() {
	}
	
	public InstanceMetaData(Instance instance, FitnessValue fitness) {
		this.instance = instance;
		this.fitness = fitness;
		this.numberOfSteps = 0;
	}
	
	public void setSharedBundleHits(int sharedBundleHits) {
		this.sharedBundleHits = sharedBundleHits;
	}
	
	public void setNumberOfFitnessEvaluations(int numberOfFitnessEvaluations) {
		this.numberOfSteps = numberOfFitnessEvaluations;
	}
	
	public void setNodeVisitStats(Map<Integer, Integer> nodeVisitStats) {
		this.nodeVisitStats = nodeVisitStats;
	}
	
	public void setFitnessEvaluationTime(double fitnessEvaluationTime) {
		this.fitnessEvaluationTime = fitnessEvaluationTime;
	}
	
	public double getFitnessEvaluationTime() {
		return fitnessEvaluationTime;
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	
	public void setInstanceGenerationTime(double instanceGenerationTime) {
		this.instanceGenerationTime = instanceGenerationTime;
	}
	
	public Instance getInstance() {
		return instance;
	}
	
	public FitnessValue getFitness() {
		return fitness;
	}
	
	public int getNumberOfSteps() {
		return numberOfSteps;
	}
	
	public void setHistory(List<FitnessValue> bestFitnessHistory, List<Integer> stepsHistory) {
		this.bestFitnessHistory = bestFitnessHistory;
		this.stepsHistory = stepsHistory;
	}
	
	public void setNumberOfCacheHits(int numberOfCacheHits) {
		this.numberOfCacheHits = numberOfCacheHits;
	}
	
	public double getInstanceGenerationTime() {
		return instanceGenerationTime;
	}
	
	public void setNumberOfCanonicalCacheHits(int numberOfCanonicalCacheHits) {
		this.numberOfCanonicalCacheHits = numberOfCanonicalCacheHits;
	}
	
	public void setNumberOfLazySavedFitnessEvals(int numberOfLazySavedFitnessEvals) {
		this.numberOfLazySavedFitnessEvals = numberOfLazySavedFitnessEvals;
	}
	
	public void setCanonicalDistance(double canonicalDistance) {
		this.canonicalDistance = canonicalDistance;
	}
	
	public void setCanonizationTime(double canonizationTime) {
		this.canonizationTime = canonizationTime;
	}
	
	public void print(String dirname) {
		printMetaData(dirname);
//		printNodeVisitStats(dirname);
		printProblemSpecificData(dirname);
	}
	
	public abstract void printProblemSpecificData(String dirname);
	
	public void printMetaData(PrintWriter out) {
		out.println("fitness = " + fitness);
		out.println("step-count = " + numberOfSteps);
		out.println("time = " + time);
		//out.println("instance-gen-time = " + instanceGenerationTime);
		out.println("fitness-eval-time = " + fitnessEvaluationTime);
		out.println("number-of-cache-hits = " + numberOfCacheHits);
		out.println("number-of-canonical-cache-hits = " + numberOfCanonicalCacheHits);
		out.println("canon-distance = " + canonicalDistance);
		out.println("canonization-time = " + canonizationTime);
		out.println("number-of-lazy-saved-fitness-evals = " + numberOfLazySavedFitnessEvals);
		out.println("shared-bundle-hits = " + sharedBundleHits);
		if (bestFitnessHistory != null) {
			for (int i = 0; i < bestFitnessHistory.size(); i++) {
				out.println(stepsHistory.get(i) + " "
						+ bestFitnessHistory.get(i));
			}
		}
	}
	 
	public void printMetaData(String dirname) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(dirname + "/" + fitness + "_metadata"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		printMetaData(out);
		out.close();
	}
	
	public void printNodeVisitStats(String dirname) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(dirname + "/graph_stats"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int min = 0;
		int max = 0;
		double mean = 0;
		double sum = 0;
		
		for (Entry<Integer,Integer> e : nodeVisitStats.entrySet()) {
			if (e.getKey() < min) {
				min = e.getKey();
			} 
			if (e.getKey() > max) {
				max = e.getKey();
			}
			
			mean += e.getKey() * e.getValue();
			sum += e.getValue();
		}
		
		mean /= sum;
		
		out.println("min = " + min);
		out.println("mean = " + mean);
		out.println("max = " + max);
		
		for (Entry<Integer,Integer> e : nodeVisitStats.entrySet()) {
			out.println(e.getKey() + " " + e.getValue());
		}
		out.close();
	}
	
	@Override 
	public String toString() {
		return getFitness() + "";
	}
}
