package ru.ifmo.optimization.instance.fsm.task.tests.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.tests.generator.config.TestGeneratorConfig;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.RandomFSMGeneratorForTests;
import ru.ifmo.util.RandomProvider;

public class TestsGenerator implements Runnable {
	private class TestAndCoveredTransitions {
		private AutomatonTest test;
		private Set<Transition> coveredTransitions;
		
		public TestAndCoveredTransitions(AutomatonTest test, Set<Transition> coveredTransitions) {
			this.test = test;
			this.coveredTransitions = coveredTransitions;
		}

		public AutomatonTest getTest() {
			return test;
		}

		public Set<Transition> getCoveredTransitions() {
			return coveredTransitions;
		}
	}
	
	public abstract class TestLengthProvider {
		public abstract int getTestLength();
	}
	
	public class ConstTestLengthProvider extends TestLengthProvider {
		private int length;
		
		public ConstTestLengthProvider(int length) {
			this.length = length;
		}
		
		@Override
		public int getTestLength() {
			return length;
		}
	}
	
	public Collection<AutomatonTest> generateTests(FSM fsm, List<String> events, TestLengthProvider testLengthProvider, int numberOfTests) {
		Set<AutomatonTest> tests = new HashSet<AutomatonTest>(numberOfTests);
		Set<Transition> coveredTransitions = new HashSet<Transition>();
		int numberOfTransitions = fsm.getNumberOfStates() * events.size();
		while (tests.size() < numberOfTests) {			
			TestAndCoveredTransitions metaData = generateTest(fsm, events, testLengthProvider.getTestLength());
			tests.add(metaData.getTest());
			coveredTransitions.addAll(metaData.getCoveredTransitions());
			System.out.println("tests size = " + tests.size() + "; covered " + coveredTransitions.size() + " out of " + numberOfTransitions);
		}

		return tests;
	}
	
	private TestAndCoveredTransitions generateTest(FSM fsm, List<String> events, int length) {
		int currentState = fsm.getInitialState();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		for (int i = 0; i < length; i++) {
			int eventIndex = RandomProvider.getInstance().nextInt(events.size());
			input.add(events.get(eventIndex));
			Transition t = fsm.getTransition(currentState, eventIndex);
			Transition tr = new Transition(currentState, t.getEndState(), t.getEvent(), t.getAction());
			visitedTransitions.add(tr);
			output.add(t.getAction());
			currentState = t.getEndState();
		}
		return new TestAndCoveredTransitions(new AutomatonTest(input, output), visitedTransitions);
	}
	
	protected void printTests(Collection<AutomatonTest> tests, String filename) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		out.println(tests.size());
		for (AutomatonTest test : tests) {
			out.println(test);
		}
		out.close();
	}
	
 	public void run() {
 		TestGeneratorConfig config = new TestGeneratorConfig();
		List<String> events = config.getEvents();
		List<String> actions = new ArrayList<String>();
		FSM.setEvents(events);
		actions.add("");
		actions.add("0");
		actions.add("1");
		FSM fsm = RandomFSMGeneratorForTests.generateRandomFSM(
				config.getNumberOfStates(), events, 
				actions, config.getMaximumLengthOfOutputSequence());
		
		Set<Transition> visitedTransitions = new HashSet<Transition>();
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			for (int j = 0; j < fsm.getNumberOfEvents(); j++) {
				Transition t = fsm.getTransition(i, j);
				Transition tr = new Transition(i, t.getEndState(), t.getEvent(), t.getAction());
				visitedTransitions.add(tr);
			}
		}
		
		FsmMetaData md = new FsmMetaData(fsm, visitedTransitions, new FitnessValue(100));
		md.printToGraphViz(".");
		md.printTransitionDiagram(".");
		
		printTests(generateTests(fsm, events, new ConstTestLengthProvider(config.getLengthOfTestInTrainingSet()), config.getTrainingSetSize()), "training-set");
		printTests(generateTests(fsm, events, new ConstTestLengthProvider(config.getLengthOfTestInTestSet()), config.getTestSetSize()), "test-set");
 	}
	
	public static void main(String[] args) {
		new Thread(new TestsGenerator()).start();
	}
}
