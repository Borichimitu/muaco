package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.comparator.MaxSingleObjectiveComparator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

/**
 * Class implementing calculation of a fitness function of an automaton
 * based on tests.
 * @author Daniil Chivilikhin
 */
public abstract class TestsTask extends AbstractAutomatonTask {
	protected AutomatonTest[] tests;
	protected ArrayList<String> events;
	protected String[] outputs;
	protected String[] actionsForEvolving;
	protected String[] actions;
	protected boolean terminateWhenAllTestsPass;
	protected boolean penalizeForIncompliance;
	
	public TestsTask(AbstractTaskConfig config) {
		super(1);
		this.config = config;
		desiredFitness = config.getDesiredFitness();
		desiredNumberOfStates = Integer.parseInt(config.getProperty("desired-number-of-states"));
		terminateWhenAllTestsPass = Boolean.parseBoolean(config.getProperty("terminate-when-all-tests-pass"));
		penalizeForIncompliance = Boolean.parseBoolean(config.getProperty("penalize-for-incompliance"));
		comparator = new MaxSingleObjectiveComparator();
		FitnessValue.setComparatorData(comparator, new boolean[]{true});
	}
	
	protected void readTests(String filename) {
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int numberOfTests = in.nextInt();
		tests = new AutomatonTest[numberOfTests];
		
		for (int i = 0; i < numberOfTests; i++) {
			String[] inputAndOutput = in.next().split("->");
			String[] inputSequence = inputAndOutput[0].split("_");
			String[] outputSequence = new String[] {""};
			if (inputAndOutput.length > 1) {
				outputSequence = inputAndOutput[1].split("_");
			}
			tests[i] = new AutomatonTest(inputSequence, outputSequence);
		}
	}
	
	public abstract void configure(String propertiesFilename);
	
	protected FsmMetaData runFsmOnTest(FSM fsm, int testIndex, boolean scenario) {
		int currentState = fsm.getInitialState();

		Set<Transition> visitedTransitions = new HashSet<Transition>();
		List<String> answers = new ArrayList<String>();
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
			
//			if (scenario) {
//				if (answer.length() == 0) {
//					answers.add("");
//				}
//			}
			if (scenario) {
				answers.add(answer);
			} else {
				String buf = answer;

				while (buf.length() > 0) {
					if (buf.charAt(0) == 'z') {
						int index = -1;
						int indexOfNextZ = buf.indexOf('z', 1);
						int indexOfNextQuestion = buf.indexOf('?', 1);
						if (indexOfNextZ != -1) {
							if (indexOfNextQuestion != -1) {
								index = Math.min(indexOfNextZ, indexOfNextQuestion);
							} else {
								index = indexOfNextZ;
							}
						} else if (indexOfNextZ == -1) {
							if (indexOfNextQuestion != -1) {
								index = indexOfNextQuestion;
							} else {
								answers.add(buf);
								buf = "";
								break;
							}
						}

						String action = buf.substring(0, index);
						answers.add(action);
						buf = buf.substring(action.length());
					} else {
						answers.add(buf.substring(0, 2));
						buf = buf.substring(2);
					}
				}
			}
		}
		String answerArray[] = answers.toArray(new String[0]);

		double f = Math.max(tests[testIndex].getOutput().length, answerArray.length) == 0 
					? 1.0
					: tests[testIndex].getLevenshteinDistance(answerArray) / Math.max(tests[testIndex].getOutput().length, answerArray.length);
		return new FsmMetaData(fsm, visitedTransitions, new FitnessValue(f));
	}


	public String[] getAutomatonOutputs() {
		return outputs;
	}

	public void printAutomataOutputs(String filename) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < outputs.length; i++) {
			out.println(outputs[i]);
		}
		out.close();
	}
	
	@Override
	public AutomatonTaskConstraints getConstraints() {
		return new AutomatonTaskConstraints();
	}
	
	@Override
	public void reset() {
		
	}
	
	@Override
	public Comparator<FitnessValue> getComparator() {
		return comparator;
	}
}
