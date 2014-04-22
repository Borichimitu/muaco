package ru.ifmo.optimization.instance;

import java.util.Comparator;
import java.util.List;

import ru.ifmo.optimization.task.AbstractOptimizationTask;
import ru.ifmo.util.Array;

public class FitnessValue extends Array implements Comparable<FitnessValue> {
	public static Comparator<FitnessValue> comparator;
	public static boolean[] maximize;
	
	public FitnessValue(double ... values) {
		super(values);
	}
	
	public FitnessValue(FitnessValue other) {
		super(other.data);
	}
	
	public static void setComparatorData(Comparator<FitnessValue> cmp, boolean[] max) {
		comparator = cmp;
		maximize = max;
	}
	
	@Override
	public int compareTo(FitnessValue arg0) {
		return comparator.compare(this, arg0);
	}
	
	public boolean betterThan(FitnessValue other) {
		return comparator.compare(this, other) > 0;
	}
	
	public boolean betterThanOrEqualTo(FitnessValue other) {
		return comparator.compare(this, other) >= 0;
	}
	
	public static FitnessValue average(List<FitnessValue> values) {
		if (values.size() == 1) {
			return values.get(0);
		}
		FitnessValue result = new FitnessValue(values.get(0));
		for (int i = 1; i < values.size(); i++) {
			for (int j = 0; j < values.get(i).data.length; j++) {
				result.data[j] += values.get(i).data[j];
			}
		}
		
		for (int i = 0; i < result.data.length; i++) {
			result.data[i] /= (double)values.size();
		}
		return result;
	}
	
	public static FitnessValue getMinValue() {
		double[] result = new double[AbstractOptimizationTask.DIMENSIONALITY];
		for (int i = 0; i < result.length; i++) {
			result[i] = maximize[i] ? Double.MIN_VALUE : Double.MAX_VALUE;
		}
		return new FitnessValue(result);
	}
	
	public FitnessValue add(FitnessValue other) {
		for (int i = 0; i < data.length; i++) {
			data[i] += other.data[i];
		}
		return this;
	}
	
	public FitnessValue divideBy(double value) {
		for (int i = 0; i < data.length; i++) {
			data[i] /= value;
		}
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			sb.append(data[i]);
			if (i < data.length - 1) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
}
