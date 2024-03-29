/* 
 * Developed by eVelopers Corporation, 2009
 */
package ru.ifmo.ctddev.genetic.transducer.verifier;

import ru.ifmo.automata.statemachine.*;
import ru.ifmo.automata.statemachine.impl.ControlledObjectStub;
import ru.ifmo.automata.statemachine.impl.EventProviderStub;
import ru.ifmo.automata.statemachine.impl.StateMachine;
import ru.ifmo.automata.statemachine.impl.SimpleState;
import ru.ifmo.ctddev.genetic.transducer.algorithm.Const;
import ru.ifmo.ltl.grammar.predicate.IPredicateFactory;
import ru.ifmo.ltl.grammar.predicate.PredicateFactory;
import ru.ifmo.ltl.grammar.LtlNode;
import ru.ifmo.ltl.grammar.LtlUtils;
import ru.ifmo.ltl.converter.ILtlParser;
import ru.ifmo.ltl.converter.LtlParser;
import ru.ifmo.ltl.buchi.ITranslator;
import ru.ifmo.ltl.buchi.IBuchiAutomata;
import ru.ifmo.ltl.buchi.translator.JLtl2baTranslator;
import ru.ifmo.ltl.LtlParseException;
import ru.ifmo.ctddev.genetic.transducer.algorithm.Transition;
import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.scenario.TestGroup;
import ru.ifmo.verifier.IVerifier;
import ru.ifmo.verifier.IDfsListener;
import ru.ifmo.verifier.automata.IIntersectionTransition;
import ru.ifmo.verifier.impl.SimpleVerifier;

import java.util.*;

import org.apache.commons.lang.StringUtils;

/**
 * @author kegorov
 *         Date: Jun 18, 2009
 */
public class VerifierFactory implements IVerifierFactory {
    private static final Random RANDOM = new Random();

    private ModifiableAutomataContext context;
    private IPredicateFactory<IState> predicates = new PredicateFactory<IState>();
    private ILtlParser parser;
    private IBuchiAutomata[][] preparedFormulas;

    private IDfsListener marker = new TransitionMarker();
    private TransitionCounter counter = new TransitionCounter();

    private FST fst;

    public VerifierFactory(String[] setOfInputs, String[] setOfOutputs) {
        IControlledObject co = new ControlledObjectStub("co", setOfOutputs);
        String[] filteredInputs = getEvents(setOfInputs);
		IEventProvider ep = new EventProviderStub("ep", filteredInputs);

        context = new ModifiableAutomataContext(co, ep);
        parser = new LtlParser(context, predicates);
    }

    public void prepareFormulas(TestGroup[] groups) throws LtlParseException {
        ITranslator translator = new JLtl2baTranslator();

        preparedFormulas = new IBuchiAutomata[groups.length][];

        for (int i = 0; i < groups.length; i++) {
            List<String> formulas = groups[i].getFormulas();
            IBuchiAutomata[] group = new IBuchiAutomata[formulas.size()];
            int j = 0;
            for (String f : formulas) {
                LtlNode node = parser.parse(f);
                node = LtlUtils.getInstance().neg(node);
                group[j++] = translator.translate(node);
            }
            preparedFormulas[i] = group;
        }
    }
    
    public void prepareFormulas(List<String> formulas) throws LtlParseException {
    	 ITranslator translator = new JLtl2baTranslator();

         preparedFormulas = new IBuchiAutomata[1][];

         IBuchiAutomata[] group = new IBuchiAutomata[formulas.size()];
         int j = 0;
         for (String f : formulas) {
        	 LtlNode node = parser.parse(f);
        	 node = LtlUtils.getInstance().neg(node);
        	 group[j++] = translator.translate(node);
         }
         preparedFormulas[0] = group;
    }

