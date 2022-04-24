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
 * Frequence of items of class 1, freq(x, T1)
 */
public class Freq1 extends PatternMeasure {

    @Override
    public Set<Measure> maxConvert() {
        return new HashSet<>(Collections.singletonList(this));
    }

    @Override
    public Set<Measure> minConvert() {
        return new HashSet<>();
    }

    @Override
    public String getId() {
        return "freq1";
    }
}
