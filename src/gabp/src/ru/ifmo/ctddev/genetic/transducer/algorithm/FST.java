package ru.ifmo.ctddev.genetic.transducer.algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import ru.ifmo.ctddev.genetic.transducer.algorithm.fitnessevaluator.FitnessEvaluator;
import ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions.ComplianceChecker;
import ru.ifmo.ctddev.genetic.transducer.algorithm.util.ITransitionChecker;
import ru.ifmo.ctddev.genetic.transducer.algorithm.util.Pair;
import ru.ifmo.ctddev.genetic.transducer.scenario.Path;
import ru.ifmo.ctddev.genetic.transducer.scenario.TestGroup;

public class FST {
	public static FitnessCalculator fitnessCalculator = null;
	public static FitnessEvaluator fitnessEvaluator = null;

	public static int cntFitnessRun = 0;
	public static int cntLazySaved = 0;
	public static int cntBfsSaved = 0;
	
	public static boolean USE_LAZY_FITNESS_CALCULATION;
    public static boolean USE_BFS_CACHE;
	
	private final static Random RANDOM = new Random();

	private int initialState;
	private final int stateNumber;
	private Transition[][] states;
	
	private final String[] setOfInputs;
	private final String[] setOfOutputs;
	
	private boolean isLabelled;
	
	private boolean fitnessCalculated = false;
	private double fitness;
	
	private boolean needToComputeFitness;
	
	public FST(Transition[][] states, int initialState, String[] setOfInputs, String[] setOfOutputs) {
		this.initialState = initialState;
		this.stateNumber = states.length;
		
		this.states = new Transition[stateNumber][];
		
		for (int i = 0; i < stateNumber; i++) {
			this.states[i] = states[i].clone();
		}
		this.setOfInputs = setOfInputs.clone();
		this.setOfOutputs = setOfOutputs.clone();
		this.isLabelled = false;
		needToComputeFitness = !isLabelled || !fitnessCalculated;
	}
	
