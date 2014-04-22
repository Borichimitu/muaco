package ru.ifmo.optimization.algorithm.muaco.ant;

import java.util.ArrayList;
import java.util.List;

import ru.ifmo.optimization.algorithm.muaco.graph.Node;
import ru.ifmo.optimization.algorithm.muaco.graph.Path;
import ru.ifmo.optimization.instance.FitnessValue;

public class AntStats<Instance> {
	private FitnessValue bestFitness;
	private Node bestNode;
	private Instance bestInstance;
	private int lastBestFitnessColonyIterationNumber;
	private int colonyIterationIndex;
	private double bestNodeGenerationTime;
	private double totalAntPathLength = 0;
	private double totalAntPathCount = 0;
	private List<FitnessValue> bestFitnessHistory = new ArrayList<FitnessValue>();
	private List<Integer> stepsHistory = new ArrayList<Integer>();
	
	public AntStats(int colonyIterationIndex, FitnessValue bestFitness, Node bestNode, int lastBestFitnessOccurence) {
		this.colonyIterationIndex = colonyIterationIndex;
		this.bestFitness = bestFitness;
		this.bestNode = bestNode;
		this.lastBestFitnessColonyIterationNumber = lastBestFitnessOccurence;
	}
	
	public FitnessValue getBestFitness() {
		return bestFitness;
	}
	
	public Node getBestNode() {
		return bestNode;
	}
	
	public Instance getBestInstance() {
		return bestInstance;
	}
	
	public int getLastBestFitnessOccurence() {
		return lastBestFitnessColonyIterationNumber;
	}
	
	public void setBest(Node bestNode, Instance bestInstance, double bestTime) {
		this.bestNode = bestNode;
		this.bestInstance = bestInstance;
		this.bestNodeGenerationTime = bestTime;
		this.bestFitness = bestNode.getFitness();
		
		if (Math.abs(bestNode.getFitness().data[0] - bestFitness.data[0]) > 1e-5) {
			throw new RuntimeException();
		}
	}
	
	public void setLastBestFitnessColonyIterationNumber(int lastBestFitnessOccurence) {
		this.lastBestFitnessColonyIterationNumber = lastBestFitnessOccurence;
	}
	
	public void addAntPathData(List<Path> paths) {
		for (Path path : paths) {
			totalAntPathLength += path.getLength();
		}
		totalAntPathCount += paths.size();
	}
	
	public double getMeanAntPathLength() {
		return totalAntPathCount == 0 ? 0 : totalAntPathLength / totalAntPathCount;
	}
	
	public void addHistory(int steps, FitnessValue bestFitness) {
		stepsHistory.add(steps);
		bestFitnessHistory.add(bestFitness);
	}
	
	public List<FitnessValue> getBestFitnessHistory() {
		return bestFitnessHistory;
	}
	
	public List<Integer> getStepsHistory() {
		return stepsHistory;
	}
	
	public int getColonyIterationNumber() {
		return colonyIterationIndex;
	}
	
	public void setColonyIterationNumber(int colonyIterationIndex) {
		this.colonyIterationIndex = colonyIterationIndex;
	}
	
	public double getBestNodeGenerationTime() {
		return bestNodeGenerationTime;
	}

    public double getLastFitness() {
        if (stepsHistory.isEmpty()) {
            return 0;
        }
        return stepsHistory.get(stepsHistory.size() - 1);
    }
}
