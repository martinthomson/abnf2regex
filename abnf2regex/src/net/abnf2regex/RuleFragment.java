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
 * An abstract definition representing a single piece of a rule. Fragments have contents (defined in extending classes)
 * and a number of occurences.
 */
public abstract class RuleFragment implements Cloneable
{
    private static final String REGEX_PAREN_OPEN = "(?:"; //$NON-NLS-1$
    private static final String REGEX_PAREN_CLOSE = ")"; //$NON-NLS-1$

    /** The number of times this fragment may occur. */
    private OccurenceRange occurences = new OccurenceRange(1, 1);

    /**
     * Set the occurence range for this fragment.
     * 
     * @param or the occurence range
     */
    public void setOccurences(OccurenceRange or)
    {
        this.occurences = or;
    }

    /**
     * Get the number of occurences permitted for this fragment.
     * 
     * @return the occurence range
     */
    public OccurenceRange getOccurences()
    {
        return this.occurences;
    }

    /**
     * Determine whether this fragment needs to be enclosed within parentheses when being expressed in ABNF.
     * 
     * @return true if it does need to be enclosed in parentheses.
     */
    protected boolean needsAbnfParens()
    {
        return !this.occurences.isOneOnly();
    }

    /**
     * Generate ABNF from this rule fragment.
     * 
     * @return a string containing ABNF for this fragment
     */
    public String toAbnf()
    {
        StringBuilder bld = new StringBuilder();
        boolean needsParens = this.needsAbnfParens();
        this.occurences.addAbnfLeadin(bld, needsParens);
        this.buildAbnf(bld);
        this.occurences.addAbnfTrail(bld, needsParens);
        return bld.toString();
    }

    /**
     * Build ABNF for this rule, overridden by extending classes to provide the contents of the fragment. This class
     * provides the occurence range and any enclosing parentheses.
     * 
     * @param bld the {@link StringBuilder} used to build the string
     * @return bld, to allow for call chaining
     */
    protected abstract StringBuilder buildAbnf(StringBuilder bld);

    /**
     * Write a regular expression to the given writer.
     * 
     * @param pw the writer to write to
     * @param usedNames a set of names that have already been used on this call stack--names that shouldn't be used
     *            again.
     * @throws RuleResolutionException if an unresolved rule exists anywhere
     */
    public void writeRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
    {
        if (this.needsRegexParens())
        {
            pw.print(RuleFragment.REGEX_PAREN_OPEN);
        }
        this.buildRegex(pw, usedNames);
        if (this.needsRegexParens())
        {
            pw.print(RuleFragment.REGEX_PAREN_CLOSE);
        }
        pw.print(this.occurences.getRegexOccurences());
    }

    /**
     * Determine if the regular expression syntax for this fragment requires parentheses.
     * 
     * @return true if it does need parentheses.
     */
    protected boolean needsRegexParens()
    {
        return !this.occurences.isOneOnly();
    }

    /**
     * Build a regular expression. This class provides occurence ranges and parenthese, extending classes override this
     * method to supply the body.
     * 
     * @param pw the {@link PrintWriter} instance to write to
     * @param usedNames a set of names that have already been used on this call stack--names that shouldn't be used
     *            again.
     * @throws RuleResolutionException when unresolved rules are used
     */
    protected abstract void buildRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException;

    /**
     * Add the contents of the given fragment to this one. THe two fragments must be of the same type for this to work.
     * 
     * @param frag the fragment to append
     * @throws IllegalArgumentException if the two fragments aren't of the same type
     */
    public abstract boolean append(RuleFragment frag);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.toAbnf();
    }

    @Override
    public abstract Object clone();

}
