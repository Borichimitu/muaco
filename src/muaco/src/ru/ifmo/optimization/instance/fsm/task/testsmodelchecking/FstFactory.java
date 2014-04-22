package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.algorithm.Transition;
import ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions.ComplianceChecker;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.util.StringUtils;

public class FstFactory {
	public static FST createFST(FSM fsm, String[] actions) {
		int stateNumber = fsm.getNumberOfStates();
		String[] setOfInputs = new String[fsm.getEvents().size()];
		for (int i = 0; i < setOfInputs.length; i++) {
			setOfInputs[i] = fsm.getEvents().get(i);
		}
		String[] setOfOutputs = Arrays.copyOf(actions, actions.length);
		Transition[][] states = new Transition[stateNumber][];

		for (int i = 0; i < stateNumber; i++) {
			List<Transition> newTransitions = new ArrayList<Transition>();
			for (int j = 0; j < fsm.transitions[i].length; j++) {
				if (fsm.transitions[i][j].getEndState() == -1) {
					continue;
				}
				FSM.Transition otherTransition = fsm.transitions[i][j];
				int outputLength = Math.max(0, StringUtils.count(otherTransition.getAction(), 'z') + StringUtils.count(otherTransition.getAction(), '?') / 2);
				Transition newTransition = new Transition(otherTransition.getEvent(), Math.max(1, outputLength), otherTransition.getEndState());
				List<String> output = new ArrayList<String>();
				if (outputLength > 0) {
					String s = "";
					int l = 0;
					while (l < otherTransition.getAction().length()) {
						char c = otherTransition.getAction().charAt(l);
						if (c == 'z' && s.length() > 0) {
							output.add(s);
							s = "" + c;
							l++;
						} else if (c == '?') {
							if (s.length() > 0) {
								output.add(s);
							}
							output.add("???");
							l += 2;
							s = "";
						} else {
							s += c;
							l++;
						}
					}
					if (s.length() > 0) {
						output.add(s);
					}
					newTransition.setOutput(output.toArray(new String[]{}));

				} else {
					newTransition.setOutput(new String[]{""});
				}
				newTransitions.add(newTransition);
			}
			states[i] = newTransitions.toArray(new Transition[1]);
			if (newTransitions.size() == 0) {
				states[i] = new Transition[0];
			}
		}

		for (int i = 0; i < stateNumber; i++) {
			states[i] = FST.removeDuplicates(states[i], ComplianceChecker.getComplianceChecker());
		}
		FST fst = new FST(states, fsm.getInitialState(), setOfInputs, setOfOutputs);
		fst.setNeedToComputeFitness(true);
		return fst;
	}
	
	public static FSM FSTtoFSM(FST fst) {
		FSM fsm = new FSM(fst.getNumberOfStates());

		for (int i = 0; i < fst.getNumberOfStates(); i++) {
			for (Transition t : fst.getStates()[i]) {
				int eventId = FSM.EVENTS.indexOf(t.getInput());
				StringBuilder output = new StringBuilder();
				for (String s : t.getOutput()) {
					output.append(s);
				}
				fsm.transitions[i][eventId] = new FSM.Transition(i, t.getNewState(), t.getInput(), output.toString());
			}
		}
		return fsm;
	}
	
}
