package net.abnf2regex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A rule fragment that contains a literal expression, in the form:
 *
 * <pre>
 * (&quot;%&quot;[&quot;x&quot; / &quot;d&quot; / &quot;b&quot;] * HEXDIG[&quot;-&quot; * HEXDIG] * (&quot;.&quot; * HEXDIG[&quot;-&quot; * HEXDIG]))
 * </pre>
 */
public class LiteralFragment extends RuleFragment
{
    /** A list of character ranges that forms the literal sequence. */
    protected final List<CharRange> ranges = new ArrayList<CharRange>();

    /**
     * An empty (zero length) literal fragment.
     */
    public LiteralFragment()
    {
        // NOP
    }

    /**
     * Parse out a literal sequence, starting from the leading '%', which is assumed to have been recognized by the
     * caller using {@link AbnfReader#peek()}.
     *
     * @param abnf the input reader
     * @throws IOException when reading fails for any reason
     */
    public static LiteralFragment parse(AbnfReader abnf) throws IOException, AbnfParseException
    {
        LiteralFragment frag = new LiteralFragment();
        abnf.read(); // leading '%'
        int radix = getRadix(abnf);
        CharRange range = null;
        int c = 0;
        while (Character.digit(abnf.peek(), radix) >= 0 || abnf.peek() == '.' || abnf.peek() == '-')
        {
            int dig = abnf.read();
            if (dig == '.')
            {
                frag.addChar(c, range);
                range = null;
                c = 0;
            }
            else if (dig == '-')
            {
                range = new CharRange(c);
                c = 0;
            }
            else
            {
                c = c * radix + Character.digit((char) dig, radix);
            }
        }
        frag.addChar(c, range);
        return frag;
    }

    private static int getRadix(AbnfReader abnf) throws IOException, AbnfParseException
    {
        int radix;
        char radch = Character.toLowerCase((char) abnf.read());
        if (radch == 'b')
        {
            radix = 2;
        }
        else if (radch == 'd')
        {
            radix = 10;
        }
        else if (radch == 'x')
        {
            radix = 16;
        }
        else
        {
            throw new AbnfParseException("Invalid radix for % sequence", abnf); //$NON-NLS-1$
        }
        return radix;
    }

    private void addChar(int c, CharRange range)
    {
        if (range != null)
        {
            range.setEnd(c);
        }
        else
        {
            range = new CharRange(c);
        }
        this.ranges.add(range);
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#buildAbnf(java.lang.StringBuilder)
     */
    @Override
    protected StringBuilder buildAbnf(StringBuilder bld)
    {
        boolean cont = false;
        boolean started = false;
        for (CharRange cr : this.ranges)
        {
            if (cr.getEnd() > cr.getStart())
            {
                if (started)
                {
                    bld.append(' ');
                }
                cr.buildAbnf(bld, false);
                cont = false;
            }
            else
            {
                cr.buildAbnf(bld, cont);
                cont = true;
            }
            started = true;
        }
        return bld;
    }

    @Override
    protected boolean needsAbnfParens()
    {
        if (this.ranges.size() > 1)
        {
            for (CharRange cr : this.ranges)
            {
                if (cr.getEnd() > cr.getStart())
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void buildRegex(PrintWriter pw, Set<String> usedNames) throws RuleResolutionException
    {
        for (CharRange cr : this.ranges)
        {
            pw.print(RegexSyntax.getCurrent().range(cr));
        }
    }

    /**
     * If this literal rule contains only one character range, return that range.
     *
     * @return the single character range, or null
     */
    public CharRange singleCharRange()
    {
        if (this.ranges.size() == 1)
        {
            return this.ranges.get(0);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#append(net.abnf2regex.RuleFragment)
     */
    @Override
    public boolean append(RuleFragment frag)
    {
        if (frag instanceof LiteralFragment)
        {
            LiteralFragment lit = (LiteralFragment) frag;
            this.ranges.addAll(lit.ranges);
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#needsParens()
     */
    @Override
    protected boolean needsRegexParens()
    {
        return this.ranges.size() != 1 && super.needsRegexParens();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#clone()
     */
    @Override
    public Object clone()
    {
        LiteralFragment copy = new LiteralFragment();
        copy.setOccurences(this.getOccurences());
        copy.ranges.addAll(this.ranges);
        return copy;
    }

}
