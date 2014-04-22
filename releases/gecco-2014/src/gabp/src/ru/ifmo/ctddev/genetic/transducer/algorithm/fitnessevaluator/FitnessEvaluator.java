package ru.ifmo.ctddev.genetic.transducer.algorithm.fitnessevaluator;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;

public abstract class FitnessEvaluator {
	public abstract double getFitness(FST fst);
}
