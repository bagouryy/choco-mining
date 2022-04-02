package io.gitlab.chaver.mining.patterns.measure.operand;

public class OperandFactory {

    public static MeasureOperand add(MeasureOperand o1, MeasureOperand o2) {
        return new Add(o1, o2);
    }

    public static MeasureOperand mul(MeasureOperand o1, MeasureOperand o2) {
        return new Mul(o1, o2);
    }

    public static MeasureOperand div(MeasureOperand o1, MeasureOperand o2) {
        return new Div(o1, o2);
    }

    public static MeasureOperand sub(MeasureOperand o1, MeasureOperand o2) {
        return new Sub(o1, o2);
    }

    public static MeasureOperand constant() {
        return new Constant();
    }
}
