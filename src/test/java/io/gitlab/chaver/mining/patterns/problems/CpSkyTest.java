/*
 * This file is part of io.gitlab.chaver:data-mining
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.problems;

public class CpSkyTest extends PatternProblemTest {

    @Override
    public PatternProblem getProblem() {
        return new CpSky();
    }
}
