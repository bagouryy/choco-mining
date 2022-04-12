package io.gitlab.chaver.mining.patterns.problems;

import io.gitlab.chaver.chocotools.problem.BuildModelException;
import io.gitlab.chaver.chocotools.problem.ChocoProblem;
import io.gitlab.chaver.chocotools.problem.SetUpException;
import io.gitlab.chaver.chocotools.search.loop.monitors.SolutionRecorderMonitor;
import io.gitlab.chaver.chocotools.util.ISolutionProvider;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import io.gitlab.chaver.mining.patterns.io.PatternProblemProperties;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.attribute.*;
import io.gitlab.chaver.mining.patterns.measure.operand.MeasureOperand;
import io.gitlab.chaver.mining.patterns.measure.pattern.*;
import io.gitlab.chaver.mining.patterns.search.loop.monitors.SkypatternMonitor;
import io.gitlab.chaver.mining.patterns.search.strategy.selectors.variables.MinCov;
import io.gitlab.chaver.mining.patterns.util.MeasureListConverter;
import io.gitlab.chaver.mining.patterns.util.PatternCreator;
import io.gitlab.chaver.mining.patterns.util.TransactionGetter;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;

public abstract class PatternProblem extends ChocoProblem<Pattern, PatternProblemProperties> {
    @Option(names = "-d", required = true, description = "Path of the transactional database")
    private String dataPath;
    @Option(names = "--skym", description = "Skypattern measures", converter = MeasureListConverter.class,
            paramLabel = "<skym>")
    private List<Measure> skypatternMeasures = new LinkedList<>();
    @Option(names = "--clom", description = "Closed pattern measures", converter = MeasureListConverter.class,
            paramLabel = "<clom>")
    protected List<Measure> closedMeasures = new LinkedList<>();
    @Option(names = "--addm", description = "Additional measures", converter = MeasureListConverter.class,
            paramLabel = "<addm>")
    private List<Measure> additionalMeasures = new LinkedList<>();
    @Option(names = {"--nc"}, description = "Ignore class of the transactions")
    private boolean noClasses;
    /*@Option(names = {"--trace"}, description = "Trace search tree in Graphviz format")
    private String tracePath;
    @Option(names = {"--str"}, description = "Strategy to branch on item", paramLabel = "<str>")
    private String itemSelectorStrategy = MinCovSelector.TYPE;*/
    @Option(names = "--lmin", description = "Min length of the pattern (default : ${DEFAULT-VALUE})",
            defaultValue = "1")
    private int lengthMin;
    @Option(names = "--fmin", description = "Min freq of the pattern (default : ${DEFAULT-VALUE})", defaultValue = "1")
    protected int freqMin;
    @Option(names = "--no-infgr", description = "No infinite growth-rate")
    private boolean noInfiniteGr;
    @Option(names = "--trans", description = "Save transactions of the patterns")
    private boolean saveTrans;

    private List<Measure> allMeasures;
    protected Database database;
    private ISolutionProvider<Pattern> solutionProvider;

    // CP variables
    protected BoolVar[] items;
    protected Map<String, IntVar> measureVars = new HashMap<>();

    protected void itemVars() {
        items = model.boolVarArray("items", database.getNbItems());
        for (int i = 0; i < database.getNbClass(); i++) {
            model.arithm(items[i], "=", 0).post();
        }
    }

    private void createMeasureVar(Measure m) throws BuildModelException {
        if (measureVars.containsKey(m.getId())) return;
        int num = (m instanceof AttributeMeasure) ? ((AttributeMeasure) m).getNum() : -1;
        if (m.getClass() == Freq.class) freqVar();
        else if (m.getClass() == Length.class) lengthVar();
        else if (m.getClass() == Area.class) areaVar();
        else if (m.getClass() == AllConf.class) aconfVar();
        else if (m.getClass() == GrowthRate.class) growthRateVar();
        else if (m.getClass() == Mean.class) meanValueVar(num);
        else if (m.getClass() == Min.class) minValueVar(num);
        else if (m.getClass() == Max.class) maxValueVar(num);
        else throw new BuildModelException("Can't create var for this measure : " + m);
    }

    protected void lengthVar() {
        String lengthId = length().getId();
        IntVar length = model.intVar(lengthId, lengthMin, database.getNbItems());
        model.count(1, items, length).post();
        measureVars.put(lengthId, length);
    }

    protected abstract void freqVar();
    protected abstract void freq1Var();

