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
