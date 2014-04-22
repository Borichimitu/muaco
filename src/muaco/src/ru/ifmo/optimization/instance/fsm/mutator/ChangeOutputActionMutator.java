package ru.ifmo.optimization.instance.fsm.mutator;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.optimization.instance.fsm.task.AutomatonTaskConstraints;
import ru.ifmo.util.RandomProvider;
import ru.ifmo.util.Util;

public class ChangeOutputActionMutator implements Mutator<FSM, FsmMutation> {
	
	private String[] actions;
	
	public ChangeOutputActionMutator(String[] actions, AutomatonTaskConstraints constraints) {
		this.actions = actions;
	}
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		FSM mutated = new FSM(individual);
		
		if (!Util.hasTransitions(mutated)) {
			return null;
		}
		
		int state = RandomProvider.getInstance().nextInt(mutated.getNumberOfStates());
		while (Util.numberOfExistingTransitions(mutated.transitions[state]) == 0) {
			state = RandomProvider.getInstance().nextInt(mutated.getNumberOfStates());
		}
		
		int event = RandomProvider.getInstance().nextInt(mutated.getNumberOfEvents());
		while (mutated.transitions[state][event].getEndState() == -1) {
			event = RandomProvider.getInstance().nextInt(mutated.getNumberOfEvents());
		}
		String newAction = null;
		String oldAction = mutated.transitions[state][event].getAction();
		while (true) {
			int newActionIndex = RandomProvider.getInstance().nextInt(actions.length);
			newAction = actions[newActionIndex];
			if (newAction.equals(oldAction)) {
				continue;
			}
			break;
		}

		mutated.transitions[state][event].setAction(newAction);
		FsmTransitionMutation mutation = new FsmTransitionMutation(state, event, mutated.transitions[state][event].getEndState(), newAction);
		return new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, new MutationCollection<FsmMutation>(mutation));
	}
	
	@Override
	public FSM applySimple(FSM individual) {
		return (FSM) apply(individual).getInstance();
	}
}
