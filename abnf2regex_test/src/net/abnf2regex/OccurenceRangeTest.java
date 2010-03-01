package net.abnf2regex;

import java.io.IOException;

import org.easymock.EasyMock;
import org.easymock.classextension.EasyClassMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link OccurenceRange}.
 */
public class OccurenceRangeTest
{
    /**
     * Test method for {@link net.abnf2regex.OccurenceRange#addAbnfLeadin(java.lang.StringBuilder, boolean)}.
     */
    @Test
    public void testAddAbnfLeadin()
    {
        reallyTestAbnfLeadin(new OccurenceRange(0, 1), "[", "["); //$NON-NLS-1$ //$NON-NLS-2$
        reallyTestAbnfLeadin(new OccurenceRange(0, -1), "*"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurenceRange(0, 2), "*2"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurenceRange(1, 1), ""); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurenceRange(1, 3), "1*3"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurenceRange(1, -2), "1*"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurenceRange(4, 4), "4"); //$NON-NLS-1$
        reallyTestAbnfLeadin(new OccurenceRange(5, -3), "5*"); //$NON-NLS-1$
    }

    /**
     * Test that an Occurence range generates the correct ABNF leading for various values of occurence range. Mostly,
     * when parentheses are needed, a trailing '(' is found. this provides that.
     *
     * @param or the occurence range
     * @param woParens the expected value when parens aren't needed
     */
    private void reallyTestAbnfLeadin(OccurenceRange or, String woParens)
    {
        this.reallyTestAbnfLeadin(or, woParens, woParens + '(');
    }

    /**
     * Test that an Occurence range generates the correct ABNF leading for various values of occurence range.
     *
     * @param or the occurence range
     * @param woParens the expected value when parens aren't needed
     * @param wParens the expected value when parens are needed
     */
    private void reallyTestAbnfLeadin(OccurenceRange or, String woParens, String wParens)
    {
        StringBuilder bld = new StringBuilder();
        Assert.assertSame(bld, or.addAbnfLeadin(bld, false));
        Assert.assertEquals(woParens, bld.toString());
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfLeadin(bld, true));
        Assert.assertEquals(wParens, bld.toString());
    }

    /**
     * Test method for {@link net.abnf2regex.OccurenceRange#addAbnfTrail(java.lang.StringBuilder, boolean)}.
     */
    @Test
    public void testAddAbnfTrail()
    {
        StringBuilder bld = new StringBuilder();

        // test that for 0..1 we always get a close bracket
        OccurenceRange or = new OccurenceRange(0, 1);
        Assert.assertSame(bld, or.addAbnfTrail(bld, false));
        Assert.assertEquals("]", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfTrail(bld, true));
        Assert.assertEquals("]", bld.toString()); //$NON-NLS-1$

        // test that for anything else, needParens determines if there are close parentheses
        or = new OccurenceRange(1, 2);
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfTrail(bld, false));
        Assert.assertEquals("", bld.toString()); //$NON-NLS-1$
        or = new OccurenceRange(3, -4);
        bld.setLength(0);
        Assert.assertSame(bld, or.addAbnfTrail(bld, true));
        Assert.assertEquals(")", bld.toString()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link net.abnf2regex.OccurenceRange#isOnce()}.
     */
    @Test
    public void testIsOneOnly()
    {
        Assert.assertTrue(new OccurenceRange(1, 1).isOnce());
        Assert.assertTrue(OccurenceRange.ONCE.isOnce());
        Assert.assertFalse(new OccurenceRange(1, 2).isOnce());
        Assert.assertFalse(new OccurenceRange(0, 1).isOnce());
        Assert.assertFalse(new OccurenceRange(3, -5).isOnce());
    }

    /**
     * Test method for {@link net.abnf2regex.OccurenceRange#getRegexOccurences()}.
     */
    @Test
    public void testGetRegexOccurences()
    {
        Assert.assertEquals("?", new OccurenceRange(0, 1).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("*", new OccurenceRange(0, -1).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{0,2}", new OccurenceRange(0, 2).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("", new OccurenceRange(1, 1).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{1,3}", new OccurenceRange(1, 3).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("+", new OccurenceRange(1, -6).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{4}", new OccurenceRange(4, 4).getRegexOccurences()); //$NON-NLS-1$
        Assert.assertEquals("{5,}", new OccurenceRange(5, -7).getRegexOccurences()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link net.abnf2regex.OccurenceRange#getMin()}.
     */
    @Test
    public void testGetMinMax()
    {
        OccurenceRange or = new OccurenceRange(2, 3);
        Assert.assertEquals(Integer.valueOf(2), Integer.valueOf(or.getMin()));
        Assert.assertEquals(Integer.valueOf(3), Integer.valueOf(or.getMax()));
    }

    /**
     * Check {@link net.abnf2regex.OccurenceRange#add(OccurenceRange)} method.
     */
    @Test
    public void testAdd()
    {
        Assert.assertEquals(new OccurenceRange(4, 5), new OccurenceRange(1, 2).add(new OccurenceRange(3, 3)));
        Assert.assertEquals(new OccurenceRange(4, OccurenceRange.UNBOUNDED),
                            new OccurenceRange(1, OccurenceRange.UNBOUNDED).add(new OccurenceRange(3, 3)));
        Assert.assertEquals(new OccurenceRange(4, OccurenceRange.UNBOUNDED), new OccurenceRange(1, 2)
                        .add(new OccurenceRange(3, OccurenceRange.UNBOUNDED)));
    }

    /**
     * Check {@link net.abnf2regex.OccurenceRange#multiply(OccurenceRange)} method.
     */
    @Test
    public void testMultiply()
    {
        Assert.assertEquals(new OccurenceRange(6, 6), new OccurenceRange(2, 2).multiply(new OccurenceRange(3, 3)));
        Assert.assertEquals(new OccurenceRange(4, OccurenceRange.UNBOUNDED),
                            new OccurenceRange(1, OccurenceRange.UNBOUNDED)
                                            .multiply(new OccurenceRange(4, OccurenceRange.UNBOUNDED)));
        Assert.assertEquals(new OccurenceRange(4, OccurenceRange.UNBOUNDED),
                            new OccurenceRange(4, OccurenceRange.UNBOUNDED)
                                            .multiply(new OccurenceRange(1, OccurenceRange.UNBOUNDED)));
        Assert.assertEquals(new OccurenceRange(2, 3), OccurenceRange.ONCE.multiply(new OccurenceRange(2, 3)));
        Assert.assertEquals(new OccurenceRange(4, 5), new OccurenceRange(4, 5).multiply(OccurenceRange.ONCE));
        Assert.assertEquals(new OccurenceRange(2, OccurenceRange.UNBOUNDED), OccurenceRange.ONCE
                        .multiply(new OccurenceRange(2, OccurenceRange.UNBOUNDED)));
        Assert.assertEquals(new OccurenceRange(4, OccurenceRange.UNBOUNDED),
                            new OccurenceRange(4, OccurenceRange.UNBOUNDED).multiply(OccurenceRange.ONCE));
        Assert.assertNull(new OccurenceRange(1, 2).multiply(new OccurenceRange(3, 3)));
        Assert.assertNull(new OccurenceRange(2, OccurenceRange.UNBOUNDED)
                        .multiply(new OccurenceRange(3, OccurenceRange.UNBOUNDED)));
        Assert.assertNull(new OccurenceRange(2, OccurenceRange.UNBOUNDED)
                        .multiply(new OccurenceRange(3, OccurenceRange.UNBOUNDED)));
    }

    /**
     * Test method for {@link net.abnf2regex.OccurenceRange#parse(net.abnf2regex.AbnfReader)}.
     */
    @Test
    public void testParse()
    {
        try
        {
            reallyTestParse(1, 1, false, false, false); // - no occurence modifier
            reallyTestParse(0, -1, false, true, false); // *
            reallyTestParse(0, 1, false, true, true); // *1 - alternative to [rule]
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
     * @param starPresent true if there is no star, either when there is no occurences modifier, or if min==max
     * @param maxPresent true if the end of the string is indicated
     * @throws IOException
     */
    private void reallyTestParse(int min, int max, boolean minPresent, boolean starPresent, boolean maxPresent)
                    throws IOException
    {
        AbnfReader reader = EasyClassMock.createMock(AbnfReader.class);
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

        EasyClassMock.replay(reader);
        OccurenceRange or = OccurenceRange.parse(reader);
        Assert.assertEquals(new OccurenceRange(min, max), or);
        EasyClassMock.verify(reader);
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
     * Test method for {@link net.abnf2regex.OccurenceRange#equals(java.lang.Object)}.
     */
    @Test
    public void testEquals()
    {
        junitx.framework.Assert.assertEqualsHashCodeSymbiotic(OccurenceRange.class);

        // simple comparison
        Assert.assertTrue(new OccurenceRange(4, 5).equals(new OccurenceRange(4, 5)));
        Assert.assertTrue(new OccurenceRange(4, 5).hashCode() == new OccurenceRange(4, 5).hashCode());

        // any negative produces the same result
        Assert.assertTrue(new OccurenceRange(6, -1).equals(new OccurenceRange(6, -8)));
        Assert.assertTrue(new OccurenceRange(6, -1).hashCode() == new OccurenceRange(6, -8).hashCode());

        // if max < min, range is turned min..min
        Assert.assertTrue(new OccurenceRange(6, 6).equals(new OccurenceRange(6, 3)));
        Assert.assertTrue(new OccurenceRange(6, 6).hashCode() == new OccurenceRange(6, 3).hashCode());

        // misc tests
        Assert.assertFalse(new OccurenceRange(6, 7).equals(new OccurenceRange(6, 8)));
        Assert.assertFalse(new OccurenceRange(9, 10).equals(new OccurenceRange(11, 10)));
        Assert.assertFalse(new OccurenceRange(12, 12).equals("hello world")); //$NON-NLS-1$
    }

    /**
     * Check {@link net.abnf2regex.OccurenceRange#toString()} method.
     */
    @Test
    public void testToString()
    {
        Assert.assertEquals("4-5", new OccurenceRange(4, 5).toString()); //$NON-NLS-1$
        Assert.assertEquals("4+", new OccurenceRange(4, -5).toString()); //$NON-NLS-1$
    }

}
