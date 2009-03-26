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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.abnf2regex.easymock.Capture;
import net.abnf2regex.easymock.EasyMockHelper;

import org.easymock.EasyMock;
import org.easymock.classextension.EasyClassMock;
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
        Assert.assertTrue(fragment.getOccurences().isOneOnly());
    }

    /**
     * Test that getting and setting occurences work.
     */
    @Test
    public void testGetSetOccurences()
    {
        EasyClassMock.replay(this.rf, this.or);
        Assert.assertSame(this.or, this.rf.getOccurences());
        EasyClassMock.verify(this.rf, this.or);
    }

    /**
     * Test whether or not parentheses are needed, for this class, this depends on the occurence range.
     */
    @Test
    public void testNeedsParens()
    {
        EasyMock.expect(Boolean.valueOf(this.or.isOneOnly())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(this.or.isOneOnly())).andReturn(Boolean.TRUE);
        EasyMock.expect(Boolean.valueOf(this.or.isOneOnly())).andReturn(Boolean.FALSE);
        EasyMock.expect(Boolean.valueOf(this.or.isOneOnly())).andReturn(Boolean.FALSE);
        EasyClassMock.replay(this.rf, this.or);

        Assert.assertFalse(this.rf.needsAbnfParens());
        Assert.assertFalse(this.rf.needsRegexParens());

        Assert.assertTrue(this.rf.needsAbnfParens());
        Assert.assertTrue(this.rf.needsRegexParens());
        EasyClassMock.verify(this.rf, this.or);
    }

    /**
     * Tests the toAbnf/toString methods, which should be the same.
     */
    @Test
    public void testToAbnfOrString()
    {
        List<Capture<StringBuilder>> sameStringBufList = new ArrayList<Capture<StringBuilder>>();
        EasyClassMock.checkOrder(this.or, true);

        for (int i = 0; i < 2; ++i)
        {
            Capture<StringBuilder> sameStringBuf = new Capture<StringBuilder>();
            sameStringBufList.add(sameStringBuf);

            EasyMock.expect(Boolean.valueOf(this.or.isOneOnly())).andReturn(Boolean.TRUE);
            EasyMock.expect(this.or.addAbnfLeadin(sameStringBuf.capture(), EasyMock.eq(false))).andAnswer(sameStringBuf);
            EasyMock.expect(this.rf.buildAbnf(sameStringBuf.capture())).andAnswer(sameStringBuf);
            EasyMock.expect(this.or.addAbnfTrail(sameStringBuf.capture(), EasyMock.eq(false))).andAnswer(sameStringBuf);
        }
        EasyClassMock.replay(this.rf, this.or);

        String abnf = this.rf.toAbnf();
        Assert.assertEquals(sameStringBufList.get(0).getValue().toString(), abnf);

        String str = this.rf.toString();
        Assert.assertEquals(sameStringBufList.get(1).getValue().toString(), str);
        Assert.assertEquals(str, abnf);

        EasyClassMock.verify(this.rf, this.or);
    }

    /**
     * Test that writing a regex calls the appropriate methods.
     */
    @Test
    public void testWriteRegexSingle()
    {
        try
        {
            PrintWriter pw = EasyClassMock.createMock(PrintWriter.class);
            Set<String> usedNames = new HashSet<String>();
            String regOccurs = "{n}"; //$NON-NLS-1$

            EasyMock.expect(Boolean.valueOf(this.or.isOneOnly())).andReturn(Boolean.TRUE).times(1, 2);
            EasyMock.expect(this.or.getRegexOccurences()).andReturn(regOccurs);
            this.rf.buildRegex(pw, usedNames);
            pw.print(regOccurs);

            EasyClassMock.replay(this.rf, this.or, pw);

            this.rf.writeRegex(pw, usedNames);

            EasyClassMock.verify(this.rf, this.or, pw);
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
            PrintWriter pw = EasyClassMock.createMock(PrintWriter.class);
            Set<String> usedNames = new HashSet<String>();
            String regOccurs = "{n}"; //$NON-NLS-1$

            EasyMock.expect(Boolean.valueOf(this.or.isOneOnly())).andReturn(Boolean.FALSE).times(1, 2);
            pw.print("(?:"); //$NON-NLS-1$
            this.rf.buildRegex(pw, usedNames);
            pw.print(")"); //$NON-NLS-1$
            EasyMock.expect(this.or.getRegexOccurences()).andReturn(regOccurs);
            pw.print(regOccurs);

            EasyClassMock.replay(this.rf, this.or, pw);

            this.rf.writeRegex(pw, usedNames);

            EasyClassMock.verify(this.rf, this.or, pw);
        }
        catch (RuleResolutionException ex1)
        {
            Assert.fail();
        }
    }
}
