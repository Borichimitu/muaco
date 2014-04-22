package ru.ifmo.optimization.algorithm.muaco.ant.config;

import java.util.Properties;

import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;

public abstract class AntConfig {
	protected AbstractPathSelector pathSelector;
	private Properties params;

	public AntConfig(AbstractPathSelector pathSelector, Properties params) {
		this.pathSelector = pathSelector;
		this.params = params;
	}
	
	public AbstractPathSelector getPathSelector() {
		return pathSelector;
	}
	
	public String getParam(String name) {
		return params.getProperty(name);
	}
	
	public int getIntegerParam(String name) {
		return Integer.parseInt(params.getProperty(name));
	}
	
	public double getDoubleParam(String name) {
		return Double.parseDouble(params.getProperty(name));
	}
}
