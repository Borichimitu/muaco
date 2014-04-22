package ru.ifmo.optimization.instance.fsm.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutomatonTaskConstraints {
	private Map<Integer, List<String>> constraints;
	private Map<Integer, List<String>> preferredActions;
	
	public AutomatonTaskConstraints() {
		constraints = new HashMap<Integer, List<String>>();
		preferredActions = new HashMap<Integer, List<String>>();
	}
	
	public void addConstraint(int eventId, String action) {
		List<String> constraint = constraints.get(eventId);
		if (constraint == null) {
			constraint = new ArrayList<String>();
			constraint.add(action);
			constraints.put(eventId, constraint);
		} else {
			constraint.add(action);
		}
	}
	
	public void addPreferredAction(int eventId, String action) {
		List<String> preferred = preferredActions.get(eventId);
		if (preferred == null) {
			preferred = new ArrayList<String>();
			preferred.add(action);
			preferredActions.put(eventId, preferred);
		} else {
			preferred.add(action);
		}
	}
	
	public boolean hasConstraints(int eventId) {
		return constraints.containsKey(eventId);
	}
	
	public boolean hasPreferredActions(int eventId) {
		return preferredActions.containsKey(eventId);
	}
	
	public List<String> getConstraints(int eventId) {
		return constraints.get(eventId);
	}
	
	public List<String> getPreferredActions(int eventId) {
		return preferredActions.get(eventId);
	}
}
