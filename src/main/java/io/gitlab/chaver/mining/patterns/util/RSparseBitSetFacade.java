package io.gitlab.chaver.mining.patterns.util;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;

import java.util.BitSet;

public class RSparseBitSetFacade implements BitSetFacade {

    private long[][] dataset;
    private RSparseBitSet bitSet;

    public final static String TYPE = "sparse";

    public RSparseBitSetFacade(Database database, Model model, int nbits) {
        dataset = database.getDatasetAsLongArray();
        bitSet = new RSparseBitSet(model, nbits);
    }

    public RSparseBitSetFacade(Database database, Model model, long[] words) {
        dataset = database.getDatasetAsLongArray();
        bitSet = new RSparseBitSet(model, words);
    }


    @Override
    public boolean isEmpty() {
        return bitSet.isEmpty();
    }

    @Override
    public int cardinality() {
        return bitSet.cardinality();
    }

    @Override
    public int maskCardinality() {
        return bitSet.maskCardinality();
    }

    @Override
    public void and(int i) {
        bitSet.and(dataset[i]);
    }

    @Override
    public int andCount(int i) {
        return bitSet.andCount(dataset[i]);
    }

    @Override
    public void andMask(int i) {
        bitSet.andMask(dataset[i]);
    }

    @Override
    public void resetMask() {
        bitSet.resetMask();
    }

    @Override
    public boolean isSubsetOf(int i) {
        return bitSet.isSubsetOf(dataset[i]);
    }

    @Override
    public boolean maskIsSubsetOf(int i) {
        return bitSet.maskIsSubsetOf(dataset[i]);
    }

    @Override
    public BitSet getWords() {
        return bitSet.convertToBitset();
    }
}
