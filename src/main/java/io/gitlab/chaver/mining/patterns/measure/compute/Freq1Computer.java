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

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.util.BitSetFacade;
import io.gitlab.chaver.mining.patterns.util.BitSetFactory;
import org.chocosolver.solver.Model;

public class Freq1Computer extends CoverComputer {

    public Freq1Computer(TransactionalDatabase database, Model model) {
        super(database, model);
    }

    @Override
    public BitSetFacade getBitSet(Model model) {
        return BitSetFactory.getBitSet1(type, database, model);
    }
}
