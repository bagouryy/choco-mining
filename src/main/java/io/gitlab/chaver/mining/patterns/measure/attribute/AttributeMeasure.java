/*
 * This file is part of io.gitlab.chaver:data-mining
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
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
