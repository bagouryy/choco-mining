package io.gitlab.chaver.mining.patterns.measure.attribute;

import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.operand.MeasureOperand;

import java.util.Set;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;
import static io.gitlab.chaver.mining.patterns.measure.operand.OperandFactory.*;

public class Mean extends AttributeMeasure {

    private final MeasureOperand op = div(add(min(getNum()), max(getNum())), constant());

    public Mean(int num) {
        super(num);
    }

    @Override
    public Set<Measure> maxConvert() {
        return op.maxConvert();
    }

    @Override
    public Set<Measure> minConvert() {
        return op.minConvert();
    }

    @Override
    public String getId() {
        return "mean" + getNum();
    }
}
