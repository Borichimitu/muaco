package ru.ifmo.optimization.instance.comparator;

import java.util.Comparator;

import ru.ifmo.optimization.instance.FitnessValue;

public class MaxSingleObjectiveComparator implements Comparator<FitnessValue>{
	@Override
	public int compare(FitnessValue arg0, FitnessValue arg1) {
		return Double.compare(arg0.data[0], arg1.data[0]);
	}
}
