package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.util.BitSetFacade;
import io.gitlab.chaver.mining.patterns.util.ConstraintSettings;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;
import java.util.stream.IntStream;

import static io.gitlab.chaver.mining.patterns.util.BitSetFactory.*;


/**
 * Given a set of boolean variables x and an integer variable f, ensures that f = freq(x)
 * Fore more information, see Schaus et al. - CoverSize : A global constraint for frequency-based itemset mining
 */
public class CoverSize extends Propagator<IntVar> {

    private final BoolVar[] items; // x
    private final BitSetFacade cover; // cover of x
    private final IntVar freq; // f
    private final int[] freeItems; // free items (i.e. not instanciated variables)
    private final IStateInt lastIndexFree; // all items between [firstIndex, lastIndexFree[ are free
    private final int firstIndex; // min index (= nb of classes of the database)

    public CoverSize(Database database, IntVar freq, BoolVar[] items) {
        super(ArrayUtils.concat(items, freq));
        cover = getBitSet(ConstraintSettings.BITSET_TYPE, database, model);
        this.freq = freq;
        this.items = items;
        this.freeItems = IntStream.range(0, database.getNbItems()).toArray();
        this.lastIndexFree = getModel().getEnvironment().makeInt(items.length);
        this.firstIndex = database.getNbClass();
    }

    public CoverSize(Database database, IntVar freq, BoolVar[] items, boolean classCover) {
        super(ArrayUtils.concat(items, freq));
        cover = classCover ? getBitSet1(ConstraintSettings.BITSET_TYPE, database, model) :
                getBitSet(ConstraintSettings.BITSET_TYPE, database, model);
        this.freq = freq;
        this.items = items;
        this.freeItems = IntStream.range(0, database.getNbItems()).toArray();
        this.lastIndexFree = getModel().getEnvironment().makeInt(items.length);
        this.firstIndex = database.getNbClass();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nU = lastIndexFree.get();
        for (int i = nU - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (items[idx].isInstantiated()) {
                nU = removeItem(i, nU, idx);
                if (items[idx].isInstantiatedTo(1)) {
                    cover.and(idx);
                }
            }
        }
        for (int i = nU - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (cover.andCount(idx) < freq.getLB()) {
                nU = removeItem(i, nU, idx);
                items[idx].setToFalse(this);
            }
        }
        cover.resetMask();
        for (int i = nU - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            cover.andMask(idx);
        }
        int freqLB = cover.maskCardinality();
        int freqUB = cover.cardinality();
        freq.updateBounds(freqLB, freqUB, this);

        if (freqUB == freq.getLB()) {
            // remove all unbound items that are not super sets of coverage
            // because these would necessarily decrease the coverage
            for (int i = nU - 1; i >= firstIndex ; i--) {
                int idx = freeItems[i];
                if (!cover.isSubsetOf(idx)) {
                    nU = removeItem(i, nU, idx);
                    items[idx].setToFalse(this);
                }
            }
        }
        lastIndexFree.set(nU);
    }

    private int removeItem(int i, int nU, int idx) {
        int lastU = nU - 1;
        freeItems[i] = freeItems[lastU];
        freeItems[lastU] = idx;
        return lastU;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }

    public BitSet getCover() {
        return cover.getWords();
    }
}
