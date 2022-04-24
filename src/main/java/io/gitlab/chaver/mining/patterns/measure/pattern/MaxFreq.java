/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.pattern;

import io.gitlab.chaver.mining.patterns.measure.Measure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * max(x.freq)
 */
public class MaxFreq extends PatternMeasure {

    @Override
    public String getId() {
        return "max(x.freq)";
    }

    @Override
    public Set<Measure> maxConvert() {
        return Collections.unmodifiableSet(new HashSet<>());
    }

    @Override
    public Set<Measure> minConvert() {
        return Collections.singleton(this);
    }
}
