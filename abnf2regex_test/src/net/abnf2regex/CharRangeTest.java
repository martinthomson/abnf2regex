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
     * Test method for {@link net.abnf2regex.CharRange#buildAbnf(java.lang.StringBuilder, boolean)}.
     */
    @Test
    public void testToAbnf()
    {
        StringBuilder bld = new StringBuilder();
        Assert.assertSame(bld, new CharRange(0x40).buildAbnf(bld, false));
        Assert.assertEquals("%x40", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        new CharRange(0x40).buildAbnf(bld, true);
        Assert.assertEquals(".40", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        new CharRange(0x40, 0x38).buildAbnf(bld, false);
        Assert.assertEquals("%x40", bld.toString()); //$NON-NLS-1$
        bld.setLength(0);
        new CharRange(0x40, 0x44).buildAbnf(bld, false);
        Assert.assertEquals("%x40-44", bld.toString()); //$NON-NLS-1$
        Assert.assertEquals("%x50-64", new CharRange(0x50, 0x64).toString()); //$NON-NLS-1$
    }

    /**
     * Test that comparison works.
     */
    @Test
    public void testComparison()
    {
        Assert.assertTrue(new CharRange(0x40, 0x42).compareTo(new CharRange(0x42, 0x44)) < 0);
        Assert.assertTrue(new CharRange(0x40, 0x42).compareTo(new CharRange(0x40, 0x44)) < 0);
        Assert.assertTrue(new CharRange(0x42, 0x42).compareTo(new CharRange(0x42, 0x44)) < 0);
        Assert.assertTrue(new CharRange(0x40, 0x42).compareTo(new CharRange(0x40, 0x42)) == 0);
        Assert.assertTrue(new CharRange(0x42, 0x46).compareTo(new CharRange(0x42, 0x44)) > 0);
        Assert.assertTrue(new CharRange(0x46, 0x48).compareTo(new CharRange(0x40, 0x44)) > 0);
    }

    /**
     * Test that merging works.
     */
    @Test
    public void testMerging()
    {
        checkEquals(new CharRange(0x40, 0x44), new CharRange(0x40, 0x42).merge(new CharRange(0x42, 0x44)));
        checkEquals(new CharRange(0x40, 0x44), new CharRange(0x40, 0x41).merge(new CharRange(0x42, 0x44)));
        checkEquals(new CharRange(0x40, 0x44), new CharRange(0x42, 0x44).merge(new CharRange(0x40, 0x42)));
        checkEquals(new CharRange(0x40, 0x44), new CharRange(0x42, 0x44).merge(new CharRange(0x40, 0x41)));
        checkEquals(new CharRange(0x40, 0x44), new CharRange(0x40, 0x44).merge(new CharRange(0x40, 0x41)));
        checkEquals(new CharRange(0x40, 0x44), new CharRange(0x42, 0x44).merge(new CharRange(0x40, 0x44)));
    }

    /**
     * Check .equals();
     */
    @Test
    public void testEquals()
    {
        CharRange cr = new CharRange(0x40, 0x40);
        checkEquals(cr, cr);
        Assert.assertFalse(cr.equals(null));
        Assert.assertFalse(cr.equals(new CharRange(0x40, 0x44)));
        Assert.assertFalse(cr.equals(new CharRange(0x44, 0x40)));
        Assert.assertFalse(cr.equals("foo")); //$NON-NLS-1$
    }

    /** Not only does this check for equality, it checks that the contract for equals and hashCode is met. */
    private void checkEquals(Object a, Object b)
    {
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }
}
