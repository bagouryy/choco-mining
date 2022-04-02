package io.gitlab.chaver.mining.patterns.measure.operand;

public abstract class BinaryOperand implements MeasureOperand {

    private MeasureOperand o1;
    private MeasureOperand o2;

    public BinaryOperand(MeasureOperand o1, MeasureOperand o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public MeasureOperand getO1() {
        return o1;
    }

    public MeasureOperand getO2() {
        return o2;
    }
}
