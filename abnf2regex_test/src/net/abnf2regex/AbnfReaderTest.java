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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

import junit.framework.Assert;
import junitx.framework.AccessProxy;
import junitx.framework.TestAccessException;

import org.easymock.EasyMock;
import org.easymock.classextension.EasyClassMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link AbnfReader}
 */
public class AbnfReaderTest
{
    private Reader mockReader;
    private AbnfReader abnf;

    /**
     * Build mocks.
     */
    @Before
    public void setUp()
    {
        this.mockReader = EasyClassMock.createMock(Reader.class);
        this.abnf = new AbnfReader(this.mockReader);
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#AbnfReader(java.io.Reader)}.
     */
    @Test
    public void testInstantiation()
    {
        AccessProxy getIn = new AccessProxy(FilterReader.class);
        try
        {
            Assert.assertSame(this.mockReader, getIn.get(this.abnf, "in")); //$NON-NLS-1$
        }
        catch (TestAccessException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#peek()}.
     */
    @Test
    public void testPeek()
    {
        try
        {
            final int val = 42;
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(val));
            EasyClassMock.replay(this.mockReader);

            Assert.assertEquals(val, this.abnf.peek());
            // repeats shouldn't hit the wrapped reader
            Assert.assertEquals(val, this.abnf.peek());
            EasyClassMock.verify(this.mockReader);
        }
        catch (IOException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#read()}.
     */
    @Test
    public void testRead()
    {
        try
        {
            final int val1 = 42;
            final int val2 = 9;
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(val1));
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(val2));
            EasyClassMock.replay(this.mockReader);

            Assert.assertEquals(val1, this.abnf.read());

            // prime the peeked value
            Assert.assertEquals(val2, this.abnf.peek());

            // no more calls allowed, reset and see what happens.
            EasyClassMock.verify(this.mockReader);
            EasyClassMock.reset(this.mockReader);
            EasyClassMock.replay(this.mockReader);

            Assert.assertEquals(val2, this.abnf.read());

            EasyClassMock.verify(this.mockReader);
        }
        catch (IOException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#eof()}.
     */
    @Test
    public void testEof()
    {
        try
        {
            final int val1 = 42;
            final int val2 = -1; // end of file!
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(val1));
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(val2));
            EasyClassMock.replay(this.mockReader);

            // not eof:
            Assert.assertEquals(val1, this.abnf.peek());
            Assert.assertFalse(this.abnf.eof());

            // next character:
            Assert.assertEquals(val1, this.abnf.read());

            // eof:
            Assert.assertEquals(val2, this.abnf.peek());
            Assert.assertTrue(this.abnf.eof());

            EasyClassMock.verify(this.mockReader);
        }
        catch (IOException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#gobbleWhitespace()}.
     */
    @Test
    public void testGobbleWhitespace()
    {
        try
        {
            reallyTestGobbleWhitespace("\r"); //$NON-NLS-1$
            reallyTestGobbleWhitespace(" \n"); //$NON-NLS-1$
            reallyTestGobbleWhitespace("\t x"); //$NON-NLS-1$
            reallyTestGobbleWhitespace("\t\r"); //$NON-NLS-1$
        }
        catch (IOException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * @param input must have only one non-whitespace character at the end
     * @throws IOException never, really
     */
    private void reallyTestGobbleWhitespace(String input) throws IOException
    {
        for (int i = 0; i < input.length(); ++i)
        {
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(input.charAt(i)));
        }
        EasyClassMock.replay(this.mockReader);
        Assert.assertEquals(input.length() - 1, this.abnf.gobbleWhitespace());
        this.abnf.read(); // skip the non-whitespace character in preparation for the next case
        EasyClassMock.verify(this.mockReader);
        EasyClassMock.reset(this.mockReader);
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#findNextLine()}.
     */
    @Test
    public void testFindNextLine()
    {
        try
        {
            this.reallyTestFindNextLine("\nx"); //$NON-NLS-1$
            this.reallyTestFindNextLine("abc\r\\"); //$NON-NLS-1$
            this.reallyTestFindNextLine("abc\r\n\t"); //$NON-NLS-1$

            reallyTestFindNextLineEof('b');
            reallyTestFindNextLineEof('\n');
            reallyTestFindNextLineEof('\r');
        }
        catch (IOException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test that {@link net.abnf2regex.AbnfReader#findNextLine()} can find the next line for '\n', '\r' and "\r\n".
     *
     * @param input the string to test, the last character must be the first character after a newline
     * @throws IOException never, really
     */
    private void reallyTestFindNextLine(String input) throws IOException
    {
        // should return the first character at the next line (the last character)
        for (int i = 0; i < input.length(); ++i)
        {
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(input.charAt(i)));
        }
        EasyClassMock.replay(this.mockReader);
        this.abnf.findNextLine();
        Assert.assertEquals(input.charAt(input.length() - 1), this.abnf.read());
        EasyClassMock.verify(this.mockReader);
        EasyClassMock.reset(this.mockReader);
    }

    /**
     * Test that {@link net.abnf2regex.AbnfReader#findNextLine()} doesn't barf when it hits the end of file.
     *
     * @param ch the character that comes before the eof character
     * @throws IOException never, really
     */
    private void reallyTestFindNextLineEof(char ch) throws IOException
    {
        EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(ch));
        EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(-1));
        EasyClassMock.replay(this.mockReader);
        this.abnf.findNextLine();
        Assert.assertEquals(-1, this.abnf.read());
        EasyClassMock.verify(this.mockReader);
        EasyClassMock.reset(this.mockReader);
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#parseName()}.
     */
    @Test
    public void testParseName()
    {
        try
        {
            this.reallyTestParseName("name", ' '); //$NON-NLS-1$
            this.reallyTestParseName("-n-am-e-", '='); //$NON-NLS-1$
            this.reallyTestParseName("_", '\t'); //$NON-NLS-1$
            // and check for eof
            this.reallyTestParseName("name", -1); //$NON-NLS-1$
        }
        catch (IOException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test that {@link net.abnf2regex.AbnfReader#parseName()} works.
     *
     * @param input the string to test, the last character must be the first character after a newline
     * @param nextChar the character encounted after the name (not a letter or digit and not '-' or '_')
     * @throws IOException never, really
     */
    private void reallyTestParseName(String input, int nextChar) throws IOException
    {
        // should return the first character at the next line (the last character)
        for (int i = 0; i < input.length(); ++i)
        {
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(input.charAt(i)));
        }
        EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(nextChar));
        EasyClassMock.replay(this.mockReader);
        Assert.assertEquals(input, this.abnf.parseName());
        Assert.assertEquals(nextChar, this.abnf.read());
        EasyClassMock.verify(this.mockReader);
        EasyClassMock.reset(this.mockReader);
    }

    /**
     * Test method for {@link net.abnf2regex.AbnfReader#parseNumber()}.
     */
    @Test
    public void testParseNumber()
    {
        try
        {
            this.reallyTestParseNumber(12, 10,' ');
            this.reallyTestParseNumber(3, 4, '=');
            this.reallyTestParseNumber(99, 16, '\t');
            // and check for eof too
            this.reallyTestParseNumber(8, 12, -1);

            // no number means zero
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(-1));
            EasyClassMock.replay(this.mockReader);
            Assert.assertEquals(0, this.abnf.parseNumber());
            Assert.assertEquals(-1, this.abnf.read());
            EasyClassMock.verify(this.mockReader);
            EasyClassMock.reset(this.mockReader);
        }
        catch (IOException ex)
        {
            Assert.fail(ex.toString());
        }
    }

    /**
     * Test that {@link net.abnf2regex.AbnfReader#parseNumber()} works.
     *
     * @param number the number to test, the last character must be the first character after a newline
     * @param nextChar the character encounted after the name (not a letter or digit and not '-' or '_')
     * @throws IOException never, really
     */
    private void reallyTestParseNumber(int number, int radix, int nextChar) throws IOException
    {
        String input = Integer.toString(number, radix);
        // should return the first character at the next line (the last character)
        for (int i = 0; i < input.length(); ++i)
        {
            EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(input.charAt(i)));
        }
        EasyMock.expect(Integer.valueOf(this.mockReader.read())).andReturn(Integer.valueOf(nextChar));
        EasyClassMock.replay(this.mockReader);
        Assert.assertEquals(number, this.abnf.parseNumber(radix));
        Assert.assertEquals(nextChar, this.abnf.read());
        EasyClassMock.verify(this.mockReader);
        EasyClassMock.reset(this.mockReader);
    }


}
