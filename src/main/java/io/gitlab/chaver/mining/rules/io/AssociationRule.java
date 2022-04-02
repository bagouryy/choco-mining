package io.gitlab.chaver.mining.rules.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.DecimalFormat;

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
        StringBuilder str = new StringBuilder("{").append(pattern[0]);
        for (int i = 1; i < pattern.length; i++) {
            str.append(", ").append(pattern[i]);
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
     * @param nbTransactions number of transactions in the database
     * @return corresponding string
     */
    public String toString(int nbTransactions) {
        return convertToString(x) + " => " + convertToString(y) + ", supZ=" + df.format(support(nbTransactions)) +
                ", conf=" + df.format(conf()) + ", lift=" + df.format(lift(nbTransactions));
    }
}
