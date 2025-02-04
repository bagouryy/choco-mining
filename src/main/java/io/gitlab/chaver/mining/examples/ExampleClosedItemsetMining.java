/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
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
import io.gitlab.chaver.mining.patterns.search.strategy.selectors.variables.MinCov;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
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
        TransactionalDatabase database = new DatReader("data/mushroom.dat").read();
        // Create the Choco model
        Model model = new Model("Closed Itemset Mining");
        // Array of Boolean variables where x[i] == 1 represents the fact that i belongs to the itemset
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        // Integer variable that represents the frequency of x with the bounds [1, nbTransactions]
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        // Integer variable that represents the length of x with the bounds [1, nbItems]
        IntVar length = model.intVar("length", 1, database.getNbItems());

        int catSize = 3;
        int nbCat = database.getNbItems()/catSize;
        int m = 16;

        BoolVar[][] categories = new BoolVar[nbCat][catSize];
        IntVar[] catCount = model.intVarArray("catCount", nbCat, 0, 1);

        for (int i = 0; i < nbCat; i++) {
            for (int j = 0; j < catSize; j++) {
                int itemIndex = i * catSize + j;
                if (itemIndex < database.getNbItems()) {
                    categories[i][j] = x[itemIndex];
                } else {
                    categories[i][j] = model.boolVar(false);
                }
            }
        }

        for (int i = 0; i < nbCat; i++) {
            model.max(catCount[i], categories[i]).post();
        }

        model.sum(catCount, ">=", m).post();


        // Ensures that length = sum(x)
        model.sum(x, "=", length).post();
        // Ensures that freq = frequency(x)
        ConstraintFactory.coverSize(database, freq, x).post();
        // Ensures that x is a closed itemset
        ConstraintFactory.coverClosure(database, x).post();
        // Added Constraint
        model.arithm(freq,">=",2).post();
        model.arithm(length,">=",3).post();


        Solver solver = model.getSolver();
        // Variable heuristic : select item i such that freq(x U i) is minimal
        // Value heuristic : instantiate it first to 0
        solver.setSearch(Search.intVarSearch(
                new MinCov(model, database),
                new IntDomainMin(),
                x
        ));
        // Create a list to store all the closed itemsets
        List<Pattern> closedPatterns = new LinkedList<>();
        while (solver.solve()) {
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
        solver.printStatistics();
    }
}
