package io.gitlab.chaver.mining.patterns.measure.pattern;

import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.operand.MeasureOperand;

import java.util.Set;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;
import static io.gitlab.chaver.mining.patterns.measure.operand.OperandFactory.div;

/**
 * All-confidence measure (see Omiecinski - Alternative interest measures for mining associations in database)
 */
public class AllConf extends PatternMeasure {

    private final MeasureOperand op = div(freq(), maxFreq());

    @Override
    public String getId() {
        return "allconf";
    }

    @Override
    public Set<Measure> maxConvert() {
        return op.maxConvert();
    }

    @Override
    public Set<Measure> minConvert() {
        return op.minConvert();
    }
}
