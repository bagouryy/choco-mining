package io.gitlab.chaver.mining.patterns.measure.attribute;

import io.gitlab.chaver.mining.patterns.measure.Measure;

import java.util.HashSet;
import java.util.Set;

public class Sum extends AttributeMeasure {

    public Sum(int num) {
        super(num);
    }

    @Override
    public Set<Measure> maxConvert() {
        return new HashSet<>();
    }

    @Override
    public Set<Measure> minConvert() {
        return new HashSet<>();
    }

    @Override
    public String getId() {
        return "sum" + getNum();
    }
}
