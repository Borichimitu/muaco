/* 
 * Developed by eVelopers Corporation, 2009
 */
package ru.ifmo.ctddev.genetic.transducer.verifier;

import java.util.List;

import ru.ifmo.ctddev.genetic.transducer.algorithm.FST;
import ru.ifmo.ctddev.genetic.transducer.scenario.TestGroup;
import ru.ifmo.ltl.LtlParseException;

/**
 * @author kegorov
 *         Date: Jun 18, 2009
 */
public interface IVerifierFactory {

    /**
     * Prepare Buchi automata for formulas
     * @param groups test groups
     * @throws LtlParseException
     */
    void prepareFormulas(TestGroup[] groups) throws LtlParseException;
    
    void prepareFormulas(List<String> formulas) throws LtlParseException;

    void configureStateMachine(FST fst);

    /**
     * Verify prepared formulas.
     * @return number of marked transitions per group
     */
    int[] verify();
}
