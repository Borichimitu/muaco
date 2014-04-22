package ru.ifmo.util;

import java.util.Arrays;

public class Array {
	public double data[];
	
	public Array(double data[]) {
		this.data = Arrays.copyOf(data, data.length);
	}
	
	public Array(Array other) {
		this.data = Arrays.copyOf(other.data, other.data.length);
	}
	
	public int length() {
		return data.length;
	}
	
	public Array add(Array other) {
		for (int i = 0; i < data.length; i++) {
			data[i] += other.data[i];
		}
		return this;
	}
	
	public Array subtract(Array other) {
		for (int i = 0; i < data.length; i++) {
			data[i] -= other.data[i];
		}
		return this;
	}
	
	public Array multiply(double value) {
		for (int i = 0; i < data.length; i++) {
			data[i] *= value;
		}
		return this;
	}
	
	public static Array getValues(int length, double value) {
		double result[] = new double[length];
		Arrays.fill(result, value);
		return new Array(result);
	}
	
	public static Array max(Array first, Array second) {
		Array result = new Array(first);
		for (int i = 0; i < first.data.length; i++) {
			result.data[i] = Math.max(first.data[i], second.data[i]);
		}
		return result;
	}
}
