/*
 * This file is part of io.gitlab.chaver:data-mining
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneratorTest {

    private final String resPath = "src/test/resources/";

    private void testFindGenerators(String dataPath, int nbExpectedGenerator) throws IOException {
        Model model = new Model("generator test");
        Database database = new DatReader(dataPath, 0, true).readFiles();
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        model.post(new Constraint("Cover Size", new CoverSize(database, freq, x)));
        model.post(new Constraint("Generator", new Generator(database, x)));
        List<Solution> sols = model.getSolver().findAllSolutions();
        assertEquals(nbExpectedGenerator, sols.size());
    }

    @Test
    public void findGeneratorsZoo() throws IOException {
        testFindGenerators(resPath + "zoo/zoo.basenum", 9977);
    }

    @Test
    public void findGeneratorsIris() throws IOException {
        testFindGenerators(resPath + "iris/iris.dat", 166);
    }

    @Test
    public void findGeneratorGlass() throws IOException {
        testFindGenerators(resPath + "glass/glass.dat", 11397);
    }

}