	public FST(FST other) {
		this.initialState = other.initialState;
		this.stateNumber = other.stateNumber;
		
		this.states = new Transition[stateNumber][];
		for (int i = 0; i < stateNumber; i++) {
			this.states[i] = new Transition[other.states[i].length];
			for (int j = 0; j < other.states[i].length; j++) {
				states[i][j] = new Transition(other.states[i][j].getInput(), other.states[i][j].getOutputSize(), other.states[i][j].getNewState());
			}
		}
		
		this.setOfInputs = other.setOfInputs.clone();
		this.setOfOutputs = other.setOfOutputs.clone();
		this.isLabelled = false;
		needToComputeFitness = !isLabelled || !fitnessCalculated;
	}
	
	
	public FST(String ulyantsevFilename, int numberOfStates) {
		Scanner in = null;
		try {
			in = new Scanner(new File(ulyantsevFilename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.isLabelled = true;
		this.stateNumber = numberOfStates;
		initialState = 0;
		
		Set<String> inputs = new HashSet<String>();
		Set<String> outputs = new HashSet<String>();
		
		ArrayList<Transition>[] transitions = new ArrayList[stateNumber];
		for (int i = 0; i < transitions.length; i++) {
			transitions[i] = new ArrayList<Transition>();
		}
		
		while (in.hasNextLine()) {
			String s = in.nextLine();
			
			if (s.contains("initial-state")) {
				initialState = Integer.parseInt(s.split(" = ")[1]);
			}
			
			if (!s.contains("label")) {
				continue;
			}
			
			String[] startStateAndRest = s.split("->");
			int startState = Integer.parseInt(startStateAndRest[0].trim());
			String[] endStateAndRest = startStateAndRest[1].split("\\[label =");
			int endState = Integer.parseInt(endStateAndRest[0].trim());
			String[] eventAndRest = endStateAndRest[1].split("\\(");
			String event = eventAndRest[0].trim().replaceAll("~", "!").replaceAll("\"", "");
			String[] actions = eventAndRest[1].replace(")", "").replace("\"];", "").trim().split(", ");
			
			inputs.add(event);
			for (String a : actions) {
				if (a.length() > 0) {
					outputs.add(a);
				}
			}
			
			Transition t = new Transition(event, actions.length, endState);
			t.setOutput(actions);
			transitions[startState].add(t);
		}
		
		states = new Transition[stateNumber][];
		
		for (int i = 0; i < transitions.length; i++) {
			states[i] = new Transition[transitions[i].size()];
			for (int j = 0; j < states[i].length; j++) {
				states[i][j] = transitions[i].get(j);
			}
		}
		
		this.setOfInputs = inputs.toArray(new String[0]);
		this.setOfOutputs = outputs.toArray(new String[0]);
		
		try {
			ComplianceChecker.createComplianceChecker(setOfInputs);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (int i = 0; i < stateNumber; i++) {
			states[i] = removeDuplicates(states[i]);
		}
		
		in.close();
	}
	
	public String[] getSetOfInputs() {
		return setOfInputs;
	}
	
	public String[] getSetOfOutputs() {
		return setOfOutputs;
	}
	
	public int getNumberOfStates() {
		return stateNumber;
	}
	
	public FST copyWithOutputAndMarks() {
		FST result = new FST(this);
		for (int i = 0; i < stateNumber; i++) {
			for (int j = 0; j < result.states[i].length; j++) {
				result.states[i][j].setOutput(states[i][j].getOutput());
				result.states[i][j].used = states[i][j].used;
			}
		}
		return result;
	}
	
	public double fitness() {
		return fitness;
	}

    public Transition[][] getStates() {
        return states;
    }

    public int getInitialState() {
        return initialState;
    }
    
    public boolean isFitnessCalculated() {
    	return fitnessCalculated;
    }
    
    public void setFitness(double fitness) {
    	this.fitness = fitness;
    }
    
    public void setFitnessCalculated(boolean value) {
    	fitnessCalculated = value;
    }
     
	public static FST randomIndividual(int stateNumber, String[] setOfInputs, String[] setOfOutputs) {
		int inputsCnt = setOfInputs.length;
		Transition[][] states = new Transition[stateNumber][];
		for (int i = 0; i < stateNumber; i++) {
			int[] p = new int[inputsCnt];
			for (int j = 0; j < inputsCnt; j++) {
				p[j] = j;
			}
			for (int j = inputsCnt - 1; j >= 1; j--) {
				int k = RANDOM.nextInt(j);
				int t = p[j];
				p[j] = p[k];
				p[k] = t;
			}
			
			int degree = RANDOM.nextInt(Math.min(inputsCnt, 5));
			
			Transition[] currentState = new Transition[degree];
			
			for (int j = 0; j < degree; j++) {
				currentState[j] = new Transition(setOfInputs[p[j]], 1, RANDOM.nextInt(stateNumber));
			}
			states[i] = currentState;
		}
		for (int i = 0; i < stateNumber; i++) {
			states[i] = removeDuplicates(states[i]);
		}
		
		return new FST(states, RANDOM.nextInt(stateNumber), setOfInputs, setOfOutputs);
	}
	
	public static FST[] crossOver(FST fst1, FST fst2, List<TestGroup> groups) {
        switch (RANDOM.nextInt(3)) {
            case 0:
                return fst1.crossOver(fst2);
            case 1:
                return fst1.crossOverBasedOnTests(fst2, groups);
            case 2:
                return fst1.crossOverBasedOnLtl(fst2);
            default:
                throw new RuntimeException("Unexpected crossover type.");
        }
	}
	
	private void markUsedTransitions(List<Path> tests) {
		ArrayList<Pair> tlist = new ArrayList<Pair>();
        for (Path t : tests) {
        	double fitness = fitnessCalculator.calcFitnessForTest(this, t) * t.size();
            tlist.add(new Pair(t, fitness));
        }
        
		Collections.sort(tlist);
		
		int testsCnt = Math.max(Const.MIN_USED_TESTS, tests.size() / 5);

        for (int i = 0; i < testsCnt; i++) {
			Path test = tlist.get(i).getTest();
			int currentState = initialState;
			for (String s : test.getInput()) {
				boolean found = false;
				for (Transition t : states[currentState]) {
					if (t.accepts(s)) {
						currentState = t.getNewState();
						found = true;
						t.markUsed();
						break;
					}
				}
				if (!found) {
					break;
				}
			}
		}
	}

    private void markUnused() {
        for (Transition[] state : states) {
            for (Transition t : state) {
                t.markUnused();
            }
        }
    }

    public FST[] crossOverBasedOnTests(FST that, List<TestGroup> groups) {
        this.markUnused();
        that.markUnused();
        for (TestGroup g : groups) {
            this.markUsedTransitions(g.getTests());
		    that.markUsedTransitions(g.getTests());
        }
		int initialState1 = this.initialState;
		int initialState2 = that.initialState;
		
		Transition[][] states1 = new Transition[stateNumber][];
		Transition[][] states2 = new Transition[stateNumber][];
		
		for (int i = 0; i < stateNumber; i++) {
			
			ArrayList<Transition> toStates1 = new ArrayList<Transition>();
			ArrayList<Transition> toStates2 = new ArrayList<Transition>();
			
			{
				for (Transition t : this.states[i]) {
					if (t.used && !t.isUsedByNegativeTest()) {
						toStates1.add(t.copy(setOfInputs, setOfOutputs, stateNumber));
					}
				}
				for (Transition t : that.states[i]) {
					if (t.used && !t.isUsedByNegativeTest()) {
						toStates1.add(t.copy(setOfInputs, setOfOutputs, stateNumber));
					}
				}
				for (Transition t : this.states[i]) {
					if (!t.used || t.isUsedByNegativeTest()) {
						toStates1.add(t.copy(setOfInputs, setOfOutputs, stateNumber));
					}
				}
			}
			
			{
				for (Transition t : that.states[i]) {
					if (t.used && !t.isUsedByNegativeTest()) {
						toStates2.add(t.copy(setOfInputs, setOfOutputs, stateNumber));
					}
				}
				for (Transition t : this.states[i]) {
					if (t.used && !t.isUsedByNegativeTest()) {
						toStates2.add(t.copy(setOfInputs, setOfOutputs, stateNumber));
					}
				}
				for (Transition t : that.states[i]) {
					if (!t.used || t.isUsedByNegativeTest()) {
						toStates2.add(t.copy(setOfInputs, setOfOutputs, stateNumber));
					}
				}
			}

			states1[i] = removeDuplicates(toStates1.toArray(new Transition[toStates1.size()]));
			states2[i] = removeDuplicates(toStates2.toArray(new Transition[toStates2.size()]));
		}
		
		return new FST[] {
				new FST(states1, initialState1, setOfInputs, setOfOutputs),
				new FST(states2, initialState2, setOfInputs, setOfOutputs)
		};
		
    }

    public FST[] crossOverBasedOnLtl(FST that) {
        return crossOverByMarked(that, new ITransitionChecker() {
            public boolean isMarked(Transition t) {
                return t.isVerified() && !t.isUsedByVerifier();
            }
        });
    }

    public FST[] crossOverByMarked(FST that, ITransitionChecker check) {
		int initialState1;
		int initialState2;

		if (RANDOM.nextBoolean()) {
			initialState1 = this.initialState;
			initialState2 = that.initialState;
		} else {
			initialState1 = that.initialState;
			initialState2 = this.initialState;
		}

		Transition[][] states1 = new Transition[stateNumber][];
		Transition[][] states2 = new Transition[stateNumber][];

		for (int i = 0; i < stateNumber; i++) {
			int degree1 = this.states[i].length;
			int degree2 = that.states[i].length;

			states1[i] = new Transition[degree1];
			states2[i] = new Transition[degree2];
			int p1 = 0;
			int p2 = 0;

			ArrayList<Transition> list = new ArrayList<Transition>();
			for (Transition t : this.states[i]) {
				if (check.isMarked(t)) {
					states1[i][p1++] = t.copy(setOfInputs, setOfOutputs, stateNumber);
				} else {
					list.add(t);
				}
			}
			for (Transition t : that.states[i]) {
				if (check.isMarked(t)) {
					states2[i][p2++] = t.copy(setOfInputs, setOfOutputs, stateNumber);
				} else {
					list.add(t);
				}
			}
			Collections.shuffle(list);

			degree1 -= p1;
			degree2 -= p2;

			for (int j = 0; j < degree1; j++) {
				states1[i][p1++] = list.get(j).copy(setOfInputs, setOfOutputs, stateNumber);
			}
			for (int j = 0; j < degree2; j++) {
				states2[i][p2++] = list.get(degree1 + j).copy(setOfInputs, setOfOutputs, stateNumber);
			}
			states1[i] = removeDuplicates(states1[i]);
			states2[i] = removeDuplicates(states2[i]);
		}

		return new FST[] {
				new FST(states1, initialState1, setOfInputs, setOfOutputs),
				new FST(states2, initialState2, setOfInputs, setOfOutputs)
		};
	}
	
	public FST[] crossOver(FST that) {
		int initialState1;
		int initialState2;
		
		if (RANDOM.nextBoolean()) {
			initialState1 = this.initialState;
			initialState2 = that.initialState;
		} else {
			initialState1 = that.initialState;
			initialState2 = this.initialState;
		}
		
		Transition[][] states1 = new Transition[stateNumber][];
		Transition[][] states2 = new Transition[stateNumber][];
		
		for (int i = 0; i < stateNumber; i++) {
			int degree1 = this.states[i].length;
			int degree2 = that.states[i].length;
			
			ArrayList<Transition> list = new ArrayList<Transition>();
			for (Transition t : this.states[i]) {
				list.add(t);
			}
			for (Transition t : that.states[i]) {
				list.add(t);
			}
			Collections.shuffle(list);

			if (RANDOM.nextBoolean()) {
				int t = degree1;
				degree1 = degree2;
				degree2 = t;
			} 
			states1[i] = new Transition[degree1];
			states2[i] = new Transition[degree2];
			
			for (int j = 0; j < degree1; j++) {
				states1[i][j] = list.get(j).copy(setOfInputs, setOfOutputs, stateNumber);
			}
			for (int j = 0; j < degree2; j++) {
				states2[i][j] = list.get(degree1 + j).copy(setOfInputs, setOfOutputs, stateNumber);
			}
			states1[i] = removeDuplicates(states1[i]);
			states2[i] = removeDuplicates(states2[i]);
		}
		
		return new FST[] {
				new FST(states1, initialState1, setOfInputs, setOfOutputs),
				new FST(states2, initialState2, setOfInputs, setOfOutputs)
		};
	}
	
	
	public static Transition[] removeDuplicates(Transition[] transitions, ComplianceChecker complianceChecker) {
		ArrayList<Transition> list = new ArrayList<Transition>();
		for (Transition t : transitions) {
			boolean compliant = true;
			for (Transition t1 : list) {
				compliant &= complianceChecker.checkCompliancy(t.input, t1.input);
			}
			if (compliant) {
				list.add(t);
			}
		}
		Transition[] res = new Transition[list.size()];
		{
			int i = 0;
			for (Transition t : list) {
				res[i++] = t;
			}
		}
		return res;
	}
	
	
	public static Transition[] removeDuplicates(Transition[] transitions) {
		return removeDuplicates(transitions, ComplianceChecker.getComplianceChecker());
	}

	public FST mutate() {
		boolean computeFitness = !isLabelled | !fitnessCalculated | needToComputeFitness;
		int newInitialState;
		if (RANDOM.nextDouble() < Const.MUTATION_THRESHOLD) {
			newInitialState = RANDOM.nextInt(stateNumber);
			computeFitness = true;
//			if (!computeFitness) {
//				computeFitness = (newInitialState != initialState);
//			}
		} else {
			newInitialState = initialState;
		}
		Transition[][] newStates = new Transition[stateNumber][];
		for (int i = 0; i < stateNumber; i++) {
			newStates[i] = new Transition[states[i].length];
			for (int j = 0; j < states[i].length; j++) {
				newStates[i][j] = states[i][j].copy(setOfInputs, setOfOutputs, stateNumber);

                Transition t = states[i][j];
                if ((t.isUsedByVerifier() || t.isUsedByNegativeTest()) && (RANDOM.nextDouble() < Const.VERIFIER_MUTATION_PROBABILITY)) {
                	 if (t.used) {
                     	computeFitness = true;
                     }
                    //mutate if is in counterexample or last transition in negative test
                    newStates[i][j] = t.mutate(setOfInputs, setOfOutputs, stateNumber);
                    if (!newStates[i][j].getInput().equals(t.getInput())) {
                    	computeFitness = true;
                    }
                }
			}
		}
		for (int i = 0; i < stateNumber; i++) {
			for (int j = 0; j < newStates[i].length; j++) {
				if (RANDOM.nextDouble() < Const.MUTATION_THRESHOLD) {
					if (newStates[i][j].used) {
						computeFitness = true;
					}
					Transition newT = newStates[i][j].mutate(setOfInputs, setOfOutputs, stateNumber);
					if (!newT.getInput().equals(newStates[i][j].getInput())) {
						computeFitness = true;
					}
					newStates[i][j] = newT;
				}
			}
		}
		for (int i = 0; i < stateNumber; i++) {
			if (RANDOM.nextDouble() < Const.MUTATION_THRESHOLD) {
				computeFitness = true;
				if (RANDOM.nextBoolean()) {					
					if (newStates[i].length > 0) {
						List<Transition> deleted = new ArrayList<Transition>();
						newStates[i] = deleteTransition(newStates[i], deleted);
					}
				} else {
					if (newStates[i].length < setOfInputs.length) {
						newStates[i] = addTransition(newStates[i]);
					}
				}
			}
		}
		for (int i = 0; i < stateNumber; i++) {
			newStates[i] = removeDuplicates(newStates[i]);
		}
		FST result = new FST(newStates, newInitialState, setOfInputs, setOfOutputs);		
		result.fitness = fitness;
		result.needToComputeFitness = computeFitness;
		return result;
	}
	
	private Transition[] addTransition(Transition[] t) {
		Transition[] res = new Transition[t.length + 1];
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < t.length; i++) {
			res[i] = t[i];
			set.add(t[i].input);
		}
		ArrayList<String> notUsed = new ArrayList<String>();
		for (String input : setOfInputs) {
			if (!set.contains(input)) {
				notUsed.add(input);
			}
		}
		String input = notUsed.get(RANDOM.nextInt(notUsed.size()));
		res[t.length] = new Transition(input, 1, RANDOM.nextInt(stateNumber));
		return res;
	}
	
	private Transition[] deleteTransition(Transition[] s, List<Transition> deleted) {
		Transition[] res = new Transition[s.length - 1];
        int toDelete;
        //if transition is in verifier stack or is last transition in negative test than remove it
		for (toDelete = 0; (toDelete < s.length); toDelete++) {
            Transition t = s[toDelete];
            if (t.isUsedByVerifier() || t.isUsedByNegativeTest()) {
                break;
            }
        }
        if (toDelete >= s.length) {
            toDelete = RANDOM.nextInt(s.length);
        }
        deleted.add(s[toDelete]);

		int j = 0;
		for (int i = 0; i < s.length; i++) {
			if (i != toDelete) {
				res[j++] = s[i];
			}
		}
		return res;
	}	
	
	public double getFitness() {
		return fitnessEvaluator.getFitness(this);
//		if (!fitnessCalculated) {
//			if (needToComputeFitness) {
//				//try looking into the canonicalInstancesCache
//				int newId[] = new int[states.length];
//				FST canonicalFST = getCanonicalFST(newId);
//				if (canonicalInstancesCache.contains(canonicalFST)) {
//					CanonicalCachedData data = canonicalInstancesCache.getFirstNonCanonicalInstance(canonicalFST);
//					FST cachedInstance = data.getFST();
//					fitness = cachedInstance.fitness;
//					if (fitness > 0) {
//						fitness += fitnessCalculator.correctFitness(cachedInstance, this);
//					}
//					transformUsedTransitions(data.getNewId(), newId, cachedInstance);
//					
//					cntBfsSaved++;
//				} else {
//					cntFitnessRun++;
//					fitness = fitnessCalculator.calcFitness(this);
//					needToComputeFitness = false;
//					FST copy = this.copyWithOutputAndMarks();
//					copy.fitness = fitness;
//					canonicalInstancesCache.add(new CanonicalCachedData(copy, newId), canonicalFST);
//				}
//			} else {
//				cntLazySaved++;
//			}
//			fitnessCalculated = true;
//		}
//		return fitness;
	}

    /**
     * Get all transitions count
     * @return transition count
     */
	public int getTransitionsCount() {
		int res = 0;
		for (int i = 0; i < stateNumber; i++) {
			res += states[i].length;
		}
		return res;
	}

    /**
     * Get reached transitions count
     * @return reached transitions count
     */
    public int getUsedTransitionsCount() {
        return getUsedTransitionsCount(initialState, new boolean[states.length]);
    }

    private int getUsedTransitionsCount(int state, boolean[] vizited) {
        vizited[state] = true;
        int res = states[state].length;
        for (Transition t : states[state]) {
            if (!vizited[t.getNewState()]) {
                res += getUsedTransitionsCount(t.getNewState(), vizited);
            }
        }
        return res;
    }
	
	public void doLabelling(ArrayList<Path> tests) {
		if (isLabelled) {
			return;
		}
		for (Transition[] state : states) {
			for (Transition t : state) {
				t.beginLabeling();
			}
		}
		for (Path test : tests) {
			int currentState = initialState;
			String[] inputSequence = test.getInput();
			String[] outputSequence = test.getFixedOutput();
            boolean fixedOutput = true;
            if (outputSequence.length == 0) {
                outputSequence = test.getOutput();
                fixedOutput = false;
            }
            
            assert !fixedOutput || (inputSequence.length == outputSequence.length);

			int pOutput = 0;
			for (String s : inputSequence) {
				boolean found = false;
				for (Transition t : states[currentState]) {
					if (t.accepts(s)) {
                        if (fixedOutput) {
                            t.setOutputSize(1);
                            t.addOutputSequence(outputSequence[pOutput++]);
                        } else {
                            StringBuilder sequence = new StringBuilder();
                            for (int i = 0; i < t.getOutputSize(); i++) {
                                if (i > 0) {
                                    sequence.append(",");
                                }
                                if (pOutput < outputSequence.length) {
                                    sequence.append(outputSequence[pOutput++]);
                                } else {
                                    sequence.append("???");
                                }
                            }
                            t.addOutputSequence(sequence.toString());
                        }
                        currentState = t.getNewState();
						found = true;
						break;
					}
				}
				if (!found) {
					break;
				}
			}
		}
		for (Transition[] state : states) {
			for (Transition t : state) {
				t.labelByMostFrequent();
			}
		}
		isLabelled = true;
	}
	
	public String[] transform(String[] inputSequence) {
		ArrayList<String> list = new ArrayList<String>();
		int currentState = initialState;
		for (String s : inputSequence) {
			boolean found = false;
			for (Transition t : states[currentState]) {
				if (t.accepts(s)) {
					for (String s1 : t.getOutput()) {
						list.add(s1);
					}
					currentState = t.getNewState();
					found = true;
					t.markUsed();
					break;
				}
			}
			if (!found) {
				if (list.size() > 0) {
					return list.toArray(new String[0]);
				}
				return null;
			}
		}
		return list.toArray(new String[0]);
	}
	
    public boolean validateNegativeTest(String[] inputSequence) {
        if (ArrayUtils.isEmpty(inputSequence)) {
            throw new IllegalArgumentException("Unexpected inputSequence");
        }
        int currentState = initialState;
        Transition lastTransition = null;
        for (String s : inputSequence) {
            lastTransition = null;
            for (Transition t : states[currentState]) {
                if (t.accepts(s)) {
                    currentState = t.getNewState();
                    lastTransition = t;
                    break;
                }
            }
            if (lastTransition == null) {
                break;
            }
        }    
        if (lastTransition == null) {
            return true;
        } else {
            lastTransition.setUsedByNegativeTest(true);
            return false;
        }
    }
    
    public void unmarkAllTransitions() {
        for (Transition[] s : states) {
            for (Transition t : s) {
                t.markUnused();
                t.setUsedByNegativeTest(false);
                t.setUsedByVerifier(false);
                t.setVerified(false);
            }
        }
    }
    
    public FST getCanonicalFST(int newId[]) {
    	Arrays.fill(newId, -1);
    	Queue<Integer> queue = new LinkedList<Integer>();
    	int id = 0;
    	queue.add(initialState);
    	newId[initialState] = id++;
    	while (!queue.isEmpty()) {
    		int currentState = queue.poll();
    		loop: for (int eventId = 0; eventId < setOfInputs.length; eventId++) {
    			for (Transition t : states[currentState]) {
    				if (t.accepts(setOfInputs[eventId])) {
    					int childId = t.getNewState();
    					if (newId[childId] == -1) {
    						newId[childId] = id++;
    						queue.add(childId);
    						continue loop;
    					}
    				}
    			}
    		}
    	}
    	FST result = transformTransitions(newId);
    	result.fitness = fitness;
    	return result;
    }
    
    private FST transformTransitions(int newId[]) {
    	Transition[][] newStates = new Transition[states.length][];
    	for (int i = 0; i < stateNumber; i++) {
    		if (newId[i] != -1) {
    			newStates[newId[i]] = new Transition[states[i].length];
    			for (int j = 0; j < states[i].length; j++) {
    				Transition t = states[i][j];
    				newStates[newId[i]][j] = new Transition(t.getInput(), t.getOutputSize(), newId[t.getNewState()]);
    				newStates[newId[i]][j].setOutput(t.getOutput());
    			}
    		}
    	}
    	
    	for (int i = 0; i < stateNumber; i++) {
    		if (newStates[i] == null) {
    			newStates[i] = new Transition[0];
    		}
    		Arrays.sort(newStates[i], new Comparator<Transition>(){
				@Override
				public int compare(Transition o1, Transition o2) {
					return o1.getInput().compareTo(o2.getInput());
				}
    		});
    	}
    	
    	return new FST(newStates, 0, setOfInputs, setOfOutputs);
    }
    
    public Long computeStringHash() {
        return Digest.RSHash(stringForHashing());
    }
    
    public Transition getTransition(int state, String input) {
    	for (Transition t : states[state]) {
    		if (t.getInput().equals(input)) {
    			return t;
    		}
    	}
    	return null;
    }
    
    public void transformUsedTransitions(int[] newId1, int[] newId2, FST originalFST) {
    	int oldId2[] = new int[states.length];
    	Arrays.fill(oldId2, -1);
    	for (int i = 0; i < newId2.length; i++) {
    		if (newId2[i] != -1) {
    			oldId2[newId2[i]] = i;
    		}
    	}
    	
    	int f12[] = new int[states.length];
    	Arrays.fill(f12, -1);
    	for (int i = 0; i < f12.length; i++) {
    		if (newId1[i] == -1) {
    		} else {
    			f12[i] = oldId2[newId1[i]];
    		}
    	}
    	
    	unmarkAllTransitions();
    	for (int i = 0; i < states.length; i++) {
    		if (f12[i] != -1) {
    			for (Transition t : originalFST.states[i]) {
    				Transition transition = getTransition(f12[i], t.getInput());
    				transition.used = t.used;
    				transition.setOutput(t.getOutput());
    			}
    		}
    	}

    }

	public static void printAutomaton(PrintWriter out, FST fst, List<Path> tests, double time) {
		out.println("fitness = " + fst.getFitness());
		out.println("steps = " + FST.cntFitnessRun);
		out.println("time = " + time);
		out.println("lazy-saved-fitness-evals = " + FST.cntLazySaved);
		out.println("canonical-cache-hits = " + FST.cntBfsSaved);
		out.println("initial-state = " + fst.initialState);
		for (int i = 0; i < fst.setOfInputs.length; i++) {
			out.print(fst.setOfInputs[i] + " ");
		}
		out.println();
		for (int i = 0; i < fst.stateNumber; i++) {
			for (int j = 0; j < fst.states[i].length; j++) {
//				out.print(fst.states[i][j] + " | ");
				out.println(fst.states[i][j]);
			}
			out.println();
		}
//		for (Path p : tests) {
//			String[] output = fst.transform(p.getInput());
//			String[] answer = p.getOutput();
//			out.println(Arrays.toString(p.getInput()));
//			out.println(Arrays.toString(answer));
//			out.println(Arrays.toString(output));
//			out.println();
//		}
//        try {
//            UnimodModelWriter.write(out, fst, "ru.ifmo.ControlledObjectStub", "ru.ifmo.EventPrividerStub");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
	
	public boolean needToComputeFitness() {
		return needToComputeFitness;
	}
	
	public void setNeedToComputeFitness(boolean value) {
		needToComputeFitness = value;
	}
	
	public String stringForHashing() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stateNumber; i++) {
			for (int j = 0; j < states[i].length; j++) {
				Transition t = states[i][j];
				sb.append(i);
				sb.append(t.getInput());
				sb.append(t.getNewState());
				sb.append(t.getOutputSize());
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		String a = stateNumber + " " + setOfInputs.length + "\n";
		for (int i = 0; i < stateNumber; i++) {
			a += 1;
		}
		a += "\n";
		for (int i = 0; i < stateNumber; i++) {
			for (int j = 0; j < states[i].length; j++) {
				String output = "";
				
				if (states[i][j].getOutput() == null) {
					output = null;
				} else {
					output = states[i][j].getOutput().length == 0 ? "" : states[i][j].getOutput()[0];
				}
				a += "(" + i + ", " + j + ") - > " + "(" + states[i][j].getNewState() + ", " + output + ")\n";
			}
		}
		return a;
	}
	
	public String printWithUsed() {
		String a = stateNumber + " " + setOfInputs.length + "\n";
		for (int i = 0; i < stateNumber; i++) {
			a += 1;
		}
		a += "\n";
		for (int i = 0; i < stateNumber; i++) {
			for (int j = 0; j < states[i].length; j++) {
				String output = "";

				if (states[i][j].getOutput() == null) {
					output = null;
				} else {
					output = (states[i][j].getOutput().length == 0 ? "" : states[i][j].getOutput()[0]) + " (" + states[i][j].getOutputSize() + ")";
				}
				a += "(" + i + ", " + j + ") - > " + "(" + states[i][j].getNewState() + ", " + output + "; used=" + states[i][j].used + ")\n";
			}
		}
		return a;
	}
}
