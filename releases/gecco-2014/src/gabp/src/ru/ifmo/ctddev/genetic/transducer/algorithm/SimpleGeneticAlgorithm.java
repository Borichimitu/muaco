package ru.ifmo.ctddev.genetic.transducer.algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import ru.ifmo.ctddev.genetic.transducer.scenario.Path;
import ru.ifmo.ctddev.genetic.transducer.scenario.TestGroup;
import ru.ifmo.ctddev.genetic.transducer.verifier.IVerifierFactory;
import ru.ifmo.ctddev.genetic.transducer.verifier.VerifierFactory;
import ru.ifmo.ltl.LtlParseException;

public class SimpleGeneticAlgorithm {
	private final double desiredFitness;
	private final int populationSize;
	private final int maxStates;
	
	private FST[] curGeneration;
	private Random random = new Random();
	
	private final double partStay;
	
	private final double mutationProb;
	
	private final int timeSmallMutation;
	
	private final int timeBigMutation;

	private final String[] setOfInputs;
	private final String[] setOfOutputs;
	
	private final List<TestGroup> groups;
    private final ArrayList<Path> tests;
	
	public SimpleGeneticAlgorithm(Parameters parameters,
              String[] setOfInputs, String[] setOfOutputs, List<TestGroup> groups) {
		this.populationSize = parameters.getPopulationSize();
		this.maxStates = parameters.getStateNumber();
		this.partStay = parameters.getPartStay();
		this.mutationProb = parameters.getMutationProbability();
		this.timeSmallMutation = parameters.getTimeSmallMutation();
		this.timeBigMutation = parameters.getTimeBigMutation();
		this.setOfInputs = setOfInputs;
		this.setOfOutputs = setOfOutputs;
		this.groups = groups;
        this. tests = new ArrayList<Path>();
        for (TestGroup g : groups) {
            tests.addAll(g.getTests());
        }

        IVerifierFactory verifier = new VerifierFactory(setOfInputs, setOfOutputs);
        try {
            verifier.prepareFormulas(groups.toArray(new TestGroup[groups.size()]));
        } catch (LtlParseException e) {
            throw new RuntimeException(e);   //TODO: do something less stupid
        }
        FST.fitnessCalculator = new FitnessCalculator(verifier, groups, tests);
        
//        Transition[][] tr = new Transition[3][];
//        tr[0] = new Transition[4];
//        tr[0][0] = new Transition("M", 1, 0);
//        tr[0][1] = new Transition("A", 0, 1);
//        tr[0][2] = new Transition("T", 1, 0);
//        tr[0][3] = new Transition("H", 1, 0);
//        
//        tr[1] = new Transition[4];
//        tr[1][0] = new Transition("M", 1, 1);
//        tr[1][1] = new Transition("T", 1, 1);
//        tr[1][2] = new Transition("H", 1, 1);
//        tr[1][3] = new Transition("A", 0, 2);
//        
//        tr[2] = new Transition[6];
//        tr[2][0] = new Transition("T [x2]", 2, 2);
//        tr[2][1] = new Transition("T [!x1 & !x2]", 1, 2);
//        tr[2][2] = new Transition("H", 1, 2);
//        tr[2][3] = new Transition("A", 1, 0);
//        tr[2][4] = new Transition("T [x1]", 2, 2);
//        tr[2][5] = new Transition("M", 1, 2);
//        
//        
//        FST test = new FST(tr, 0, setOfInputs, setOfOutputs);
//        System.out.println(FST.fitnessCalculator.calcFitness(test));
//        System.exit(0);
        

        this.desiredFitness = FST.fitnessCalculator.evaluateDesiredFitness(groups) + parameters.getDesiredFitness();
	}

	ArrayList<Double> generations = new ArrayList<Double>();
	
	private void init() {
		curGeneration = new FST[populationSize];
		for (int i = 0; i < populationSize; i++) {
			FST toAdd = FST.randomIndividual(maxStates, setOfInputs, setOfOutputs);
			curGeneration[i] = toAdd;
		}
		
//		int ok = 0;
//		for (int i = 0; i < 10000; i++) {
//			FST fst1 = FST.randomIndividual(maxStates, setOfInputs, setOfInputs);
//			int newId1[] = new int[maxStates];
//			FST canonical1 = fst1.getCanonicalFST(newId1);
//			
//			double fitness1 = fst1.getFitness();
//			if (fitness1 < 1e-5) {
//				continue;
//			}
//			double canonicalFitness1 = canonical1.getFitness();
//			if (Math.abs(fitness1 - canonicalFitness1) > 1e-5) {
//				throw new RuntimeException("diff = " + Math.abs(fitness1 - canonicalFitness1));
//			}
//			
//			while (true) {
//				FST fst2 = fst1.mutate().mutate().mutate(); 
//						//FST.randomIndividual(maxStates, setOfInputs, setOfInputs);
//				int newId2[] = new int[maxStates];
//				FST canonical2 = fst2.getCanonicalFST(newId2);
//				
//				if (Math.abs(fst2.getFitness() - canonical2.getFitness()) > 1e-5) {
//					throw new RuntimeException();
//				}
//				
//				if (canonical1.computeStringHash().equals(canonical2.computeStringHash())) {
//					double canonicalFitness2 = canonical2.getFitness();
//					if (Math.abs(canonicalFitness1 - canonicalFitness2) > 1e-5) {
//						throw new RuntimeException();
//					} else {
//						ok++;
////						System.out.println("ok");
//					}
//					break;
//				}
//			}
//		}
//		
//		System.out.println(ok);
		
//		
//		while (true) {
//			FST fst = FST.randomIndividual(maxStates, setOfInputs, setOfOutputs);
//			fst.getFitness();
//			FST mutated = fst.mutate();
//			if (!mutated.needToComputeFitness()){// && !fst.toString().equals(mutated.toString())) {
//				System.out.println(fst.printWithUsed());
//				System.out.println();
//				System.out.println(mutated.printWithUsed());
//				System.out.println();
//
//				System.out.println("diff=" + Math.abs(mutated.getFitness() - fst.getFitness()));
//				System.out.println();
//				System.out.println(mutated.printWithUsed());
//				if (Math.abs(mutated.getFitness() - fst.getFitness()) < 1e-5) {
//					continue;
//				} else {
//					break;
//				}
//			}
//		}
//		System.exit(0);
	}
	