    protected void freq2Var() {
        String freq2Id = freq2().getId();
        IntVar freq2 = model.intVar(freq2Id, 0, database.getClassCount()[1]);
        IntVar freq = measureVars.get(freq().getId());
        IntVar freq1 = measureVars.get(freq1().getId());
        model.arithm(freq, "-", freq1, "=", freq2).post();
        measureVars.put(freq2Id, freq2);
    }

    protected void areaVar() {
        if (!measureVars.containsKey(freq().getId())) freqVar();
        if (!measureVars.containsKey(length().getId())) lengthVar();
        IntVar freq = measureVars.get(freq().getId());
        IntVar length = measureVars.get(length().getId());
        measureVars.put(area().getId(), freq.mul(length).intVar());
    }

    protected void growthRateVar() {
        if (!measureVars.containsKey(freq1().getId())) freq1Var();
        if (!measureVars.containsKey(freq2().getId())) freq2Var();
        String growthRateId = growthRate().getId();
        IntVar freq1 = measureVars.get(freq1().getId());
        IntVar freq2 = measureVars.get(freq2().getId());
        int d1 = database.getClassCount()[0];
        int d2 = database.getClassCount()[1];
        int grUB = noInfiniteGr ? IntVar.MAX_INT_BOUND - 1 : IntVar.MAX_INT_BOUND;
        IntVar growthRate = model.intVar(growthRateId, 0, grUB);
        // freq1 == 0 && growthRate == 0 (i.e. no items are of class 1)
        ReExpression e1 = (freq1.eq(0)).and(growthRate.eq(0));
        // freq1 > 0 && freq == freq1 && growthRate == MAX_INT_BOUND (i.e. all items are of class 1)
        ReExpression e2 = (freq1.gt(0)).and(freq2.eq(0)).and(growthRate.eq(IntVar.MAX_INT_BOUND));
        // Compute growth rate, if freq == freq1 then we add 1 in (freq - freq1) to avoid 0 in denominator
        IntVar computedGr = freq1.mul(d2).div((freq2.add(freq2.eq(0))).mul(d1)).intVar();
        // freq1 > 0 && freq > freq1 && growthRate == computedGr
        ReExpression e3 = (freq1.gt(0)).and(freq2.gt(0)).and(growthRate.le(computedGr)).and(growthRate.eq(computedGr));
        e1.or(e2).or(e3).post();
        measureVars.put(growthRateId, growthRate);
    }

    protected void maxFreqVar() {
        int[] itemFreq = database.computeItemFreq();
        IntVar[] itemFreqVar = model.intVarArray(database.getNbItems(), 0, database.getNbTransactions());
        for (int i = 0; i < database.getNbItems(); i++) {
            // itemFreqVar[i] = itemFreq[i] if items[i] == 1 else 0
            model.arithm(items[i], "*", model.intVar(itemFreq[i]), "=", itemFreqVar[i]).post();
        }
        String maxFreqId = maxFreq().getId();
        IntVar maxFreq = model.intVar(maxFreqId, 0, database.getNbTransactions());
        // Compute max value of itemFreqVar
        model.max(maxFreq, itemFreqVar).post();
        measureVars.put(maxFreqId, maxFreq);
    }

    protected void aconfVar() {
        if (!measureVars.containsKey(freq().getId())) freqVar();
        if (!measureVars.containsKey(maxFreq().getId())) maxFreqVar();
        IntVar freq = measureVars.get(freq().getId());
        IntVar maxFreq = measureVars.get(maxFreq().getId());
        int coeff = 10000;
        IntVar aconf = model.intVar(allConf().getId(), 0, coeff);
        aconf.eq(freq.mul(coeff).div(maxFreq)).post();
        measureVars.put(allConf().getId(), aconf);
    }

    protected void minValueVar(int num) {
        int[] values = database.getValues()[num];
        int valUB = Arrays.stream(values).max().getAsInt();
        IntVar[] valuesMin = model.intVarArray("valuesMin_" + num, database.getNbItems(), 0, valUB);
        for (int i = 0; i < database.getNbItems(); i++) {
            ReExpression e1 = items[i].eq(0).and(valuesMin[i].eq(valUB));
            ReExpression e2 = items[i].eq(1).and(valuesMin[i].eq(values[i]));
            (e1).or(e2).post();
        }
        IntVar minVal = model.intVar("minVal_" + num, 0, valUB);
        model.min(minVal, valuesMin).post();
        measureVars.put(min(num).getId(), minVal);
    }

