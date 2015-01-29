package net.abnf2regex;

/**
 * Thrown when ABNF contains rules that are referenced by name in other rules,
 * but those rules are not properly resolved.
 *
 * @see RuleDictionary#resolve()
 */
public class RuleResolutionException extends Exception
{
    private static final long serialVersionUID = -9101503126559607394L;

    /**
     * Make a new exception with a message.
     *
     * @param message the message
     */
    public RuleResolutionException(String message)
    {
        super(message);
    }
}
