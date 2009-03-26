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

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests for {@link CharRange}.
 */
public class CharRangeTest
{
    /**
     * Test method for {@link net.abnf2regex.CharRange#CharRange(int)}.
     */
    @Test
    public void testCharRangeInstantiation()
    {
        CharRange cr = new CharRange(1, 2);
        Assert.assertEquals(1, cr.getStart());
        Assert.assertEquals(2, cr.getEnd());

        cr = new CharRange(3);
        Assert.assertEquals(3, cr.getStart());
        Assert.assertEquals(3, cr.getEnd());

        cr.setEnd(4);
        Assert.assertEquals(4, cr.getEnd());
    }

    /**
     * Test method for {@link net.abnf2regex.CharRange#toAbnf(java.lang.StringBuilder, boolean)}.
     */
    @Test
    public void testToAbnf()
    {
        StringBuilder bld = new StringBuilder();
        Assert.assertSame(bld, new CharRange(0x40).toAbnf(bld, true));
        Assert.assertEquals("%x40", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        new CharRange(0x40).toAbnf(bld, false);
        Assert.assertEquals(".40", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        new CharRange(0x40, 0x38).toAbnf(bld, true);
        Assert.assertEquals("%x40", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        new CharRange(0x40, 0x44).toAbnf(bld, true);
        Assert.assertEquals("%x40-44", bld.toString()); //$NON-NLS-1$
        Assert.assertEquals("%x50-64", new CharRange(0x50, 0x64).toString()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link net.abnf2regex.CharRange#toRegex()}.
     */
    @Test
    public void testToRegex()
    {
        Assert.assertEquals("\\d", new CharRange('0', '9').toRegex()); //$NON-NLS-1$
        Assert.assertEquals("[c-q]", new CharRange('c', 'q').toRegex()); //$NON-NLS-1$
        Assert.assertEquals("\\t", new CharRange('\t').toRegex()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link net.abnf2regex.CharRange#regexChar(int)}.
     */
    @Test
    public void testRegexChar()
    {
        char[] dodgy = new char[] { '.', '\\', '?', '*', '+', '(', ')', '|', '[', ']', '-' };
        for (char d : dodgy)
        {
            Assert.assertEquals("\\" + d, CharRange.regexChar(d)); //$NON-NLS-1$
        }
        Assert.assertEquals("\\t", CharRange.regexChar('\t')); //$NON-NLS-1$
        Assert.assertEquals("\\n", CharRange.regexChar('\n')); //$NON-NLS-1$
        Assert.assertEquals("\\r", CharRange.regexChar('\r')); //$NON-NLS-1$
        Assert.assertEquals("a", CharRange.regexChar('a')); //$NON-NLS-1$
        Assert.assertEquals("\\x04", CharRange.regexChar(4)); //$NON-NLS-1$
        Assert.assertEquals("\\xe7", CharRange.regexChar(0xe7)); //$NON-NLS-1$
        Assert.assertEquals("\\u0909", CharRange.regexChar(0x909)); //$NON-NLS-1$
        Assert.assertEquals("\\u1aaa", CharRange.regexChar(0x1aaa)); //$NON-NLS-1$
    }
}
