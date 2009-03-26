/**
 * Copyright (c) Andrew Corporation,
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Andrew Corporation. You shall not disclose such confidential
 * information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Andrew Corporation.
 */
package net.abnf2regex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A program that turns ABNF into regular expressions. Uses the ABNF syntax defined in <a
 * href="http://tools.ietf.org/html/rfc5234">RFC 5234</a>.
 *
 * TODO Create test harnesses.
 *
 * TODO Add options to select a particular pattern for output, rather than simply printing the entire file.
 *
 * TODO Add options to allow capture parentheses to be used for certain patterns instead of the non-capturing groups
 * currently used.
 *
 * TODO Add options to set different regex schemes, Java, Perl, Javascript, sed, etc...
 *
 * TODO Complete javadoc on all classses.
 *
 * @author mathomson
 */
public class Abnf2Regex
{
    /** A pattern that matches the scheme on a URI, identifying as such. */
    private static final String URI_PATTERN = "^[a-zA-Z](?:(?:[\\+\\-\\.]|[a-zA-Z]|\\d))*:"; //$NON-NLS-1$

    /**
     * The main program that turns ABNF into regular expressions.
     *
     * @param args the list of files or URLs that contain ABNF
     * @throws IOException if something goes wrong.
     */
    public static void main(String[] args) throws IOException
    {
        RuleDictionary dict = new RuleDictionary();

        if (args.length == 0)
        {
            dict.parse(System.in);
        }
        else
        {
            Abnf2Regex.parseInputs(dict, args);
        }
        dict.resolve();
        dict.write(new PrintWriter(System.out));
    }

    /**
     * Parses inputs and creates a rule dictionary.
     *
     * @param dict the rule dictionary to build.
     * @param inputs a list of file names or URIs to load.
     * @throws IOException when there are IO errors
     */
    private static void parseInputs(RuleDictionary dict, String... inputs) throws IOException
    {
        Pattern urlScheme = Pattern.compile(Abnf2Regex.URI_PATTERN);

        for (String fname : inputs)
        {
            Matcher isUrl = urlScheme.matcher(fname);
            if (isUrl.matches())
            {
                URL f = new URL(fname);
                dict.parse(f.openStream());
            }
            else
            {
                File f = new File(fname);
                dict.parse(new FileInputStream(f));
            }
        }
    }

}
