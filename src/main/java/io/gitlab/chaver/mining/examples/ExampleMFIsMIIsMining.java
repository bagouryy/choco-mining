/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.examples;

import io.gitlab.chaver.mining.patterns.constraints.*;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Example of MFI/MII mining (from paper Belaid et al. - Contraint Programming for Mining Borders of Frequent Itemsets)
 */
public class ExampleMFIsMIIsMining {

    static BoolVar[] createModel(String dataPath, boolean mfi, int s) throws Exception {
        TransactionalDatabase database = new DatReader(dataPath).read();
        Model model = new Model();
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        int freqLB = mfi ? s : 0;
        int freqUB = mfi ? database.getNbTransactions() : (s - 1);
        IntVar freq = model.intVar("freq", freqLB, freqUB);
        model.post(new Constraint("FrequentSubs", new FrequentSubs(database, s, x)));
        model.post(new Constraint("InfrequentSupers", new InfrequentSupers(database, s, x)));
        model.post(new Constraint("CoverSize", new CoverSize(database, freq, x)));
        if (mfi) {
            model.post(new Constraint("CoverClosure", new CoverClosure(database, x)));
        }
        else {
            model.post(new Constraint("Generator", new Generator(database, x)));
        }
        int[] itemFreq = database.computeItemFreq();
        BoolVar[] xSorted = IntStream
                .range(0, database.getNbItems())
                .boxed()
                .sorted(Comparator.comparingInt(i -> itemFreq[i]))
                .map(i -> x[i])
                .toArray(BoolVar[]::new);
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(
                new InputOrder<>(model),
                new IntDomainMax(),
                xSorted
        ));
        return x;
    }

    public static void main(String[] args) throws Exception {
        String[] itemLabels = {"A", "B", "C", "D", "E"};
        // MFIs mining
        final BoolVar[] xMFI = createModel("src/test/resources/mfi/ex.dat", true, 3);
        Model mfiModel = xMFI[0].getModel();
        System.out.println("Maximal Frequent Itemsets (MFIs) : ");
        while (mfiModel.getSolver().solve()) {
            String[] mfi = IntStream
                    .range(0, itemLabels.length)
                    .filter(i -> xMFI[i].getValue() == 1)
                    .mapToObj(i -> itemLabels[i])
                    .toArray(String[]::new);
            System.out.println(Arrays.toString(mfi));
        }
        System.out.println();
        // MIIs mining
        final BoolVar[] xMII = createModel("src/test/resources/mfi/ex.dat", false, 3);
        Model miiModel = xMII[0].getModel();
        System.out.println("Minimal Infrequent Itemsets (MIIs) : ");
        while (miiModel.getSolver().solve()) {
            String[] mii = IntStream
                    .range(0, itemLabels.length)
                    .filter(i -> xMII[i].getValue() == 1)
                    .mapToObj(i -> itemLabels[i])
                    .toArray(String[]::new);
            System.out.println(Arrays.toString(mii));
        }
    }
}
