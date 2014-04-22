package ru.ifmo.optimization.algorithm.muaco.pathselector.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import ru.ifmo.optimization.algorithm.muaco.pathselector.roulette.AbstractRouletteEdgeSelector;
import ru.ifmo.optimization.algorithm.muaco.pathselector.roulette.SingleObjectiveRouletteEdgeSelector;

public class PathSelectorConfig {
	private static enum RouletteEdgeSelectorType {
		SINGLE_OBJECTIVE,
		BI_OBJECTIVE
	}
	
	private Properties properties;
	
	public PathSelectorConfig(String filename) {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getIntProperty(String name) {
		if (properties.containsKey(name)) {
			return Integer.parseInt(properties.getProperty(name));
		}
		return 0;
	}
	
	public double getDoubleProperty(String name) {
		return Double.parseDouble(properties.getProperty(name));
	}
	
	public boolean getBooleanProperty(String name) {
		return Boolean.parseBoolean(properties.getProperty(name));
	}
	
	public double getAlpha() {
		return Double.parseDouble(properties.getProperty("alpha"));
	}
	
	public double getBeta() {
		return Double.parseDouble(properties.getProperty("beta"));
	}
	
	public String getStringProperty(String name) {
		return properties.getProperty(name);
	}
	
	public AbstractRouletteEdgeSelector getRouletteEdgeSelector() {
		RouletteEdgeSelectorType type;
		try {
			type = RouletteEdgeSelectorType.valueOf(getStringProperty("edge-selector"));
		} catch (Exception e) {
			return null;
		}
		
		switch (type) {
		case SINGLE_OBJECTIVE:
			return new SingleObjectiveRouletteEdgeSelector(getAlpha(), getBeta());
		}
		return null;
	}
}
