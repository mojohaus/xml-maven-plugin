/*
 * Copyright 2017 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.mojo.xml.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author rlamont
 */
public class ValidationErrorHandler implements ErrorHandler{
    
    private final List<ErrorRecord> errors=new ArrayList<ErrorRecord>();
    private final List<ErrorRecord> publicErrors = Collections.unmodifiableList(errors);
    private int warningCount=0;
    private int errorCount=0;
    private int fatalCount=0;
    private File context;

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        warningCount++;
        errors.add(new ErrorRecord(ErrorType.WARNING, exception,context));
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        errorCount++;
        errors.add(new ErrorRecord(ErrorType.ERROR, exception,context));
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        fatalCount++;
        errors.add(new ErrorRecord(ErrorType.FATAL, exception,context));
    }
    
    public List<ErrorRecord> getErrors(){
        return publicErrors;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getFatalCount() {
        return fatalCount;
    }

    public void setContext(File context) {
        this.context = context;
    }
    
    
    
    
    
    public enum ErrorType{
        WARNING{
            @Override
            public String toString() {
                return "warning";
            }
            
        },
        ERROR{
            @Override
            public String toString() {
                return "error";
            }
            
        },
        FATAL{
            @Override
            public String toString() {
                return "fatal error";
            }
            
        }
    }
    
    public class ErrorRecord{
        final ErrorType type;
        final SAXParseException exception;
        final File context;

        public ErrorRecord(ErrorType type, SAXParseException exception,File context) {
            this.type = type;
            this.exception = exception;
            this.context=context;
        }

        public boolean isError() {
            return type==ErrorType.ERROR;
        }

        public boolean isWarning() {
            return type==ErrorType.WARNING;
        }

        public boolean isFatal() {
            return type==ErrorType.ERROR;
        }
        
        public ErrorType getType(){
            return type;
        }

        public SAXParseException getException() {
            return exception;
        }

        public File getContext() {
            return context;
        }
        
        
        
        
    }
}
