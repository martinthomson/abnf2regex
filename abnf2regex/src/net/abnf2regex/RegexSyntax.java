package net.abnf2regex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class embodies
 */
@SuppressWarnings("nls")
public class RegexSyntax
{
    /** The name of the java syntax */
    public static final String SYNTAX_JAVA = "java";
    /** The name of the XML Schema syntax */
    public static final String SYNTAX_XMLSCHEMA = "xmlschema";
    /** The name of the XML Schema (ascii) syntax */
    public static final String SYNTAX_XMLSCHEMA_ASCII = "xmlschema-ascii";
    /** The name of the POSIX syntax */
    public static final String SYNTAX_POSIX = "posix";
    /** The name of the Unix grep syntax, an alias for {@link #SYNTAX_POSIX} */
    public static final String SYNTAX_GREP = "grep";
    /** The name of the Unix sed syntax, an alias for {@link #SYNTAX_POSIX} */
    public static final String SYNTAX_SED = "sed";
    /** The name of the Perl syntax */
    public static final String SYNTAX_PERL = "perl";
    /** The name of the syntax */
    private final String name;
    /** The wildcard character */
    private String wildcard = ".";
    /** the start of a grouping (e.g., "(?:") */
    private String groupingStart;
    /** the end of a grouping (e.g., ")") */
    private String groupingEnd;
    /** The choice separator */
    private String choice = "|";
    /** The suffix used for any number of repetitions of a particle */
    private String occurenceAny = "*";
    /** The suffix used for zero or one repetitions of a particle */
    private String occurenceOptional = "?";
    /** The suffix used for one or more repetitions of a particle */
    private String occurenceOneOrMore = "+";
    /** The start of an occurence range suffix */
    private String occurencesStart = "{";
    /** The end of an occurence range suffix */
    private String occurencesEnd = "}";
    /** The start of a list */
    private String listStart = "[";
    /** The end of a list */
    private String listEnd = "]";
    /** Whether special digit ranges are detected. */
    private boolean specialRanges = true;

    /**
     * Create a new syntax.
     *
     * @param _name the name of the syntax (case insensitive)
     */
    RegexSyntax(String _name)
    {
        this.name = _name.toLowerCase();
    }

    /** The current syntax */
    private static RegexSyntax current;
    /** The set of syntaxes */
    private static Map<String, RegexSyntax> syntaxes = new HashMap<String, RegexSyntax>();

    static
    {
        RegexSyntax java1 = new JavaRegexSyntax();
        RegexSyntax java = java1;
        RegexSyntax.current = java;
        RegexSyntax.syntaxes.put(java.getName(), java);
        RegexSyntax xmlschema = new XmlSchemaRegexSyntax(false);
        RegexSyntax.syntaxes.put(xmlschema.getName(), xmlschema);
        RegexSyntax xmlschemaascii = new XmlSchemaRegexSyntax(true);
        RegexSyntax.syntaxes.put(xmlschemaascii.getName(), xmlschemaascii);
        RegexSyntax posix = new PosixRegexSyntax();
        RegexSyntax.syntaxes.put(posix.getName(), posix);
        RegexSyntax.syntaxes.put(RegexSyntax.SYNTAX_GREP, posix);
        RegexSyntax.syntaxes.put(RegexSyntax.SYNTAX_SED, posix);
        RegexSyntax perl = new PerlRegexSyntax();
        RegexSyntax.syntaxes.put(perl.getName(), perl);
    }

    /**
     * Get the name of the syntax.
     *
     * @return the name of the syntax
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the current
     */
    public static RegexSyntax getCurrent()
    {
        return current;
    }

    /**
     * Set the current syntax
     *
     * @param name the name of the syntax to use
     * @throws RegexSyntaxNotFoundException if the name does not exist
     */
    public static void setCurrent(String name) throws RegexSyntaxNotFoundException
    {
        String lcName = name.toLowerCase();
        if (!RegexSyntax.syntaxes.containsKey(lcName))
        {
            throw new RegexSyntaxNotFoundException("Syntax does not exist.", lcName);
        }
        RegexSyntax.current = RegexSyntax.syntaxes.get(lcName);
    }

    /**
     * Learn what syntaxes are supported.
     *
     * @return a set of syntaxes that are supported
     */
    public static Set<String> getSyntaxNames()
    {
        return Collections.unmodifiableSet(RegexSyntax.syntaxes.keySet());
    }

    /**
     * Set the grouping values
     *
     * @param start the start of a grouping (e.g., "(?:")
     * @param end the end of a grouping (")")
     */
    protected void setGrouping(String start, String end)
    {
        this.groupingStart = start;
        this.groupingEnd = end;
    }

