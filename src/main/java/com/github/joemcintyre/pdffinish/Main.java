/**
 * PDF Finish, updates PDF metadata and table of contents.
 * 
 * @author Joe McIntyre 
 * 
 * Copyright (c) 2014, Joe McIntyre
 * License: MIT
 */
package com.github.joemcintyre.pdffinish;

import java.io.File;
import org.apache.commons.cli.*;

/**
 * Process command line and initiate application.
 */
public class Main {
    public final static int NO_ERROR = 0;
    public final static int GENERAL_ERROR = 1;
    
    private static Options options = null;

    /**
     * Program entry point.
     * @param args Command line arguments.
     */
    public static void main (String args[]) {
        int exitCode = 0;
    
        try {
            exitCode = invoke (args);
        } catch (Exception e) {
            System.out.println ("Uncaught exception " + e);
            e.printStackTrace ();
            exitCode = GENERAL_ERROR;
        }

        System.exit (exitCode);
    }

    /**
     * Method available for testing, allowing checking exit code without
     * invoking System.exit.
     * @param args Command line arguments.
     * @return Exit code.
     */
    public static int invoke (String args[]) {
        populateOptions ();

        int result = NO_ERROR;
        File fileInput = null;
        String filenameOutput = null;
        File fileConfig = null;

        CommandLine cmd = processCommandLine (args);
        if (cmd == null) {
            printUsage ();
            return (GENERAL_ERROR);
        }

        String filenameInput = cmd.getOptionValue ("i");
        if (filenameInput == null) {
            System.out.println ("PDF input file not specified");
            printUsage ();
            return (GENERAL_ERROR);
        } else {
            fileInput = new File (filenameInput);
            if (fileInput.exists () == false) {
                System.out.println ("PDF input file does not exist");
                printUsage ();
                return (GENERAL_ERROR);
            }
        }

        PDFFinish finish = new PDFFinish ();
        if (cmd.hasOption ("s")) {
            result = finish.showInfo (fileInput);
        } else {
            filenameOutput = cmd.getOptionValue ("o");
            if (filenameOutput == null) {
                System.out.println ("PDF output file not specified");
                printUsage ();
                return (GENERAL_ERROR);
            }

            String filenameConfig = cmd.getOptionValue ("c");
            if (filenameConfig == null) {
                System.out.println ("Configuration file not specified");
                printUsage ();
                return (GENERAL_ERROR);
            } else {
                fileConfig = new File (filenameConfig);
                if (fileConfig.exists () == false) {
                    System.out.println ("Configuation file does not exist");
                    printUsage ();
                    return (GENERAL_ERROR);
                }
            }
            result = finish.generatePDF (fileConfig, fileInput, filenameOutput);
        }
        return (result);
    }

    /**
     * Populate command line options, making available for command line
     * processing and usage message functions.
     */
    private static void populateOptions () {
        options = new Options ();
        options.addOption ("s", "show", false, "Show PDF metadata and ToC");
        options.addOption ("v", "version", false, "Show version number");
        options.addOption ("h", "help", false, "Print this message");
        
        OptionBuilder.withArgName ("inputFile");
        OptionBuilder.hasArg ();
        OptionBuilder.withDescription ("input PDF file");
        options.addOption (OptionBuilder.create ("i"));
        
        OptionBuilder.withArgName ("outputFile");
        OptionBuilder.hasArg ();
        OptionBuilder.withDescription ("output PDF file");
        options.addOption (OptionBuilder.create ("o"));

        OptionBuilder.withArgName ("configFile");
        OptionBuilder.hasArg ();
        OptionBuilder.withDescription ("configuration file (JSON)");
        options.addOption (OptionBuilder.create ("c"));
    }

    /**
     * Print usage message to stdout.
     */
    private static void printUsage () {
        System.out.println ();
        HelpFormatter help = new HelpFormatter ();
        help.printHelp ("pdfFinish [options]", options);        
    }

    /**
     * Process command line arguments.
     * @param args Command line arguments.
     * @return Command object, or null on error.
     */
    private static CommandLine processCommandLine (String args[]) {
        CommandLine cmd = null;

        boolean valid = false;
        CommandLineParser parser = new BasicParser ();
        try {
            cmd = parser.parse (options, args);
            if (cmd.hasOption ("v")) {
                System.out.println ("Version 0.1.0");
                System.exit (NO_ERROR);
            } else if (cmd.hasOption ("h")) {
                printUsage ();
                System.exit (NO_ERROR);
            } else {
                if (cmd.hasOption ("s")) {
                    if (cmd.hasOption ("i")) {
                        if (cmd.hasOption ("o") || cmd.hasOption ("c")) {
                            System.out.println ("Cannot specify config or output file with show option");
                        } else {
                            valid = true;
                        }
                    } else {
                        System.out.println ("Missing input file for show option");
                    }
                } else {
                    if (cmd.hasOption ("i") && cmd.hasOption ("o") && cmd.hasOption ("c")) {
                        valid = true;
                    } else {
                        System.out.println ("Must specify input, output and configuration files");
                    }
                }
                
                if (valid == false) {
                    return (null);
                }
            }
        } catch (ParseException e) {
            System.out.println ("Error processing command: " + e);
            return (null);
        }

        return (cmd);
    }
}
