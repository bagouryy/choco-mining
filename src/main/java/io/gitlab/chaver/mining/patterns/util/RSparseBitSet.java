package io.gitlab.chaver.mining.patterns.util;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.IStateLong;
import org.chocosolver.solver.Model;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Reversible bitset : for more information, see the following papers :
 * Compact-Table: Efficiently Filtering Table Constraints with Reversible Sparse Bit-Sets (Demeulenaere et al.)
 *
 */
public class RSparseBitSet {

    /*
     * BitSets are packed into arrays of "words."  Currently a word is
     * a long, which consists of 64 bits, requiring 6 address bits.
     * The choice of word size is determined purely by performance concerns.
     */
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

    private IStateLong[] words;
    private int[] index;
    private IStateInt limit;

    private long[] mask;
    private int[] indexMask;
    private int limitMask;

    /**
     * Instanciate a RSparseBitSet with nbits set to 1
     * @param model model used to create backtracking variables
     * @param nbits fixed number of bits to set to 1
     */
    public RSparseBitSet(Model model, int nbits) {
        if (nbits < 1) {

        }
        BitSet b = new BitSet(nbits);
        b.set(0, nbits);
        long[] wordsToCopy = b.toLongArray();
        int size = wordsToCopy.length;
        words = new IStateLong[size];
        for (int i = 0; i < size; i++) {
            words[i] = model.getEnvironment().makeLong(wordsToCopy[i]);
        }
        limit = model.getEnvironment().makeInt(size - 1);
        index = IntStream.range(0, size).toArray();
    }

    /**
     * Instantiate a RSparseBitSet with words
     * @param model model used to create backtracking variables
     * @param words words to create the bitset
     */
    public RSparseBitSet(Model model, long[] words) {
        this.words = new IStateLong[words.length];
        for (int i = 0; i < words.length; i++) {
            this.words[i] = model.getEnvironment().makeLong(words[i]);
        }
        limit = model.getEnvironment().makeInt(words.length - 1);
        index = IntStream.range(0, words.length).toArray();
        for (int i = limit.get(); i >= 0 ; i--) {
            checkWord(index[i], i);
        }
    }

    /**
     * Check if all bits are equal to 0
     * @return true if all bits are equals to 0
     */
    public boolean isEmpty() {
        return limit.get() == -1;
    }

    /**
     * Number of bits set to 1
     * @return number of bits set to 1
     */
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i <= limit.get(); i++) {
            int offset = index[i];
            sum += Long.bitCount(words[offset].get());
        }
        return sum;
    }

    public int maskCardinality() {
        int sum = 0;
        for (int i = 0; i <= limitMask; i++) {
            int offset = indexMask[i];
            sum += Long.bitCount(mask[offset]);
        }
        return sum;
    }

    /**
     * Bitwise AND between words and m
     * @param m array to intersect with
     */
    public void and(long[] m) {
        for (int i = limit.get(); i >= 0; i--) {
            int offset = index[i];
            long w = words[offset].get() & getValue(m, offset);
            words[offset].set(w);
            checkWord(offset, i);
        }
    }

    /**
     * Bitwise AND between words and m, count the number of bits set to 1 in the result (words is not modified)
     * @param m array to intersect with
     */
    public int andCount(long[] m) {
        int sum = 0;
        for (int i = limit.get(); i >= 0; i--) {
            int offset = index[i];
            sum += Long.bitCount(words[offset].get() & getValue(m, offset));
        }
        return sum;
    }

    public void resetMask() {
        mask = copyWords();
        indexMask = index.clone();
        limitMask = limit.get();
    }

    public void andMask(long[] m) {
        for (int i = limitMask; i >= 0; i--) {
            int offset = indexMask[i];
            long w = mask[offset] & getValue(m, offset);
            mask[offset] = w;
            checkMask(offset, i);
        }
    }

    private long[] copyWords() {
        long[] copyWords = new long[words.length];
        for (int i = 0; i < words.length; i++) {
            copyWords[i] = words[i].get();
        }
        return copyWords;
    }

    /**
     * Check if words is a subset of m (ex : 100 is a subset of 110)
     * @param m superset
     * @return true if words is a subset of m
     */
    public boolean isSubsetOf(long[] m) {
        for (int i = 0; i <= limit.get(); i++) {
            int offset = index[i];
            if ((~getValue(m, offset) & words[offset].get()) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if mask is a subset of m (ex : 100 is a subset of 110)
     * @param m superset
     * @return true if mask is a subset of m
     */
    public boolean maskIsSubsetOf(long[] m) {
        for (int i = 0; i <= limitMask; i++) {
            int offset = indexMask[i];
            if ((~getValue(m, offset) & mask[offset]) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return value of specified index offset
     * @param m array of long
     * @param offset index
     * @return m[offset] if offset < m.length, 0 otherwise
     */
    private long getValue(long[] m, int offset) {
        return offset < m.length ? m[offset] : 0;
    }

    /**
     * Check if words[offset] is equals to 0, if it's the case, then we swap index[i] and index[limit] and we decrease
     *  limit of 1
     * @param offset index[i]
     * @param i
     */
    private void checkWord(int offset, int i) {
        if (words[offset].get() == 0) {
            int limitValue = limit.get();
            index[i] = index[limitValue];
            index[limitValue] = offset;
            limit.set(limitValue - 1);
        }
    }

    private void checkMask(int offset, int i) {
        if (mask[offset] == 0) {
            indexMask[i] = indexMask[limitMask];
            indexMask[limitMask] = offset;
            limitMask--;
        }
    }

    @Override
    public String toString() {
        long[] wordVals = new long[words.length];
        for (int i = 0; i < words.length; i++) {
            wordVals[i] = words[i].get();
        }
        return BitSet.valueOf(wordVals).toString();
    }

    public BitSet convertToBitset() {
        return BitSet.valueOf(copyWords());
    }

    public BitSet convertMaskToBitset() {
        return BitSet.valueOf(mask);
    }
}
