package ru.ifmo.optimization.instance.fsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.optimization.instance.InstanceGenerator;
import ru.ifmo.optimization.instance.fsm.task.AbstractAutomatonTask;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.RandomProvider;
import ru.ifmo.util.Util;

public class InitialFSMGenerator implements InstanceGenerator {
	public FSM createInstance(int numberOfStates, List<String> events, 
											       String[] actions, AutomatonTaskConstraints constraints) {
		FSM.Transition transitions[][] = new FSM.Transition[numberOfStates][events.size()];
		
		List<String> allActions = new ArrayList<String>();
		allActions.addAll(Arrays.asList(actions));
		
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < events.size(); j++) {
				List<String> actionsToChooseFrom = new ArrayList<String>();
				actionsToChooseFrom.addAll(allActions);
				
				if (constraints.hasPreferredActions(j)) {
					actionsToChooseFrom = constraints.getPreferredActions(j);
				} 
				
				if (constraints.hasConstraints(j)) {
					actionsToChooseFrom.removeAll(constraints.getConstraints(j));
				}
				
				String action = actionsToChooseFrom.get(RandomProvider.getInstance().nextInt(actionsToChooseFrom.size()));
				int nextState = RandomProvider.getInstance().nextInt(numberOfStates);
				transitions[i][j] = new FSM.Transition(i, nextState, events.get(j), action);
			}
		}
		
//		return new FSM(numberOfStates, transitions);
		return Util.makeCompliantFSM(new FSM(numberOfStates, transitions)).getInstance();
	}
	
	@Override
	public FSM createInstance(AbstractOptimizationTask t) {
		AbstractAutomatonTask task = (AbstractAutomatonTask)t;
		return createInstance(task.getDesiredNumberOfStates(), task.getEvents(), task.getActions(), task.getConstraints());
	}
}
