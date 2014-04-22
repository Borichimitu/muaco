package ru.ifmo.optimization.runner.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import ru.ifmo.optimization.algorithm.muaco.factory.MuACOFactory;
import ru.ifmo.optimization.instance.fsm.algorithm.factory.OptimizationAlgorithmFactory;
import ru.ifmo.optimization.instance.fsm.task.factory.FsmTaskFactory;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class OptimizationRunnerConfig {
	private static enum AlgorithmType {
		MUACO 
	}
	
	public static enum InstanceType {
		FSM
	}
	
	private Properties properties;
	
	public OptimizationRunnerConfig(String configFileName) {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configFileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public AbstractTaskFactory getTaskFactory() {
		switch (getInstanceType()) {
		case FSM:
			return new FsmTaskFactory(new AbstractTaskConfig(getTaskConfigFileName()));
		default:
			return null;
		}
	}
	
	public InstanceType getInstanceType() {
		return InstanceType.valueOf(properties.getProperty("instance-type"));
	}
	
	public String solutionDirName() {
		return properties.getProperty("solution-dir-name");
	}
	
	public int numberOfExperiments() {
		return Integer.parseInt(properties.getProperty("number-of-experiments"));
	}
	
	public String acoConfigFileName() {
		return properties.getProperty("aco-config-file-name");
	}
	
	public String getTaskConfigFileName() {
		return properties.getProperty("task-config-file-name");
	}
	
	
	public int getMaxEvalutions() {
		return Integer.parseInt(properties.getProperty("max-evaluations", "-1"));
	}
	
	public double getMaxRunTime() {
		return Double.parseDouble(properties.getProperty("max-run-time", "-1"));
	}
	
	public OptimizationAlgorithmFactory getOptimizationAlgorithmFactory() {
		AlgorithmType algorithmType = AlgorithmType.valueOf(properties.getProperty("algorithm-type"));
		switch (algorithmType) {
		case MUACO:
			return new MuACOFactory(this);
		}
		return null;
	}
}
