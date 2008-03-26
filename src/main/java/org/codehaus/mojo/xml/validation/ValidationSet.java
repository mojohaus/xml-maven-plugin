package org.codehaus.mojo.xml.validation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;


/**
 * An instance of this class is used to specify a set of files,
 * which are validated against a common schema.
 */
public class ValidationSet
{
    private String publicId;

    private String systemId;

    private String schemaLanguage;

    private boolean validating;

    private File dir;

    private String[] includes;

    private String[] excludes;

    private boolean skipDefaultExcludes;

    /**
     * Returns a directory, which is scanned for files to validate.
     */
    public File getDir()
    {
        return this.dir;
    }

    /**
     * Returns patterns of files, which are being excluded from
     * the validation set.
     */
    public String[] getExcludes()
    {
        return excludes;
    }

    /**
     * Returns patterns of files, which are being included into
     * the validation set.
     */
    public String[] getIncludes()
    {
        return includes;
    }

    /**
     * Returns the schemas public ID. May be null, if the schema is
     * loaded through its system ID or if the documents are being
     * validated for wellformedness only.
     */
    public String getPublicId()
    {
        return publicId;
    }

    /**
     * Returns the schema language. May be null, if the documents
     * are being validated for wellformedness only, or if the default
     * schema language (W3C XML Schema) is being used. See
     * http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/validation/SchemaFactory.html
     * for possible values.
     */
    public String getSchemaLanguage()
    {
        return schemaLanguage;
    }

    /**
     * Returns the schemas system ID. May be null, if the schema is
     * loaded through its public ID or if the documents are being
     * validated for wellformedness only.
     */
    public String getSystemId()
    {
        return systemId;
    }

    /**
     * Returns, whether Maven's default excludes are being ignored.
     * Defaults to false (Default excludes are being used).
     */
    public boolean isSkipDefaultExcludes()
    {
        return skipDefaultExcludes;
    }

    /**
     * If the documents are being validated for wellformedness only:
     * Returns, whether the parser should be validating. (In
     * other words: Whether documents must contain a document type
     * or xml schema declaration.) The property is ignored otherwise.
     * The default value is false.
     */
    public boolean isValidating()
    {
        return validating;
    }

    /**
     * Sets a directory, which is scanned for files to validate.
     */
    public void setDir( File pDir )
    {
        dir = pDir;
    }

    /**
     * Sets patterns of files, which are being excluded from
     * the validation set.
     */
    public void setExcludes( String[] pExcludes )
    {
        excludes = pExcludes;
    }

    /**
     * Sets patterns of files, which are being included into
     * the validation set.
     */
    public void setIncludes( String[] pIncludes )
    {
        includes = pIncludes;
    }

    /**
     * Sets the schemas public ID. May be null, if the schema is
     * loaded through its system ID or if the documents are being
     * validated for wellformedness only.
     */
    public void setPublicId( String pPublicId )
    {
        publicId = pPublicId;
    }

    /**
     * Sets the schema language. May be null, if the documents
     * are being validated for wellformedness only, or if the default
     * schema language (W3C XML Schema) is being used. See
     * http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/validation/SchemaFactory.html
     * for possible values.
     */
    public void setSchemaLanguage( String pSchemaLanguage )
    {
        schemaLanguage = pSchemaLanguage;
    }

    /**
     * Sets, whether Maven's default excludes are being ignored.
     * Defaults to false (Default excludes are being used).
     */
    public void setSkipDefaultExcludes( boolean pSkipDefaultExcludes )
    {
        skipDefaultExcludes = pSkipDefaultExcludes;
    }

    /**
     * Sets the schemas system ID. May be null, if the schema is
     * loaded through its public ID or if the documents are being
     * validated for wellformedness only.
     */
    public void setSystemId( String pSystemId )
    {
        systemId = pSystemId;
    }

    /**
     * If the documents are being validated for wellformedness only:
     * Sets, whether the parser should be validating. (In
     * other words: Whether documents must contain a document type
     * or xml schema declaration.) The property is ignored otherwise.
     * The default value is false.
     */
    public void setValidating( boolean pValidating )
    {
        validating = pValidating;
    }
}
