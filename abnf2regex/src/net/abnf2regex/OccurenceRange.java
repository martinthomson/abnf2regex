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

import java.io.IOException;

/**
 * A class representing a range of occurences. Used by rule fragments to express how many repetitions of that fragment
 * might occur.
 */
public class OccurenceRange
{
    /** The minimum number of occurences permitted. */
    private final int minOccurs;
    /** The maximum number of occurences permitted. A negative value indicates no upper bound on occurences. */
    private final int maxOccurs;

    /**
     * Create a new {@link OccurenceRange} with the assigned values.
     *
     * @param min the minimum number of occurences.
     * @param max the maximum number of occurences, less than zero indicates unbounded.
     */
    public OccurenceRange(int min, int max)
    {
        this.minOccurs = min;
        // normalization helps .equals work later
        this.maxOccurs = (max < 0) ? -1 : Math.max(max, min);
    }

    private StringBuilder addAbnfOccurences(StringBuilder bld)
    {
        if (this.minOccurs == this.maxOccurs)
        {
            if (this.minOccurs != 1)
            {
                bld.append(this.minOccurs);
            }
        }
        else
        {
            if (this.minOccurs != 0)
            {
                bld.append(this.minOccurs);
            }
            bld.append('*');
            if (this.maxOccurs >= 0)
            {
                bld.append(this.maxOccurs);
            }
        }
        return bld;
    }

    /**
     * Add the leading occurence indication and optional parentheses for a rule in ABNF form.
     *
     * @param bld the string builder to build on.
     * @param needsParens whether the caller has determined that parentheses are necessary.
     * @return bld, to allow for call chaining
     */
    public StringBuilder addAbnfLeadin(StringBuilder bld, boolean needsParens)
    {
        if (this.minOccurs == 0 && this.maxOccurs == 1)
        {
            bld.append('[');
        }
        else
        {
            this.addAbnfOccurences(bld);
            if (needsParens)
            {
                bld.append('(');
            }
        }
        return bld;
    }

    /**
     * Add the trailing occurence parentheses for a rule in ABNF form.
     *
     * @param bld the string builder to build on.
     * @param needsParens whether the caller has determined that parentheses are necessary.
     * @return bld, to allow for call chaining
     */
    public StringBuilder addAbnfTrail(StringBuilder bld, boolean needsParens)
    {
        if (this.minOccurs == 0 && this.maxOccurs == 1)
        {
            bld.append(']');
        }
        else if (needsParens)
        {
            bld.append(')');
        }
        return bld;
    }

    /**
     * Convenience method to indicate whether the occurence range indicates one instance only.
     *
     * @return true if {@link #minOccurs} and {@link #maxOccurs} are both 1.
     */
    public boolean isOneOnly()
    {
        return this.minOccurs == 1 && this.maxOccurs == 1;
    }

    /**
     * Build a string to append to a regex node to indicate
     *
     * @return a string containing the occurences, ?, *, +, {n,m}
     */
    public String getRegexOccurences()
    {
        StringBuilder bld = new StringBuilder();
        if (this.minOccurs == this.maxOccurs)
        {
            if (this.minOccurs != 1)
            {
                bld.append('{');
                bld.append(this.minOccurs);
                bld.append('}');
            }
        }
        else if (this.minOccurs == 0 && this.maxOccurs == 1)
        {
            bld.append('?');
        }
        else if (this.minOccurs == 0 && this.maxOccurs < 0)
        {
            bld.append('*');
        }
        else if (this.minOccurs == 1 && this.maxOccurs < 0)
        {
            bld.append('+');
        }
        else
        {
            bld.append('{');
            bld.append(this.minOccurs);
            bld.append(',');
            if (this.maxOccurs >= 0)
            {
                bld.append(this.maxOccurs);
            }
            bld.append('}');
        }
        return bld.toString();
    }

    /**
     * Get the minimum of the range.
     *
     * @return the minimum
     */
    public int getMin()
    {
        return this.minOccurs;
    }

    /**
     * Get the maximum of the range.
     *
     * @return the maximum
     */
    public int getMax()
    {
        return this.maxOccurs;
    }

    /**
     * Parse an ABNF occurence range out.
     *
     * @param abnf the reader to read from
     * @return a new occurence range
     * @throws IOException on IO errors
     */
    public static OccurenceRange parse(AbnfReader abnf) throws IOException
    {
        int min = -2; // special illegal value for this method
        int max = -2; // special illegal value for this method
        if (Character.isDigit(abnf.peek()))
        {
            min = abnf.parseNumber();
            max = min;
        }
        if (abnf.peek() == '*')
        {
            if (min == -2)
            {
                min = 0;
            }
            max = -1;
            abnf.read();

            if (Character.isDigit(abnf.peek()))
            {
                max = abnf.parseNumber();
            }
        }
        if (min == -2)
        {
            min = 1;
        }
        if (max == -2)
        {
            max = 1;
        }
        return new OccurenceRange(min, max);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof OccurenceRange)
        {
            OccurenceRange other = (OccurenceRange) obj;
            return this.minOccurs == other.minOccurs && this.maxOccurs == other.maxOccurs;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.minOccurs | (this.maxOccurs << 16) | (this.maxOccurs >>> 16);
    }
}
