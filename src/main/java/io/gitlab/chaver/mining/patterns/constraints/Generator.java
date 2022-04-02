package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.util.BitSetFacade;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static io.gitlab.chaver.mining.patterns.util.BitSetFactory.getBitSet;


/**
 * Given a set of boolean variables x, ensures that x is a generator
 * (i.e. there exists no item i \in x such that freq(x \ {i}) = freq(x)
 * Fore more information, see Belaid et al. - Constraint programming for association rules
 */
public class Generator extends Propagator<BoolVar> {

    private final BoolVar[] items;
    private final Database database;
    private final BitSet[] dataset;
    private final BitSetFacade cover;
    private final Map<Integer, BitSet> subCovers = new HashMap<>();
    private final int[] freeItems;
    private final IStateInt lastIndexFree;
    private final int[] presentItems;
    private final IStateInt lastIndexPresent;
    private final int firstIndex;

    public Generator(BoolVar[] items, Database database, String bitSetType) {
        super(items);
        this.items = items;
        this.database = database;
        this.dataset = database.getVerticalRepresentation();
        this.cover = getBitSet(bitSetType, database, getModel());
        this.freeItems = IntStream.range(0, database.getNbItems()).toArray();
        this.lastIndexFree = getModel().getEnvironment().makeInt(items.length);
        this.firstIndex = database.getNbClass();
        this.presentItems = freeItems.clone();
        this.lastIndexPresent = getModel().getEnvironment().makeInt(firstIndex);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nFree = lastIndexFree.get();
        int nPres = lastIndexPresent.get();
        // Compute cover and free/present items
        for (int i = nFree - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (items[idx].isInstantiated()) {
                nFree = removeItem(i, nFree, idx);
                if (items[idx].isInstantiatedTo(1)) {
                    nPres = addItem(nPres, idx);
                    cover.and(idx);
                }
            }
        }
        // compute subcovers
        int coverSize = cover.cardinality();
        for (int j = nPres - 1; j >= firstIndex ; j--) {
            int idx = presentItems[j];
            if (!subCovers.containsKey(idx)) subCovers.put(idx, new BitSet(database.getNbTransactions()));
            BitSet subCover = subCovers.get(idx);
            subCover.set(0, database.getNbTransactions());
            for (int i = nPres - 1; i >= firstIndex ; i--) {
                if (j != i) {
                    int idx2 = presentItems[i];
                    subCover.and(dataset[idx2]);
                }
            }
            if (subCover.cardinality() == coverSize) fails();
        }
        // remove all items that do not lead to a generator
        for (int i = nFree - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (isGenerator(idx, coverSize, nPres)) {
                nFree = removeItem(i, nFree, idx);
                items[idx].setToFalse(this);
            }
        }
        lastIndexFree.set(nFree);
        lastIndexPresent.set(nPres);
    }

    private boolean isGenerator(int idx, int coverSize, int nP) {
        int intersectionSize = cover.andCount(idx);
        if (coverSize == intersectionSize) {
            return true;
        }
        for (int j = firstIndex; j < nP; j++) {
            BitSet subCover = (BitSet) subCovers.get(presentItems[j]).clone();
            subCover.and(dataset[idx]);
            if (subCover.cardinality() == intersectionSize) return true;
        }
        return false;
    }

    private int removeItem(int i, int nU, int idx) {
        int lastU = nU - 1;
        freeItems[i] = freeItems[lastU];
        freeItems[lastU] = idx;
        return lastU;
    }

    private int addItem(int nPos, int idx) {
        presentItems[nPos] = idx;
        return nPos + 1;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.combine(IntEventType.INCLOW);
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }
}
