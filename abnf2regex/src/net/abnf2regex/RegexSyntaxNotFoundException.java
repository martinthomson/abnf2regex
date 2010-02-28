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
