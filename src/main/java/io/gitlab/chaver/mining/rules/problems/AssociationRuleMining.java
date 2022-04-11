package io.gitlab.chaver.mining.rules.problems;

import io.gitlab.chaver.chocotools.io.JsonResultReader;
import io.gitlab.chaver.chocotools.io.ProblemResult;
import io.gitlab.chaver.chocotools.io.ProblemResultReader;
import io.gitlab.chaver.chocotools.problem.ChocoProblem;
import io.gitlab.chaver.chocotools.problem.SetUpException;
import io.gitlab.chaver.mining.patterns.constraints.CoverClosure;
import io.gitlab.chaver.mining.patterns.constraints.CoverSize;
import io.gitlab.chaver.mining.patterns.constraints.Generator;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import io.gitlab.chaver.mining.patterns.io.PatternProblemProperties;
import io.gitlab.chaver.mining.rules.io.ArMeasuresView;
import io.gitlab.chaver.mining.rules.io.AssociationRule;
import io.gitlab.chaver.mining.rules.search.loop.monitors.ArMonitor;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.gitlab.chaver.mining.patterns.util.PatternUtil.findClosedPattern;
import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

@Command(name = "arm", description = "Association rule mining")
public class AssociationRuleMining extends ChocoProblem<AssociationRule, ArMeasuresView> {

    @Option(names = {"-d", "--data"}, required = true, description = "Datafile to use")
    private String dataPath;
    //@Option(names = {"--nc"}, description = "Ignore classes of transactions")
    private boolean noClasses = true;
    // @Option(names = {"--dat"}, description = "Use DAT file format")
    private boolean datFormat = true;
    @Option(names = {"--rt"}, description = "Rule type : ${COMPLETION-CANDIDATES} (default : ${DEFAULT-VALUE})")
    private RuleType ruleType = RuleType.ar;
    @Option(names = "--fmin", description = "Min frequency of the rule (absolute value)")
    private int minFreq;
    @Option(names = "--rfmin", description = "Min frequency of the rule (relative value)")
    private double relativeMinFreq;
    @Option(names = "--cmin", description = "Min confidence of the rule")
    private double minConf;
    @Option(names = "--sky", description = "Skypatterns file (impose constraint)")
    private String skyPath;

    private Database database;
    private ArMonitor arMonitor;
    private Map<Set<Integer>, Set<Integer>> closedPatterns;

    private enum RuleType { ar, mnr }

    @Override
    public void parseArgs() throws SetUpException {
        try {
            database = new DatReader(dataPath, 0, noClasses).readFiles();
            if (relativeMinFreq != 0 && minFreq != 0) {
                throw new SetUpException("--fmin and --rfmin are mutually exclusive (specify only one)");
            }
            if (relativeMinFreq != 0) {
                minFreq = (int) Math.round(relativeMinFreq * database.getNbTransactions());
            }
            if ((minFreq == 0 || minConf == 0) && (skyPath == null)) {
                throw new SetUpException("You should precise (--fmin and --cmin) or --sky");
            }
            if (skyPath != null) {
                closedPatterns = getClosedPatterns();
            }
        }
        catch (IOException e) {
            throw new SetUpException(e.getMessage());
        }
    }

    /*private int getAllConfidenceMin() {
        String allConfidenceMin = "acmin";
        if (constraints.containsKey(allConfidenceMin)) {
            return (int) Math.round(Double.parseDouble(constraints.get(allConfidenceMin)) * 10000);
        }
        return 0;
    }*/

