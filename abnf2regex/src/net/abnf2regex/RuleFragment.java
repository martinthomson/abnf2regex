package net.abnf2regex;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract definition representing a single piece of a rule. Fragments have
 * contents (defined in extending classes) and a number of occurrences.
 */
public abstract class RuleFragment implements Cloneable
{
    /** The number of times this fragment may occur. */
    private OccurrenceRange occurrences = OccurrenceRange.ONCE;

    /**
     * Set the occurrence range for this fragment.
     *
     * @param or the occurrence range
     */
    public void setOccurences(OccurrenceRange or)
    {
        this.occurrences = or;
    }

    /**
     * Get the number of occurrences permitted for this fragment.
     *
     * @return the occurrence range
     */
    public OccurrenceRange getOccurences()
    {
        return this.occurrences;
    }

    /**
     * Determine whether this fragment needs to be enclosed within parentheses
     * when being expressed in ABNF.
     *
     * @return true if it does need to be enclosed in parentheses.
     */
    protected boolean needsAbnfParens()
    {
        return !this.occurrences.isOnce();
    }

    /**
     * Generate ABNF from this rule fragment.
     * 
     * @param usedNames
     * @return a string containing ABNF for this fragment
     */
    public String toAbnf(Set<String> usedNames)
    {
        StringBuilder bld = new StringBuilder();
        boolean needsParens = this.needsAbnfParens();
        this.occurrences.addAbnfLeadin(bld, needsParens);
        this.buildAbnf(bld, usedNames);
        this.occurrences.addAbnfTrail(bld, needsParens);
        return bld.toString();
    }

    /**
     * Build ABNF for this rule, overridden by extending classes to provide the
     * contents of the fragment. This class provides the occurrence range and
     * any enclosing parentheses.
     *
     * @param bld the {@link StringBuilder} used to build the string
     * @param usedNames the rule names that have already been used
     * @return <code>bld</code>, to allow for call chaining
     */
    protected abstract StringBuilder buildAbnf(StringBuilder bld, Set<String> usedNames);

    /**
     * Write a regular expression to the given writer.
     *
     * @param pw the writer to write to
     * @throws RuleResolutionException if an unresolved rule exists anywhere
     */
    public void writeRegex(PrintWriter pw) throws RuleResolutionException
    {
        this.writeRegex(pw, new HashSet<String>());
    }

    /**
     * Write a regular expression to the given writer.
     *
     * @param pw the writer to write to
     * @param usedNames a set of names that have already been used on this call
     *            stack--names that shouldn't be used again.
     * @throws RuleResolutionException if an unresolved rule exists anywhere
     */
    protected void writeRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
    {
        RegexSyntax syntax = RegexSyntax.getCurrent();
        if (this.needsRegexParens())
        {
            pw.print(syntax.getGroupingStart());
        }
        this.buildRegex(pw, usedNames);
        if (this.needsRegexParens())
        {
            pw.print(syntax.getGroupingEnd());
        }
        pw.print(this.getOccurences().getRegexOccurences());
    }

    /**
     * Determine if the regular expression syntax for this fragment requires
     * parentheses.
     *
     * @return true if it does need parentheses.
     */
    protected boolean needsRegexParens()
    {
        return !this.occurrences.isOnce();
    }

    /**
     * Build a regular expression. This class provides occurrence ranges and
     * parentheses, extending classes override this method to supply the body.
     *
     * @param pw the {@link PrintWriter} instance to write to
     * @param usedNames a set of names that have already been used on this call
     *            stack--names that shouldn't be used again.
     * @throws RuleResolutionException when unresolved rules are used
     */
    protected abstract void buildRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException;

    /**
     * Add the contents of the given fragment to this one. THe two fragments
     * must be of the same type for this to work.
     *
     * @param frag the fragment to append
     * @throws IllegalArgumentException if the two fragments aren't of the same
     *             type
     */
    public abstract boolean append(RuleFragment frag);

    @Override
    public String toString()
    {
        return this.toAbnf(new HashSet<String>());
    }

    @Override
    public abstract Object clone();
}
