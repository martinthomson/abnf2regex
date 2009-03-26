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
import java.util.Set;

/**
 * A named fragment references another rule by name. Initially, instances of this class are unresolved and any attempt
 * to generate a regular expresstion throw a {@link RuleResolutionException}. The rule must be resolved by calling
 * {@link #resolve(Rule)}.
 */
public class NamedFragment extends RuleFragment
{
    /** The name of the referenced rule. */
    private final String name;
    /** The actual value of the referenced rule, or null if this rule is unresolved. */
    private Rule resolved;

    /**
     * Create a named fragment that references a named rule.
     *
     * @param nm the name of the referenced rule
     */
    public NamedFragment(String nm)
    {
        this.name = nm;
    }

    /**
     * Get the name of the referenced rule.
     *
     * @return the rule name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Resolve this fragment, by assigning a rule to it. Note that the rule name is not checked for consistency, that is
     * the caller's responsibility.
     *
     * @param r the rule that this fragment resolves to.
     */
    public void resolve(Rule r)
    {
        this.resolved = r;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#append(net.abnf2regex.RuleFragment)
     */
    @Override
    public boolean append(RuleFragment frag)
    {
        if (frag instanceof NamedFragment)
        {
            NamedFragment nf = (NamedFragment) frag;
            if (nf.getName().equals(this.getName()))
            {
                OccurenceRange newRange =
                    new OccurenceRange(this.getOccurences().getMin() + nf.getOccurences().getMin(), this.getOccurences().getMax() +
                                                                                            nf.getOccurences().getMax());
                this.setOccurences(newRange);
                return true;
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
        return bld.append(this.name);
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#buildRegex(java.lang.StringBuilder)
     */
    @Override
    protected void buildRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
    {
        if (this.resolved == null)
        {
            throw new RuleResolutionException("Unresolved fragment: " + this.name); //$NON-NLS-1$
        }
        this.resolved.writeRegex(pw, usedNames);
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#needsAbnfParens()
     */
    @Override
    protected boolean needsAbnfParens()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#needsRegexParens()
     */
    @Override
    protected boolean needsRegexParens()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#clone()
     */
    @Override
    public Object clone()
    {
        NamedFragment copy = new NamedFragment(this.getName());
        copy.setOccurences(this.getOccurences());
        copy.resolve(this.resolved);
        return copy;
    }

}
