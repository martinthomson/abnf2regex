package net.abnf2regex;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
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
        boolean added = false;
        if (frag instanceof ChoiceFragment)
        {
            GroupFragment choice = (GroupFragment) frag;
            added = this.appendAll(choice);
        }
        else if (frag instanceof SequenceFragment)
        {
            SequenceFragment seq = (SequenceFragment) frag;
            if (seq.length() == 1)
            {
                added = this.appendAll(seq);
            }
        }
        if (!added)
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
        return true;
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
        String singles = getSingleCharacterList(copy);
        if (singles.length() > 0)
        {
            started = true;
            pw.print(singles);
        }

        for (RuleFragment rf : copy)
        {
            if (started)
            {
                pw.print(RegexSyntax.getCurrent().getChoiceSeparator());
            }
            started = true;
            rf.writeRegex(pw, usedNames);
        }
    }

    /**
     * Get a string for the single character fragments in the choice, so we can turn it into a [list]
     *
     * @param copy a copy of the fragments in this choice, which can (and will) have any used elements removed.
     */
    private String getSingleCharacterList(Collection<RuleFragment> copy)
    {
        List<CharRange> singles = extractSingles(copy);
        if (singles.size() > 0)
        {
            mergeRanges(singles);

            RegexSyntax syntax = RegexSyntax.getCurrent();
            StringBuilder bld = new StringBuilder();
            for (CharRange cr : singles)
            {
                bld.append(syntax.range(cr, false));
            }
            if ((singles.size() > 1) || (bld.length() > 2))
            {
                bld.insert(0, syntax.getListStart());
                bld.append(syntax.getListEnd());
            }

            return bld.toString();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Extract all fragments from the list that contain single character rules.
     *
     * TODO: this method doesn't properly handle java syntax regular expressions that contain characters that require
     * two <code>char</code> entries.
     *
     * @param copy a copy, from which single character rules are extracted.
     * @return a collection of single characters.
     */
    private List<CharRange> extractSingles(Collection<RuleFragment> copy)
    {
        List<CharRange> singles = new ArrayList<CharRange>();
        Iterator<RuleFragment> it = copy.iterator();
        while (it.hasNext())
        {
            RuleFragment rf = it.next();
            if (rf.getOccurences().isOnce())
            {
                if (rf instanceof LiteralFragment)
                {
                    LiteralFragment lf = (LiteralFragment) rf;
                    CharRange scr = lf.singleCharRange();
                    if (scr != null)
                    {
                        it.remove();
                        singles.add(scr);
                    }
                }
                else if (rf instanceof StringFragment)
                {
                    StringFragment sf = (StringFragment) rf;

                    int sc = sf.singleChar();
                    if (sc >= 0)
                    {
                        convertStringToLiterals(singles, sc);
                        it.remove();
                    }
                }
            }
        }
        return singles;
    }

    /**
     * Add a single character string.
     *
     * @param singles the string builder to add to
     * @param sc the single character to add
     */
    private void convertStringToLiterals(Collection<CharRange> singles, int sc)
    {
        if (Character.isLetter(sc))
        {
            int upperCase = Character.toUpperCase(sc);
            singles.add(new CharRange(upperCase, upperCase));
            int lowerCase = Character.toLowerCase(sc);
            singles.add(new CharRange(lowerCase, lowerCase));
        }
        else
        {
            singles.add(new CharRange(sc, sc));
        }
    }

    /**
     * Merge character ranges.
     *
     * @param singles the list of character ranges
     * @return a list containing sorted, merged fragments.
     */
    private List<CharRange> mergeRanges(List<CharRange> singles)
    {
        Collections.sort(singles);
        List<CharRange> copy = new ArrayList<CharRange>();
        CharRange saved = singles.get(0);
        for (CharRange cr : singles)
        {
            CharRange merge = saved.merge(cr);
            if (merge != null)
            {
                saved = merge;
            }
            else
            {
                copy.add(saved);
                saved = cr;
            }
        }
        return copy;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#needsRegexParens()
     */
    @Override
    protected boolean needsRegexParens()
    {
        // Note: choices never have a length of 1 after simplification, so checking for that doesn't make sense.

        for (RuleFragment rf : this.fragments)
        {
            if (!rf.getOccurences().isOnce())
            {
                return true;
            }
            if (rf instanceof LiteralFragment)
            {
                LiteralFragment lf = (LiteralFragment) rf;
                if (lf.singleCharRange() == null)
                {
                    return true;
                }
            }
            else if (rf instanceof StringFragment)
            {
                StringFragment sf = (StringFragment) rf;

                if (sf.singleChar() < 0)
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
