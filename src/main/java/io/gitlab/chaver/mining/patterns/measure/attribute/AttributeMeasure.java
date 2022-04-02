package io.gitlab.chaver.mining.patterns.measure.attribute;

import io.gitlab.chaver.mining.patterns.measure.Measure;

public abstract class AttributeMeasure extends Measure {

    private int num;

    public AttributeMeasure(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
