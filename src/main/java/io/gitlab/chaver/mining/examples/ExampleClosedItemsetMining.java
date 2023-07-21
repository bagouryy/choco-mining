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


import io.gitlab.chaver.mining.patterns.constraints.factory.ConstraintFactory;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;


/**
 * Example of closed pattern mining (a closed pattern is an itemset which has no superset with the same frequency)
 */
public class ExampleClosedItemsetMining {

    public static void main(String[] args) throws Exception {
        // Read the transactional database
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();
        // Create the Choco model
        Model model = new Model("Closed Itemset Mining");
        // Array of Boolean variables where x[i] == 1 represents the fact that i belongs to the itemset
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        // Integer variable that represents the frequency of x with the bounds [1, nbTransactions]
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        // Integer variable that represents the length of x with the bounds [1, nbItems]
        IntVar length = model.intVar("length", 1, database.getNbItems());
        // Ensures that length = sum(x)
        model.sum(x, "=", length).post();
        // Ensures that freq = frequency(x)
        ConstraintFactory.coverSize(database, freq, x).post();
        // Ensures that x is a closed itemset
        ConstraintFactory.coverClosure(database, x).post();
        // Create a list to store all the closed itemsets
        List<Pattern> closedPatterns = new LinkedList<>();
        while (model.getSolver().solve()) {
            int[] itemset = IntStream.range(0, x.length)
                    .filter(i -> x[i].getValue() == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            // Add the closed itemset with its frequency to the list
            closedPatterns.add(new Pattern(itemset, new int[]{freq.getValue()}));
        }
        System.out.println("List of closed itemsets for the dataset contextPasquier99 w.r.t. freq(x):");
        // Print all the closed itemsets with their frequency
        for (Pattern closed : closedPatterns) {
            System.out.println(Arrays.toString(closed.getItems()) +
                    ", freq=" + closed.getMeasures()[0]);
        }
    }
}
