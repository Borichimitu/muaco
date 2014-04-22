package ru.ifmo.optimization.instance.fsm.mutator.efsm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.Mutator;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.util.RandomProvider;
import ru.ifmo.util.Util;

public class EFSMAddOrDeleteTransitionMutator implements Mutator<FSM, FsmMutation> {
	
	private List<String> events;
	private Set<String> eventsSet;
	private double deleteTransitionProbability;
	
	public EFSMAddOrDeleteTransitionMutator(List<String> events, double deleteTransitionProbability) {
		this.events = events;
		eventsSet = new HashSet<String>();
		eventsSet.addAll(events);
		this.deleteTransitionProbability = deleteTransitionProbability;
	}
	
	private int getTransitionIdToDelete(Transition[] transitions) {
		int transitionToDelete = RandomProvider.getInstance().nextInt(Util.numberOfExistingTransitions(transitions));
		int existingTransitionCount = 0;
		for (int i = 0; i < transitions.length; i++) {
			if (transitions[i].getEndState() != -1) {
				if (existingTransitionCount == transitionToDelete) {
					return i;
				}
				existingTransitionCount++;
			}
		}
		//should be impossible
		return -1;
	}
	
	
	@Override
	public MutatedInstanceMetaData<FSM, FsmMutation> apply(FSM individual) {
		FSM mutated = new FSM(individual);
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		
		for (int state = 0; state < mutated.getNumberOfStates(); state++) {
			if (RandomProvider.getInstance().nextDouble() < deleteTransitionProbability) { 
    			if (RandomProvider.getInstance().nextBoolean()) {
    				if (Util.hasTransition(mutated.transitions[state])) {
    					//delete a transition
    					int eventToDelete = getTransitionIdToDelete(mutated.transitions[state]);
    					
    					FsmTransitionMutation mutation = new FsmTransitionMutation(state, eventToDelete, -1, "1", true);
    					mutated.transitions[state][eventToDelete].setEndState(-1);
    					mutated.transitions[state][eventToDelete].setAction("1");
    					mutations.add(mutation);
    				}
    			} else {
    				if (Util.numberOfExistingTransitions(mutated.transitions[state]) < events.size()) {
    					//add a transition
    					
    					//first, get a set of all used input events
    					Set<String> usedEvents = new HashSet<String>();
    					for (Transition t : mutated.transitions[state]) {
    						if (t.getEndState() == -1) {
    							continue;
    						}
    						usedEvents.add(t.getEvent());
    					}
    					List<String> unusedEvents = new ArrayList<String>();
    					for (String e : events) {
    						if (!usedEvents.contains(e)) {
    							unusedEvents.add(e);
    						}
    					}
    					
    					//select random unused event, add a random transition with this event
    					String input = unusedEvents.get(RandomProvider.getInstance().nextInt(unusedEvents.size()));
    					int indexOfInput = events.indexOf(input);
    					String newAction = "1";//actions[RandomProvider.instance().get().nextInt(actions.length)];
//    					String newAction = actions[RandomProvider.instance().get().nextInt(actions.length)];
    					int newDestinationState = RandomProvider.getInstance().nextInt(mutated.getNumberOfStates());
    					mutated.transitions[state][indexOfInput].setAction(newAction);
    					mutated.transitions[state][indexOfInput].setEndState(newDestinationState);
    					FsmTransitionMutation mutation = new FsmTransitionMutation(state, indexOfInput, newDestinationState, newAction, true);
    					mutations.add(mutation);
    				}
    			}
    		}
		}
		return Util.makeCompliantFSM(new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations));
	}

	@Override
	public FSM applySimple(FSM individual) {
		return (FSM) apply(individual).getInstance();
	}

}
