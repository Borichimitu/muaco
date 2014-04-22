/* 
 * Developed by eVelopers Corporation, 2009
 */
package ru.ifmo.ctddev.genetic.transducer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.algorithm.SimpleGeneticAlgorithm;
import ru.ifmo.ctddev.genetic.transducer.algorithm.fitnessevaluator.PlainFitnessEvaluator;
import ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions.ComplianceChecker;
import ru.ifmo.ctddev.genetic.transducer.io.FileFormatException;
import ru.ifmo.ctddev.genetic.transducer.io.ITestsReader;
import ru.ifmo.ctddev.genetic.transducer.io.OneGroupTestsReader;
import ru.ifmo.ctddev.genetic.transducer.scenario.TestGroup;
import ru.ifmo.ltl.LtlParseException;

/**
 * @author kegorov
 *         Date: Jun 26, 2009
 */
public class Experiment {
    private ITestsReader reader;
    private Writer out;
    private int i;

    public Experiment(ITestsReader reader, File file) throws IOException, LtlParseException {
        this.reader = reader;
        goToLastResult(file);
        out = new FileWriter(file, true);
    }

    public static void main(String[] args) throws FileFormatException, IOException, LtlParseException {
        if (args.length != 4) {
            System.out.println("Usage: java [-options] -jar gabp.jar FILE MAX_FITNESS_EVALS MAX_RUN_TIME N_EXPERIMENTS\n\n" +
                    "Where\n" +
                    "\tFILE - xml file with tests and LTL-formulas\n" + 
                    "\tMAX_FITNESS_EVALS - max number of allowed fitness evaluations\n" +
                    "\tMAX_RUN_TIME - max run time in seconds\n" +
                    "\tN_EXPERIMENTS - number of experiment repeats\n");
            return;
        }

        int maxFitnessEvals = Integer.parseInt(args[1]);
        int maxRunTime = Integer.parseInt(args[2]);
        int numberOfExperiments = Integer.parseInt(args[3]);

       	FST.fitnessEvaluator = new PlainFitnessEvaluator();
        
        if (maxFitnessEvals < 0) {
        	maxFitnessEvals = Integer.MAX_VALUE;
        }
        
        Experiment e = new Experiment(new OneGroupTestsReader(new File(args[0]), false),
                                      new File("avrFitness.csv"));
        try {
            while (e.getStep() < numberOfExperiments) {
            	FST.cntFitnessRun = 0;
            	FST.cntLazySaved = 0;
            	FST.cntBfsSaved = 0;
                System.out.println("================= experiment " + e.getStep() + " ============================");
                e.run(maxFitnessEvals, maxRunTime);
                System.out.println("Fitness calculated: " + FST.cntFitnessRun);
                System.out.println("Lazy saved: " + FST.cntLazySaved);
                System.out.println("Bfs saved: " + FST.cntBfsSaved);
            }

            System.out.println("Average fitness calculated.");
        } catch (Exception exc) {
        	exc.printStackTrace();
        } finally {
            e.close();
        }
    }

    protected void goToLastResult(File f) throws IOException {
        if (!f.exists()) {
            return;
        }
        BufferedReader reader = new BufferedReader(new FileReader(f));
        try {
            //count line numbers, don't check format
            for (i = 0; reader.readLine() != null; i++);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
    }

    protected void printResult(FST best) {
        String res = FST.cntFitnessRun + "\n";
        try {
            out.write(res);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(res);
        }
    }

    public int getStep() {
        return i;
    }

    public void run(int maxFitnessEvals, double maxRunTime) {
        FST.cntFitnessRun = 0;
        long start = System.currentTimeMillis();

        List<TestGroup> groups = reader.getGroups();

		String[] setOfInputs = reader.getSetOfInputs();
		String[] setOfOutputs = reader.getSetOfOutputs();

		try {
			ComplianceChecker.createComplianceChecker(setOfInputs);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ComplianceChecker cc = ComplianceChecker.getComplianceChecker();
		
        SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm(reader.getAlgorithmParameters(),
                setOfInputs, setOfOutputs, groups);

		FST best = sga.go(maxFitnessEvals, maxRunTime);
		double time = (System.currentTimeMillis() - start) / 1e3;
		
		File dir = new File("attempts/attempt" + i);
		dir.mkdir();
        try {
            PrintWriter out = new PrintWriter(new File(dir.getPath() + "/" + best.getFitness() + ".metadata"));
            FST.printAutomaton(out, best, sga.getTests(), time);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        printResult(best);
        i++;
    }

    public void close() throws IOException {
        out.close();
    }
}
