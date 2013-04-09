package net.abnf2regex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.abnf2regex.easymock.Capture;
import net.abnf2regex.easymock.EasyMockHelper;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RuleFragment}.
 */
public class RuleFragmentTest
{
    private RuleFragment rf;
    private OccurenceRange or;

    /**
     * Build a default {@link RuleFragment}
     */
    @Before
    public void beforeAll()
    {
        this.rf = EasyMockHelper.fillAbstractWithMock(RuleFragment.class);
        this.or = EasyMockHelper.createCompleteMock(OccurenceRange.class);
        this.rf.setOccurences(this.or);
    }

    /**
     * Test instantiation.
     */
    @Test
    public void testInstantiation()
    {
        RuleFragment fragment = new RuleFragment()
        {
            @Override
            public boolean append(RuleFragment frag)
            {
                return false;
            }

            @Override
            protected StringBuilder buildAbnf(StringBuilder bld)
            {
                return null;
            }

            @Override
            protected void buildRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
            {
                // NOP
            }

            @Override
            public Object clone()
            {
                return null;
            }
        };
        Assert.assertTrue(fragment.getOccurences().isOnce());
    }

    /**
     * Test that getting and setting occurences work.
     */
    @Test
    public void testGetSetOccurences()
    {
        EasyMock.replay(this.rf, this.or);
        Assert.assertSame(this.or, this.rf.getOccurences());
        EasyMock.verify(this.rf, this.or);
    }

    /**
     * Test whether or not parentheses are needed, for this class, this depends on the occurence range.
     */
    @Test
    public void testNeedsParens()
    {
        EasyMock.expect(Boolean.valueOf(this.or.isOnce())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(this.or.isOnce())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(this.or.isOnce())).andReturn(Boolean.FALSE);
        EasyMock.expect(Boolean.valueOf(this.or.isOnce())).andReturn(Boolean.FALSE);
        EasyMock.replay(this.rf, this.or);

        Assert.assertFalse(this.rf.needsAbnfParens());
        Assert.assertFalse(this.rf.needsRegexParens());

        Assert.assertTrue(this.rf.needsAbnfParens());
        Assert.assertTrue(this.rf.needsRegexParens());
        EasyMock.verify(this.rf, this.or);
    }

    /**
     * Tests the toAbnf/toString methods, which should be the same.
     */
    @Test
    public void testToAbnfOrString()
    {
        List<Capture<StringBuilder>> sameStringBufList = new ArrayList<Capture<StringBuilder>>();
        EasyMock.checkOrder(this.or, true);

        for (int i = 0; i < 2; ++i)
        {
            Capture<StringBuilder> sameStringBuf = new Capture<StringBuilder>();
            sameStringBufList.add(sameStringBuf);

            EasyMock.expect(Boolean.valueOf(this.or.isOnce())).andReturn(Boolean.TRUE);
            EasyMock.expect(this.or.addAbnfLeadin(sameStringBuf.capture(), EasyMock.eq(false))).andAnswer(sameStringBuf);
            EasyMock.expect(this.rf.buildAbnf(sameStringBuf.capture())).andAnswer(sameStringBuf);
            EasyMock.expect(this.or.addAbnfTrail(sameStringBuf.capture(), EasyMock.eq(false))).andAnswer(sameStringBuf);
        }
        EasyMock.replay(this.rf, this.or);

        String abnf = this.rf.toAbnf();
        Assert.assertEquals(sameStringBufList.get(0).getValue().toString(), abnf);

        String str = this.rf.toString();
        Assert.assertEquals(sameStringBufList.get(1).getValue().toString(), str);
        Assert.assertEquals(str, abnf);

        EasyMock.verify(this.rf, this.or);
    }

    /**
     * Test that writing a regex calls the appropriate methods.
     */
    @Test
    public void testWriteRegexSingle()
    {
        try
        {
            PrintWriter pw = EasyMock.createMock(PrintWriter.class);
            Set<String> usedNames = new HashSet<String>();
            String regOccurs = "{n}"; //$NON-NLS-1$

            EasyMock.expect(Boolean.valueOf(this.or.isOnce())).andReturn(Boolean.TRUE).times(1, 2);
            EasyMock.expect(this.or.getRegexOccurences()).andReturn(regOccurs);
            this.rf.buildRegex(pw, usedNames);
            pw.print(regOccurs);

            EasyMock.replay(this.rf, this.or, pw);

            this.rf.writeRegex(pw, usedNames);

            EasyMock.verify(this.rf, this.or, pw);
        }
        catch (RuleResolutionException ex1)
        {
            Assert.fail();
        }
    }

    /**
     * Test that writing a regex calls the appropriate methods.
     */
    @Test
    public void testWriteRegexMultiple()
    {
        try
        {
            PrintWriter pw = EasyMock.createMock(PrintWriter.class);
            Set<String> usedNames = new HashSet<String>();
            String regOccurs = "{n}"; //$NON-NLS-1$

            EasyMock.expect(Boolean.valueOf(this.or.isOnce())).andReturn(Boolean.FALSE).times(1, 2);
            pw.print("(?:"); //$NON-NLS-1$
            this.rf.buildRegex(pw, usedNames);
            pw.print(")"); //$NON-NLS-1$
            EasyMock.expect(this.or.getRegexOccurences()).andReturn(regOccurs);
            pw.print(regOccurs);

            EasyMock.replay(this.rf, this.or, pw);

            this.rf.writeRegex(pw, usedNames);

            EasyMock.verify(this.rf, this.or, pw);
        }
        catch (RuleResolutionException ex1)
        {
            Assert.fail();
        }
    }
}
