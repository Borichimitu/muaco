package ru.ifmo.optimization.runner;

import java.io.File;

import ru.ifmo.optimization.AbstractOptimizationAlgorithm;
import ru.ifmo.optimization.OptimizationAlgorithmCutoff;
import ru.ifmo.optimization.algorithm.muaco.config.MuACOConfig;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.task.AbstractTaskFactory;
import ru.ifmo.optimization.runner.config.OptimizationRunnerConfig;
import ru.ifmo.util.RandomProvider;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class OptimizationRunner {
	private AbstractOptimizationAlgorithm optimizationAlgorithm;
	private AbstractTaskFactory taskFactory;
	private OptimizationRunnerConfig config;
	
	public OptimizationRunner(OptimizationRunnerConfig config) {
		this.config = config;
		taskFactory = config.getTaskFactory();
	}
	
	public void run() {
		String attemptsDirName = config.solutionDirName();
		File attemptsDir = new File(attemptsDirName);
		attemptsDir.mkdir();
		
		RandomProvider.initialize(new MuACOConfig("muaco.properties").getNumberOfThreads());
		for (int i = 0; i < config.numberOfExperiments(); i++) {
//			RandomProvider.initialize(2345245);
			OptimizationAlgorithmCutoff.getInstance().setCutoff(config.getMaxEvalutions(), config.getMaxRunTime(), System.currentTimeMillis());
			optimizationAlgorithm = config.getOptimizationAlgorithmFactory().createOptimizationAlgorithm(taskFactory);			
			String dirName = attemptsDirName + "/attempt" + i + "/";
			File dir = new File(dirName);
			dir.mkdir();
			long start = System.currentTimeMillis();
			InstanceMetaData best = optimizationAlgorithm.runAlgorithm();
			best.setTime((System.currentTimeMillis() - start) / 1000.0);
			best.setInstanceGenerationTime((best.getInstanceGenerationTime() - start) / 1000.0);
			best.setFitnessEvaluationTime(RunStats.TIME_FITNESS_COMPUTATION);
			best.setNumberOfCacheHits(RunStats.N_CACHE_HITS);
			best.setNumberOfCanonicalCacheHits(RunStats.N_CANONICAL_CACHE_HITS);
			best.setNumberOfLazySavedFitnessEvals(RunStats.N_SAVED_EVALS_LAZY);
			best.setSharedBundleHits(RunStats.GRAPH_BUNDLE_HITS.intValue());
//			best.setCanonicalDistance((double)RunStats.N_CANONICAL_DIST / (double)RunStats.N_CANONICAL_CACHE_HITS);
			best.setCanonizationTime(RunStats.TIME_CANONIZATION);
			best.setCanonicalDistance(RunStats.ERROR);// / (RunStats.N_CANONICAL_CACHE_HITS == 0 ? 1 : RunStats.N_CANONICAL_CACHE_HITS));
			RunStats.reset();
			best.print(dirName);
		}
	}
}
