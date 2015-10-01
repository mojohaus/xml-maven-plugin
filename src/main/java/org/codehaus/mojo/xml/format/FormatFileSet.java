package org.codehaus.mojo.xml.format;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.FileSet;

/**
 * An extension of {@link FileSet} that adds {@link #encoding} and {@link #indentSize} fields.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class FormatFileSet
    extends FileSet
{
    private static final long serialVersionUID = 2128636607981252229L;

    private static final List<String> DEFAULT_INCLUDED_EXTENSIONS =
        Collections.unmodifiableList( Arrays.asList( "xml", "xsl" ) );

    public static final List<String> DEFAULT_EXCLUDES = Collections.unmodifiableList( Arrays.asList( "**/.*" ) );

    public static FormatFileSet getDefault( File baseDir, String encoding, int indentSize )
    {

        FormatFileSet result = new FormatFileSet();
        result.setDirectory( baseDir.getAbsolutePath() );
        result.setEncoding( encoding );
        result.setIndentSize( indentSize );

        List<String> includes = new ArrayList<String>( DEFAULT_INCLUDED_EXTENSIONS.size() * 2 );
        for ( String ext : DEFAULT_INCLUDED_EXTENSIONS )
        {
            includes.add( "*." + ext );
            includes.add( "src/**/*." + ext );
        }
        result.setIncludes( includes );
        result.setExcludes( DEFAULT_EXCLUDES );

        return result;
    }

    /** The encoding used to read the XML files. */
    private String encoding;

    /** The number of spaces expected for indentation. */
    private int indentSize;

    /**
     * @return the number of spaces for indentation
     */
    public int getIndentSize()
    {
        return indentSize;
    }

    /**
     * @return the encoding used to read the XML files
     */
    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    public void setIndentSize( int indentSize )
    {
        this.indentSize = indentSize;
    }

}
