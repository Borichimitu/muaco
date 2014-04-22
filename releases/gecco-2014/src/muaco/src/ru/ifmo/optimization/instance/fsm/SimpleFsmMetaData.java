package ru.ifmo.optimization.instance.fsm;

import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;

public class SimpleFsmMetaData extends FitInstance<FSM> {
	public SimpleFsmMetaData(FSM fsm, FitnessValue fitness) {
		super(fsm, fitness);
	}
}
