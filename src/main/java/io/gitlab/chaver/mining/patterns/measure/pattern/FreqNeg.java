/*
 * This file is part of io.gitlab.chaver:data-mining
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.pattern;

import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.operand.MeasureOperand;

import java.util.Set;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;
import static io.gitlab.chaver.mining.patterns.measure.operand.OperandFactory.*;

public class FreqNeg extends PatternMeasure {

    private final MeasureOperand op = sub(constant(), freq());

    @Override
    public String getId() {
        return "freq-";
    }

    @Override
    public Set<Measure> maxConvert() {
        return op.maxConvert();
    }

    @Override
    public Set<Measure> minConvert() {
        return op.minConvert();
    }
}
