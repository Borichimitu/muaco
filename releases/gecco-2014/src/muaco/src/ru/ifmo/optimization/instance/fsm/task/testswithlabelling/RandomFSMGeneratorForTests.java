package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.util.List;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.util.RandomProvider;

public class RandomFSMGeneratorForTests {
	public static FSM generateRandomFSM (int numberOfStates, List<String> events, List<String> actions, int maxNumberOfActions) {
		FSM.Transition transitions[][] = new FSM.Transition[numberOfStates][events.size()];
		
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < events.size(); j++) {
				int numberOfActions = Math.max(RandomProvider.getInstance().nextInt(maxNumberOfActions), 1);
				String sequence = "";
				for (int k = 0; k < numberOfActions; k++) {
					sequence += actions.get(RandomProvider.getInstance().nextInt(actions.size()));
				}
				int nextState = RandomProvider.getInstance().nextInt(numberOfStates);
				transitions[i][j] = new FSM.Transition(i, nextState, events.get(j), sequence);
			}
		}
		
		return new FSM(numberOfStates, transitions);
	}
}
