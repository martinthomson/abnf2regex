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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A class that adds a few helpful functions to the standard reader class, such as the ability to peek ahead at the next
 * character.
 */
public class AbnfReader extends FilterReader
{
    /** A special value for peeked that indicates that it has no value. */
    private static final int NO_PEEK = -5;
    /** The next value, set to {@link #NO_PEEK} if there isn't one. */
    private int peeked = NO_PEEK;

    /** The current line number. */
    private int line = 1;
    /** The stream name. */
    private final String filename;
    /** used by the next line counter */
    private int lastEof = -1;
    /** used by the character counter */
    private int column = 1;

    /**
     * Gets the line number of the next character to be read.
     *
     * @return the line
     */
    public int getLine()
    {
        return this.line;
    }

    /**
     * Gets the column of the next character to be read.
     *
     * @return the column
     */
    public int getColumn()
    {
        return this.column;
    }

    /**
     * Get the name of the stream.
     *
     * @return the filename
     */
    public String getFilename()
    {
        return this.filename;
    }

    /**
     * Create a new reader.
     *
     * @param r The stream to filter.
     * @param _filename the name of the file/stream that is being read.
     */
    public AbnfReader(Reader r, String _filename)
    {
        super(r);
        this.filename = _filename;
    }

    /**
     * Take a look at the next character, without affecting the stream position.
     *
     * @return the next character as an int, -1 if there are no more characters.
     * @throws IOException if the read fails
     */
    public int peek() throws IOException
    {
        if (this.peeked == NO_PEEK)
        {
            this.peeked = super.read();
        }
        return this.peeked;
    }

    private int reallyRead() throws IOException
    {
        if (this.peeked != NO_PEEK)
        {
            int r = this.peeked;
            this.peeked = NO_PEEK;
            return r;
        }
        return super.read();
    }

    @Override
    public int read() throws IOException
    {
        int c = reallyRead();
        if ((c == '\n' && this.lastEof != '\r') || c == '\r') // count EOL
        {
            this.line++;
            this.column = 1;
        }
        else if (c != '\n')
        {
            this.column ++;
        }
        this.lastEof = c;
        return c;
    }

    /**
     * Determine whether the end of file has arrived. It does this by {@link #peek()}ing.
     *
     * @return true if there is no more content in the file.
     * @throws IOException if the read fails
     */
    public boolean eof() throws IOException
    {
        return this.peek() == -1;
    }

    /**
     * Consumes input as long as that input is whitespace and not and end-of-line character.
     *
     * @return the amount of whitespace consumed.
     * @throws IOException if a read fails
     * @see Character#isWhitespace(int)
     */
    public int gobbleWhitespace() throws IOException
    {
        // prime this.peeked for the looping part, which uses none of the facilities provided by this class and
        // maintains this.peeked for itself
        this.peek();
        int ws = 0;
        while (Character.isWhitespace(this.peeked) && (this.peeked != '\n') && (this.peeked != '\r'))
        {
            ++ws;
            this.peeked = super.read();
        }
        return ws;
    }

    /**
     * Consumes input up to the next line or the end of the file. Supports "\n", "\r", and "\r\n" end-of-line.
     *
     * @throws IOException if a read fails
     */
    public void findNextLine() throws IOException
    {
        while (!this.eof() && this.peek() != '\r' && this.peek() != '\n')
        {
            this.read();
        }
        if (!this.eof())
        {
            int last = this.read();
            if (!this.eof() && last == '\r' && this.peek() == '\n')
            {
                this.read();
            }
        }
    }

    /**
     * Parses out a name string, composed of letters, digits and '-' or '_'.
     *
     * @return The name string, which might be empty if no characters of the desired type were present.
     * @throws IOException if a read fails.
     */
    public String parseName() throws IOException
    {
        StringBuilder bld = new StringBuilder();
        while (!this.eof() && (Character.isLetterOrDigit(this.peek()) || this.peek() == '-' || this.peek() == '_'))
        {
            bld.append((char) this.read());
        }
        return bld.toString();
    }

    /**
     * Parses out a positive integer. Consumes digits until there are no more, so integer overflow is possible.
     *
     * @param radix the radix of the number.
     * @return The number, 0 if there were no digits.
     * @throws IOException if a read fails.
     */
    public int parseNumber(int radix) throws IOException
    {
        int num = 0;
        while (!this.eof() && Character.digit((char) this.peek(), radix) >= 0)
        {
            num = num * radix + Character.digit((char) this.read(), radix);
        }
        return num;
    }

    /**
     * Parses out a positive integer, radix 10.
     *
     * @return the number, radix 10.
     * @throws IOException if a read fails.
     * @see #parseNumber(int)
     */
    public int parseNumber() throws IOException
    {
        return this.parseNumber(10);
    }
}
