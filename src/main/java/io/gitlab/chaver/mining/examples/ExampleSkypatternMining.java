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

import io.gitlab.chaver.mining.patterns.constraints.AdequateClosureWC;
import io.gitlab.chaver.mining.patterns.constraints.CoverSize;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.operand.MeasureOperand;
import io.gitlab.chaver.mining.patterns.search.strategy.selectors.variables.MinCov;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.objective.ParetoMaximizer;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;

/**
 * Example of skypattern mining w.r.t. the set of measures M = {freq(x),area(x),allconf(x)}
 */
public class ExampleSkypatternMining {

    public static void main(String[] args) throws Exception {
        String dataPath = "src/test/resources/contextPasquier99/contextPasquier99.dat";
        Database database = new DatReader(dataPath).readFiles();
        Model model = new Model("skypattern mining");
        List<Measure> M = Arrays.asList(freq(), area(), allConf());
        // Compute M' such that M is maximally M-skylineable
        Set<Measure> M_prime = MeasureOperand.maxConvert(M);
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        IntVar area = freq.mul(length).intVar();
        model.sum(x, "=", length).post();
        int[] itemFreq = database.computeItemFreq();
        IntVar[] itemFreqVar = model.intVarArray(database.getNbItems(), 0, database.getNbTransactions());
        for (int i = 0; i < database.getNbItems(); i++) {
            // itemFreqVar[i] = itemFreq[i] if items[i] == 1 else 0
            model.arithm(x[i], "*", model.intVar(itemFreq[i]), "=", itemFreqVar[i]).post();
        }
        String maxFreqId = maxFreq().getId();
        IntVar maxFreq = model.intVar(maxFreqId, 0, database.getNbTransactions());
        // Compute max value of itemFreqVar
        model.max(maxFreq, itemFreqVar).post();
        IntVar aconf = freq.mul(10000).div(maxFreq).intVar();
        model.post(new Constraint("Cover Size", new CoverSize(database, freq, x)));
        model.post(new Constraint("Adequate Closure", new AdequateClosureWC(database, new ArrayList<>(M_prime), x)));
        IntVar[] objectives = new IntVar[]{freq, area, aconf};
        ParetoMaximizer maximizer = new ParetoMaximizer(objectives);
        model.post(new Constraint("Pareto", maximizer));
        Solver solver = model.getSolver();
        solver.plugMonitor(maximizer);
        solver.setSearch(Search.intVarSearch(
                new MinCov(model, database),
                new IntDomainMin(),
                x
        ));
        while (solver.solve());
        System.out.println("List of skypatterns w.r.t. {freq(x),area(x),allconf(x)} for the dataset contextPasquier99:");
        for (Solution solution : maximizer.getParetoFront()) {
            int[] itemset = IntStream
                    .range(0, x.length)
                    .filter(i -> solution.getIntVal(x[i]) == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            System.out.println(Arrays.toString(itemset) + ", freq=" + solution.getIntVal(freq) + ", area=" +
                    solution.getIntVal(area) + ", aconf=" + solution.getIntVal(aconf));
        }

    }
}
