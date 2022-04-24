/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.operand;

import io.gitlab.chaver.mining.patterns.measure.Measure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface MeasureOperand {
    Set<Measure> maxConvert();
    Set<Measure> minConvert();

    static Set<Measure> maxConvert(Collection<Measure> mSet) {
        Set<Measure> skyM = new HashSet<>();
        mSet.forEach(m -> skyM.addAll(m.maxConvert()));
        return skyM;
    }

    static Set<Measure> minConvert(Collection<Measure> mSet) {
        Set<Measure> skyM = new HashSet<>();
        mSet.forEach(m -> skyM.addAll(m.minConvert()));
        return skyM;
    }
}
