package ru.ifmo.optimization.algorithm.muaco.ant.current;

import java.util.Properties;

import ru.ifmo.optimization.algorithm.muaco.ant.config.AntConfig;
import ru.ifmo.optimization.algorithm.muaco.pathselector.AbstractPathSelector;

public class CurrentAntConfig extends AntConfig {
	public CurrentAntConfig(AbstractPathSelector pathSelector, Properties params) {
		super(pathSelector, params);
	}
	
	
}
