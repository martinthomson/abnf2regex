package net.abnf2regex;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link RegexSyntax}
 */
@SuppressWarnings("nls")
public class TestRegexSyntax
{
    private RegexSyntax syntax;

    /**
     * Setup the syntax.
     */
    @Before
    public void setup()
    {
        try
        {
            RegexSyntax.setCurrent(RegexSyntax.SYNTAX_JAVA);
        }
        catch (RegexSyntaxNotFoundException ex)
        {
            Assert.fail(ex.getMessage());
        }
        this.syntax = RegexSyntax.getCurrent();
    }

    /**
     * Test method for {@link net.abnf2regex.RegexSyntax#character(int)}.
     */
    @Test
    public void testSingleCharacter()
    {
        char[] dodgy = new char[] { '.', '\\', '?', '*', '+', '(', ')', '|', '[', ']', '-' };
        for (char d : dodgy)
        {
            Assert.assertEquals("\\" + d, syntax.character(d));
        }
        Assert.assertEquals("\\t", syntax.character('\t'));
        Assert.assertEquals("\\n", syntax.character('\n'));
        Assert.assertEquals("\\r", syntax.character('\r'));
        Assert.assertEquals("a", syntax.character('a'));
        Assert.assertEquals("\\x04", syntax.character(4));
        Assert.assertEquals("\\xe7", syntax.character(0xe7));
        Assert.assertEquals("\\u0909", syntax.character(0x909));
        Assert.assertEquals("\\u1aaa", syntax.character(0x1aaa));
    }

    /**
     * Test method for {@link net.abnf2regex.RegexSyntax#range(CharRange)}.
     */
    @Test
    public void testCharacterRange()
    {
        Assert.assertEquals("\\d", syntax.range(new CharRange('0', '9')));
        Assert.assertEquals("[c-q]", syntax.range(new CharRange('c', 'q')));
        Assert.assertEquals("\\t", syntax.range(new CharRange('\t')));
    }

}
