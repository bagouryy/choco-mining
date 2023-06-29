/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.io;

/**
 * Represents an association rule with antecedent x and consequent y, such that z = x U y
 */
public interface IRule {

    int[] getX();
    int[] getY();
    int getFreqX();
    int getFreqY();
    int getFreqZ();

}
