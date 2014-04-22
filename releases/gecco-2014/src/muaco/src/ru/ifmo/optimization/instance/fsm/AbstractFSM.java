package ru.ifmo.optimization.instance.fsm;


/**
 * Abstract class representing an extended finite-state machine.
 * @author Daniil Chivilikhin
 *
 */
public abstract class AbstractFSM {
	public abstract int getNumberOfEvents();

	public abstract int getInitialState();
	public abstract void setInitialState(int state);
	public abstract int getNumberOfStates();
	
	public interface Transition  {
		public int getStartState();
		public int getEndState();
		public void setEndState(int state);
		public String toString();
		public String getActions();
	}
}
