package org.codehaus.mojo.xml.format;

import java.io.File;

/**
 * A violation of a prescribed XML formatting.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class XmlFormatViolation
{
    private final int column;

    private final File file;

    private final int lineNumber;

    private final String message;

    public XmlFormatViolation( File file, int lineNumber, int column, String message )
    {
        super();
        this.file = file;
        this.lineNumber = lineNumber;
        this.column = column;
        this.message = message;
    }

    /**
     * @return the column where the violation was detected. The first column number is 1
     */
    public int getColumn()
    {
        return column;
    }

    /**
     * @return the file in which the violation was detected.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @return the line number where the violation was detected. The first line number is 1
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * @return the message describing the violation
     */
    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return file.getAbsolutePath() + ":" + lineNumber + "," + column + ": " + message;
    }
}