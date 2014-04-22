package ru.ifmo.optimization.instance.fsm.task.transducer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.SimpleFsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.TestsTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.util.StringUtils;

public class TestsWithoutLabelingTask extends TestsTask {

	public TestsWithoutLabelingTask(AbstractTaskConfig config) {
		super(config);
		configure("tests.properties");
		comparator = new MaxSingleObjectiveComparator();
		FitnessValue.setComparatorData(comparator, new boolean[]{true});
	}
	
	@Override
	protected FsmMetaData runFsmOnTest(FSM fsm, int testIndex, boolean scenario) {
		int currentState = fsm.getInitialState();

		Set<Transition> visitedTransitions = new HashSet<Transition>();
		StringBuilder answers = new StringBuilder();
		for (int i = 0; i < tests[testIndex].getInput().length; i++) {
			int eventIndex = events.indexOf(tests[testIndex].getInput()[i]);
			String answer = fsm.transitions[currentState][eventIndex].getAction();
			int nextState = fsm.transitions[currentState][eventIndex].getEndState();
			if (nextState == -1) {
				break;
			}
			Transition tr = new Transition(currentState, nextState, tests[testIndex].getInput()[i], answer);
			visitedTransitions.add(tr);
			currentState = nextState;
			answers.append(answer);
		}
		String answer = answers.toString();

		double f = Math.max(tests[testIndex].getOutputString().length(), answer.length()) == 0 
					? 1.0
					: (double)StringUtils.levenshteinDistance(tests[testIndex].getOutputString(), answer) / Math.max(tests[testIndex].getOutputString().length(), answer.length());
		return new FsmMetaData(fsm, visitedTransitions, new FitnessValue(f));
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM fsm, FitnessValue bestFitness) {
		long start = System.nanoTime();
		numberOfFitnessEvaluations++;
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(fsm, i, false);
			f1 += 1.0 - fsmRunData.getFitness().data[0];
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		fsm.clearUsedTransitions();
		
		FitInstance<FSM> result = new SimpleFsmMetaData(fsm, 
				new FitnessValue(100.0 * (f1 / (double)tests.length)));
		RunStats.TIME_FITNESS_COMPUTATION += (System.nanoTime() - start) / 1e9;
		return result;
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm, FitnessValue bestFitness) {
		long start = System.nanoTime();
		numberOfFitnessEvaluations++;
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(fsm, i, false);
			f1 += 1.0 - fsmRunData.getFitness().data[0];
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		fsm.clearUsedTransitions();
		InstanceMetaData<FSM> result =  new FsmMetaData(fsm, visitedTransitions, 
				new FitnessValue(100.0 * (f1 / (double)tests.length)));
		RunStats.TIME_FITNESS_COMPUTATION += (System.nanoTime() - start) / 1e9;
		return result;
	}
	
	@Override
	public void configure(String propertiesFilename) {
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(new File(propertiesFilename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		events = new ArrayList<String>();
		String eventArray[] = config.getProperty("events").split(" ");
		for (int i = 0; i < eventArray.length; i++) {
			events.add(eventArray[i]);
		}
		Collections.sort(events);
		actionsForEvolving = config.getProperty("actions").split(",");
		readTests(config.getProperty("tests"));
		outputs = new String[tests.length];
	}
	
	@Override
	public String[] getActions() {
		return actionsForEvolving;
	}
	
	@Override
	public ArrayList<String> getEvents() {
		return events;
	}

	@Override
	public Comparator<FitnessValue> getComparator() {
		return comparator;
	}
	
	@Override
	public FitnessValue correctFitness(FitnessValue fitness, FSM cachedInstance, FSM trueInstance) {
		return new FitnessValue(0);
	}
}
