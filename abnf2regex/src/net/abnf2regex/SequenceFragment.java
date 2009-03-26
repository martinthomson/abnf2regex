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
import java.util.Set;

/**
 * A {@link GroupFragment} that contains a sequence of fragments - the most basic building block of ABNF.
 */
public class SequenceFragment extends GroupFragment
{
    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#append(net.abnf2regex.RuleFragment)
     */
    @Override
    public boolean append(RuleFragment frag)
    {
        if (frag instanceof SequenceFragment)
        {
            SequenceFragment seq = (SequenceFragment) frag;
            if (frag.getOccurences().isOneOnly())
            {
                this.fragments.addAll(seq.fragments);
            }
            else
            {
                this.fragments.addLast(frag);
            }
        }
        else if (!mergeWithLast(frag))
        {
            this.fragments.addLast(frag);
        }
        return true;
    }

    /**
     * Merge the given fragment with the last fragment.  The fragment is appended to the last instance if they are both of the same type.
     *
     * @param frag the fragment to append.
     * @return true iff the merge was successful
     */
    private boolean mergeWithLast(RuleFragment frag)
    {
        if (this.fragments.size() > 0)
        {
            RuleFragment last = this.fragments.getLast();
            if (last.getClass().isAssignableFrom(frag.getClass()))
            {
                return last.append(frag);
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#buildAbnf(java.lang.StringBuilder)
     */
    @Override
    protected StringBuilder buildAbnf(StringBuilder bld)
    {
        for (RuleFragment frag : this.fragments)
        {
            bld.append(frag.toAbnf()).append(' ');
        }
        bld.setLength(bld.length() - 1);
        return bld;
    }

    /* (non-Javadoc)
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
        for (RuleFragment frag : this.fragments)
        {
            frag.writeRegex(pw, usedNames);
        }
    }

    /**
     * Replaces all of the fragments from this with all the fragments in 'last'.  Resets last to be empty.
     * @param extractFrom the sequence to extract all fragments from
     */
    public void extractAll(SequenceFragment extractFrom)
    {
        this.fragments = extractFrom.fragments;
        extractFrom.fragments = new ArrayDeque<RuleFragment>();
    }
}
