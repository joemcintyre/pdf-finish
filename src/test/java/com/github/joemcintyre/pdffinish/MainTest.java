package com.github.joemcintyre.pdffinish;

import com.github.joemcintyre.pdffinish.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Main processing.
 */
public class MainTest extends TestCase {
    /**
     * Create the test case
     * @param testName name of the test case
     */
    public MainTest (String testName) {
        super (testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite () {
        return new TestSuite (MainTest.class);
    }

    /**
     * Test show option with no input file specified (short)
     * Args: -s without -i
     * Should fail, missing -i 
     */
    public void testInvokeShowShort () {
        String args[] = {"-s"};
        int result = Main.invoke (args);
        assertEquals (result, Main.GENERAL_ERROR);
    }

    /**
     * Test show option with no input file specified (long)
     * Args: -s without -i
     * Should fail, missing -i 
     */
    public void testInvokeShowLong () {
        String args[] = {"--show"};
        int result = Main.invoke (args);
        assertEquals (result, Main.GENERAL_ERROR);
    }

    /**
     * Test invalid option flag
     * Args: -z
     * Should fail, no option -z
     */
    public void testInvalidOption () {
        String args[] = {"-z"};
        int result = Main.invoke (args);
        assertEquals (result, Main.GENERAL_ERROR);
    }

    /**
     * Test version information flag (short)
     * Args: -v
     * Should exit with exit code 0
     */
    public void testVersionShortOption () {
        String args[] = {"-v"};
        int result = Main.invoke (args);
        assertEquals (result, Main.NO_ERROR);
    }

    /**
     * Test version information flag (long)
     * Args: --version
     * Should exit with exit code 0
     */
    public void testVersionLongOption () {
        String args[] = {"--version"};
        int result = Main.invoke (args);
        assertEquals (result, Main.NO_ERROR);
    }

    /**
     * Test help flag (short)
     * Args: -h
     * Should exit with exit code 0
     */
    public void testHelpShortOption () {
        String args[] = {"-h"};
        int result = Main.invoke (args);
        assertEquals (result, Main.NO_ERROR);
    }

    /**
     * Test help flag (long)
     * Args: --help
     * Should exit with exit code 0
     */
    public void testHelpLongOption () {
        String args[] = {"--help"};
        int result = Main.invoke (args);
        assertEquals (result, Main.NO_ERROR);
    }
}
