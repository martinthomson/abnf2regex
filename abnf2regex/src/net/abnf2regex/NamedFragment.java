package net.abnf2regex;

import java.io.PrintWriter;
import java.util.Set;

/**
 * A named fragment references another rule by name. Initially, instances of
 * this class are unresolved and any attempt to generate a regular expression
 * throw a {@link RuleResolutionException}. The rule must be resolved by calling
 * {@link #resolve(Rule)}.
 */
public class NamedFragment extends RuleFragment
{
    /** The name of the referenced rule. */
    private final String name;
    /**
     * The actual value of the referenced rule, or null if this rule is
     * unresolved.
     */
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
     * Resolve this fragment, by assigning a rule to it. Note that the rule name
     * is not checked for consistency, that is the caller's responsibility.
     *
     * @param r the rule that this fragment resolves to.
     */
    public void resolve(Rule r)
    {
        this.resolved = r;
    }

    /**
     * The resolved rule, if any.
     *
     * @return a rule
     */
    public Rule getResolvedRule()
    {
        return this.resolved;
    }

    @Override
    public boolean append(RuleFragment frag)
    {
        if (frag instanceof NamedFragment)
        {
            NamedFragment nf = (NamedFragment) frag;
            if (nf.getName().equals(this.getName()))
            {
                this.setOccurences(this.getOccurences().add(nf.getOccurences()));
                return true;
            }
        }
        return false;
    }

    @Override
    protected StringBuilder buildAbnf(StringBuilder bld, Set<String> usedNames)
    {
        return bld.append(this.name);
    }

    @Override
    protected void buildRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
    {
        if (this.resolved == null)
        {
            throw new RuleResolutionException("Unresolved fragment: " + this.name); //$NON-NLS-1$
        }
        this.resolved.writeRegex(pw, usedNames);
    }

    @Override
    protected boolean needsAbnfParens()
    {
        return false;
    }

    @Override
    public Object clone()
    {
        NamedFragment copy = new NamedFragment(this.getName());
        copy.setOccurences(this.getOccurences());
        copy.resolve(this.resolved);
        return copy;
    }

}
