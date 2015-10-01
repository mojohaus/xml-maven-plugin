package org.codehaus.mojo.xml.format;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Deque;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@link DefaultHandler} implementation that detects formatting violations and reports them to the supplied
 * {@link #violationHandler}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class IndentCheckSaxHandler
    extends DefaultHandler
{

    /**
     * An entry that can be stored on a stack
     */
    private static class ElementEntry
    {
        private final String elementName;

        private final IndentCheckSaxHandler.Indent expectedIndent;

        private final IndentCheckSaxHandler.Indent foundIndent;

        public ElementEntry( String elementName, IndentCheckSaxHandler.Indent foundIndent )
        {
            super();
            this.elementName = elementName;
            this.foundIndent = foundIndent;
            this.expectedIndent = foundIndent;
        }

        public ElementEntry( String elementName, IndentCheckSaxHandler.Indent foundIndent,
                             IndentCheckSaxHandler.Indent expectedIndent )
        {
            super();
            this.elementName = elementName;
            this.foundIndent = foundIndent;
            this.expectedIndent = expectedIndent;
        }

        @Override
        public String toString()
        {
            return "<" + elementName + "> " + foundIndent;
        }
    }

    /**
     * An indent occurrence within a file characterized by {@link #lineNumber} and {@link #size}.
     */
    private static class Indent
    {

        /**
         * An {@link Indent} usable at the beginning of a typical XML file.
         */
        public static final IndentCheckSaxHandler.Indent START = new Indent( 1, 0 );

        /**
         * The line number where this {@link Indent} occurs. The first line number in a file is {@code 1}.
         */
        private final int lineNumber;

        /** The number of spaces in this {@link Indent}. */
        private final int size;

        public Indent( int lineNumber, int size )
        {
            super();
            this.lineNumber = lineNumber;
            this.size = size;
        }

        @Override
        public String toString()
        {
            return "Indent [size=" + size + ", lineNumber=" + lineNumber + "]";
        }
    }

    private final StringBuilder charBuffer = new StringBuilder();

    private int charLineNumber;

    /** The file being checked */
    private final File file;

    /** The number of spaces for indentation */
    private final int indentSize;

    private IndentCheckSaxHandler.Indent lastIndent = Indent.START;

    /** The locator set by {@link SAXParser} */
    private Locator locator;

    /** The element stack */
    private Deque<IndentCheckSaxHandler.ElementEntry> stack =
        new java.util.ArrayDeque<IndentCheckSaxHandler.ElementEntry>();

    /** The {@link XmlFormatViolationHandler} for reporting found violations */
    private final XmlFormatViolationHandler violationHandler;

    public IndentCheckSaxHandler( File file, int indentSize, XmlFormatViolationHandler violationHandler )
    {
        super();
        this.file = file;
        this.indentSize = indentSize;
        this.violationHandler = violationHandler;
    }

    /**
     * Stores the passed characters into {@link #charBuffer}.
     *
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters( char[] ch, int start, int length )
        throws SAXException
    {
        charBuffer.append( ch, start, length );
        charLineNumber = locator.getLineNumber();
    }

    /**
     * Checks indentation for an end element.
     *
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    @Override
    public void endElement( String uri, String localName, String qName )
        throws SAXException
    {
        flushCharacters();
        if ( stack.isEmpty() )
        {
            throw new IllegalStateException( "Stack must not be empty when closing the element " + qName
                + " around line " + locator.getLineNumber() + " and column " + locator.getColumnNumber() );
        }
        IndentCheckSaxHandler.ElementEntry startEntry = stack.pop();
        int indentDiff = lastIndent.size - startEntry.expectedIndent.size;
        int expectedIndent = startEntry.expectedIndent.size;
        if ( lastIndent.lineNumber != startEntry.foundIndent.lineNumber && indentDiff != 0 )
        {
            /*
             * diff should be zero unless we are on the same line as start element
             */
            int opValue = expectedIndent - lastIndent.size;
            String op = opValue > 0 ? "Insert" : "Delete";
            String units = opValue == 1 ? "space" : "spaces";
            String message = op + " " + Math.abs( opValue ) + " " + units + ". Expected " + expectedIndent + " found "
                + lastIndent.size + " spaces before end element </" + qName + ">";
            XmlFormatViolation violation =
                new XmlFormatViolation( file, locator.getLineNumber(), locator.getColumnNumber(), message );
            violationHandler.handle( violation );
        }
    }

    /**
     * Sets {@link lastIndent} based on {@link #charBuffer} and resets {@link #charBuffer}.
     */
    private void flushCharacters()
    {
        int indentLength = 0;
        int len = charBuffer.length();
        /*
         * Count characters from end of ignorable whitespace to first end of line we hit
         */
        for ( int i = len - 1; i >= 0; i-- )
        {
            char ch = charBuffer.charAt( i );
            switch ( ch )
            {
                case '\n':
                case '\r':
                    lastIndent = new Indent( charLineNumber, indentLength );
                    charBuffer.setLength( 0 );
                    return;
                case ' ':
                case '\t':
                    indentLength++;
                    break;
                default:
                    /*
                     * No end of line foundIndent in the trailing whitespace. Leave the foundIndent from previous
                     * ignorable whitespace unchanged
                     */
                    charBuffer.setLength( 0 );
                    return;
            }
        }
    }

    /**
     * Just delegates to {@link #characters(char[], int, int)}, since this method is not called in all situations where
     * it could be naively expected.
     *
     * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace( char[] chars, int start, int length )
        throws SAXException
    {
        characters( chars, start, length );
    }

    /**
     * Always returns an empty {@link InputSource} to avoid loading of any DTDs.
     *
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity( String publicId, String systemId )
        throws SAXException, IOException
    {
        return new InputSource( new StringReader( "" ) );
    }

    /** @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator) */
    @Override
    public void setDocumentLocator( Locator locator )
    {
        this.locator = locator;
    }

    /**
     * Checks indentation for a start element.
     *
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes )
        throws SAXException
    {
        flushCharacters();
        IndentCheckSaxHandler.ElementEntry currentEntry = new ElementEntry( qName, lastIndent );
        if ( !stack.isEmpty() )
        {
            IndentCheckSaxHandler.ElementEntry parentEntry = stack.peek();
            /*
             * note that we use parentEntry.expectedIndent rather than parentEntry.foundIndent this is to make the
             * messages more useful
             */
            int indentDiff = currentEntry.foundIndent.size - parentEntry.expectedIndent.size;
            int expectedIndent = parentEntry.expectedIndent.size + indentSize;
            if ( indentDiff == 0 && currentEntry.foundIndent.lineNumber == parentEntry.foundIndent.lineNumber )
            {
                /*
                 * Zero foundIndent acceptable only if current is on the same line as parent This is OK, therefore do
                 * nothing
                 */
            }
            else if ( indentDiff != indentSize )
            {
                /* generally unexpected foundIndent */
                int opValue = expectedIndent - currentEntry.foundIndent.size;
                String op = opValue > 0 ? "Insert" : "Delete";
                String message = op + " " + Math.abs( opValue ) + " spaces. Expected " + expectedIndent + " found "
                    + currentEntry.foundIndent.size + " spaces before start element <" + currentEntry.elementName + ">";

                XmlFormatViolation violation =
                    new XmlFormatViolation( file, locator.getLineNumber(), locator.getColumnNumber(), message );
                violationHandler.handle( violation );

                /* reset the expected indent in the entry we'll push */
                currentEntry =
                    new ElementEntry( qName, lastIndent, new Indent( lastIndent.lineNumber, expectedIndent ) );
            }
        }
        stack.push( currentEntry );
    }

}