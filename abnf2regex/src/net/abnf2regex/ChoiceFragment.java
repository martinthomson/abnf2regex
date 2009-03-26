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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

/**
 * A fragment that contains alternatives. In ABNF these choices are separated by '/' characters.
 */
public class ChoiceFragment extends GroupFragment
{
    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#append(net.abnf2regex.RuleFragment)
     */
    @Override
    public boolean append(RuleFragment frag)
    {
        if (frag instanceof ChoiceFragment)
        {
            ChoiceFragment other = (ChoiceFragment) frag;
            if (other.getOccurences().isOneOnly())
            {
                this.fragments.addAll(other.fragments);
            }
            else
            {
                this.fragments.add(frag);
            }
        }
        else if (frag instanceof SequenceFragment)
        {
            SequenceFragment seq = (SequenceFragment) frag;
            if (seq.length() == 1)
            {
                this.fragments.add(seq.removeLast());
            }
            else
            {
                this.fragments.add(frag);
            }
        }
        else
        {
            this.fragments.add(frag);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#buildAbnf(java.lang.StringBuilder)
     */
    @Override
    protected StringBuilder buildAbnf(StringBuilder bld)
    {
        boolean first = true;
        for (RuleFragment rf : this.fragments)
        {
            if (!first)
            {
                bld.append(" / "); //$NON-NLS-1$
            }
            else
            {
                first = false;
            }
            bld.append(rf.toAbnf());
        }
        return bld;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#needsAbnfParens()
     */
    @Override
    protected boolean needsAbnfParens()
    {
        return this.length() != 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#buildRegex(java.lang.StringBuilder)
     */
    @Override
    protected void buildRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
    {
        Deque<RuleFragment> copy = new ArrayDeque<RuleFragment>(this.fragments);
        boolean started = false;
        String singles = getSingles(copy);
        if (singles.length() > 0)
        {
            started = true;
            pw.print(singles);
        }

        for (RuleFragment rf : copy)
        {
            if (started)
            {
                pw.print('|');
            }
            started = true;
            rf.writeRegex(pw, usedNames);
        }
    }

    /**
     * @param copy a copy of the fragments in this choice, which can (and will) have any used elements removed.
     */
    private String getSingles(Deque<RuleFragment> copy)
    {
        StringBuilder singles = new StringBuilder();
        int count = 0;
        Iterator<RuleFragment> it = copy.iterator();
        while (it.hasNext())
        {
            RuleFragment rf = it.next();
            if (rf instanceof LiteralFragment)
            {
                LiteralFragment lf = (LiteralFragment) rf;
                CharRange scr = lf.singleCharRange();
                if (scr != null)
                {
                    it.remove();
                    count += addLiteralSingle(singles, scr);
                }
            }
            else if (rf instanceof StringFragment)
            {
                StringFragment sf = (StringFragment) rf;

                int sc = sf.singleChar();
                if (sc >= 0)
                {
                    count += addStringSingle(singles, sc);
                    it.remove();
                }
            }
        }
        if (count > 1)
        {
            singles.insert(0, '[');
            singles.append(']');
        }
        return singles.toString();
    }

    /**
     * Add a single character from a string literal.
     *
     * @param singles the string builder to add to
     * @param sc the single character to add
     * @return the number of characters added to the sequence.
     */
    private int addStringSingle(StringBuilder singles, int sc)
    {
        if (Character.isLetter(sc))
        {
            singles.append((char) Character.toLowerCase(sc));
            singles.append((char) Character.toUpperCase(sc));
            return 2;
        }
        singles.append(CharRange.regexChar((char) sc));
        return 1;
    }

    /**
     * @param singles
     * @param scr
     * @return
     */
    private int addLiteralSingle(StringBuilder singles, CharRange scr)
    {
        if (scr.getStart() == '0' && scr.getEnd() == '9')
        {
            singles.append("\\d"); //$NON-NLS-1$
            return 1;
        }

        singles.append(CharRange.regexChar(scr.getStart()));
        if (scr.getEnd() > scr.getStart())
        {
            singles.append('-');
            singles.append(CharRange.regexChar(scr.getEnd()));
            return 2;
        }
        return 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#needsRegexParens()
     */
    @Override
    protected boolean needsRegexParens()
    {
        if (this.length() == 1)
        {
            return false;
        }
        for (RuleFragment rf : this.fragments)
        {
            if (rf instanceof LiteralFragment)
            {
                LiteralFragment lf = (LiteralFragment) rf;
                CharRange scr = lf.singleCharRange();
                if (scr == null)
                {
                    return true;
                }
            }
            else if (rf instanceof StringFragment)
            {
                StringFragment sf = (StringFragment) rf;

                int sc = sf.singleChar();
                if (sc < 0)
                {
                    return true;
                }
            }
            else
            {
                return true;
            }
        }

        return false;
    }

}
