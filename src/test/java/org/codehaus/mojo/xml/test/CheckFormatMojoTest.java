package org.codehaus.mojo.xml.test;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The check-format goal on the default file set, which is the POM and {@code src/main/xml/**}.
 */
class CheckFormatMojoTest extends AbstractXmlMojoTestCase {

    private final Log log = mock(Log.class);

    @BeforeEach
    void enableDebug() {
        when(log.isDebugEnabled()).thenReturn(true);
    }

    @Override
    protected String getGoal() {
        return "check-format";
    }

    @Test
    void passesOnCorrectlyIndentedFiles() throws Exception {
        AbstractXmlMojo mojo = newMojo("src/test/check-format-pass");
        mojo.setLog(log);

        mojo.execute();

        verify(log).debug(matches("No XML formatting violations found in file .*2-spaces-correct-no-schema\\.xml"));
        verify(log)
                .debug(matches("No XML formatting violations found in file .*2-spaces-correct-no-schema-short\\.xml"));
        verify(log).debug(matches("No XML formatting violations found in file .*pom\\.xml"));
        verify(log).debug("Checked the formatting of 3 files");
    }

    @Test
    void failsOnAMisindentedElement() throws Exception {
        AbstractXmlMojo mojo = newMojo("src/test/check-format-fail");
        mojo.setLog(log);

        assertThrows(MojoFailureException.class, mojo::execute);

        verify(log)
                .error(
                        endsWith(
                                "2-spaces-broken-no-schema-short.xml:5,14: Delete 1 spaces. Expected 4 found 5 spaces before start element <text-1>"));
        verify(log).debug(matches("No XML formatting violations found in file .*pom\\.xml"));
        verify(log).debug("Checked the formatting of 2 files");
    }
}
