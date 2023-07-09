/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;

public abstract class AttributeMeasureComputer extends IntMeasureComputer {

    protected int num;

    public AttributeMeasureComputer(Database database, Model model, int num) {
        super(database, model);
        this.num = num;
    }

    protected int getItemValue(int i) {
        return database.getValues()[num][i];
    }
}