    /**
     * If skypattern constraint, associate each skypattern to its closure
     * @return a map which associates each skypattern to its closure
     * @throws IOException if the skypattern file doesn't exist
     */
    private Map<Set<Integer>, Set<Integer>> getClosedPatterns() throws IOException {
        if (skyPath == null) return new HashMap<>();
        Map<Set<Integer>, Set<Integer>> closedPatterns = new HashMap<>();
        ProblemResultReader<Pattern, PatternProblemProperties> reader = new JsonResultReader<>(skyPath);
        ProblemResult<Pattern, PatternProblemProperties> result = reader.readResult(Pattern[].class,
                PatternProblemProperties.class);
        for (Pattern p : result.getSolutions()) {
            Set<Integer> x = Arrays.stream(p.getItems()).boxed().collect(Collectors.toSet());
            Set<Integer> y = Arrays.stream(findClosedPattern(p, database)).boxed().collect(Collectors.toSet());
            closedPatterns.put(x, y);
        }
        return closedPatterns;
    }

    private Set<Integer> patternsUnion(Set<Set<Integer>> patterns) {
        Set<Integer> union = new HashSet<>();
        patterns.forEach(union::addAll);
        return union;
    }

    private BoolVar[] skypatternConstraint(BoolVar[] y, BoolVar[] z) {
        if (closedPatterns == null) return new BoolVar[0];
        Set<Integer> patternsUnion = patternsUnion(new HashSet<>(closedPatterns.values()));
        BoolVar[] skyVar = new BoolVar[closedPatterns.size()];
        Map<Integer, Integer> itemsMap = database.getItemsMap();
        for (int i = 0; i < database.getNbItems(); i++) {
            if (!patternsUnion.contains(database.getItems()[i])) {
                model.arithm(z[i], "=", 0).post();
            }
        }
        int i = 0;
        //System.out.println(closedPatterns);
        for (Map.Entry<Set<Integer>, Set<Integer>> entry : closedPatterns.entrySet()) {
            ReExpression sky = null;
            Set<Integer> skypattern = entry.getKey();
            Set<Integer> closedPattern = entry.getValue();
            Set<Integer> itemsOnlyInClosure = new HashSet<>(closedPattern);
            itemsOnlyInClosure.removeAll(skypattern);
            for (int item : patternsUnion) {
                ReExpression temp;
                int idx = itemsMap.get(item);
                if (!closedPattern.contains(item)) {
                    temp = z[idx].eq(0);
                }
                else if (itemsOnlyInClosure.contains(item)) {
                    temp = y[idx].eq(1);
                }
                else {
                    temp = z[idx].eq(1);
                }
                sky = sky == null ? temp : sky.and(temp);
            }
            skyVar[i] = sky.boolVar();
            i++;
        }
        model.sum(skyVar, ">=", 1).post();
        return skyVar;
    }

    /*private Set<Integer> getZeroItems() {
        String presentItemsKey = "items";
        if (! constraints.containsKey(presentItemsKey)) return new HashSet<>();
        Set<Integer> zeroItems = IntStream.range(database.getNbClass(), database.getNbItems())
                .boxed()
                .collect(Collectors.toSet());
        Map<String, Integer> itemsMap = new HashMap<>();
        for (int i = 0; i < database.getNbItems(); i++) {
            itemsMap.put(Integer.toString(database.getItems()[i]), i);
        }
        Set<String> presentItems = new HashSet<>(Arrays.asList(constraints.get(presentItemsKey).split("&")));
        for (String item : presentItems) {
            zeroItems.remove(itemsMap.get(item));
        }
        return zeroItems;
    }

    private void aconfConstraint(BoolVar[] z, IntVar freqZ) {
        IntVar maxFreq = model.intVar("maxFreq", 0, database.getNbTransactions());
        int[] itemFreq = new FreqMetric(database).compute();
        IntVar[] itemFreqVar = model.intVarArray(database.getNbItems(), 0, database.getNbTransactions());
        for (int i = 0; i < database.getNbItems(); i++) {
            model.arithm(z[i], "*", model.intVar(itemFreq[i]), "=", itemFreqVar[i]).post();
        }
        model.max(maxFreq, itemFreqVar).post();
        freqZ.mul(10000).ge(maxFreq.mul(getAllConfidenceMin())).post();
    }

    private void forbiddenAntecedentItemsConstraint(BoolVar[] x) {
        if (!constraints.containsKey("!x")) return;
        Map<Integer, Integer> itemsMap = database.getItemsMap();
        Arrays.stream(constraints.get("!x").split("a"))
                .mapToInt(Integer::parseInt)
                .forEach(i -> x[itemsMap.get(i)].eq(0).post());
    }

    private void forbiddenConsequentItemsConstraint(BoolVar[] y) {
        if (!constraints.containsKey("!y")) return;
        Map<Integer, Integer> itemsMap = database.getItemsMap();
        Arrays.stream(constraints.get("!y").split("a"))
                .mapToInt(Integer::parseInt)
                .forEach(i -> y[itemsMap.get(i)].eq(0).post());
    }*/

