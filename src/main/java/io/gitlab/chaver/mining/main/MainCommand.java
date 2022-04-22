/*
 * This file is part of io.gitlab.chaver:data-mining
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.main;

import io.gitlab.chaver.chocotools.util.ProblemExceptionHandlerProd;
import io.gitlab.chaver.mining.patterns.problems.ClosedSky;
import io.gitlab.chaver.mining.patterns.problems.CpSky;
import io.gitlab.chaver.mining.rules.problems.AssociationRuleMining;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "mine", subcommands = {ClosedSky.class, CpSky.class, AssociationRuleMining.class})
public class MainCommand {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MainCommand())
                .setExecutionExceptionHandler(new ProblemExceptionHandlerProd())
                .execute(args);
        System.exit(exitCode);
    }
}
