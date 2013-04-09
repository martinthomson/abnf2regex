package net.abnf2regex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * A fragment that represents a double-quoted literal.
 */
public class StringFragment extends RuleFragment
{
    private String str;

    /**
     * Create a new String based on the given string.
     *
     * @param _str the contents of the fragment
     */
    public StringFragment(String _str)
    {
        this.str = _str;
    }

    /**
     * Parse from ABNF. It is assumed that the caller has found a '"' character using {@link AbnfReader#peek()}.
     *
     * @param abnf the reader
     * @throws IOException when there are IO errors, {@link java.io.EOFException} when the end of file occurs before the
     *             end of the string
     */
    public static StringFragment parse(AbnfReader abnf) throws IOException
    {
        StringBuilder bld = new StringBuilder();
        abnf.read(); // skip leading '"'
        while (abnf.peek() != '"')
        {
            bld.append((char) abnf.read());
        }
        abnf.read(); // skip trailing '"'
        return new StringFragment(bld.toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#buildAbnf(java.lang.StringBuilder)
     */
    @Override
    protected StringBuilder buildAbnf(StringBuilder bld)
    {
        bld.append('"').append(this.str).append('"');
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
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#buildRegex(java.lang.StringBuilder)
     */
    @Override
    protected void buildRegex(PrintWriter pw, Set<String> usedNames)
    {
        for (char ch : this.str.toCharArray())
        {
            if (Character.isLetter(ch))
            {
                pw.print('[');
                pw.print(Character.toUpperCase(ch));
                pw.print(Character.toLowerCase(ch));
                pw.print(']');
            }
            else
            {
                pw.print(RegexSyntax.getCurrent().character(ch));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#append(net.abnf2regex.RuleFragment)
     */
    @Override
    public boolean append(RuleFragment frag)
    {
        if (frag instanceof StringFragment && this.getOccurences().equals(frag.getOccurences()))
        {
            StringFragment lit = (StringFragment) frag;
            this.str += lit.str;
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
        return this.str.codePointCount(0, this.str.length()) != 1 && super.needsRegexParens();
    }

    /**
     * Extract a single character if this is a single character rule.
     *
     * @return -1 if the string is other than 1 character long.
     */
    public int singleChar()
    {
        if (this.str.codePointCount(0, this.str.length()) == 1)
        {
            return this.str.codePointAt(0);
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.abnf2regex.RuleFragment#clone()
     */
    @Override
    public Object clone()
    {
        StringFragment copy = new StringFragment(this.str);
        copy.setOccurences(this.getOccurences());
        return copy;
    }
}
