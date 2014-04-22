package ru.ifmo.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ifmo.ctddev.genetic.transducer.algorithm.guardconditions.ComplianceChecker;
import ru.ifmo.optimization.algorithm.muaco.graph.MutationCollection;
import ru.ifmo.optimization.algorithm.muaco.mutator.MutatedInstanceMetaData;
import ru.ifmo.optimization.instance.fsm.FSM;
import ru.ifmo.optimization.instance.fsm.FSM.Transition;
import ru.ifmo.optimization.instance.fsm.mutation.FsmMutation;
import ru.ifmo.optimization.instance.fsm.mutation.FsmTransitionMutation;


/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class Util {
	public static List<Integer> intRange(int start, int finish) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = start; i <= finish; i++) {
			result.add(i);
		}
		return result;
	}

    private static int iLambda(double lambda) {
        return (int)(255 * (lambda - 380) / (760-380));
    }

    public static String valueToColor(double value, double minValue, double maxValue) {
        final double kkk = 0.012;
        double c = (value - minValue) / (maxValue - minValue);
        int n = (int)(255.0 * c);
        int R = (int)(255.0 * Math.exp(-Math.pow(kkk * (n - iLambda(650)),2)));
        int G = (int)(255.0 * Math.exp(-Math.pow(kkk * (n - iLambda(570)),2)));
        int B = (int)(255.0 * Math.exp(-Math.pow(kkk * (n - iLambda(490)),2)));

        String Rstring = Integer.toHexString(R);
        String Gstring = Integer.toHexString(G);
        String Bstring = Integer.toHexString(B);

        if (Rstring.length() == 1) {
            Rstring = "0" + Rstring;
        }

        if (Gstring.length() == 1) {
            Gstring = "0" + Gstring;
        }

        if (Bstring.length() == 1) {
            Bstring = "0" + Bstring;
        }
        return "\"#" + Rstring + Gstring + Bstring + "\"";
    }
    
    public static MutatedInstanceMetaData<FSM, FsmMutation> makeCompliantFSM(FSM instance) {
    	return makeCompliantFSM(new MutatedInstanceMetaData<FSM, FsmMutation>(
    			instance, new MutationCollection<FsmMutation>()));
    }
    
    public static MutatedInstanceMetaData<FSM, FsmMutation> 
    makeCompliantFSM(MutatedInstanceMetaData<FSM, FsmMutation> mutatedInstanceMetaData) {
		FSM mutated = mutatedInstanceMetaData.getInstance();
		MutationCollection<FsmMutation> mutations = new MutationCollection<FsmMutation>();
		mutations.addAll(mutatedInstanceMetaData.getMutations());
		
		List<Integer> eventsSequence = new ArrayList<Integer>();
		for (int i = 0; i < mutated.getEvents().size(); i++) {
			eventsSequence.add(i);
		}
		Collections.shuffle(eventsSequence);
		
		//check compliance of transitions
		ComplianceChecker complianceChecker = ComplianceChecker.getComplianceChecker();
		for (int state = 0; state < mutated.getNumberOfStates(); state++) {
			boolean compliantTransitions[] = new boolean[mutated.transitions[state].length];
			Arrays.fill(compliantTransitions, false);
			
//			for (int i : eventsSequence) {
			for (int i = 0; i < mutated.transitions[state].length; i++) {
				Transition t = mutated.transitions[state][i];
				if (t.getEndState() == -1) {
					continue;
				}
				
				boolean compliant = true;
				for (int j = 0; j < mutated.transitions[state].length; j++) {
					Transition t1 = mutated.transitions[state][j];
					if (t1.getEndState() == -1) {
						continue;
					}
					if (i == j) {
						continue;
					}
					
					compliant &= complianceChecker.checkCompliancy(t.getEvent(), t1.getEvent());
				}
				if (compliant) {
					compliantTransitions[i] = true;
				}
			}
			

			for (int i = 0; i < mutated.transitions[state].length; i++) {
//			for (int i : eventsSequence) {
				if (mutated.transitions[state][i].getEndState() == -1) {
					continue;
				}
				if (compliantTransitions[i]) {
					continue;
				}
				//delete bad transitions
				mutated.getTransition(state, i).setEndState(-1);
				mutated.getTransition(state, i).setAction("1");
				FsmTransitionMutation mutation = new FsmTransitionMutation(state, i, -1, "1");
				mutations.add(mutation);
			}
		}
		
		return new MutatedInstanceMetaData<FSM, FsmMutation>(mutated, mutations);
    }
    
    public static int numberOfIncompliantTransitions(List<Transition> transitions) {
    	ComplianceChecker complianceChecker = ComplianceChecker.getComplianceChecker();
    	boolean compliantTransitions[] = new boolean[transitions.size()];
		Arrays.fill(compliantTransitions, false);
		
		for (int i = 0; i < transitions.size(); i++) {
			Transition t = transitions.get(i);
			if (t.getEndState() == -1) {
				continue;
			}
			
			boolean compliant = true;
			for (int j = 0; j < transitions.size(); j++) {
				Transition t1 = transitions.get(j);
				if (t1.getEndState() == -1) {
					continue;
				}
				if (i == j) {
					continue;
				}
				if (!compliantTransitions[j]) {
					continue;
				}
				
				compliant &= complianceChecker.checkCompliancy(t.getEvent(), t1.getEvent());
			}
			if (compliant) {
				compliantTransitions[i] = true;
			}
		}
		
		int result = 0;
		for (int i = 0; i < transitions.size(); i++) {
			if (transitions.get(i).getEndState() == -1) {
				continue;
			}
			if (!compliantTransitions[i]) {
				result++;
			}
		}
		return result;
    }
    
	public static int numberOfExistingTransitions(Transition[] transitions) {
		int result = 0;
		for (Transition t : transitions) {
			if (t == null) {
				continue;
			}
			if (t.getEndState() != -1) {
				result++;
			}
		}
		return result;
	}
	
	public static boolean hasTransition(Transition[] transitions) {
		for (Transition t : transitions) {
			if (t.getEndState() != -1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasTransitions(FSM individual) {
		for (int state = 0; state < individual.getNumberOfStates(); state++) {
			if (numberOfExistingTransitions(individual.transitions[state]) > 0) {
				return true;
			}
		}
		return false;
	}
	
	public static double maxArrayElement(double[] array) {
		double max = Double.MIN_VALUE;
	    for (double v : array) {
	    	if (v > max) {
	    		max = v;
	    	}
	    }
	    return max;
	}
	
	public static double[][] readDoubleMatrix(String filePath) throws IOException {
        ArrayList<ArrayList<Double>> dataList = new ArrayList<ArrayList<Double>>();

        BufferedReader br = new BufferedReader(new FileReader(filePath));

        String line = "";
        while ((line = br.readLine()) != null) {
            ArrayList<Double> lineData = new ArrayList<Double>();
            for (String s : line.split(" |\t")) {
                if (s.length() > 0) {
                    lineData.add(Double.parseDouble(s));
                }
            }
            dataList.add(lineData);
        }
        
        br.close();

        int n = dataList.size(), m = dataList.get(0).size();
        
        double[][] ans = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                ans[i][j] = dataList.get(i).get(j);
            } 
        }
        
        return ans;
    }
	
	public static int[][] readIntMatrix(String filePath) throws IOException {
		double[][] doubleArray = readDoubleMatrix(filePath);

		int n = doubleArray.length, m = doubleArray[0].length;

		int[][] ans = new int[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				ans[i][j] = (int) doubleArray[i][j];
			} 
		}
		return ans;
	}
}
