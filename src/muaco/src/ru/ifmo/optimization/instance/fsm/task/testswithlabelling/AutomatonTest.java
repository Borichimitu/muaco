package ru.ifmo.optimization.instance.fsm.task.testswithlabelling;

import java.util.Arrays;
import java.util.List;

import ru.ifmo.util.StringUtils;

/**
 * Class representing a test example for an FSM.
 * Stores the input and the output lists in the form 
 * of string arrays.
 */
public class AutomatonTest {
	private String[] input;
	private String[] output;
	private String inputString;
	private String outputString;
	
	public AutomatonTest(String[] input, String[] output) {
		this.input = input.clone();
		this.output = output.clone();
		inputString = "";
		for (String s : input) {
			inputString += s;
		}
		outputString = "";
		for (String s : output) {
			outputString += s;
		}
	}
	
	public AutomatonTest(List<String> input, List<String> output) {
		this.input = new String[input.size()];
		this.output = new String[output.size()];
		inputString = "";
		outputString = "";
		for (int i = 0; i < input.size(); i++) {
			this.input[i] = input.get(i);
			inputString += input.get(i);
		}
		for (int i = 0; i < output.size(); i++) {
			this.output[i] = output.get(i);
			outputString += output.get(i);
		}
	}
	
	public AutomatonTest(AutomatonTest test) {
		this.input = Arrays.copyOf(test.input, test.input.length);
		this.output = Arrays.copyOf(test.output, test.output.length);
		this.inputString = test.inputString;
		this.outputString = test.outputString;
	}

	public String[] getInput() {
		return input;
	}
	
	public String[] getOutput() {
		return output;
	}
	public String getOutputString() {
		return outputString;
	}
 	
	public double getLevenshteinDistance(String[] answer) {
		return StringUtils.levenshteinDistance(answer, output);
	}
	
	public double getLevenshteinDistance(String answer) {
		return StringUtils.levenshteinDistance(outputString, answer);
	}
	
	public double editDistance(String[] b) {
		int n = output.length;
		int m = b.length;
		double[][] d = new double[n + 1][m + 1];
		
		for (int i = 0; i <= n; i++) {
			d[i][0] = i;
		}
		for (int j = 0; j <= m; j++) {
			d[0][j] = j;
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				int cost;
				if (output[i].equals(b[j])) {
					cost = 0;
				} else {
					cost = 1;
				}
				d[i + 1][j + 1] = Math.min(Math.min(
									d[i][j + 1] + 1, 
									d[i + 1][j] + 1), 
									d[i][j] + cost);
			}
		}
		
		return d[n][m];
	}
	
	public int getHammingDistance(String answer) {
		return StringUtils.hammingDistance(answer, outputString);
	}
	
	@Override 
	public String toString() {
		String code = "";
		for (int i = 0; i < input.length - 1; i++) {
			code += input[i] + "_";
		}
		code += input[input.length - 1] + "->";
		
		for (int i = 0; i < output.length - 1; i++) {
			if (output[i].equals("")) {
				continue;
			}
			code += output[i] + "_";
		}
		if (!output[output.length - 1].equals("")) {
			code += output[output.length - 1];
		}
		if (code.endsWith("_")) {
			code = code.substring(0, code.length() - 1);
		}
		return code;
	}
	
	@Override
	public int hashCode() {
		return inputString.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		AutomatonTest other = (AutomatonTest)o;
		return hashCode() == other.hashCode();
	}
}