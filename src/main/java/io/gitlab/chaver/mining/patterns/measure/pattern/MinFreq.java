package io.gitlab.chaver.mining.patterns.measure.pattern;

import io.gitlab.chaver.mining.patterns.measure.Measure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MinFreq extends PatternMeasure {

    @Override
    public String getId() {
        return "min(x.freq)";
    }

    @Override
    public Set<Measure> maxConvert() {
        return Collections.singleton(this);
    }

    @Override
    public Set<Measure> minConvert() {
        return Collections.unmodifiableSet(new HashSet<>());
    }
}
