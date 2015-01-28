package net.abnf2regex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * An ABNF rule, with a name and a sequence of rule fragments.
 */
public class Rule
{
    /** The name of the rule. */
    private final String name;
    /** A rule always has a sequence of fragments, used as a container for other fragments. */
    private GroupFragment mainFragment = new SequenceFragment();
    /** When recursively creating regular expressions, this is used to indicate where recursion has occurred. */
    private static final Set<String> warned = new HashSet<String>();

    /**
     * Create a new ABNF rule
     *
     * @param _name the name of the rule
     */
    public Rule(String _name)
    {
        this.name = _name;
    }

    /**
     * Generate a regular expression from the rule.
     *
     * @return a String containing a regular expression.
     * @throws RuleResolutionException when {@link NamedFragment} instances are unresolved.
     */
    public String toRegex() throws RuleResolutionException
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        this.writeRegex(pw, new HashSet<String>());
        return stringWriter.toString();
    }

    /**
     * Get the rule name
     *
     * @return the name of the rule
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return this.getName() + " = " + this.toAbnf() + '\n'; //$NON-NLS-1$
    }

    /**
     * Get the main container for this rule.
     *
     * @return
     */
    /* package private */GroupFragment getMainFragment()
    {
        return this.mainFragment;
    }

    /**
     * Generate normalized ABNF text from the rule.
     *
     * @return a String containing an ABNF rule.
     */
    public String toAbnf()
    {
        return this.mainFragment.toAbnf();
    }

    /**
     * Write a regular expression to the specified {@link PrintWriter}, taking care not to recurse infinitely.
     *
     * @param pw the print writer to output to
     * @param usedNames a set of rules that have already been called on this call stack.
     * @throws RuleResolutionException when {@link NamedFragment} instances are unresolved.
     */
    public void writeRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
    {
        if (usedNames.contains(this.getName()))
        {
            if (!Rule.warned.contains(this.getName()))
            {
                System.err.println("; Warning: rule '" + this.getName() + "' recurses."); //$NON-NLS-1$ //$NON-NLS-2$
                Rule.warned.add(this.getName());
            }
            RegexSyntax syntax = RegexSyntax.getCurrent();
            pw.print(syntax.getWildcard() +  syntax.getOccurenceAny());
            return;
        }

        usedNames.add(this.getName());
        this.mainFragment.writeRegex(pw, usedNames);
        usedNames.remove(this.getName());
    }
}
