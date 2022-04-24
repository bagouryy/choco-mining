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

public abstract class MeasureComputer {

    protected Database database;

    public MeasureComputer(Database database) {
        this.database = database;
    }

    /**
     * Compute m(x+ U {i})
     * @param i item
     */
    public abstract void compute(int i);

    /**
     * m(x+ U {i}) == m(x+)
     * @param i item
     * @return true if the above condition is verified
     */
    public abstract boolean isConstant(int i);

    /**
     * m(x+ U {i} U {j}) == m(x+ U {j})
     * @param i item
     * @param j item
     * @return true if the above condition is verified
     */
    public abstract boolean isConstant(int i, int j);
}
