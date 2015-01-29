package net.abnf2regex;

import java.io.IOException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link OccurrenceRange}.
 */
public class OccurenceRangeTest
{
    /**
     * Test method for
     * {@link net.abnf2regex.OccurrenceRange#addAbnfLeadin(java.lang.StringBuilder, boolean)}
     * .
     */
    @Test
    public void testAddAbnfLeadin()
    {
        reallyTestAbnfLeadin(new OccurrenceRange(0, 1), "[", "["); //$NON-NLS-1$ //$NON-NLS-2$
        reallyTestAbnfLeadin(new OccurrenceRange(0, -1), "*"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurrenceRange(0, 2), "*2"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurrenceRange(1, 1), ""); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurrenceRange(1, 3), "1*3"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurrenceRange(1, -2), "1*"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurrenceRange(4, 4), "4"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurrenceRange(5, -3), "5*"); //$NON-NLS-1$
    }

    /**
     * Test that an Occurence range generates the correct ABNF leading for
     * various values of occurence range. Mostly, when parentheses are needed, a
     * trailing '(' is found. this provides that.
     *
     * @param or the occurence range
     * @param woParens the expected value when parens aren't needed
     */
    private void reallyTestAbnfLeadin(OccurrenceRange or, String woParens)
    {
        this.reallyTestAbnfLeadin(or, woParens, woParens + '(');
    }

    /**
     * Test that an Occurence range generates the correct ABNF leading for
     * various values of occurence range.
     *
     * @param or the occurence range
     * @param woParens the expected value when parens aren't needed
     * @param wParens the expected value when parens are needed
     */
    private void reallyTestAbnfLeadin(OccurrenceRange or, String woParens, String wParens)
    {
        StringBuilder bld = new StringBuilder();
        Assert.assertSame(bld, or.addAbnfLeadin(bld, false));
        Assert.assertEquals(woParens, bld.toString());
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfLeadin(bld, true));
        Assert.assertEquals(wParens, bld.toString());
    }

    /**
     * Test method for
     * {@link net.abnf2regex.OccurrenceRange#addAbnfTrail(java.lang.StringBuilder, boolean)}
     * .
     */
    @Test
    public void testAddAbnfTrail()
    {
        StringBuilder bld = new StringBuilder();

        // test that for 0..1 we always get a close bracket
        OccurrenceRange or = new OccurrenceRange(0, 1);
        Assert.assertSame(bld, or.addAbnfTrail(bld, false));
        Assert.assertEquals("]", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfTrail(bld, true));
        Assert.assertEquals("]", bld.toString()); //$NON-NLS-1$

        // test that for anything else, needParens determines if there are close
        // parentheses
        or = new OccurrenceRange(1, 2);
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfTrail(bld, false));
        Assert.assertEquals("", bld.toString()); //$NON-NLS-1$
        or = new OccurrenceRange(3, -4);
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfTrail(bld, true));
        Assert.assertEquals(")", bld.toString()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link net.abnf2regex.OccurrenceRange#isOnce()}.
     */
    @Test
    public void testIsOneOnly()
    {
        Assert.assertTrue(new OccurrenceRange(1, 1).isOnce());
        Assert.assertTrue(OccurrenceRange.ONCE.isOnce());
        Assert.assertFalse(new OccurrenceRange(1, 2).isOnce());
        Assert.assertFalse(new OccurrenceRange(0, 1).isOnce());
        Assert.assertFalse(new OccurrenceRange(3, -5).isOnce());
    }

    /**
     * Test method for
     * {@link net.abnf2regex.OccurrenceRange#getRegexOccurences()}.
     */
    @Test
    public void testGetRegexOccurences()
    {
        Assert.assertEquals("?", new OccurrenceRange(0, 1).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("*", new OccurrenceRange(0, -1).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{0,2}", new OccurrenceRange(0, 2).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("", new OccurrenceRange(1, 1).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{1,3}", new OccurrenceRange(1, 3).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("+", new OccurrenceRange(1, -6).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{4}", new OccurrenceRange(4, 4).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{5,}", new OccurrenceRange(5, -7).getRegexOccurences()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link net.abnf2regex.OccurrenceRange#getMin()}.
     */
    @Test
    public void testGetMinMax()
    {
        OccurrenceRange or = new OccurrenceRange(2, 3);
        Assert.assertEquals(Integer.valueOf(2), Integer.valueOf(or.getMin()));
        Assert.assertEquals(Integer.valueOf(3), Integer.valueOf(or.getMax()));
    }

    /**
     * Check {@link net.abnf2regex.OccurrenceRange#add(OccurrenceRange)} method.
     */
    @Test
    public void testAdd()
    {
        Assert.assertEquals(new OccurrenceRange(4, 5), new OccurrenceRange(1, 2).add(new OccurrenceRange(3, 3)));
        Assert.assertEquals(new OccurrenceRange(4, OccurrenceRange.UNBOUNDED), new OccurrenceRange(1,
                OccurrenceRange.UNBOUNDED).add(new OccurrenceRange(3, 3)));
        Assert.assertEquals(new OccurrenceRange(4, OccurrenceRange.UNBOUNDED),
                            new OccurrenceRange(1, 2).add(new OccurrenceRange(3, OccurrenceRange.UNBOUNDED)));
    }

    /**
     * Check {@link net.abnf2regex.OccurrenceRange#multiply(OccurrenceRange)}
     * method.
     */
    @Test
    public void testMultiply()
    {
        Assert.assertEquals(new OccurrenceRange(6, 6), new OccurrenceRange(2, 2).multiply(new OccurrenceRange(3, 3)));
        Assert.assertEquals(new OccurrenceRange(4, OccurrenceRange.UNBOUNDED), new OccurrenceRange(1,
                OccurrenceRange.UNBOUNDED).multiply(new OccurrenceRange(4, OccurrenceRange.UNBOUNDED)));
        Assert.assertEquals(new OccurrenceRange(0, OccurrenceRange.UNBOUNDED), new OccurrenceRange(1,
                OccurrenceRange.UNBOUNDED).multiply(new OccurrenceRange(0, 1)));
        Assert.assertEquals(new OccurrenceRange(4, OccurrenceRange.UNBOUNDED), new OccurrenceRange(4,
                OccurrenceRange.UNBOUNDED).multiply(new OccurrenceRange(1, OccurrenceRange.UNBOUNDED)));
        Assert.assertEquals(new OccurrenceRange(2, 3), OccurrenceRange.ONCE.multiply(new OccurrenceRange(2, 3)));
        Assert.assertEquals(new OccurrenceRange(4, 5), new OccurrenceRange(4, 5).multiply(OccurrenceRange.ONCE));
        Assert.assertEquals(new OccurrenceRange(2, OccurrenceRange.UNBOUNDED),
                            OccurrenceRange.ONCE.multiply(new OccurrenceRange(2, OccurrenceRange.UNBOUNDED)));
        Assert.assertEquals(new OccurrenceRange(4, OccurrenceRange.UNBOUNDED), new OccurrenceRange(4,
                OccurrenceRange.UNBOUNDED).multiply(OccurrenceRange.ONCE));
        Assert.assertNull(new OccurrenceRange(1, 2).multiply(new OccurrenceRange(3, 3)));
        Assert.assertNull(new OccurrenceRange(2, OccurrenceRange.UNBOUNDED).multiply(new OccurrenceRange(3,
                OccurrenceRange.UNBOUNDED)));
        Assert.assertNull(new OccurrenceRange(2, OccurrenceRange.UNBOUNDED).multiply(new OccurrenceRange(3,
                OccurrenceRange.UNBOUNDED)));
    }

    /**
     * Test method for
     * {@link net.abnf2regex.OccurrenceRange#parse(net.abnf2regex.AbnfReader)}.
     */
    @Test
    public void testParse()
    {
        try
        {
            reallyTestParse(1, 1, false, false, false); // - no occurence
                                                        // modifier
            reallyTestParse(0, -1, false, true, false); // *
            reallyTestParse(0, 1, false, true, true); // *1 - alternative to
                                                      // [rule]
            reallyTestParse(0, 2, false, true, true); // *2
            reallyTestParse(1, 3, true, true, true); // 1*3
            reallyTestParse(4, 4, true, false, false); // 4
            reallyTestParse(5, -2, true, true, false); // 5*
        }
        catch (IOException ex)
        {
            Assert.fail("Unexpected exception: " + ex); //$NON-NLS-1$
        }
    }

    /**
     * Test a single parse.
     *
     * @param min the expected minimum value
     * @param max the expected maximum value
     * @param minPresent true if a minimum value is explicitly indicated
     * @param starPresent true if there is no star, either when there is no
     *            occurences modifier, or if min==max
     * @param maxPresent true if the end of the string is indicated
     * @throws IOException
     */
    private void reallyTestParse(int min, int max, boolean minPresent, boolean starPresent, boolean maxPresent)
            throws IOException
    {
        AbnfReader reader = EasyMock.createMock(AbnfReader.class);
        if (minPresent)
        {
            testParseNumber(reader, min);
        }
        else
        {
            EasyMock.expect(Integer.valueOf(reader.peek())).andReturn(Integer.valueOf('?'));
        }
        if (starPresent)
        {
            EasyMock.expect(Integer.valueOf(reader.peek())).andReturn(Integer.valueOf('*'));
            EasyMock.expect(Integer.valueOf(reader.read())).andReturn(Integer.valueOf('*'));
        }
        else
        {
            EasyMock.expect(Integer.valueOf(reader.peek())).andReturn(Integer.valueOf('?'));
        }
        if (maxPresent)
        {
            testParseNumber(reader, max);
        }
        else if (starPresent)
        {
            EasyMock.expect(Integer.valueOf(reader.peek())).andReturn(Integer.valueOf('?'));
        }

        EasyMock.replay(reader);
        OccurrenceRange or = OccurrenceRange.parse(reader);
        Assert.assertEquals(new OccurrenceRange(min, max), or);
        EasyMock.verify(reader);
    }

    /**
     * Provide the expectations for reading of a number.
     *
     * @param reader the reader mock
     * @param num the number to provide
     * @throws IOException never, actually, but no sense catching it here
     */
    private void testParseNumber(AbnfReader reader, int num) throws IOException
    {
        Integer codePoint = Integer.valueOf(Integer.toString(num).codePointAt(0));
        EasyMock.expect(Integer.valueOf(reader.peek())).andReturn(codePoint);
        EasyMock.expect(Integer.valueOf(reader.parseNumber())).andReturn(Integer.valueOf(num));
    }

    /**
     * Test method for
     * {@link net.abnf2regex.OccurrenceRange#equals(java.lang.Object)}.
     */
    @Test
    public void testEquals()
    {
        // junitx.framework.Assert.assertEqualsHashCodeSymbiotic(OccurenceRange.class);

        // simple comparison
        Assert.assertTrue(new OccurrenceRange(4, 5).equals(new OccurrenceRange(4, 5)));
        Assert.assertTrue(new OccurrenceRange(4, 5).hashCode() == new OccurrenceRange(4, 5).hashCode());

        // any negative produces the same result
        Assert.assertTrue(new OccurrenceRange(6, -1).equals(new OccurrenceRange(6, -8)));
        Assert.assertTrue(new OccurrenceRange(6, -1).hashCode() == new OccurrenceRange(6, -8).hashCode());

        // if max < min, range is turned min..min
        Assert.assertTrue(new OccurrenceRange(6, 6).equals(new OccurrenceRange(6, 3)));
        Assert.assertTrue(new OccurrenceRange(6, 6).hashCode() == new OccurrenceRange(6, 3).hashCode());

        // misc tests
        Assert.assertFalse(new OccurrenceRange(6, 7).equals(new OccurrenceRange(6, 8)));
        Assert.assertFalse(new OccurrenceRange(9, 10).equals(new OccurrenceRange(11, 10)));
        Assert.assertFalse(new OccurrenceRange(12, 12).equals("hello world")); //$NON-NLS-1$
    }

    /**
     * Check {@link net.abnf2regex.OccurrenceRange#toString()} method.
     */
    @Test
    public void testToString()
    {
        Assert.assertEquals("4-5", new OccurrenceRange(4, 5).toString()); //$NON-NLS-1$
        Assert.assertEquals("4+", new OccurrenceRange(4, -5).toString()); //$NON-NLS-1$
    }

}
