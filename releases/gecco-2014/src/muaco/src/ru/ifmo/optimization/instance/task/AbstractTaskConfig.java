package ru.ifmo.optimization.instance.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import ru.ifmo.optimization.instance.FitnessValue;

public class AbstractTaskConfig {
	protected Properties properties;
	
	public AbstractTaskConfig(String configFileName) {
		try {
			properties = new Properties();
			properties.load(new FileInputStream(new File(configFileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String propertyName) {
		return properties.getProperty(propertyName);
	}
	
	public String getTaskName() {
		return properties.getProperty("class-name");
	}
	
	public FitnessValue getDesiredFitness() {
		String desiredFitness = properties.getProperty("desired-fitness");
		String[] stringValues = desiredFitness.split(" ");
		double[] values = new double[stringValues.length];
		for (int i = 0; i < stringValues.length; i++) {
			values[i] = Double.parseDouble(stringValues[i]);
		}
		return new FitnessValue(values);
	}
}
