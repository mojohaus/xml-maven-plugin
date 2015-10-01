package org.codehaus.mojo.xml.format;

/**
 * An interface for reporting {@link XmlFormatViolation}s.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface XmlFormatViolationHandler
{

    /**
     * Called when an {@link XmlFormatViolation} is found.
     *
     * @param violation the reported violation
     */
    void handle( XmlFormatViolation violation );
}