package ru.ifmo.ctddev.genetic.transducer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.algorithm.SimpleGeneticAlgorithm;
import ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions.ComplianceChecker;
import ru.ifmo.ctddev.genetic.transducer.io.FileFormatException;
import ru.ifmo.ctddev.genetic.transducer.io.ITestsReader;
import ru.ifmo.ctddev.genetic.transducer.io.OneGroupTestsReader;
import ru.ifmo.ctddev.genetic.transducer.scenario.Path;
import ru.ifmo.ctddev.genetic.transducer.scenario.TestGroup;

public class Starter {

	public static void main(String[] args) throws IOException, FileFormatException {
//	    if (args.length != 2) {
//            System.out.println("Usage: java [-options] -jar gabp.jar FILE MAX_FITNESS_EVALS\n\n" +
//                    "Where\n" +
//                    "\tFILE - xml file with tests and LTL-formulas\n"
//                    + "\tMAX_FITNESS_EVALS - max number of allowed fitness evaluations.");
//            return;
//        }
	    int maxFitnessEvals = -1;//Integer.parseInt(args[1]);

//		ITestsReader reader = new TestsReader(new File(args[0]), false);
//        ITestsReader reader = new OneGroupTestsReader(new File(args[0]), false);
	    ITestsReader reader = new OneGroupTestsReader(new File("clock.xml"), false);

        List<TestGroup> groups = reader.getGroups();

		String[] setOfInputs = reader.getSetOfInputs();
		String[] setOfOutputs = reader.getSetOfOutputs();
		
        int i = 0;
        for (TestGroup g : groups) {
            System.out.println("--------------- Group " + i++ + " ----------------");
            for (String f : g.getFormulas()) {
                System.out.println(f);
            }
            System.out.println();

            for (Path p : g.getTests()) {
                for (String s1 : p.getInput()) {
                    System.out.print(s1 + " ");
                }
                System.out.println();
                for (String s2 : p.getOutput()) {
                    System.out.print(s2 + " ");
                }
                System.out.println();
            }
            System.out.println();
        }

        try {
            ComplianceChecker.createComplianceChecker(setOfInputs);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm(reader.getAlgorithmParameters(),
                setOfInputs, setOfOutputs, groups);

		long startTime = System.currentTimeMillis();

		FST best = sga.go(maxFitnessEvals < 0 ? Integer.MAX_VALUE : maxFitnessEvals, Double.MAX_VALUE);
		{
			PrintWriter out = new PrintWriter(new File("attempts/attempt0/" + best.getFitness() + ".metadata"));
			FST.printAutomaton(out, best, sga.getTests(), 0);
			out.close();
		}

		// ����������� �� ������ ������ - ��������� ��������, ������������ ��� ��������� ��������� ������
		
		long finishTime = System.currentTimeMillis();

		System.out.println("Fitness calculated " + FST.cntFitnessRun + " times.");
        System.out.println("Calculation time = " + (finishTime - startTime) / 1000 + " seconds.");
	}
	
}
