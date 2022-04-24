/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.util;

import io.gitlab.chaver.mining.patterns.constraints.CoverSize;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClosedSkyTransactionGetter implements TransactionGetter {

    private CoverSize coverSize;

    @Override
    public int[] getTransactions() {
        return coverSize.getCover().stream().toArray();
    }
}
