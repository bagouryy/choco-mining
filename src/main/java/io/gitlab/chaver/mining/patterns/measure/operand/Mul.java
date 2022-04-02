package io.gitlab.chaver.mining.patterns.measure.operand;

import io.gitlab.chaver.mining.patterns.measure.Measure;

import java.util.Set;

public class Mul extends BinaryOperand {

    public Mul(MeasureOperand o1, MeasureOperand o2) {
        super(o1, o2);
    }

    @Override
    public Set<Measure> maxConvert() {
        Set<Measure> convertResult = getO1().maxConvert();
        convertResult.addAll(getO2().maxConvert());
        return convertResult;
    }

    @Override
    public Set<Measure> minConvert() {
        Set<Measure> convertResult = getO2().minConvert();
        convertResult.addAll(getO2().minConvert());
        return convertResult;
    }
}
