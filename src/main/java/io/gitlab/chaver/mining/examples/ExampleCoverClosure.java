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

import io.gitlab.chaver.mining.patterns.constraints.CoverClosure;
import io.gitlab.chaver.mining.patterns.constraints.CoverSize;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Example of closed patterns mining (a closed pattern is an itemset which has no superset with the same frequency)
 */
public class ExampleCoverClosure {

    public static void main(String[] args) throws Exception {
        String dataPath = "src/test/resources/contextPasquier99/contextPasquier99.dat";
        Model model = new Model("cover closure test");
        Database database = new DatReader(dataPath).readFiles();
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        model.post(new Constraint("Cover Size", new CoverSize(database, freq, x)));
        model.post(new Constraint("Cover Closure", new CoverClosure(database, x)));
        List<Pattern> closedPatterns = new LinkedList<>();
        while (model.getSolver().solve()) {
            int[] itemset = IntStream.range(0, x.length)
                    .filter(i -> x[i].getValue() == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            closedPatterns.add(new Pattern(itemset, new int[]{freq.getValue()}));
        }
        System.out.println("List of closed patterns for the dataset contextPasquier99 w.r.t. M = {freq(x)} :");
        for (Pattern closed : closedPatterns) {
            System.out.println(Arrays.toString(closed.getItems()) + ", freq=" + closed.getMeasures()[0]);
        }
    }
}
