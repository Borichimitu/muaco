package ru.ifmo.optimization.instance.fsm.task.tests.generator.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestGeneratorConfig {
	private Properties properties = new Properties();
	
	public TestGeneratorConfig() {
		try {
			properties.load(new FileInputStream(new File("test-generator.properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getNumberOfStates() {
		return Integer.parseInt(properties.getProperty("desired-number-of-states"));
	}
	
	public List<String> getEvents() {
		List<String> events = new ArrayList<String>();
		String eventArray[] = properties.getProperty("events").split(" ");
		for (int i = 0; i < eventArray.length; i++) {
			events.add(eventArray[i]);
		}
		return events;
	}
	
	public List<String> getActions() {
		String[] act = properties.getProperty("actions").split(" ");
		List<String> actions = new ArrayList<String>();
		for (String s : act) {
			actions.add(s);
		}
		return actions;
	}
	
	public int getTrainingSetSize() {
		return Integer.parseInt(properties.getProperty("training-set-size"));
	}
	
	public int getTestSetSize() {
		return Integer.parseInt(properties.getProperty("test-set-size"));
	}
	
	public int getLengthOfTestInTrainingSet() {
		return Integer.parseInt(properties.getProperty("training-set-test-length"));
	}
	
	public int getLengthOfTestInTestSet() {
		return Integer.parseInt(properties.getProperty("test-set-test-length"));
	}
	
	public int getMaximumLengthOfOutputSequence() {
		return Integer.parseInt(properties.getProperty("max-length-of-output-sequence"));
	}
	
	public double getNoiseLevel() {
		return Double.parseDouble(properties.getProperty("noise-level"));
	}
}
