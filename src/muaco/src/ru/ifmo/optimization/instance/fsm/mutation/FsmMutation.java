package ru.ifmo.optimization.instance.fsm.mutation;

import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;

public abstract class FsmMutation implements InstanceMutation<FSM> {
	public int getStartState() {
		return -1;
	}
	
	public int getEventId() {
		return -1;
	}
	
	public int getEndState() {
		return -1;
	}
	
	public String getAction() {
		return null;
	}
	
	public boolean isAddDelete() {
		return false;
	}
}
