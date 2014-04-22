package ru.ifmo.optimization.instance.fsm.task.testsmodelchecking;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions.ComplianceChecker;
import ru.ifmo.ctddev.genetic.transducer.verifier.IVerifierFactory;
import ru.ifmo.ctddev.genetic.transducer.verifier.VerifierFactory;
import ru.ifmo.ltl.LtlParseException;
import ru.ifmo.optimization.algorithm.stats.RunStats;
import ru.ifmo.optimization.instance.FitInstance;
import ru.ifmo.optimization.instance.FitnessValue;
import ru.ifmo.optimization.instance.InstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.FsmMetaData;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.AutomatonTest;
import ru.ifmo.optimization.instance.fsm.task.testswithlabelling.TestsWithLabelingTask;
import ru.ifmo.optimization.instance.task.AbstractTaskConfig;

public class TestsModelCheckingTask extends TestsWithLabelingTask {

	private final IVerifierFactory verifier;
	private final List<String> formulas;
	private static final double FORMULAS_COST = 1;
	private static final double TESTS_COST = 1;
	private boolean useScenarios;
	private boolean useNegativeScenarios;
	private boolean useFormulas;
	private AutomatonTest[] negativeScenarios = new AutomatonTest[0];
	
	private static List<String> loadFormulas(String path) {
		List<String> formulas = new ArrayList<String>();
		Scanner in = null;
		try {
			in = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (in.hasNext()) {
			formulas.add(in.nextLine());
		}
		in.close();
		
		return formulas;
	}
	
	public TestsModelCheckingTask(AbstractTaskConfig config) {
		super(config);
		
		String[] setOfInputs = new String[events.size()];
		for (int i = 0; i < events.size(); i++) {
			setOfInputs[i] = events.get(i);
		}
		
		try {
			ComplianceChecker.createComplianceChecker(events);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		useFormulas = Boolean.parseBoolean(testsProperties.getProperty("use-formulas"));
		if (useFormulas) {
			verifier = new VerifierFactory(setOfInputs, actions);
			try {
				formulas = loadFormulas(testsProperties.getProperty("formulas"));
				verifier.prepareFormulas(formulas);
			} catch (LtlParseException e) {
				throw new RuntimeException(e);   
			}
		} else {
			verifier = null;
			formulas = new ArrayList<String>();
		}
		
		useScenarios = Boolean.parseBoolean(testsProperties.getProperty("use-scenarios"));
		if (useScenarios) {
			loadScenarios(testsProperties.getProperty("scenarios"));
			actionsForEvolving = new String[]{"1"};
			outputs = new String[tests.length];
		}
		
		useNegativeScenarios = Boolean.parseBoolean(testsProperties.getProperty("use-negative-scenarios"));
		if (useNegativeScenarios) {
			loadNegativeScenarios(testsProperties.getProperty("negative-scenarios"));
		}
	}
	
	protected void loadScenarios(String filename) {
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int numberOfTests = in.nextInt();
		in.nextLine();
		
		tests = new AutomatonTest[numberOfTests];
		for (int i = 0; i < numberOfTests; i++) {
			List<String> inputSequence = new ArrayList<String>();
			List<String> outputSequence = new ArrayList<String>();
			
			String scenario = in.nextLine();
			String[] elements = scenario.split(";");
			
			for (String element : elements) {
				String[] eventActions = element.trim().split("/");
				String event = eventActions[0];
				String actions = eventActions.length > 1 ? eventActions[1] : "";
				actions = actions.replaceAll(",", "");
				inputSequence.add(event);
				outputSequence.add(actions);
			}
			tests[i] = new AutomatonTest(inputSequence.toArray(new String[0]), outputSequence.toArray(new String[0]));
		}
	}
	
	protected void loadNegativeScenarios(String filename) {
		Scanner in = null;
		try {
			in = new Scanner(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int numberOfTests = in.nextInt();
		negativeScenarios = new AutomatonTest[numberOfTests];
		for (int i = 0; i < numberOfTests; i++) {
			String[] sequence = in.next().split("_");
			negativeScenarios[i] = new AutomatonTest(sequence, new String[]{""});
		}
	}
	
	protected boolean validateNegativeScenario(FSM fsm, AutomatonTest test) {
		int currentState = fsm.getInitialState();
		Transition lastTransition = null;
		
		for (String event : test.getInput()) {
			lastTransition = null;
			int indexOfEvent = fsm.getEvents().indexOf(event);
			Transition transition = fsm.transitions[currentState][indexOfEvent];
			if (transition.getEndState() != -1) {
				currentState = transition.getEndState();
				lastTransition = transition;
			} else {
				break;
			}
		}

		return lastTransition == null;
	}
	
	@Override
	public FitInstance<FSM> getFitInstance(FSM fsm, FitnessValue bestFitness) {
		InstanceMetaData<FSM> instanceMD = getInstanceMetaData(fsm, bestFitness);
		return new FitInstance<FSM>(instanceMD.getInstance(), instanceMD.getFitness());
	}
	
	
	@Override
	public InstanceMetaData<FSM> getInstanceMetaData(FSM fsm, FitnessValue bestFitness) {
		numberOfFitnessEvaluations++;
		long start = System.nanoTime();
		
		FSM labelled = labelFSM(fsm, useScenarios);
		Set<Transition> visitedTransitions = new HashSet<Transition>(fsm.getNumberOfStates() * fsm.getNumberOfEvents());
		double f1 = 0;
		int numberOfSuccesses = 0;
		for (int i = 0; i < tests.length; i++) {
			FsmMetaData fsmRunData = runFsmOnTest(labelled, i, useScenarios);
			if (fsmRunData.getFitness() == null) {
				visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
				continue;
			}
			if (fsmRunData.getFitness().data[0] < 1e-5) {
				numberOfSuccesses++;
			}
			f1 += 1.0 - fsmRunData.getFitness().data[0];
			
			visitedTransitions.addAll(fsmRunData.getVisitedTransitions());
		}

		double negativeScenariosSum = 0;
		if (useNegativeScenarios) {
			for (AutomatonTest negativeScenario : negativeScenarios) {
				if (!validateNegativeScenario(labelled, negativeScenario)) {
					negativeScenariosSum++;
				}
			}
		}
		
		double testsFF = TESTS_COST;
        if (tests.length > 0) {
            testsFF = (numberOfSuccesses == tests.length) ? TESTS_COST : TESTS_COST * (f1 / tests.length);
        }
        
        double negativeScenariosFF = (useNegativeScenarios) ? (negativeScenariosSum) / negativeScenarios.length : 0;
		
        FST fst = FstFactory.createFST(fsm, actions);
		
		if (fst.getUsedTransitionsCount() == 0) {
			return new FsmMetaData(fsm, visitedTransitions, new FitnessValue(0.0));
		}
		
		double ltlFF = FORMULAS_COST;
        if (formulas.size() > 0) {
        	verifier.configureStateMachine(fst);
    		int verificationResult[] = verifier.verify();
            ltlFF = FORMULAS_COST * verificationResult[0] / formulas.size() / fst.getUsedTransitionsCount();
            if ((ltlFF > FORMULAS_COST) || (ltlFF < 0)) {
                throw new RuntimeException(String.valueOf(ltlFF));
            }
        }
        
        double fitness = testsFF + ltlFF - negativeScenariosFF + 0.0001 * (100 - fsm.getNumberOfTransitions());
        RunStats.TIME_FITNESS_COMPUTATION += (System.nanoTime() - start) / 1e9;
        return new FsmMetaData(fsm, visitedTransitions, new FitnessValue(fitness));
	}
	
	@Override
	public FitnessValue correctFitness(FitnessValue fitness, FSM cachedInstance, FSM trueInstance) {
		if (fitness.data[0] < 1e-5) {
			return new FitnessValue(0);
		}
		double negativeTerm = 0.0001 * (100 - cachedInstance.getNumberOfTransitions());
		double positiveTerm = 0.0001 * (100 - trueInstance.getNumberOfTransitions());
		return new FitnessValue(positiveTerm - negativeTerm);
	}
}
