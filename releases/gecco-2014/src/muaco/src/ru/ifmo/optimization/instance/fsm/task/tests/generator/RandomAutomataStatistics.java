package ru.ifmo.optimization.instance.fsm.task.tests.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.RandomFSMGeneratorForTests;
import ru.ifmo.util.Pair;

public class RandomAutomataStatistics implements Runnable {
	public static void main(String[] args) {
		new Thread(new RandomAutomataStatistics()).start();
	}
	
	private Pair<Integer, Integer> getDepthAndNumberOfReachableStates() {
		List<String> events = new ArrayList<String>();
		List<String> actions = new ArrayList<String>();
		events.add("0");
		events.add("1");
		actions.add("0");
		actions.add("1");
		FSM fsm = RandomFSMGeneratorForTests.generateRandomFSM(50, events, actions, 1);
		return new Pair<Integer, Integer>(fsm.getDepth(), fsm.getNumberOfReachableStates());
	}
	
	private void printArrayToFile(int[] array, String filename) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (int i : array) {
			out.println(i);
		}
		
		out.close();
	}
	
	@Override
	public void run() {
		int numberOfStates = 50;
		int[] depthHistogram = new int[numberOfStates];
		int[] numberOfReachableStatesHistogram = new int[numberOfStates];
		Arrays.fill(depthHistogram, 0);
		Arrays.fill(numberOfReachableStatesHistogram, 0);
		
		for (int i = 0; i < 1000; i++) {
			Pair<Integer, Integer> p = getDepthAndNumberOfReachableStates();
			int depth = p.first;
			int numberOfReachableStates = p.second;
			depthHistogram[depth]++;
			numberOfReachableStatesHistogram[numberOfReachableStates]++;
		}
		
		printArrayToFile(depthHistogram, "depth-histogram");
		printArrayToFile(numberOfReachableStatesHistogram, "number-of-reachable-states-histogram");
	}
}
