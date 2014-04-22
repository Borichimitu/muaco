package ru.ifmo.optimization.instance.fsm;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.Constructable;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;
import ru.ifmo.optimization.instance.mutation.InstanceMutation;
import ru.ifmo.util.Digest;
import ru.ifmo.util.Dijkstra;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class FSM extends AbstractFSM implements Comparable<FSM>, Constructable<FSM> {
	public FSM.Transition[][] transitions;
	private boolean transitionUsed[][];
	private int numberOfStates;
	private int initialState = 0;
	private boolean[] terminal;
	public static List<String> EVENTS;
	public static int NUMBER_OF_EVENTS;
	
	public static void setEvents(List<String> taskEvents) {
		EVENTS = taskEvents;
		NUMBER_OF_EVENTS=EVENTS.size();
	}
	 
	public FSM(int numberOfStates) {
		this.numberOfStates = numberOfStates;
		this.transitions = new Transition[numberOfStates][NUMBER_OF_EVENTS];
		this.transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
		}
		for (int i = 0; i < transitions.length; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				transitions[i][j] = new Transition(i, -1, EVENTS.get(j), null);
			}
		}
		terminal = new boolean[numberOfStates];
	}
	
	public FSM(int numberOfStates, FSM.Transition[][] tr) {
		this.numberOfStates = numberOfStates;
		this.transitions = tr;
		this.transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
		}
		terminal = new boolean[numberOfStates];
	}
	
	public FSM(int numberOfStates, FSM.Transition[][] tr, boolean[] terminal) {
		this(numberOfStates, tr);
		this.terminal = Arrays.copyOf(terminal, terminal.length);
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
		}
	}

	public FSM(FSM other) {
		numberOfStates = other.numberOfStates;
		transitions = new FSM.Transition[numberOfStates][NUMBER_OF_EVENTS];
		transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < other.transitions[i].length; j++) {
				if (other.transitions[i][j] != null) {
					transitions[i][j] = new FSM.Transition(other.transitions[i][j]);
				}
				transitionUsed[i][j] = other.transitionUsed[i][j];
 			}
		}
		terminal = Arrays.copyOf(other.terminal, other.terminal.length);
	}
	
	public FSM(String filename) {
		Scanner in = null;

		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		numberOfStates = in.nextInt();
		in.nextInt(); //number of events
		terminal = new boolean[numberOfStates];
		
		transitions = new FSM.Transition[numberOfStates][NUMBER_OF_EVENTS];
		transitionUsed = new boolean[numberOfStates][NUMBER_OF_EVENTS];
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
		}
		String stringTerminal = in.next();
		for (int i = 0; i < numberOfStates; i++) {
			terminal[i] = (stringTerminal.charAt(i) == '0');
		}
		in.nextLine();
		
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				transitions[i][j] = new FSM.Transition(i, -1, EVENTS.get(j), "");
			}
		}
		
		while (in.hasNext()) {
			String s = in.nextLine();
			String[] list = s.split(" - > ");
			for (int k = 0; k < list.length; k++) {
				list[k] = list[k].replace("(", "");
				list[k] = list[k].replace(")", "");
				list[k] = list[k].replace(",", "");
			}

			String[] startAndEvent = list[0].split(" ");
			String[] endAndAction = list[1].split(" ");

			int startState = Integer.parseInt(startAndEvent[0]);
			int endState = Integer.parseInt(endAndAction[0]);
			int event = Integer.parseInt(startAndEvent[1]);
			String action = "";
			if (endAndAction.length > 1) {
				action = endAndAction[1];
			}
			transitions[startState][event] = new FSM.Transition(startState, endState, event == -1 ? "-1" : EVENTS.get(event), action);
		}
	}

    public boolean isFullyDefined() {
        for (int state = 0; state < getNumberOfStates(); state++) {
            for (int event = 0; event < getNumberOfEvents(); event++) {
                if (getTransition(state, event) == null) {
                    return false;
                }
            }
        }
        return true;
    }
	
	public void clearUsedTransitions() {
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				transitionUsed[i][j] = false;
			}
		}
	}
	
	public int getUsedTransitionsCount() {
		int result = 0;
		
		for (int i = 0; i < transitionUsed.length; i++) {
			for (int j = 0; j < transitionUsed[i].length; j++) {
				if (transitionUsed[i][j]) {
					result++;
				}
			}
		}
		return result;
	}
	
	public boolean isTransitionUsed(int state, int event) {
		return transitionUsed[state][event];
	}
	 
	@Override
	public int getMaxNumberOfMutations() {
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				if (transitionUsed[i][j]) {
					result++;
				}
			}
		}
		return result;
	};
	
	public List<Integer> getStartStatesOfUsedTransitions() {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				if (transitionUsed[i][j]) {
					result.add(i);
					break;
				}
			}
		}
		return result;
	}
	
	public List<Integer> getEventsOfUsedTransitions(int startState) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
			if (transitionUsed[startState][i]) {
				result.add(i);
			}
		}
		return result;
	}
	
	public List<String> getEvents() {
		return EVENTS;
	}
	
	@Override
	public void applyMutations(List<InstanceMutation<FSM>> mutations) {
		long start = System.currentTimeMillis();
		for (InstanceMutation<FSM> m : mutations) {
			m.apply(this);
		}
		RunStats.TIME_APPLY_MUTATIONS += (System.currentTimeMillis() - start) / 1000.0;
		
		//setting transitionsUsed to default just in case
		for (int i = 0; i < numberOfStates; i++) {
			Arrays.fill(this.transitionUsed[i], false);
		}
	}
	
	public int getNumberOfTransitions() {
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
				if (transitions[i][j].getEndState() != -1) {
					result++;
				}
			}
		}
		return result;
	}
	
	public int getNumberOfReachableStates() {
		Dijkstra dijsktra = new Dijkstra(this, 0);
		int[] weights = dijsktra.run();
		int numberOfReachableStates = 0;
		for (int i : weights) {
			if (i != Integer.MAX_VALUE) {
				numberOfReachableStates++;
			}
		}
		return numberOfReachableStates;
	}
	
	public int getDepth() {
		Dijkstra dijsktra = new Dijkstra(this, 0);
		int[] weights = dijsktra.run();
		int depth = 0;
		for (int i : weights) {
			if (i == Integer.MAX_VALUE) {
				continue;
			}
			depth = Math.max(depth, i);
		}
		return depth;
	}
	
	public boolean hasTransitionFromUtoV(int u, int v) {
		for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
			if (transitions[u][i].getEndState() == v) {
				return true;
			}
		}
		return false;
	}

	public boolean isStateTerminal(int stateIndex) {
		return terminal[stateIndex]; 
	}
	
	public void setStateTerminal(int state, boolean terminal) {
		this.terminal[state] = terminal;
	}
	
	public boolean[] getTerminal() {
		return terminal;
	}
	
	@Override
	public int getInitialState() {
		return initialState;
	}

	@Override
	public int getNumberOfStates() {
		return numberOfStates;
	}

	public FSM.Transition getTransition(int state, int event) {
		return transitions[state][event];
	}
	
	public void setTransition(int startState, String event, int endState, String action) {
		transitions[startState][EVENTS.indexOf(event)] = new FSM.Transition(startState, endState, event, action);
	}

	@Override
	public void setInitialState(int state) {
		this.initialState = state;
	}

	@Override
	public String toString() {
		String a = numberOfStates + " " + NUMBER_OF_EVENTS + "\n";
		for (boolean b : terminal) {
			a += b ? 0 : 1;
		}
		a += "\n";
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				a += "(" + i + ", " + j + ") - > " + transitions[i][j] + "\n";
			}
		}
		return a;
	}
	
	public void printTransitionDiagram(String filename) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.print(toString());
		out.close();
	}
	
	private String stringForHashing() {
		StringBuilder sb = new StringBuilder();
		for (boolean b : terminal) {
			sb.append(b ? 0 : 1);
		}
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				Transition t = transitions[i][j];
				sb.append(t.getEvent());
				sb.append(t.getEndState());
				String a = t.getAction();
				if (a == null) {
					sb.append("n");
					continue;
				}
				if (a.length() == 0) {
					sb.append("n");
				}  else {
					sb.append(a);
				}
			}
		}
		return sb.toString();
	}
	
	public Long computeStringHash() {
		long start = System.currentTimeMillis();
		Long result = Digest.RSHash(stringForHashing());
		RunStats.TIME_COMPUTE_HASH += (System.currentTimeMillis() - start) / 1000.0;
		return result;
	}
	
	private static boolean transitionsContainTransition(Collection<Transition> transitions, int state, String event) {
		for (Transition t : transitions) {
			if (t.getStartState() == state && t.getEvent() == event) {
				return true;
			}
		}
		return false;
	}
	
	public int dist(FSM other, 
			Collection<Transition> transitions1, 
			Collection<Transition> transitions2) {
		
		if (numberOfStates != other.getNumberOfStates()) {
			return Integer.MAX_VALUE;
		}
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				Transition thisTransition = transitions[i][j];
				Transition otherTransition = other.getTransition(i, j);
				if (!transitionsContainTransition(transitions1, i, EVENTS.get(j)) || !transitionsContainTransition(transitions2, i, EVENTS.get(j))) {
					continue;
				}
				if (thisTransition.getEndState() != otherTransition.getEndState()) {
					result++;
				}
				if (!thisTransition.getAction().equals(otherTransition.getAction())) {
					result++;
					continue;
				}
			}
		}
		return result;
	}
	
	public boolean hasTransition(int state, int event) {
		if (transitions[state].length < event + 1) {
			return false;
		}
		return transitions[state][event] != null;
	}
	
	public int dist(FSM other) { 
		if (numberOfStates != other.getNumberOfStates()) {
			return Integer.MAX_VALUE;
		}
		int result = 0;
		for (int i = 0; i < numberOfStates; i++) {
			for (int j = 0; j < transitions[i].length; j++) {
				if (!hasTransition(i, j) && other.hasTransition(i, j)) {
					result++;
					continue;
				}
				if (hasTransition(i, j) && !other.hasTransition(i, j)) {
					result++;
					continue;
				}
				
				if (!hasTransition(i, j) && !other.hasTransition(i, j)) {
					continue;
				}
				
				FSM.Transition thisTransition = transitions[i][j];
				FSM.Transition otherTransition = other.getTransition(i, j);
				
				if (thisTransition == null && otherTransition != null || 
						thisTransition != null && otherTransition == null) {
					result++;
					continue;
				}
				
				if (thisTransition == null && otherTransition == null) {
					continue;
				}
				
				if (thisTransition.getEndState() != otherTransition.getEndState()) {
					result++;
					continue;
				}
				
				if (thisTransition.getAction() == null && otherTransition.getAction() != null ||
						thisTransition.getAction() != null && otherTransition.getAction() == null) {
					result++;
					continue;
				}
				
				if (thisTransition.getAction() == null && otherTransition.getAction() == null) {
					continue;
				}
				
				if (!thisTransition.getAction().equals(otherTransition.getAction())) {
					result++;
					continue;
				}
			}
		}
		return result;
	}
	
	@Override
	public int compareTo(FSM other) {
		return computeStringHash().compareTo(other.computeStringHash());
	}
	
	@Override 
	public boolean equals(Object o) {
		FSM other = (FSM)o;
		return computeStringHash().longValue() == other.computeStringHash().longValue();
	}
	
	public int getNumberOfEvents() {
		return NUMBER_OF_EVENTS;
	}
	
	public Collection<Transition> getTransitions() {
		Collection<Transition> result = new ArrayList<Transition>();
		for (Transition[] tr : transitions) {
			for (Transition t : tr) {
				result.add(t);
			}
		}
		return result;
	}
	
	public MutationCollection<FsmMutation> getMutations(FSM other) {
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		
		for (int state = 0; state < numberOfStates; state++) {
			for (int event = 0; event < transitions[state].length; event++) {
				if (!hasTransition(state, event) && !other.hasTransition(state, event)) {
					continue;
				}
				//delete transition mutation
				if (hasTransition(state, event) && !other.hasTransition(state, event)) {
					mutations.add(new FsmTransitionMutation(state, event, -1, null));
					continue;
				}
				
				//add transition mutation
				if (!hasTransition(state, event) && other.hasTransition(state, event)) {
					mutations.add(new FsmTransitionMutation(state, event, 
							other.transitions[state][event].getEndState(), 
							other.transitions[state][event].getAction()));
					continue;
				}
				
				//both transitions are the same
				if (transitions[state][event].equals(other.transitions[state][event])) {
					continue;
				}
				
				//change transition mutation
				mutations.add(new FsmTransitionMutation(state, event, 
						other.transitions[state][event].getEndState(),
						other.transitions[state][event].getAction()));
			}
		}
		return mutations;
	}

	public static class Transition implements AbstractFSM.Transition {
		private int startState;
		private int endState;
		private String event;
		private String action;

		public Transition(int startState, int endState, String event, String action) {
			this.startState = startState;
			this.endState = endState;
			this.event = event;
			this.action = action;
		}

		public Transition(Transition other) {
			startState = other.startState;
			endState = other.endState;
			event = other.event;
			action = other.action;
		}

		@Override
		public boolean equals(Object o) {
			FSM.Transition other = (FSM.Transition)o;
			if (startState != other.startState) {
				return false;
			}
			if (endState != other.endState) { 
				return false;
			}
			
			if (action == null && other.action != null ||
					action != null && other.action == null) {
				return false;
			}
			
			if (action == null && other.action == null) {
				return true;
			}
			
			return action.equals(other.action);
		}
		
		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		@Override
		public int getEndState() {
			return endState;
		}

		@Override
		public void setEndState(int state) {
			this.endState = state;
		}

		@Override
		public String toString() {
			return "(" + endState + ", " + action + ")";
		}

		@Override
		public int getStartState() {
			return startState;
		}
		
		@Override
		public int hashCode() {
			return Digest.hash("" + startState + ";" + event + ";" + endState + ";" + action);
		}

		@Override
		public String getActions() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public void setEvent(String event) {
			this.event = event;
		}
		
		public String getEvent() {
			return event;
		}
	}

	@Override
	public FSM copyInstance(FSM other) {
		return new FSM(other);
	}
}
