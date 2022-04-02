package io.gitlab.chaver.mining.patterns.io;

import io.gitlab.chaver.chocotools.io.MeasuresView;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.search.measure.Measures;

import java.util.List;

/**
 * Properties for PatternProblem class
 */
public class PatternProblemProperties extends MeasuresView {

    @Getter @Setter
    private List<String> closedMeasures;
    @Getter @Setter
    private List<String> skyMeasures;
    @Getter @Setter
    private List<String> allMeasures;

    public PatternProblemProperties(Measures measures) {
        super(measures);
    }
}
