package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions.ComplianceChecker;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.util.Util;

public class TestsWithLabelingTask extends TestsTask {
	
	protected Properties testsProperties;
	private boolean useTests;

	public TestsWithLabelingTask(AbstractTaskConfig config) {
		super(config);
		configure(config.getProperty("tests"));
		try {
			ComplianceChecker.createComplianceChecker(events);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		comparator = new MaxSingleObjectiveComparator();
		FitnessValue.setComparatorData(comparator, new boolean[]{true});
	}

	@Override
	public void configure(String propertiesFilename) {
		testsProperties = new Properties();
		try {
			testsProperties.load(new FileInputStream(new File(propertiesFilename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		events = new ArrayList<String>();
		String eventArray[] = testsProperties.getProperty("events").split(" ");
		for (int i = 0; i < eventArray.length; i++) {
			events.add(eventArray[i]);
		}
		Collections.sort(events);
		actions = testsProperties.getProperty("actions").split(",");
		
		int minNumberOfOutputActions = Integer.parseInt(testsProperties.getProperty("min-number-of-actions"));
		int maxNumberOfOutputActions = Integer.parseInt(testsProperties.getProperty("max-number-of-actions"));
		actionsForEvolving = new String[maxNumberOfOutputActions - minNumberOfOutputActions + 1];
		for (int i = 0; i < actionsForEvolving.length; i++) {
			actionsForEvolving[i] = "" + (i + minNumberOfOutputActions);
		}
		useTests = Boolean.parseBoolean(testsProperties.getProperty("use-tests"));
		if (useTests) {
			readTests(testsProperties.getProperty("tests"));
			outputs = new String[tests.length];
		}
	}

	@Override
	public List<String> getEvents() {
		return events;
	}

	@Override
	public String[] getActions() {
		return actionsForEvolving;
	}
	
	protected FSM labelFSM(FSM fsm, boolean scenarios) {
		FSM result = new FSM(fsm);
		LabellingTable table = new LabellingTable(fsm.getNumberOfStates(), fsm.getNumberOfEvents());
		for (int i = 0; i < tests.length; i++) {
			editLabellingTable(fsm, tests[i], table, scenarios);
		}
		
		for (int i = 0; i < fsm.getNumberOfStates(); i++) {
			for (int j = 0; j < fsm.getNumberOfEvents(); j++) {
				result.transitions[i][j].setAction(table.getActions(i, j));
			}
		}
		return result;
	}
	
	private void editLabellingTable(FSM fsm, AutomatonTest test, LabellingTable table, boolean scenarios) {
		int currentState = fsm.getInitialState();
		int counter = 0;
		int i = 0; 
		for (String event : test.getInput()) {
			int eventIndex = events.indexOf(event);
			
			Transition t = fsm.transitions[currentState][eventIndex];
			if (t == null) {
				return;
			}
			if (t.getEndState() == -1) {
				return;
			}
			
			if (scenarios) {
				table.add(currentState, eventIndex, test.getOutput()[i]);
			} else {
				StringBuilder sequence = new StringBuilder();
				int numberOfActions = Integer.parseInt(t.getAction());
				for (int j = 0; j < numberOfActions; j++) {
					if (counter < test.getOutput().length) {
						sequence.append(test.getOutput()[counter++]);
					} else {
						sequence.append("??");
					}
				}
				table.add(currentState, eventIndex, sequence.toString());
			}
			currentState = t.getEndState();
			i++;
		}
	}

	@Override
	public FitInstance<FSM> getFitInstance(FSM fsm, FitnessValue bestFitness) {
		InstanceMetaData<FSM> fsmMetaData = getInstanceMetaData(fsm, bestFitness);
		return new FitInstance<FSM>(fsmMetaData.getInstance(), fsmMetaData.getFitness());
	}
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm, FitnessValue bestFitness) {
		numberOfFitnessEvaluations++;
		FSM labelled = labelFSM(fsm, false);
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		int numberOfSuccesses = 0; 
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(labelled, i, false);
			if (fsmRunData == null) {
				continue;
			}
			if (fsmRunData.getFitness().data[0] < 1e-5) {
				numberOfSuccesses++;
			}
			f1 += 1.0 - fsmRunData.getFitness().data[0];
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}
		
		int numberOfIncompliantTransitions = 0;
		
		if (penalizeForIncompliance) {
			for (int state = 0; state < fsm.getNumberOfStates(); state++) {
				List<Transition> usedTransitions = new ArrayList<Transition>();
				for (int event = 0; event < fsm.transitions[state].length; event++) {
					if (fsm.isTransitionUsed(state, event)) {
						usedTransitions.add(fsm.transitions[state][event]);
					}				
				}
				numberOfIncompliantTransitions += Util.numberOfIncompliantTransitions(usedTransitions);
			}
		}
		double incompliantTransitionsRatio = visitedTransitions.isEmpty() ? 0 : (double)numberOfIncompliantTransitions / (double)visitedTransitions.size();
		
		if (terminateWhenAllTestsPass) {
			if (numberOfSuccesses == tests.length && numberOfIncompliantTransitions == 0) {
				OptimizationAlgorithmCutoff.getInstance().terminateNow();
			}
		}
		
		if (numberOfSuccesses == tests.length) {
			double fitness = (20.0 + 0.01 * (100.0 - visitedTransitions.size())) * (1.0 - incompliantTransitionsRatio);
			return new FsmMetaData(fsm, visitedTransitions, new FitnessValue(fitness));
		}
		double fitness = (10.0 * (f1 / (double)tests.length) + 0.01 * (100.0 - visitedTransitions.size())) * (1.0 - incompliantTransitionsRatio);
		return new FsmMetaData(fsm, visitedTransitions, new FitnessValue(fitness));
	}
	
	@Override
	public void reset() {
	}

	@Override
	public Comparator<FitnessValue> getComparator() {
		return comparator;
	}
	
	@Override
	public FitnessValue correctFitness(FitnessValue fitness, FSM cachedInstance, FSM trueInstance) {
//		double negativeTerm = - 0.01 * (100.0 - cachedInstance.getUsedTransitionsCount());
//		double positiveTerm = 0.01 * (100.0 - trueInstance.getUsedTransitionsCount());
		return new FitnessValue(0);
	}
}
