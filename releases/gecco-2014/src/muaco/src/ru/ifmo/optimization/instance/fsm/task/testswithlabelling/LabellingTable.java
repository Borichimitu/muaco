package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LabellingTable {
	Map<String, Integer> table[][];

	public LabellingTable(int numberOfStates, int numberOfEvents) {
		table = new Map[numberOfStates][numberOfEvents];
	}
	
	public void add(int state, int event, String sequence) {
		Map<String, Integer> tableForTransition = table[state][event];
		if (tableForTransition == null) {
			tableForTransition = new HashMap<String, Integer>();
			tableForTransition.put(sequence, 1);
			table[state][event] = tableForTransition;
			return;
		}

		Integer count = tableForTransition.get(sequence);
		if (count == null) {
			tableForTransition.put(sequence, 1);
			return;
		} 
		
		tableForTransition.put(sequence, count + 1);
	}
	
	public String getActions(int state, int event) {
		Map<String, Integer> tableForTransition = table[state][event];
		if (tableForTransition == null) {
			return "";
		}
		int max = Integer.MIN_VALUE;
		String sequence = null;
		for (Entry<String, Integer> e : tableForTransition.entrySet()) {
			if (e.getValue() > max) {
				max = e.getValue();
				sequence = e.getKey();
			}
		}
		return sequence;
	}
} 
