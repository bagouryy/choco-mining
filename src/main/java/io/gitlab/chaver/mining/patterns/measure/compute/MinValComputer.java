/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;

import java.util.Arrays;

public class MinValComputer extends AttributeMeasureComputer {

    public MinValComputer(Database database, Model model, int num) {
        super(database, model, num);
    }

    @Override
    public int getInitValue() {
        return Arrays.stream(database.getValues()[num]).max().getAsInt();
    }

    @Override
    public void compute(int i) {
        value.set(Math.min(getItemValue(i), value.get()));
    }

    @Override
    public boolean isConstant(int i) {
        return getItemValue(i) >= value.get();
    }

    @Override
    public boolean isConstant(int i, int j) {
        return getItemValue(i) >= Math.min(value.get(), getItemValue(j));
    }
}
