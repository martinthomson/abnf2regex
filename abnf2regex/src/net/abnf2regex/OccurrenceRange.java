package net.abnf2regex;

import java.io.IOException;

/**
 * A class representing a range of occurences. Used by rule fragments to express how many repetitions of that fragment
 * might occur.
 */
public class OccurrenceRange
{
    /** A convenience for all those once comparisons. */
    public static final OccurrenceRange ONCE = new OccurrenceRange(1, 1);
    /** An unlimited number, used for maximum only */
    public static final int UNBOUNDED = -1;
    /** The minimum number of occurences permitted. */
    private final int minOccurs;
    /** The maximum number of occurences permitted. A negative value indicates no upper bound on occurences. */
    private final int maxOccurs;

    /**
     * Create a new {@link OccurrenceRange} with the assigned values.
     *
     * @param min the minimum number of occurences.
     * @param max the maximum number of occurences, less than zero indicates unbounded.
     */
    public OccurrenceRange(int min, int max)
    {
        this.minOccurs = min;
        // normalization helps .equals work later
        this.maxOccurs = (max < 0) ? OccurrenceRange.UNBOUNDED : Math.max(max, min);
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
    public boolean isOnce()
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
        RegexSyntax syntax = RegexSyntax.getCurrent();
        if (this.minOccurs == this.maxOccurs)
        {
            if (this.minOccurs != 1)
            {
                bld.append(syntax.getOccurencesStart());
                bld.append(this.minOccurs);
                bld.append(syntax.getOccurencesEnd());
            }
        }
        else if (this.minOccurs == 0 && this.maxOccurs == 1)
        {
            bld.append(syntax.getOccurenceOptional());
        }
        else if (this.minOccurs == 0 && this.maxOccurs == OccurrenceRange.UNBOUNDED)
        {
            bld.append(syntax.getOccurenceAny());
        }
        else if (this.minOccurs == 1 && this.maxOccurs == OccurrenceRange.UNBOUNDED)
        {
            bld.append(syntax.getOccurenceOneOrMore());
        }
        else
        {
            bld.append(syntax.getOccurencesStart());
            bld.append(this.minOccurs);
            bld.append(',');
            if (this.maxOccurs >= 0)
            {
                bld.append(this.maxOccurs);
            }
            bld.append(syntax.getOccurencesEnd());
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
     * Add two ranges together.
     *
     * @param other the second range
     * @return a new range that is the sum of this and the other
     */
    public OccurrenceRange add(OccurrenceRange other)
    {
        int max = this.getMax() + other.getMax();
        if (this.getMax() == OccurrenceRange.UNBOUNDED || other.getMax() == OccurrenceRange.UNBOUNDED)
        {
            max = OccurrenceRange.UNBOUNDED;
        }
        return new OccurrenceRange(this.getMin() + other.getMin(), max);
    }

    /**
     * Multiple two ranges together, if possible.
     *
     * @param other the second range
     * @return a new range that is the product of this and the other, or <code>null</code> if a multiplication is not
     *         possible
     */
    public OccurrenceRange multiply(OccurrenceRange other)
    {
        if (this.isOnce())
        {
            return other;
        }
        if (other.isOnce())
        {
            return this;
        }

        if (this.getMin() == this.getMax() && other.getMin() == other.getMax())
        {
            int simple = this.getMin() * other.getMin();
            return new OccurrenceRange(simple, simple);
        }
        if (this.getMax() == OccurrenceRange.UNBOUNDED && other.getMax() == OccurrenceRange.UNBOUNDED &&
                (this.getMin() == 1 || other.getMin() == 1))
        {
            return new OccurrenceRange(this.getMin() * other.getMin(), OccurrenceRange.UNBOUNDED);
        }
        if ((this.equals(new OccurrenceRange(0, 1)) && other.equals(new OccurrenceRange(1, OccurrenceRange.UNBOUNDED)))
                || (other.equals(new OccurrenceRange(0, 1)) && this.equals(new OccurrenceRange(1, OccurrenceRange.UNBOUNDED))))
        {
            return new OccurrenceRange(0, OccurrenceRange.UNBOUNDED);
        }
        return null;
    }

    /**
     * Parse an ABNF occurence range out.
     *
     * @param abnf the reader to read from
     * @return a new occurence range
     * @throws IOException on IO errors
     */
    public static OccurrenceRange parse(AbnfReader abnf) throws IOException
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
        return new OccurrenceRange(min, max);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof OccurrenceRange)
        {
            OccurrenceRange other = (OccurrenceRange) obj;
            return this.minOccurs == other.minOccurs && this.maxOccurs == other.maxOccurs;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.minOccurs | (this.maxOccurs << 16) | (this.maxOccurs >>> 16);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder bld = new StringBuilder();
        bld.append(this.minOccurs);
        if (this.maxOccurs >= this.minOccurs)
        {
            bld.append('-').append(this.maxOccurs);
        }
        else
        {
            bld.append('+');
        }
        return bld.toString();
    }
}