    /**
     * Get the start string for a regex group.
     *
     * @return a string
     */
    public String getGroupingStart()
    {
        return this.groupingStart;
    }

    /**
     * Get the end string for a regex group.
     *
     * @return a string
     */
    public String getGroupingEnd()
    {
        return this.groupingEnd;
    }

    /**
     * Set the values associated with occurences.
     *
     * @param _anyOccurence see {@link #occurenceAny}
     * @param _optionalOccurence see {@link #occurenceOptional}
     * @param _oneOrMoreOccurence see {@link #occurenceOneOrMore}
     * @param _occurencesStart see {@link #occurencesStart}
     * @param _occurencesEnd see {@link #occurencesEnd}
     */
    protected void setOccurences(String _anyOccurence, String _optionalOccurence, String _oneOrMoreOccurence,
                                 String _occurencesStart, String _occurencesEnd)
    {
        this.occurenceAny = _anyOccurence;
        this.occurenceOptional = _optionalOccurence;
        this.occurenceOneOrMore = _oneOrMoreOccurence;
        this.occurencesStart = _occurencesStart;
        this.occurencesEnd = _occurencesEnd;
    }

    /**
     * Get the string for any number of occurences.
     *
     * @return a string
     */
    public String getOccurenceAny()
    {
        return this.occurenceAny;
    }

    /**
     * Get the string for optional occurences.
     *
     * @return a string
     */
    public String getOccurenceOptional()
    {
        return this.occurenceOptional;
    }

    /**
     * Get the string for one or more occurences.
     *
     * @return a string
     */
    public String getOccurenceOneOrMore()
    {
        return this.occurenceOneOrMore;
    }

    /**
     * Get the string for the start of an occurence range.
     *
     * @return a string
     */
    public String getOccurencesStart()
    {
        return this.occurencesStart;
    }

    /**
     * Get the string for the end of an occurence range.
     *
     * @return a string
     */
    public String getOccurencesEnd()
    {
        return this.occurencesEnd;
    }

    /**
     * Get the string for the start of a list.
     *
     * @return a string
     */
    public String getListStart()
    {
        return this.listStart;
    }

    /**
     * Get the string for the end of a list.
     *
     * @return a string
     */
    public String getListEnd()
    {
        return this.listEnd;
    }

