/*
 * This file is part of io.gitlab.chaver:data-mining
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to read data :
 * - transactions (i.e. sets of items)
 * - value of items
 */
public abstract class DataReader {

    /** Path of the file which contains the transactions data */
    protected String dataPath;
    /** Extension of the file which contains the transactions data (e.g. txt) */
    protected String extension;
    /** number of measures which require a values file (e.g. mean, max, min) */
    protected int nbValueMeasures;
    /** number of transactions in the dataset */
    protected int nbTransactions;
    /** number of items in the dataset */
    protected int nbItems;
    /** TRUE if classes are ignored (i.e. database.nbClass = 0) */
    protected boolean noClasses;

    public DataReader(String dataPath, int nbValueMeasures) {
        this.dataPath = dataPath;
        this.nbValueMeasures = nbValueMeasures;
        String[] pathSplit = dataPath.split("\\.");
        this.extension = pathSplit[pathSplit.length - 1];
    }

    public DataReader(String dataPath, int nbValueMeasures, boolean noClasses) {
        this(dataPath, nbValueMeasures);
        this.noClasses = noClasses;
    }

    /**
     * Read files which contain transactions and values of items
     * @return a database object with the data
     */
    public abstract Database readFiles() throws IOException;

    /**
     * Read files which contain values of items
     * For instance, if we want to read three files of values of zoo.txt, the files
     *   zoo.val0, zoo.val1 and zoo.val2 will be read
     * @throws IOException if a file doesn't exist
     */
    protected int[][] readValueFiles() throws IOException {
        int[][] values = new int[nbValueMeasures][];
        String valuePath = dataPath.substring(0, dataPath.length() - (extension.length() + 1)) + ".val";
        for (int i = 0; i < nbValueMeasures; i++) {
            values[i] = readValueFile(valuePath + i).stream().mapToInt(Integer::intValue).toArray();
        }
        return values;
    }

    private List<Integer> readValueFile(String path) throws IOException{
        List<Integer> valueList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                valueList.add(new BigDecimal(line).multiply(new BigDecimal("100")).intValue());
            }
        }
        return valueList;
    }
}
