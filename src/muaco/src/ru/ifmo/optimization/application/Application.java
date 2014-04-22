package ru.ifmo.optimization.application;
import ru.ifmo.optimization.runner.OptimizationRunner;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class Application {
	public static void main(String[] args) {
		OptimizationRunner runner = new OptimizationRunner(new OptimizationRunnerConfig("experiment.properties"));
		runner.run();
	}
}
