package net.abnf2regex;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A program that turns ABNF into regular expressions. Uses the ABNF syntax defined in <a
 * href="http://tools.ietf.org/html/rfc5234">RFC 5234</a>.
 *
 * TODO Improve test harnesses and test coverage.
 *
 * TODO Add options to allow capture parentheses to be used for certain patterns instead of the non-capturing groups
 * currently used.
 *
 * TODO Complete javadoc on all classses.
 *
 * @author mathomson
 */
public class Abnf2Regex
{

    /** A pattern that matches the scheme on a URI, identifying as such. */
    private static final String URI_PATTERN = "^[a-zA-Z](?:(?:[\\+\\-\\.]|[a-zA-Z]|\\d))*:"; // $NON-NLS-1$ //$NON-NLS-1$

    /**
     * The main program that turns ABNF into regular expressions.
     *
     * @param args the list of files or URLs that contain ABNF
     * @throws IOException if something goes wrong.
     */
    public static void main(String[] args) throws IOException, AbnfParseException
    {
        RuleDictionary dict = new RuleDictionary();
        String testRule = null;
        String testString = null;
        int print = 1;

        while (args.length > 0 && args[0].charAt(0) == '-')
        {
            if ((args.length >= 3) && args[0].equals("-t")) //$NON-NLS-1$
            {
                testRule = args[1];
                testString = args[2];
                args = Arrays.copyOfRange(args, 3, args.length);
                print &= 2;
            }
            else if ((args.length >= 1) && args[0].equals("-p")) //$NON-NLS-1$
            {
                print |= 2;
                args = Arrays.copyOfRange(args, 1, args.length);
            }
            else if ((args.length >= 2) && args[0].equals("-s")) //$NON-NLS-1$
            {
                try
                {
                    RegexSyntax.setCurrent(args[1]);
                }
                catch (RegexSyntaxNotFoundException ex)
                {
                    System.out.println(ex.getMessage() + ": " + ex.getSyntaxName()); //$NON-NLS-1$
                }
                args = Arrays.copyOfRange(args, 2, args.length);
            }
        }

        if (args.length == 0)
        {
            dict.parse(System.in, "<stdin>"); // $NON-NLS-1$ //$NON-NLS-1$
        }
        else
        {
            Abnf2Regex.parseInputs(dict, args);
        }

        dict.resolve();

        if (print != 0)
        {
            dict.write(new PrintWriter(System.out));
        }

        testRule(dict, testRule, testString);
    }

    /**
     * Test if the named rule matches the given string
     *
     * @param dict the dictionary
     * @param testRule the rule
     * @param testString the string
     */
    private static void testRule(RuleDictionary dict, String testRule, String testString)
    {
        if (testRule != null)
        {
            try
            {
                RegexSyntax.setCurrent(RegexSyntax.SYNTAX_JAVA);
                String regex = dict.ruleToRegex(testRule);
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(testString);
                String matches = m.matches() ? "matches" : "does not match"; //$NON-NLS-1$ //$NON-NLS-2$
                System.out.println("Rule \"" + testRule + "\" " + matches + ": " + testString); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                System.out.println("Regex: " + regex); //$NON-NLS-1$
            }
            catch (RuleResolutionException ex)
            {
                System.err.println("Error in rule '" + testRule + "' : " + ex.getMessage()); //$NON-NLS-1$//$NON-NLS-2$
            }
            catch (RegexSyntaxNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Parses inputs and creates a rule dictionary.
     *
     * @param dict the rule dictionary to build.
     * @param inputs a list of file names or URIs to load.
     * @throws IOException when there are IO errors
     */
    private static void parseInputs(RuleDictionary dict, String... inputs) throws IOException, AbnfParseException
    {
        Pattern urlScheme = Pattern.compile(Abnf2Regex.URI_PATTERN);

        for (String fname : inputs)
        {
            Matcher isUrl = urlScheme.matcher(fname);

            if (isUrl.matches())
            {
                URL f = new URL(fname);

                dict.parse(f.openStream(), f.toString());
            }
            else
            {
                File f = new File(fname);

                dict.parse(new FileInputStream(f), f.toString());
            }
        }
    }
}
