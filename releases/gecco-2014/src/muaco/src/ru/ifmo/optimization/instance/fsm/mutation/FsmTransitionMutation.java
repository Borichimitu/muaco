package ru.ifmo.optimization.instance.fsm.mutation;

import ru.ifmo.optimization.instance.fsm.FSM;

public class FsmTransitionMutation extends FsmMutation {
	private int startState;
	private int eventId;
	private int endState;
	private String action;
	private boolean addDelete;
	
	public FsmTransitionMutation(int startState, int eventId, int endState, String action, boolean addDelete) {
		this.startState = startState;
		this.eventId = eventId;
		this.endState = endState;
		this.action = action;
		this.addDelete = addDelete;
	}
	
	public FsmTransitionMutation(int startState, int eventId, int endState, String action) {
		this.startState = startState;
		this.eventId = eventId;
		this.endState = endState;
		this.action = action;
		this.addDelete = false;
	}

	@Override
	public boolean isAddDelete() {
		return addDelete;
	}
	
	@Override
	public int getStartState() {
		return startState;
	}
	
	@Override
	public int getEventId() {
		return eventId;
	}
	
	@Override
	public int getEndState() {
		return endState;
	}
	
	@Override
	public String getAction() {
		return action;
	}
	
	@Override
	public String toString() {
		return "(" + startState + "," + eventId + ")->(" + endState + "/" + action + ")";
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		FsmTransitionMutation other = (FsmTransitionMutation)o;
		return startState == other.startState &&
		       eventId == other.eventId &&
		       endState == other.endState &&
		       action.equals(other.action);
	}
	
	@Override
	public void apply(FSM instance) {
		instance.transitions[startState][eventId].setEndState(endState);
		instance.transitions[startState][eventId].setAction(action);
	}
}
