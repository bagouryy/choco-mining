package io.gitlab.chaver.mining.patterns.search.loop.monitors;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import io.gitlab.chaver.mining.patterns.util.TransactionGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@AllArgsConstructor
public class PatternSearchMonitor implements IMonitorSolution {

    protected final @Getter List<Pattern> patterns = new LinkedList<>();
    protected Database database;
    protected BoolVar[] items;
    protected List<String> allMeasuresId;
    protected List<String> paretoMeasuresId;
    protected Map<String, IntVar> measureVars;
    protected TransactionGetter transactionGetter;

    @Override
    public void onSolution() {
        int[] itemSave = IntStream
                .range(0, items.length)
                .filter(i -> items[i].isInstantiatedTo(1))
                .map(i -> database.getItems()[i])
                .toArray();
        int[] measureSave = new int[allMeasuresId.size()];
        for (int i = 0; i < allMeasuresId.size(); i++) {
            measureSave[i] = measureVars.get(allMeasuresId.get(i)).getValue();
        }
        Pattern p = new Pattern(itemSave, measureSave);
        if (transactionGetter != null) p.setTransactions(transactionGetter.getTransactions());
        patterns.add(p);
        if (paretoMeasuresId.size() > 0) {
            for (int i = patterns.size() - 2; i >= 0; i--) {
                if (patterns.get(i).isDominatedBy(p, paretoMeasuresId.size())) patterns.remove(i);
            }
        }
    }
}
