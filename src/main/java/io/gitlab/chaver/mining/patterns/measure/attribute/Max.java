package io.gitlab.chaver.mining.patterns.measure.attribute;


import io.gitlab.chaver.mining.patterns.measure.Measure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Max extends AttributeMeasure {

    public Max(int num) {
        super(num);
    }

    @Override
    public Set<Measure> maxConvert() {
        return new HashSet<>();
    }

    @Override
    public Set<Measure> minConvert() {
        return new HashSet<>(Collections.singletonList(this));
    }

    @Override
    public String getId() {
        return "max" + getNum();
    }
}
