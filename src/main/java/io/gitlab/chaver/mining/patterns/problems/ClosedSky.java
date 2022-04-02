package io.gitlab.chaver.mining.patterns.problems;

import io.gitlab.chaver.mining.patterns.constraints.AdequateClosureDC;
import io.gitlab.chaver.mining.patterns.constraints.AdequateClosure;
import io.gitlab.chaver.mining.patterns.constraints.AdequateClosureWC;
import io.gitlab.chaver.mining.patterns.constraints.CoverSize;
import io.gitlab.chaver.mining.patterns.util.ClosedSkyTransactionGetter;
import io.gitlab.chaver.mining.patterns.util.RSparseBitSetFacade;
import io.gitlab.chaver.mining.patterns.util.TransactionGetter;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.freq;
import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.freq1;


@Command(name = "closedsky", mixinStandardHelpOptions = true, description = "ClosedSky implementation")
public class ClosedSky extends PatternProblem {

    @Option(names = "--wc", description = "Use weak consistency version of AdequateClosure")
    private boolean wc;

    private String bitsetType = RSparseBitSetFacade.TYPE;
    private CoverSize coverSize;

    @Override
    public void freqVar() {
        String freqId = freq().getId();
        IntVar freq = model.intVar(freqId, freqMin, database.getNbTransactions());
        measureVars.put(freqId, freq);
        coverSize = new CoverSize(database, freq, items, bitsetType);
        new Constraint("CoverSize x", coverSize).post();
    }

    @Override
    public void freq1Var() {
        String freq1Id = freq1().getId();
        IntVar freq1 = model.intVar(freq1Id, 0, database.getNbTransactions());
        measureVars.put(freq1Id, freq1);
        new Constraint("Freq 1", new CoverSize(database, freq1, items, bitsetType, true)).post();
    }

    @Override
    public void closedConstraint() {
        model.post(new Constraint("AdequateClosure", getAdequateClosurePropagator()));
    }

    private AdequateClosure getAdequateClosurePropagator() {
        if (wc) {
            return new AdequateClosureWC(database, items, closedMeasures);
        }
        return new AdequateClosureDC(database, items, closedMeasures);
    }

    @Override
    protected Model createModel() {
        return new Model("ClosedSky", Settings.prod());
    }

    @Override
    public TransactionGetter transactionGetter() {
        return new ClosedSkyTransactionGetter(coverSize);
    }

    public static void main(String[] args) {
        new CommandLine(new ClosedSky()).execute(args);
    }
}
