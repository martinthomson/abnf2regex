package net.abnf2regex;

/**
 * Generated when there is a parse error related to the format of the data.
 */
public class AbnfParseException extends Exception
{
    /** Serial */
    private static final long serialVersionUID = -4342800212936118132L;
    /** the line number of the error */
    private final int line;
    /** the source of the error */
    private final String file;
    /** the column number of the error */
    private final int column;

    /**
     * Generate a new exception at the given line and so forth.
     *
     * @param message the message
     * @param cause the root cause of the problem
     * @param abnf the reader
     */
    public AbnfParseException(String message, Throwable cause, AbnfReader abnf)
    {
        super(message, cause);
        this.file = abnf.getFilename();
        this.line = abnf.getLine();
        this.column = abnf.getColumn();
    }

    /**
     * Generate a new exception at the given line and so forth.
     *
     * @param message the message
     * @param abnf the reader, which is interrogated to locate the error
     */
    public AbnfParseException(String message, AbnfReader abnf)
    {
        super(message);
        this.file = abnf.getFilename();
        this.line = abnf.getLine();
        this.column = abnf.getColumn();
    }

    @Override
    public String getMessage()
    {
        return '[' + this.file + ':' + this.line + ':' + this.column + "] " + super.getMessage(); //$NON-NLS-1$
    }

    /**
     * @return the line
     */
    public int getLine()
    {
        return this.line;
    }

    /**
     * @return the column
     */
    public int getColumn()
    {
        return this.column;
    }

    /**
     * @return the file
     */
    public String getFile()
    {
        return this.file;
    }

}
