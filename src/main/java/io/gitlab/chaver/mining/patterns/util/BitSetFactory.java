/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.util;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;

public class BitSetFactory {

    public static BitSetFacade getBitSet(String type, Database database, Model model) {
        if (type.equals(RSparseBitSetFacade.TYPE)) {
            return new RSparseBitSetFacade(database, model, database.getNbTransactions());
        }
        throw new RuntimeException("Incorrect BitSet type : " + type);
    }

    public static BitSetFacade getBitSet1(String type, Database database, Model model) {
        if (type.equals(RSparseBitSetFacade.TYPE)) {
            return new RSparseBitSetFacade(database, model, database.getDatasetAsLongArray()[0]);
        }
        throw new RuntimeException("Incorrect BitSet type : " + type);
    }
}
