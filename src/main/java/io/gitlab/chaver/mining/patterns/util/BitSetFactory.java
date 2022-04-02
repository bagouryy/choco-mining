package io.gitlab.chaver.mining.patterns.util;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;

public class BitSetFactory {

    public static BitSetFacade getBitSet(String type, Database database, Model model) {
        if (type.equals(RSparseBitSetFacade.TYPE)) {
            return new RSparseBitSetFacade(database, model, database.getNbTransactions());
        }
        else if (type.equals(BitSetFacadeImpl.TYPE)) {
            return new BitSetFacadeImpl(database, model, database.getNbTransactions());
        }
        throw new RuntimeException("Incorrect BitSet type : " + type);
    }

    public static BitSetFacade getBitSet1(String type, Database database, Model model) {
        if (type.equals(RSparseBitSetFacade.TYPE)) {
            return new RSparseBitSetFacade(database, model, database.getDatasetAsLongArray()[0]);
        }
        else if (type.equals(BitSetFacadeImpl.TYPE)) {
            return new BitSetFacadeImpl(database, model, database.getDatasetAsLongArray()[0]);
        }
        throw new RuntimeException("Incorrect BitSet type : " + type);
    }
}
