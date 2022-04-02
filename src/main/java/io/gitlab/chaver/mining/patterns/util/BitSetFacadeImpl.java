package io.gitlab.chaver.mining.patterns.util;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.memory.IStateLong;
import org.chocosolver.solver.Model;

import java.util.BitSet;

public class BitSetFacadeImpl implements BitSetFacade {

    private BitSet bitSet;
    private BitSet mask;
    private IStateLong[] words;
    private final BitSet[] dataset;

    public final static String TYPE = "classic";

    public BitSetFacadeImpl(Database database, Model model, int nbits) {
        dataset = database.getVerticalRepresentation();
        bitSet = new BitSet(nbits);
        bitSet.set(0, nbits);
        initWords(model);
    }

    public BitSetFacadeImpl(Database database, Model model, long[] words) {
        dataset = database.getVerticalRepresentation();
        bitSet = BitSet.valueOf(words);
        initWords(model);
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
        return mask.cardinality();
    }

    @Override
    public void and(int i) {
        long[] w = new long[words.length];
        for (int j = 0; j < words.length; j++) {
            w[j] = words[j].get();
        }
        bitSet = BitSet.valueOf(w);
        bitSet.and(dataset[i]);
        copyWords();
    }

    @Override
    public int andCount(int i) {
        BitSet copy = (BitSet) bitSet.clone();
        copy.and(dataset[i]);
        return copy.cardinality();
    }

    @Override
    public void andMask(int i) {
        mask.and(dataset[i]);
    }

    @Override
    public void resetMask() {
        mask = (BitSet) bitSet.clone();
    }

    @Override
    public boolean isSubsetOf(int i) {
        return isSubsetOf(bitSet, i);
    }

    @Override
    public boolean maskIsSubsetOf(int i) {
        return isSubsetOf(mask, i);
    }

    @Override
    public BitSet getWords() {
        long[] wordsCopy = new long[words.length];
        for (int i = 0; i < words.length; i++) {
            wordsCopy[i] = words[i].get();
        }
        return BitSet.valueOf(wordsCopy);
    }

    private boolean isSubsetOf(BitSet b, int i) {
        BitSet copy = (BitSet) b.clone();
        copy.and(dataset[i]);
        return copy.equals(b);
    }

    private void initWords(Model model) {
        long[] wordsCopy = bitSet.toLongArray();
        words = new IStateLong[wordsCopy.length];
        for (int i = 0; i < wordsCopy.length; i++) {
            words[i] = model.getEnvironment().makeLong(wordsCopy[i]);
        }
    }

    private void copyWords() {
        long[] wordsCopy = bitSet.toLongArray();
        for (int i = 0; i < words.length; i++) {
            words[i].set(i < wordsCopy.length ? wordsCopy[i] : 0);
        }
    }
}
