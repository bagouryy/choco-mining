package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.util.BitSetFacade;
import io.gitlab.chaver.mining.patterns.util.ConstraintSettings;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;

import java.util.stream.IntStream;

import static io.gitlab.chaver.mining.patterns.util.BitSetFactory.getBitSet;


/**
 * Given a set of boolean variables x, ensures that x is a closed pattern w.r.t. {freq}
 * Fore more information, see Schaus et al. - CoverSize : A global constraint for frequency-based itemset mining
 */
public class CoverClosure extends Propagator<BoolVar> {

    private final BoolVar[] items;
    private final BitSetFacade cover;
    private final int[] freeItems;
    private final IStateInt lastIndexFree;
    private final int[] absentItems; // items instanciated to 0
    private final IStateInt lastIndexAbs;
    private final int firstIndex;

    public CoverClosure(Database database, BoolVar[] items) {
        super(items);
        cover = getBitSet(ConstraintSettings.BITSET_TYPE, database, model);
        this.items = items;
        this.freeItems = IntStream.range(0, database.getNbItems()).toArray();
        this.lastIndexFree = getModel().getEnvironment().makeInt(items.length);
        this.firstIndex = database.getNbClass();
        this.absentItems = freeItems.clone();
        this.lastIndexAbs = getModel().getEnvironment().makeInt(firstIndex);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nFree = lastIndexFree.get();
        int nAbs = lastIndexAbs.get();
        // compute cover and free/absent items
        for (int i = nFree - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (items[idx].isInstantiated()) {
                nFree = removeItem(i, nFree, idx);
                if (items[idx].isInstantiatedTo(1)) {
                    cover.and(idx);
                }
                else {
                    nAbs = addItem(nAbs, idx);
                }
            }
        }
        // fails if exists absent item idx such that freq(x U {idx}) = freq(x)
        for (int i = nAbs - 1; i >= firstIndex; i--) {
            int idx = absentItems[i];
            if (cover.isSubsetOf(idx)) fails();
        }
        // all items idx such that freq(x U {idx}) = freq(x) are added in the set of present items
        for (int i = nFree - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (cover.isSubsetOf(idx)) {
                nFree = removeItem(i, nFree, idx);
                items[idx].setToTrue(this);
            }
        }
        lastIndexFree.set(nFree);
        lastIndexAbs.set(nAbs);
    }

    private int removeItem(int i, int nU, int idx) {
        int lastU = nU - 1;
        freeItems[i] = freeItems[lastU];
        freeItems[lastU] = idx;
        return lastU;
    }

    private int addItem(int nP, int idx) {
        absentItems[nP] = idx;
        return nP + 1;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }
}