    public void configureStateMachine(FST fst) {
        Transition[][] states = fst.getStates();
        this.fst = fst;

        IControlledObject co = context.getControlledObject(null);
		IEventProvider ep = context.getEventProvider(null);

        StateMachine<IState> machine = new StateMachine<IState>("A1");

		SimpleState[] statesArr = new SimpleState[states.length];
		for (int i = 0; i < states.length; i++) {
			statesArr[i] = new SimpleState("" + i,
                    (fst.getInitialState() == i) ? StateType.INITIAL : StateType.NORMAL, 
                    Collections.<IAction>emptyList());
		}
		for (int i = 0; i < states.length; i++) {
			Transition[] currentState = states[i];
			for (Transition t : currentState) {
                //mark as not verified yet
                t.setVerified(false);
                t.setUsedByVerifier(false);

                AutomataTransition out = new AutomataTransition(
                        ep.getEvent(extractEvent(t.getInput())), null, statesArr[t.getNewState()]);
                out.setAlgTransition(t);

                for (String a: t.getOutput()) {
                    IAction action = co.getAction(a);
                    if (action != null) {
                        out.addAction(co.getAction(a));
                    }
                }
				statesArr[i].addOutcomingTransition(out);
			}
			machine.addState(statesArr[i]);
		}
        machine.addControlledObject(co.getName(), co);
		machine.addEventProvider(ep);

        context.setStateMachine(machine);
    }

    public int[] verify() {
        int[] res = new int[preparedFormulas.length];
        IVerifier<IState> verifier = new SimpleVerifier<IState>(context.getStateMachine(null).getInitialState());
//        List<IIntersectionTransition> longestList = Collections.emptyList();
        int usedTransitions = fst.getUsedTransitionsCount();

        for (int i = 0; i < preparedFormulas.length; i++) {
            int marked = 0;

            int length = preparedFormulas[i].length;
            int barrier = Math.max(Const.MIN_USED_TESTS, length / 10);

            for (IBuchiAutomata buchi : preparedFormulas[i]) {
                counter.resetCounter();
                
                List<IIntersectionTransition> list;
                if (RANDOM.nextInt(length) < barrier) {
                    list = verifier.verify(buchi, predicates, marker, counter);
                } else {
                    list = verifier.verify(buchi, predicates, counter);
                }
                if ((list != null) && (!list.isEmpty())) {
                    /*if (longestList.size() < list.size()) {
                        longestList = list;
                    }*/
                    ListIterator<IIntersectionTransition> iter = list.listIterator(list.size());

//                    int failTransitions = (buchi.size() == 2) ? 1 : 2; //1 -- invariant, 2 -- pre(post) condition
                    int failTransitions = buchi.size() - 1;

                    for (int j = 0; iter.hasPrevious() && (j < failTransitions);) {
                        IIntersectionTransition t = iter.previous();
                        if ((t.getTransition() != null)
                                && (t.getTransition().getClass() == AutomataTransition.class)) {
                            AutomataTransition trans = (AutomataTransition) t.getTransition();
                            if (trans.getAlgTransition() != null) {
                                trans.getAlgTransition().setUsedByVerifier(true);
                                j++;
                            }
                        }
                    }
                    /*for (IIntersectionTransition t : list) {
                        if ((t.getTransition() != null)
                                && (t.getTransition().getClass() == AutomataTransition.class)) {
                            AutomataTransition trans = (AutomataTransition) t.getTransition();
                            if (trans.getAlgTransition() != null) {
                                trans.getAlgTransition().setUsedByVerifier(true);
                            }
                        }
                    }*/
                    marked += counter.countVerified() / 2;
//                    System.out.println("verified = " + counter.countVerified());
                } else {
                    marked += usedTransitions;
                }
            }
            res[i] = marked;
        }

        /*for (IIntersectionTransition t : longestList) {
            AutomataTransition trans = (AutomataTransition) t.getTransition();
            if ((trans != null) && (trans.getAlgTransition() != null)) {
                trans.getAlgTransition().setUsedByVerifier(true);
            }
        }*/

        return res;
    }

    /**
     * Remove [expr] from input, return only events;
     * @param inputs inputs
     * @return events
     */
    private String[] getEvents(String[] inputs) {
        Set<String> res = new HashSet<String>();
        for (String e: inputs) {
            res.add(extractEvent(e));
        }
        return res.toArray(new String[res.size()]);
    }

    private String extractEvent(String input) {
        return StringUtils.substringBefore(input, "[").trim();
    }
}
