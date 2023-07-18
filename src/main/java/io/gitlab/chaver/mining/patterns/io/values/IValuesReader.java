/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.io.values;

import java.io.IOException;

/**
 * Read the values of items
 */
public interface IValuesReader {

    int[][] readValueFiles() throws IOException;
}
