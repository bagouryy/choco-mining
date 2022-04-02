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
