package io.gitlab.chaver.mining.patterns.util;

import lombok.AllArgsConstructor;
import org.chocosolver.solver.variables.BoolVar;

import java.util.stream.IntStream;

@AllArgsConstructor
public class CpSkyTransactionGetter implements TransactionGetter {

    private BoolVar[] transactions;

    @Override
    public int[] getTransactions() {
        return IntStream.range(0, transactions.length).filter(i -> transactions[i].getValue() == 1).toArray();
    }
}