	public FST go() {
		return go(Integer.MAX_VALUE, Double.MAX_VALUE);
	}
	
	public FST go(int maxFitnessEvals, double maxRunTime) {
		long startTime = System.currentTimeMillis();
		init();
		int genCount = 0;
		
		double lastBest = -1;
		int cntBestEqual = 0;
		
		ArrayList<Double> meanFitness = new ArrayList<Double>();
		
		double globalBestFitness = Double.MIN_VALUE;
		FST bestAutomaton = null;
		while (true) {
			double maxFitness = Integer.MIN_VALUE;
			double minFitness = Integer.MAX_VALUE;
			double sumFitness = 0;
			for (int i = 0; i < populationSize; i++) {
				if (FST.cntFitnessRun >= maxFitnessEvals) {
					if (bestAutomaton == null) {
						throw new RuntimeException("best = null at break, globalBestFitness=" + globalBestFitness);
					}
					return bestAutomaton;
				}
				sumFitness += curGeneration[i].getFitness();
				if (maxFitness < curGeneration[i].getFitness()) {
					maxFitness = curGeneration[i].getFitness();					
				}
				
				if (curGeneration[i].getFitness() > globalBestFitness) {
					bestAutomaton = new FST(curGeneration[i]);
					globalBestFitness = curGeneration[i].getFitness();
				}
				
				if ((System.currentTimeMillis() - startTime) / 1e3 > maxRunTime) {
					if (bestAutomaton == null) {
						throw new RuntimeException("best = null at break, globalBestFitness=" + globalBestFitness);
					}
					return bestAutomaton;
				}
				
				minFitness = Math.min(minFitness, curGeneration[i].getFitness());
			}
			
			generations.add(maxFitness);
			meanFitness.add(sumFitness / populationSize);
			
			
			System.out.println("Generation " + genCount + " Max fitness = " + maxFitness + " Min fitness = " + minFitness + " Sum fitness = " + sumFitness);
			if (genCount % 200 == 0) {
				try {
					PrintWriter out = new PrintWriter(new File("gen" + genCount + ".xml"));
//					out.println(bestAutomaton.);
					out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (maxFitness >= desiredFitness) {
				try {
					PrintWriter out = new PrintWriter(new File("fitness_graph"));
					for (double x : generations) {
						out.println(x);
					}
					out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					PrintWriter out = new PrintWriter(new File("mean_fitness_graph"));
					for (double x : meanFitness) {
						out.println(x);
					}
					out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return bestAutomaton;
			}
			
			if (Math.abs(maxFitness - lastBest) < 1E-15) {
				cntBestEqual++;
			} else {
				lastBest = maxFitness;
				cntBestEqual = 0;
			}
			
			FST[] nextGeneration = new FST[populationSize];
			int nextCnt = 0;
			
			Arrays.sort(curGeneration, new Comparator<FST>() {
				public int compare(FST a1, FST a2) {
					return Double.compare(a2.getFitness(), a1.getFitness());
				}}
			);
			
			int cntStay = (int) (partStay * populationSize);
			if ((populationSize - cntStay) % 2 == 1) {
				cntStay++;
			}
			for (int i = 0; i < cntStay; i++) {
				nextGeneration[nextCnt++] = curGeneration[i];
			}
			
			FST[] best = new FST[cntStay];
			for (int i = 0; i < cntStay; i++) {
				best[i] = curGeneration[i];
			}
			
			boolean[] can = new boolean[populationSize];
			Arrays.fill(can, true);
			int cntAdd = populationSize - cntStay;
			for (int i = 0; i < cntAdd / 2; i++) {
				int num1 = random.nextInt(populationSize);
				int num2 = num1;
				while (num1 == num2) {
					num2 = random.nextInt(populationSize);
				}
				if (random.nextDouble() > Const.MUTATION_PROBABILITY) {
					FST[] toAdd = FST.crossOver(curGeneration[num1], curGeneration[num2], groups);
					for (int j = 0; j < 2; j++) {
						nextGeneration[nextCnt++] = toAdd[j];
					}
				} else {
					nextGeneration[nextCnt++] = curGeneration[num1].mutate();
					nextGeneration[nextCnt++] = curGeneration[num2].mutate();
				}
			}
			
			for (int i = 0; i < populationSize; i++) {
				if (random.nextDouble() < mutationProb) {
					if (nextGeneration[i] == null) {
						System.out.println();
					}
					nextGeneration[i] = nextGeneration[i].mutate();
				}
			}

			if (cntBestEqual >= timeSmallMutation) {
				for (int i = populationSize / 10; i < populationSize; i++) {
					nextGeneration[i] = nextGeneration[i].mutate();
				}
			}

			if (cntBestEqual >= timeBigMutation) {
				int start = 0;
				for (int i = start; i < populationSize; i++) {
					nextGeneration[i] = FST.randomIndividual(maxStates, setOfInputs, setOfOutputs);
				}
			}
			
			curGeneration = nextGeneration;
			genCount++;
		}
	}

    public List<Path> getTests() {
        return Collections.unmodifiableList(tests);
    }

    public double getDesiredFitness() {
        return desiredFitness;
    }

}