    protected void maxValueVar(int num) {
        int[] values = database.getValues()[num];
        int valUB = Arrays.stream(values).max().getAsInt();
        IntVar[] valuesMax = model.intVarArray("valuesMax_" + num, database.getNbItems(), 0, valUB);
        for (int i = 0; i < database.getNbItems(); i++) {
            valuesMax[i].eq(items[i].mul(values[i])).post();
        }
        IntVar maxVal = model.intVar("maxVal_" + num, 0, valUB);
        model.max(maxVal, valuesMax).post();
        measureVars.put(max(num).getId(), maxVal);
    }

    protected void meanValueVar(int num) {
        if (!measureVars.containsKey(min(num).getId())) minValueVar(num);
        if (!measureVars.containsKey(max(num).getId())) maxValueVar(num);
        IntVar min = measureVars.get(min(num).getId());
        IntVar max = measureVars.get(max(num).getId());
        measureVars.put(mean(num).getId(), (min.add(max)).div(2).intVar());
    }

    @Override
    protected void parseArgs() throws SetUpException {
        if (closedMeasures.size() == 0 && skypatternMeasures.size() == 0) {
            throw new SetUpException("--skym or --clom must be specified");
        }
        if (closedMeasures.size() > 0 && skypatternMeasures.size() > 0) {
            throw new SetUpException("--skym and --clom can't be specified both");
        }
        if (closedMeasures.size() == 0) {
            closedMeasures = new LinkedList<>(MeasureOperand.maxConvert(skypatternMeasures));
            allMeasures = Stream
                    .of(skypatternMeasures, additionalMeasures)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if (skypatternMeasures.size() == 0) {
            allMeasures = Stream
                    .of(closedMeasures, additionalMeasures)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
        int idxValMeasure = allMeasures
                .stream()
                .filter(m -> m instanceof AttributeMeasure)
                .mapToInt(m -> ((AttributeMeasure) m).getNum())
                .max()
                .orElse(-1);
        try {
            database = new DatReader(dataPath, idxValMeasure + 1, noClasses).readFiles();
        } catch (IOException e) {
            throw new SetUpException(e.getMessage(), e);
        }
    }

    @Override
    public void buildModel() throws BuildModelException {
        itemVars();
        freqVar();
        lengthVar();
        for (Measure m : allMeasures) createMeasureVar(m);
        closedConstraint();
        plugSearchMonitor();
        solver.setSearch(Search.intVarSearch(
                new MinCov(model, database),
                new IntDomainMin(),
                items
        ));
    }

    private void plugSearchMonitor() {
        List<String> allMeasuresId = allMeasures.stream().map(Measure::getId).collect(Collectors.toList());
        TransactionGetter transactionGetter = saveTrans ? transactionGetter() : null;
        PatternCreator creator = new PatternCreator(database, items, allMeasuresId, measureVars, transactionGetter);
        if (skypatternMeasures.size() == 0) {
            SolutionRecorderMonitor<Pattern> monitor = new SolutionRecorderMonitor<>(creator);
            solver.plugMonitor(monitor);
            solutionProvider = monitor;
        }
        else {
            IntVar[] obj = skypatternMeasures.stream().map(m -> measureVars.get(m.getId())).toArray(IntVar[]::new);
            SkypatternMonitor monitor = new SkypatternMonitor(obj, creator);
            model.post(new Constraint("Pareto", monitor));
            solver.plugMonitor(monitor);
            solutionProvider = monitor;
        }
    }

    protected abstract TransactionGetter transactionGetter();

    protected abstract void closedConstraint() throws BuildModelException;

    @Override
    public List<Pattern> getSolutions() {
        return Objects.isNull(solutionProvider) ? null : solutionProvider.getSolutions();
    }

    @Override
    public PatternProblemProperties getProperties() {
        PatternProblemProperties properties = new PatternProblemProperties(solver.getMeasures());
        properties.setBestSolutionCount(getSolutions().size());
        properties.setClosedMeasures(closedMeasures.stream().map(Measure::getId).collect(Collectors.toList()));
        properties.setSkyMeasures(skypatternMeasures.stream().map(Measure::getId).collect(Collectors.toList()));
        properties.setAllMeasures(allMeasures.stream().map(Measure::getId).collect(Collectors.toList()));
        return properties;
    }

    @Override
    protected void printStats() {
        super.printStats();
        if (skypatternMeasures.size() > 0) System.out.println("\tNb skypatterns : " + getSolutions().size());
    }

    @Override
    protected void printSolutions() {
        List<String> allMeasuresId = allMeasures.stream().map(Measure::getId).collect(Collectors.toList());
        for (Pattern p : getSolutions()) {
            System.out.println(p.toString(allMeasuresId));
        }
    }
}
