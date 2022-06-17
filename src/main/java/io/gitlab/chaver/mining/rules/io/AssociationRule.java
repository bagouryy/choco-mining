/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.io;

import io.gitlab.chaver.mining.patterns.io.Database;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Represents an association rule x -> y, such that z = x U y
 */
@AllArgsConstructor
public class AssociationRule {

    public static DecimalFormat df = new DecimalFormat("0.000");

    /**
     * Antecedent of the rule
     */
    private @Getter int[] x;

    /**
     * Conclusion of the rule
     */
    private @Getter int[] y;

    /**
     * Frequency of x
     */
    private @Getter int freqX;

    /**
     * Frequency of y
     */
    private @Getter int freqY;

    /**
     * Frequency of z
     */
    private @Getter int freqZ;

    /**
     * Compute the confidence of the rule
     * @return confidence of the rule
     */
    public double conf() {
        return (double) freqZ / freqX;
    }

    /**
     * Compute relative support of the rule
     * @param nbTransactions number of transactions in the database
     * @return relative support of the rule
     */
    public double support(int nbTransactions) {
        return (double) freqZ / nbTransactions;
    }

    /**
     * Compute lift of the rule
     * @param nbTransactions number of transactions in the database
     * @return lift of the rule
     */
    public double lift(int nbTransactions) {
        return conf() * nbTransactions / freqY;
    }

    private String convertToString(int[] pattern) {
        if (pattern.length == 0) return "{}";
        StringBuilder str = new StringBuilder("{").append(pattern[0]);
        for (int i = 1; i < pattern.length; i++) {
            str.append(", ").append(pattern[i]);
        }
        str.append("}");
        return str.toString();
    }

    private String convertToString(int[] pattern, String[] labels, Database database) {
        if (pattern.length == 0) return "{}";
        Map<Integer, Integer> itemsMap = database.getItemsMap();
        StringBuilder str = new StringBuilder("{").append(labels[itemsMap.get(pattern[0])]);
        for (int i = 1; i < pattern.length; i++) {
            str.append(", ").append(labels[itemsMap.get(pattern[i])]);
        }
        str.append("}");
        return str.toString();
    }

    @Override
    public String toString() {
        return "AssociationRule{" +
                "x=" + convertToString(x) +
                ", y=" + convertToString(y) +
                ", freqX=" + freqX +
                ", freqY=" + freqY +
                ", freqZ=" + freqZ +
                '}';
    }

    /**
     * Convert rule to String
     * @param database database to consider
     * @param labels label of each item
     * @return corresponding string
     */
    public String toString(Database database, String[] labels) {
        int nbTransactions = database.getNbTransactions();
        if (labels == null) {
            return convertToString(x) + " => " + convertToString(y) + ", supZ=" + df.format(support(nbTransactions)) +
                    ", conf=" + df.format(conf()) + ", lift=" + df.format(lift(nbTransactions));
        }
        return convertToString(x, labels, database) + " => " + convertToString(y, labels, database) +
                ", supZ=" + df.format(support(nbTransactions)) +
                ", conf=" + df.format(conf()) + ", lift=" + df.format(lift(nbTransactions));
    }
}
