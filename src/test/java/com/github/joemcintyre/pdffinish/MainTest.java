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
     * Test args: -s
     * Should fail, missing -i 
     */
    public void testInvokeShow () {
        String args[] = {"-s"};
        int result = Main.invoke (args);
        assertEquals (result, Main.GENERAL_ERROR);
    }
}
