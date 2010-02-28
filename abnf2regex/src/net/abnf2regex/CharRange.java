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
public class CharRange implements Comparable<CharRange>
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
     * @param cont whether this is a continuation of a prior fragment
     * @return bld, to allow for chaining
     */
    /* package private */StringBuilder buildAbnf(StringBuilder bld, boolean cont)
    {
        if (cont)
        {
            bld.append('.');
        }
        else
        {
            bld.append("%x"); //$NON-NLS-1$
        }

        bld.append(RegexSyntax.hexChar(this.start));
        if (this.end > this.start)
        {
            bld.append('-');
            bld.append(RegexSyntax.hexChar(this.end));
        }
        return bld;
    }

    @Override
    public String toString()
    {
        return this.buildAbnf(new StringBuilder(), false).toString();
    }

    /**
     * Compare based on the start of the range, followed by the end of the range. This results in a-a &lt; a-b &lt; b-b
     * &lt; b-c.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(CharRange o)
    {
        if (this.getStart() == o.getStart())
        {
            return this.getEnd() - o.getEnd();
        }
        return this.getStart() - o.getStart();
    }

    /**
     * Merge two character ranges if they overlap or touch.
     *
     * @param other the range to merge in
     * @return A merged range, or null if there is no overlap
     */
    public CharRange merge(CharRange other)
    {
        if ((this.start <= other.start && this.end + 1 >= other.start) ||
            (other.start <= this.start && other.end + 1 >= this.start))
        {
            return new CharRange(Math.min(this.start, other.start), Math.max(this.end, other.end));
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.start * 31 + this.end;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (obj instanceof CharRange)
        {
            CharRange other = (CharRange) obj;
            return (this.start == other.start && this.end == other.end);
        }
        return false;
    }
}
