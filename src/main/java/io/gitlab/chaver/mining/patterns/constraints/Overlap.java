/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.*;
import java.util.stream.IntStream;

/**
 * A constraint inspired by ClosedDiversity (see Hien et al. - A Relaxation-based Approach for Mining Diverse Closed Patterns)
 * Given a transactional database, a vector of boolean variables x, a jaccard diversity threshold jmax and
 * a min frequency threshold theta, ensures that there exists no itemset y in the history such that
 * jaccard(x,y) &gt; jmax
 */
public class Overlap extends Propagator<IntVar> implements IMonitorSolution {

    private TransactionalDatabase database;
    private BitSet[] verticalRepresentation;
    private BoolVar[] x;
    private double jmax;
    private int theta;
    /** History of encountered itemsets */
    private List<int[]> itemsetsHistory = new ArrayList<>();
    /** History of the cover of each itemset */
    private List<BitSet> coversHistory = new ArrayList<>();

    public Overlap(TransactionalDatabase database, BoolVar[] x, double jmax, int theta) {
        super(x);
        this.database = database;
        this.verticalRepresentation = database.getVerticalRepresentation();
        this.x = x;
        this.jmax = jmax;
        this.theta = theta;
    }

    private BitSet createCover() {
        BitSet cover = new BitSet(database.getNbTransactions());
        cover.set(0, database.getNbTransactions());
        return cover;
    }

    private BitSet computeCoverUnion(BitSet cover, BitSet cover2) {
        BitSet coverUnion = (BitSet) cover.clone();
        coverUnion.and(cover2);
        return coverUnion;
    }

    private double LBJaccard(BitSet xCover, BitSet HCover) {
        BitSet inter = (BitSet) xCover.clone();
        inter.and(HCover);
        int properCoverCardinality = xCover.cardinality() - inter.cardinality();
        return (double) (theta - properCoverCardinality) / (xCover.cardinality() + HCover.cardinality() + properCoverCardinality - theta);
        //return (double) (theta - (properCoverCardinality)) / Math.min(xCover.cardinality(), HCover.cardinality());
    }

    private boolean PGrowthLB(BitSet xCover) {
        for (BitSet HCover : coversHistory) {
            if (LBJaccard(xCover, HCover) > jmax) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        BitSet xCover = createCover();
        Set<Integer> freeItems = new HashSet<>();
        for (int i = 0; i < database.getNbItems(); i++) {
            if (x[i].isInstantiatedTo(1)) {
                xCover.and(verticalRepresentation[i]);
            }
            if (!x[i].isInstantiated()) {
                freeItems.add(i);
            }
        }
        // Fails if x+ is not diversified
        if (!PGrowthLB(xCover)) {
            fails();
        }
        for (int i : freeItems) {
            if (!PGrowthLB(computeCoverUnion(xCover, verticalRepresentation[i]))) {
                x[i].setToFalse(this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }

    @Override
    public void onSolution() {
        int[] itemset = IntStream
                .range(0, database.getNbItems())
                .filter(i -> x[i].getValue() == 1)
                .toArray();
        BitSet cover = createCover();
        for (int i : itemset) {
            cover.and(verticalRepresentation[i]);
        }
        for (BitSet HCover : coversHistory) {
            if (computeJaccard(cover, HCover) > jmax) {
                return;
            }
        }
        itemsetsHistory.add(itemset);
        coversHistory.add(cover);
    }

    public List<int[]> getItemsetsHistory() {
        return itemsetsHistory;
    }

    public List<BitSet> getCoversHistory() {
        return coversHistory;
    }

    public static double computeJaccard(BitSet cov, BitSet cov2) {
        BitSet inter = (BitSet) cov.clone();
        inter.and(cov2);
        BitSet union = (BitSet) cov.clone();
        union.or(cov2);
        return (double) inter.cardinality() / union.cardinality();
    }
}
