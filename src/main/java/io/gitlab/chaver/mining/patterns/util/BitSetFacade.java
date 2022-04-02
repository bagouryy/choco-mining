package io.gitlab.chaver.mining.patterns.util;

import java.util.BitSet;

public interface BitSetFacade {

    boolean isEmpty();
    int cardinality();
    int maskCardinality();
    void and(int i);
    int andCount(int i);
    void andMask(int i);
    void resetMask();
    boolean isSubsetOf(int i);
    boolean maskIsSubsetOf(int i);
    BitSet getWords();
}