    @Override
    public void buildModel() {
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        BoolVar[] y = model.boolVarArray("y", database.getNbItems());
        BoolVar[] z = model.boolVarArray("z", database.getNbItems());
        for (int i = 0; i < database.getNbItems(); i++) {
            model.arithm(x[i], "+", y[i], "<=", 1).post();
            model.addClausesBoolOrEqVar(x[i], y[i], z[i]);
        }
        model.addClausesBoolOrArrayEqualTrue(x);
        model.addClausesBoolOrArrayEqualTrue(y);
        /*forbiddenAntecedentItemsConstraint(x);
        forbiddenConsequentItemsConstraint(y);
        Set<Integer> zeroItems = getZeroItems();
        if (zeroItems.size() > 0) {
            List<BoolVar> requiredItems = new LinkedList<>();
            for (int i = 0; i < database.getNbItems(); i++) {
                if (zeroItems.contains(i)) {
                    model.arithm(z[i], "=", 0).post();
                }
                else {
                    requiredItems.add(z[i]);
                }
            }
            model.and(requiredItems.toArray(new BoolVar[0])).post();
        }*/
        IntVar freqZ = model.intVar("freqZ", minFreq, database.getNbTransactions());
        new Constraint("frequent Z", new CoverSize(database, freqZ, z)).post();
        IntVar freqX = model.intVar("freqX", minFreq, database.getNbTransactions());
        new Constraint("frequent X", new CoverSize(database, freqX, x)).post();
        if (minConf > 0) freqZ.mul(10000).ge(freqX.mul((int) Math.round(minConf * 10000))).post();
        IntVar freqY = model.intVar("freqY", minFreq, database.getNbTransactions());
        new Constraint("frequent Y", new CoverSize(database, freqY, y)).post();
        if (ruleType.equals(RuleType.mnr)) {
            new Constraint("generator x", new Generator(database, x))
                    .post();
            new Constraint("closed z", new CoverClosure(database, z)).post();
        }
        BoolVar[] skyVars = skypatternConstraint(y, z);
        BoolVar[] heuristicVars = ArrayUtils.append(skyVars, x, y, z);
        model.getSolver().setSearch(intVarSearch(
                new InputOrder<>(model),
                new IntDomainMin(),
                heuristicVars
        ));
        arMonitor = new ArMonitor(database, x, y, freqX, freqY, freqZ);
        model.getSolver().plugMonitor(arMonitor);
    }

    @Override
    protected Model createModel() {
        return new Model("AR mining", Settings.prod());
    }

    @Override
    public List<AssociationRule> getSolutions() {
        return arMonitor.getAssociationRules();
    }

    @Override
    public ArMeasuresView getProperties() {
        ArMeasuresView measures = new ArMeasuresView(solver.getMeasures());
        measures.setNbTransactions(database.getNbTransactions());
        return measures;
    }

    @Override
    protected void printSolutions() {
        getSolutions().forEach(s -> System.out.println(s.toString(database.getNbTransactions())));
    }

    public static void main(String[] args) throws Exception {
        new CommandLine(new AssociationRuleMining()).execute(args);
    }

}
