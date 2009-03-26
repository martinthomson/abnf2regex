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

/**
 * A range of characters, typically as a result of a literal expression like %61-66.
 */
public class CharRange
{
    /** The start of the range. */
    private final int start;
    /** The end of the range. */
    private int end;

    /**
     * Create a character range with start and end of the range equal.
     *
     * @param x the character.
     */
    public CharRange(int x)
    {
        this(x, x);
    }

    /**
     * Create a character range.
     *
     * @param _start the start of the range.
     * @param _end the end of the range.
     */
    public CharRange(int _start, int _end)
    {
        this.start = _start;
        this.end = _end;
    }

    /**
     * Get the start of the range.
     *
     * @return the start
     */
    public int getStart()
    {
        return this.start;
    }

    /**
     * Get the end of the range.
     *
     * @return the end
     */
    public int getEnd()
    {
        return this.end;
    }

    /**
     * Set the end of the range
     *
     * @param c the character code for the range end.
     */
    public void setEnd(int c)
    {
        this.end = c;
    }

    /**
     * Build ABNF for the given character range.
     *
     * @param bld a {@link StringBuilder} to append to
     * @param first whether this is the first such, or whether others precede it
     * @return bld, to allow for chaining
     */
    public StringBuilder toAbnf(StringBuilder bld, boolean first)
    {
        if (first)
        {
            bld.append("%x"); //$NON-NLS-1$
        }
        else
        {
            bld.append('.');
        }

        bld.append(CharRange.hexChar(this.start));
        if (this.end > this.start)
        {
            bld.append('-');
            bld.append(CharRange.hexChar(this.end));
        }
        return bld;
    }

    /**
     * Create a regular expression for the range.
     *
     * @return a regular expression string.
     */
    public String toRegex()
    {
        StringBuilder bld = new StringBuilder();
        if ((this.start == '0') && (this.end == '9'))
        {
            bld.append("\\d"); //$NON-NLS-1$
        }
        else if (this.start < this.end)
        {
            bld.append('[');
            bld.append(CharRange.regexChar(this.start));
            bld.append('-');
            bld.append(CharRange.regexChar(this.end));
            bld.append(']');
        }
        else
        {
            bld.append(CharRange.regexChar(this.start));
        }
        return bld.toString();
    }

    /**
     * Escape a character for use in regular expressions.
     *
     * @param ch any character
     * @return a regular expression string for that character, properly escaped.
     */
    public static String regexChar(int ch)
    {
        switch (ch)
        {
            // bad = .\?*+()|[]
            // dodgy = -
            case '.':
            case '\\':
            case '?':
            case '*':
            case '+':
            case '(':
            case ')':
            case '|':
            case '[':
            case ']':
            case '-':
                return "\\" + (char) ch; //$NON-NLS-1$
            case '\t':
                return "\\t"; //$NON-NLS-1$
            case '\r':
                return "\\r"; //$NON-NLS-1$
            case '\n':
                return "\\n"; //$NON-NLS-1$
            default:
                if ((ch > 0x1f) && (ch < 0x7f)) // (use \\u for all unicode)
                {
                    return Character.toString((char) ch);
                }
                String hex = CharRange.hexChar(ch);
                if (hex.length() == 2)
                {
                    return "\\x" + hex; //$NON-NLS-1$
                }
                return "\\u" + hex; //$NON-NLS-1$
        }
    }

    private static String hexChar(int ch)
    {
        String hexChars = Integer.toHexString(ch);
        return (((hexChars.length() % 2) == 1) ? "0" : "") + hexChars; //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    public String toString()
    {
        return this.toAbnf(new StringBuilder(), true).toString();
    }
}
