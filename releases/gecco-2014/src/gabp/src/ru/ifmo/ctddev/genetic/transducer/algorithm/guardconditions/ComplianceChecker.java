package ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import bool.MyBooleanExpression;

public class ComplianceChecker {

	private static ComplianceChecker complianceChecker;
	private HashMap<String, Integer> inputToNumber;
	private boolean[][] compliant;
	
	public static void createComplianceChecker(String[] setOfInputs) throws ParseException {
		complianceChecker = new ComplianceChecker(setOfInputs);
	}
	
	public static void createComplianceChecker(List<String> listOfInputs) throws ParseException {
		String[] setOfInputs = listOfInputs.toArray(new String[1]);
		complianceChecker = new ComplianceChecker(setOfInputs);
	}

	public static ComplianceChecker getComplianceChecker() {
		return complianceChecker;
	}

	public boolean checkCompliancy(String input1, String input2) {
		return compliant[inputToNumber.get(input1.trim())][inputToNumber.get(input2.trim())];
	}
	
	private ComplianceChecker(String[] setOfInputs) throws ParseException {
		int n = setOfInputs.length;
		inputToNumber = new HashMap<String, Integer>();
		for (int i = 0; i < n; i++) {
			inputToNumber.put(setOfInputs[i], i);
		}
		
		String[] event = new String[n];
		MyBooleanExpression[] guardCondition = new MyBooleanExpression[n];
		
		for (int i = 0; i < n; i++) {
			event[i] = parseEvent(setOfInputs[i]);
//			System.err.println(event[i]);
			guardCondition[i] = parseGuardCondition(setOfInputs[i]);
		}
		
		compliant = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			compliant[i][i] = false;
			for (int j = i + 1; j < n; j++) {
				if (!event[i].equals(event[j])) {
					compliant[i][j] = true;
					compliant[j][i] = true;
					continue;
				}
				if (guardCondition[i].equals(guardCondition[j])) {
					compliant[i][j] = false;
					compliant[j][i] = false;
					continue;
				}
				if (guardCondition[i].hasSolutionWith(guardCondition[j])) {
					compliant[i][j] = false;
					compliant[j][i] = false;
					continue;
				}
				compliant[i][j] = true;
				compliant[j][i] = true;
			}
		}
	}

	private MyBooleanExpression parseGuardCondition(String s) throws ParseException {
		if (s.indexOf("[") > 0) {
//			System.err.println(s.substring(s.indexOf("[") + 1, s.indexOf("]")).trim());
			return new MyBooleanExpression(s.substring(s.indexOf("[") + 1, s.indexOf("]")).trim());
		} else {
			return new MyBooleanExpression("1");
		}
	}

	private String parseEvent(String s) {
		if (s.indexOf("[") > 0) {
			return s.substring(0, s.indexOf("[")).trim();
		} else {
			return s.trim();
		}
	}
	
	
	
}
