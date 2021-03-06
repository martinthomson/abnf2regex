package net.abnf2regex;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A program that turns ABNF into regular expressions. Uses the ABNF syntax
 * defined in <a href="http://tools.ietf.org/html/rfc5234">RFC 5234</a>. 
 * 
 * TODO Improve test harnesses and test coverage.
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
        String targetRule = null;
        String testString = null;
        int print = 1;

        while (args.length > 0 && args[0].charAt(0) == '-')
        {
            if ((args.length >= 2) && args[0].equals("-r")) //$NON-NLS-1$
            {
                targetRule = args[1];
                args = Arrays.copyOfRange(args, 2, args.length);
            }
            else if ((args.length >= 2) && args[0].equals("-t")) //$NON-NLS-1$
            {
                testString = args[1];
                args = Arrays.copyOfRange(args, 2, args.length);
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
            else
            {
                System.err.println("Usage: abnf2regex [-r <rule>] [-t <test>] [-p] [-s <syntax>] [file ...]");
                System.err.println("\t-r <rule>\tSelect a specific rule");
                System.err.println("\t-t <test>\tTest a string against a rule (requires -r)");
                System.err.println("\t-p\t\tPrints the dictionary (without -r), or the rule (with -r)");
                StringBuilder bld = new StringBuilder();
                for (String name : RegexSyntax.getSyntaxNames())
                {
                    bld.append(',').append(name);
                }
                System.err.println("\t-s <syntax>\tSelect regex syntax [" + bld.substring(1) + "]");
                return;
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
            PrintWriter output = new PrintWriter(System.out);
            if (targetRule != null)
            {
                Rule rule = dict.getRule(targetRule);
                Abnf2Regex.printRule(targetRule, rule, output);
            }
            else
            {
                dict.write(output);
            }
            output.flush();
        }

        Abnf2Regex.testRule(dict, targetRule, testString);
    }

    private static void printRule(String ruleName, Rule rule, PrintWriter output)
    {
        if (rule == null)
        {
            output.println("No such rule '" + ruleName + "'");
            return;
        }
        try
        {
            output.print(ruleName);
            output.print(": ");
            rule.writeRegex(output, new HashSet<String>());
        }
        catch (RuleResolutionException e)
        {
            System.err.println("Error resolving rule: " + e);
        }
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
        if (testRule == null || testString == null)
        {
            return;
        }
        try
        {
            RegexSyntax.setCurrent(RegexSyntax.SYNTAX_JAVA);
            String regex = dict.ruleToRegex(testRule);
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(testString);
            String matches = m.matches() ? "matches" : "does not match"; //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println("Rule \"" + testRule + "\" " + matches + ": " + testString); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Rule rule = dict.getRule(testRule);
            System.out.println("Rule: " + rule.toAbnf(new HashSet<String>())); //$NON-NLS-1$
            System.out.println("Expanded: " + dict.expandRule(rule).toAbnf(new HashSet<String>())); //$NON-NLS-1$
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
                if (!f.exists())
                {
                    throw new FileNotFoundException(f.getName());
                }

                dict.parse(new FileInputStream(f), f.toString());
            }
        }
    }
}
