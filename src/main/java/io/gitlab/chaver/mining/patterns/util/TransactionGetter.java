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

/**
 * Get all transactions which contain the given itemset
 */
public interface TransactionGetter {

    /**
     * Index of the transactions that contain a given itemset
     * @return index of the transactions
     */
    int[] getTransactions();
}
