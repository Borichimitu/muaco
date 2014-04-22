package ru.ifmo.ctddev.genetic.transducer.algorithm.fitnessevaluator;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;

public class PlainFitnessEvaluator extends FitnessEvaluator {

	@Override
	public double getFitness(FST fst) {
		if (!fst.isFitnessCalculated()) {
			FST.cntFitnessRun++;
			fst.setFitness(FST.fitnessCalculator.calcFitness(fst));
			fst.setNeedToComputeFitness(false);
			fst.setFitnessCalculated(true);
		}
		return fst.fitness();	
	}

}
