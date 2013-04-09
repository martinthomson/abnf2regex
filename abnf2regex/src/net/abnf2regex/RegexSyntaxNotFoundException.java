package net.abnf2regex;

/**
 * Throw when a regular expression syntax cannot be found.
 */
public class RegexSyntaxNotFoundException extends Exception
{
    /** UID */
    private static final long serialVersionUID = -5137959287873094832L;
    /** Syntax name */
    private final String name;

    /**
     * Make a new exception with a message.
     *
     * @param message the message
     * @param _name the name of the syntax that was sought
     */
    public RegexSyntaxNotFoundException(String message, String _name)
    {
        super(message);
        this.name = _name;
    }

    /**
     * Get the name of the syntax that was sought.
     * @return the name of the syntax that couldn't be found
     */
    public String getSyntaxName()
    {
        return this.name;
    }
}
