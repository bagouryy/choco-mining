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

public class CoverClosureTest {

    private final String resPath = "src/test/resources/";

    private void testFindClosedPatterns(String dataPath, int nbExpectedClosed) throws IOException {
        Model model = new Model("closed test");
        Database database = new DatReader(dataPath, 0, true).readFiles();
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        model.post(new Constraint("Cover Size", new CoverSize(database, freq, x)));
        model.post(new Constraint("Closed", new CoverClosure(database, x)));
        List<Solution> sols = model.getSolver().findAllSolutions();
        assertEquals(nbExpectedClosed, sols.size());
    }

    @Test
    public void findClosedZoo() throws IOException {
        testFindClosedPatterns(resPath + "zoo/zoo.basenum", 4567);
    }

    @Test
    public void findClosedIris() throws IOException {
        testFindClosedPatterns(resPath + "iris/iris.dat", 135);
    }

    @Test
    public void findClosedGlass() throws IOException {
        testFindClosedPatterns(resPath + "glass/glass.dat", 6732);
    }

}
