package net.abnf2regex;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Do some tests of the overall parsing and regular expression generation capabilities.
 */
@SuppressWarnings("nls")
public class OverallTests
{

    private RuleDictionary rd = new RuleDictionary();

    /**
     * Test that the builtin rule produces the right regex
     *
     * @param name the name of the rule
     * @param regex the expected regex
     */
    private void checkRule(String name, String regex)
    {
        checkRule(name, regex, null);
    }

    /**
     * Test that the builtin rule produces the right regex
     *
     * @param name the name of the rule
     * @param regex the expected regex
     * @param abnf the expected abnf, which may be null
     */
    private void checkRule(String name, String regex, String abnf)
    {
        try
        {
            Assert.assertTrue("resolve " + name, this.rd.resolve());
            Assert.assertEquals("regex " + name, regex, this.rd.ruleToRegex(name));
            if (abnf != null)
            {
                Assert.assertEquals("abnf " + name, abnf, this.rd.getRule(name).toAbnf());
            }
        }
        catch (RuleResolutionException ex)
        {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Test that a given pattern produces a given regular expression.
     *
     * @param name A name that is given to the rule, so that we know what went wrong, if something does go wrong...
     * @param rule The ABNF rule
     * @param regex The expected regular expression
     */
    private void test(String name, String rule, String regex)
    {
        test(name, rule, regex, rule);
    }

    /**
     * Test that a given pattern produces a given regular expression.
     *
     * @param name A name that is given to the rule, so that we know what went wrong, if something does go wrong...
     * @param rule The ABNF rule
     * @param regex The expected regular expression
     * @param abnf the expected simplified ABNF output
     */
    private void test(String name, String rule, String regex, String abnf)
    {
        String s = name + " = " + rule + "\r\n";
        try
        {
            this.rd.parse(new StringReader(s), name);
            checkRule(name, regex, abnf);
        }
        catch (IOException ioex)
        {
            Assert.fail(ioex.getMessage());
        }
        catch (AbnfParseException abnfex)
        {
            Assert.fail(abnfex.getMessage());
        }
    }

    /**
     * Test that literals are properly converted
     */
    @Test
    public void testBuiltinRules()
    {
        checkRule("ALPHA", "[A-Za-z]");
        checkRule("BIT", "[01]");
        checkRule("CHAR", "[\\x01-\\x7f]");
        checkRule("CR", "\\r");
        checkRule("LF", "\\n");
        checkRule("CRLF", "\\r\\n");
        checkRule("CTL", "[\\x00-\\x1f\\x7f]");
        checkRule("DIGIT", "\\d");
        checkRule("DQUOTE", "\"");
        checkRule("HEXDIG", "[\\dA-Fa-f]");
        checkRule("HTAB", "\\t");
        checkRule("SP", " ");
        checkRule("WSP", "[\\t ]");
        checkRule("LWSP", "(?:[\\t ]|\\r\\n[\\t ])*"); // *(WSP / CRLF WSP)
        checkRule("OCTET", "[\\x00-\\xff]");
        checkRule("VCHAR", "[!-~]");
    }

    /**
     * Test empty
     */
    @Test
    public void testEmpty()
    {
        test("empty", "", "");
    }

    /**
     * Test that literals are properly converted
     */
    @Test
    public void testLiterals()
    {
        test("hexliteral", "%x30", "0");
        test("decimalliteral", "%d48", "0", "%x30");
        test("binaryliteral", "%b110000", "0", "%x30");
    }

    /**
     * Test that cardinality can be applied correctly to literals
     */
    @Test
    public void testCardinality()
    {
        test("once", "1%x30", "0", "%x30");
        test("maybe", "*1%x30", "0?", "[%x30]");
        test("maybesquare", "[%x30]", "0?");
        test("any", "*%x30", "0*");
        test("anyzero", "0*%x30", "0*", "*%x30");
        test("oneormore", "1*%x30", "0+");
        test("three", "3%x30", "0{3}");
        test("four", "4*4%x30", "0{4}", "4%x30");
        test("fiveormore", "5*%x30", "0{5,}");
        test("uptofive", "*5%x30", "0{0,5}");
        test("nested", "2(*5%x30)", "0{0,5}{2}");
    }

    /**
     * Test that Unicode characters are correctly escaped
     */
    @Test
    public void testUnicode()
    {
        test("euro", "%x20ac", "\\u20ac");
        test("maxsingle", "%xffff", "\\uffff");
        test("maxdouble", "%x10ffff", "\\udbff\\udfff");
        test("oneoctet", "%xef", "\\xef");
    }

    /**
     * Test ranges and sequences of literals
     */
    @Test
    public void testLiteralRange()
    {
        test("onetwothree", "%x31.32.33", "123");
        test("onetwothreetwice", "2%x31.32.33", "(?:123){2}");
        test("digit", "%x30-39", "\\d");
        test("lowdigit", "%x30-33", "[0-3]");
        test("lowdigitmore", "1*%x30-33", "[0-3]+");
        test("lowdigitless", "*1%x30-33", "[0-3]?", "[%x30-33]");
        test("lowdigitseq", "%x30-33 %x34-36", "[0-3][4-6]", "(%x30-33 %x34-36)");

        // The following is invalid, but no harm in accepting it
        test("lowdigitseqbad", "%x30-33.34-36", "[0-3][4-6]", "(%x30-33 %x34-36)");
    }

    /**
     * Test strings
     */
    @Test
    public void testStrings()
    {
        test("one", "\"1\"", "1");
        test("onetwothree", "\"123\"", "123");
        test("repeated", "2\"1\"", "1{2}");
        test("lowercase", "\"a\"", "[Aa]");
        test("lowercaseseq", "\"abc\"", "[Aa][Bb][Cc]");
        test("uppercase", "\"A\"", "[Aa]");
        test("uppercaseseq", "\"ABC\"", "[Aa][Bb][Cc]");
    }

    /**
     * Test sequences
     */
    @Test
    public void testSequences()
    {
        test("literals", "%x31 %x32", "12", "%x31.32");
        test("nospace", "%x31%x32", "12", "%x31.32");
        test("repeat", "%x31 %x31", "11", "%x31.31");
        test("strings", "\"1\" \"2\"", "12", "\"12\"");
        test("repeatstr", "\"1\" \"1\"", "11", "\"11\"");
        test("mixed", "\"1\" %x32", "12", "(\"1\" %x32)");
        test("recurrencesame", "2%x31 2%x32", "(?:12){2}", "2%x31.32");
        test("recurrencedifferent", "2%x31 3%x32", "1{2}2{3}", "(2%x31 3%x32)");
        test("recurrencesamestr", "2\"1\" 2\"2\"", "(?:12){2}", "2\"12\"");
        test("recurrencedifferentstr", "2\"1\" 3\"2\"", "1{2}2{3}", "(2\"1\" 3\"2\")");

        test("groupedseq1", "2%x31 2%x32 3%x33", "(?:12){2}3{3}", "(2%x31.32 3%x33)");
        test("groupedseq2", "2%x31 (2%x32 3%x33)", "(?:12){2}3{3}", "(2%x31.32 3%x33)");
        test("groupedseq3", "2%x31 4(2%x32 3%x33)", "1{2}(?:2{2}3{3}){4}", "(2%x31 4(2%x32 3%x33))");
        test("groupedseq4", "2%x31 2(%x32 3%x33)", "(?:123{3}){2}", "2(%x31.32 3%x33)");

    }

    /**
     * Test choices
     */
    @Test
    public void testChoices()
    {
        test("literals", "%x31 / %x32", "[12]", "(%x31 / %x32)");
        test("nospace", "%x31/%x32", "[12]", "(%x31 / %x32)");
        test("extraspace", "\t%x31 /  %x32 ", "[12]", "(%x31 / %x32)");
        test("strings", "\"1\" / \"2\"", "[12]", "(\"1\" / \"2\")");
        test("alphastring", "\"a\" / \"b\"", "[ABab]", "(\"a\" / \"b\")");
        test("mixed", "\"1\" / %x32", "[12]", "(\"1\" / %x32)");
        test("multiliterals", "%x31.32 / %x32.33", "(?:12|23)", "(%x31.32 / %x32.33)");
        test("multistrings", "\"12\" / \"23\"", "(?:12|23)", "(\"12\" / \"23\")");
        test("multimixed", "\"12\" / %x32.33", "(?:12|23)", "(\"12\" / %x32.33)");
        test("recurrencesame", "2%x31 / 2%x32", "[12]{2}", "2(%x31 / %x32)");
        test("recurrencedifferent", "2%x31 / 3%x32", "(?:1{2}|2{3})", "(2%x31 / 3%x32)");
    }

    /**
     * Test the combination of choices and sequences in more complex combinations.
     */
    @Test
    public void testCombinations()
    {
        test("precedence", "%x31 2%x32 / 3%x33", "(?:12{2}|3{3})", "((%x31 2%x32) / 3%x33)");
        test("precedence2", "2%x31 2%x32 / 3%x33", "(?:(?:12){2}|3{3})", "(2%x31.32 / 3%x33)");
        test("precedence2", "%x31 / 2%x32 3%x33", "(?:1|2{2}3{3})", "(%x31 / (2%x32 3%x33))");
        test("precedence4", "%x31 2%x32 / 3%x33 4%x34", "(?:12{2}|3{3}4{4})", "((%x31 2%x32) / (3%x33 4%x34))");
        test("precedence4", "2%x31 2%x32 / 2%x33 2%x34", "(?:12|34){2}", "2(%x31.32 / %x33.34)");
        // more complex tests with three basic particles
        test("groupedchoice1", "2%x31 / 2%x32 / 3%x33", "(?:1{2}|2{2}|3{3})", "(2%x31 / 2%x32 / 3%x33)");
        test("groupedchoice2", "2%x31 / (2%x32 / 3%x33)", "(?:1{2}|2{2}|3{3})", "(2%x31 / 2%x32 / 3%x33)");
        test("groupedchoice3", "2%x31 / 4(2%x32 / 3%x33)", "(?:1{2}|(?:2{2}|3{3}){4})", "(2%x31 / 4(2%x32 / 3%x33))");
        test("groupedchoice4", "2%x31 / 2(2%x32 / 3%x33)", "(?:1|2{2}|3{3}){2}", "2(%x31 / (2%x32 / 3%x33))");
    }

    /**
     * Test named fragments. For this we'll use the builtin rules.
     */
    @Test
    public void testNamed()
    {
        test("name", "ALPHA", "[A-Za-z]");
        test("name", "DIGIT", "\\d");
        test("twoname", "2DIGIT", "\\d{2}");
        test("twonameseq", "DIGIT DIGIT", "\\d{2}", "2DIGIT");
    }

    /**
     * Test wildcard fragments.
     */
    @Test
    public void testWildcard()
    {
        test("empty", "<>", ".*");
        test("text", "<text>", ".*");
        test("repetitions", "2<text>", ".*", "<text>");
        test("two", "<text> <here>", ".*.*", "(<text> <here>)");
    }

    /**
     * Test line continuations.
     */
    @Test
    public void testContinuations()
    {
        test("onetwolf", "%x31\n\t%x32", "12", "%x31.32");
        test("onetwolf", "%x31\r\t%x32", "12", "%x31.32");
        test("onetwocrlf", "%x31\r\n\t%x32", "12", "%x31.32");
        test("oneortwo", "%x31 /\n %x32", "[12]", "(%x31 / %x32)");
        test("oneortwo2", "%x31\r\n  \t/ %x32", "[12]", "(%x31 / %x32)");
        test("parens", "%x31 (\r\n  \t%x32 / %x33)", "1[23]", "(%x31 (%x32 / %x33))");
        test("parens2", "%x31 (\r\n  \t%x32\n / %x33)", "1[23]", "(%x31 (%x32 / %x33))");
        test("parens3", "%x31 (\r\n  \t%x32\n /\r %x33)", "1[23]", "(%x31 (%x32 / %x33))");
        test("nestedparens", "%x31 ((\r\n  \t%x32 / %x33))", "1[23]", "(%x31 (%x32 / %x33))");
        test("precedence", "%x31 / %x32\r\n  \t%x33", "(?:1|23)", "(%x31 / %x32.33)");
        test("equalslash", "%x31\r\nequalslash =/ %x32", "[12]", "(%x31 / %x32)");
        test("equalslashnotfound", "%x31\r\nequalslashnf =/ %x32", "1", "%x31");
        try
        {
            Assert.assertEquals("2", this.rd.ruleToRegex("equalslashnf"));
            Assert.assertEquals("%x32", this.rd.getRule("equalslashnf").toAbnf());
        }
        catch (RuleResolutionException ex)
        {
            Assert.fail(ex.getMessage());
        }
        test("equalslashprecedence", "%x31 %x32\r\nequalslashprecedence =/ %x33 %x34", "(?:12|34)",
             "(%x31.32 / %x33.34)");
        test("equalslashprecedence2", "3%x31 / 3%x32\r\nequalslashprecedence2 =/ 4%x33 / 4%x34", "(?:[12]{3}|[34]{4})",
             "(3(%x31 / %x32) / 4(%x33 / %x34))");
    }

    // /**
    // * Print the contents of the dictionary for inspection.
    // */
    // @org.junit.After
    // public void printDictionary()
    // {
    // this.rd.write(new PrintWriter(System.out));
    // }
}