    /**
     * Escape a character for use in regular expressions.
     *
     * @param ch any character
     * @return a regular expression string for that character, properly escaped.
     */
    public String character(int ch)
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
            return this.defaultCharacter(ch);
        }
    }

    /**
     * The base implementation of character calls this method once it has
     * completed per-character processing.
     *
     * @param ch the code point for the character
     * @return a regular expression string for a single character
     */
    protected String defaultCharacter(int ch)
    {
        if ((ch > 0x1f) && (ch < 0x7f)) // (use \\u for all unicode)
        {
            return Character.toString((char) ch);
        }
        String hex = RegexSyntax.hexChar(ch);
        if (hex.length() == 2)
        {
            return "\\x" + hex;
        }
        return "\\u" + hex;
    }

    /**
     * Produces a hexadecimal string with an even number of characters, padding
     * with leading zeroes as necessary.
     *
     * @param ch the unicode code point
     * @return a hexadecimal string
     */
    public static String hexChar(int ch)
    {
        String hexChars = Integer.toHexString(ch);
        return (((hexChars.length() % 2) == 1) ? "0" : "") + hexChars;
    }

    /**
     * Set whether detecting special ranges is enabled.
     *
     * @param _specialRanges boolean
     */
    public void setSpecialRanges(boolean _specialRanges)
    {
        this.specialRanges = _specialRanges;
    }

    /**
     * Single character range method, provides brackets.
     *
     * @see #range(CharRange, boolean)
     */
    public String range(CharRange cr)
    {
        return this.range(cr, true);
    }

    /**
     * Create a regular expression for a character range.
     *
     * @param cr the range
     * @param brackets whether to add brackets or not
     * @return "\\d" (for [0-9] ranges) or the default range. Any class
     *         overriding this method must ensure returned values less than
     *         three characters long do not require brackets.
     */
    public String range(CharRange cr, boolean brackets)
    {
        if (this.specialRanges && (cr.getStart() == '0') && (cr.getEnd() == '9'))
        {
            return "\\d";
        }
        if (cr.getStart() < cr.getEnd())
        {
            StringBuilder bld = new StringBuilder();
            if (brackets)
            {
                bld.append(this.getListStart());
            }
            bld.append(this.character(cr.getStart()));
            if (cr.getStart() + 1 < cr.getEnd())
            {
                bld.append('-');
            }
            bld.append(this.character(cr.getEnd()));
            if (brackets)
            {
                bld.append(this.getListEnd());
            }
            return bld.toString();
        }
        return this.character(cr.getStart());
    }

    /**
     * Sets the choice separator (usually "|")
     *
     * @param _choice the separator for choice
     */
    protected void setChoiceSeparator(String _choice)
    {
        this.choice = _choice;
    }

    /**
     * Get the choice separator
     *
     * @return the choice separator
     */
    public String getChoiceSeparator()
    {
        return this.choice;
    }

    /**
     * The wildcard string.
     *
     * @param _wildcard the wildcard to set
     */
    protected void setWildcard(String _wildcard)
    {
        this.wildcard = _wildcard;
    }

    /**
     * Get the wildcard string, usually '.'
     *
     * @return the wildcard
     */
    public String getWildcard()
    {
        return this.wildcard;
    }

    /**
     * Java Syntax
     */
    private static final class JavaRegexSyntax extends RegexSyntax
    {
        JavaRegexSyntax()
        {
            super(RegexSyntax.SYNTAX_JAVA);
            this.setGrouping("(?:", ")");
        }

        @Override
        protected String defaultCharacter(int ch)
        {
            if (Character.charCount(ch) == 2)
            {
                char[] chars = Character.toChars(ch);
                return "\\u" + RegexSyntax.hexChar(chars[0]) + "\\u" + RegexSyntax.hexChar(chars[1]);
            }
            return super.defaultCharacter(ch);
        }
    }

    /**
     * XML Schema Syntax
     */
    private static class XmlSchemaRegexSyntax extends RegexSyntax
    {
        /** Whether ASCII encoding for the output is necessary. */
        private final boolean ascii;

        XmlSchemaRegexSyntax(boolean _ascii)
        {
            super(_ascii ? RegexSyntax.SYNTAX_XMLSCHEMA_ASCII : RegexSyntax.SYNTAX_XMLSCHEMA);
            this.ascii = _ascii;
            this.setGrouping("(", ")");
        }

        @Override
        public String character(int ch)
        {
            switch (ch)
            {
            case '&':
                return "&amp;";
            case '"':
                return "&quot;";
            case '<':
                return "&lt;";
            }
            return super.character(ch);
        }

        @Override
        protected String defaultCharacter(int ch)
        {
            if ((!this.ascii && Character.isLetterOrDigit(ch)) || (ch > 0x1f) && (ch < 0x7f))
            {
                return Character.toString((char) ch);
            }
            return "&#x" + RegexSyntax.hexChar(ch) + ';';
        }
    }

    /**
     * Posix Syntax, as used by sed, grep, etc...
     */
    private static class PosixRegexSyntax extends RegexSyntax
    {
        PosixRegexSyntax()
        {
            super(RegexSyntax.SYNTAX_POSIX);
            this.setGrouping("\\(", "\\)");
            this.setChoiceSeparator("\\|");
            this.setOccurences("\\*", "\\?", "\\+", "\\{", "\\}");
            this.setSpecialRanges(false);
        }

        @Override
        public String character(int ch)
        {
            // Character quoting is rather simple for SED
            switch (ch)
            {
            case '\n':
                return "\\n";
            case '$':
            case '*':
            case '.':
            case '[':
            case '\\':
            case '^':
                return "\\" + String.valueOf((char) ch);
            default:
                return String.valueOf((char) ch);
            }
        }
    }

    /**
     * Perl Syntax, as used by sed, grep, etc...
     */
    private static class PerlRegexSyntax extends RegexSyntax
    {
        PerlRegexSyntax()
        {
            super(RegexSyntax.SYNTAX_PERL);
        }

        @Override
        protected String defaultCharacter(int ch)
        {
            if (Character.isLetterOrDigit(ch) || (ch > 0x1f) && (ch < 0x7f))
            {
                return Character.toString((char) ch);
            }
            String hexChar = RegexSyntax.hexChar(ch);
            if (hexChar.length() > 2)
            {
                return "\\x{" + hexChar + '}';
            }
            return "\\x" + hexChar;
        }

        @Override
        public String character(int ch)
        {
            // Character quoting is rather simple for SED
            switch (ch)
            {
            case '\n':
                return "\\n";
            case '$':
            case '*':
            case '.':
            case '[':
            case '\\':
            case '^':
                return "\\" + String.valueOf((char) ch);
            default:
                return String.valueOf((char) ch);
            }
        }
    }
}
