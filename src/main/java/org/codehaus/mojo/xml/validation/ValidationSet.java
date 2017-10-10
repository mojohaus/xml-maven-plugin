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
 * An instance of this class is used to specify a set of files, which are validated against a common schema.
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
    
    private boolean xincludeAware;

    /**
     * Returns a directory, which is scanned for files to validate.
     * @return The directory to scan.
     */
    public File getDir()
    {
        return this.dir;
    }

    /**
     * Returns patterns of files, which are being excluded from the validation set.
     * @return Patters of excluded files.
     */
    public String[] getExcludes()
    {
        return excludes;
    }

    /**
     * Returns patterns of files, which are being included into the validation set.
     * @return Patters of included files.
     */
    public String[] getIncludes()
    {
        return includes;
    }

    /**
     * Returns the schemas public ID. May be null, if the schema is loaded through its system ID or if the documents are
     * being validated for wellformedness only.
     * @return The schemas public ID, if available, or null.
     */
    public String getPublicId()
    {
        return publicId;
    }

    /**
     * Returns the schema language. May be null, if the documents are being validated for wellformedness only, or if the
     * default schema language (W3C XML Schema) is being used. See
     * http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/validation/SchemaFactory.html for possible values.
     * @return The schema language, if available, or null.
     */
    public String getSchemaLanguage()
    {
        return schemaLanguage;
    }

    /**
     * Returns the schemas system ID. May be null, if the schema is loaded through its public ID or if the documents are
     * being validated for wellformedness only.
     * @return The schemas system ID, if available, or null.
     */
    public String getSystemId()
    {
        return systemId;
    }

    /**
     * Returns, whether Maven's default excludes are being ignored. Defaults to false (Default excludes are being used).
     * @return Whether to ignore Maven's default excludes, or not. (Default=false, thus by default those
     * excludes are in place.)
     */
    public boolean isSkipDefaultExcludes()
    {
        return skipDefaultExcludes;
    }

    /**
     * If the documents are being validated for wellformedness only: Returns, whether the parser should be validating.
     * (In other words: Whether documents must contain a document type or xml schema declaration.) The property is
     * ignored otherwise. The default value is false.
     * @return Whether documents are being validated, or not.
     */
    public boolean isValidating()
    {
        return validating;
    }

    /**
     * Sets a directory, which is scanned for files to validate.
     * @param pDir The directory to scan.
     */
    public void setDir( File pDir )
    {
        dir = pDir;
    }

    /**
     * Sets patterns of files, which are being excluded from the validation set.
     * @param pExcludes Patters of excluded files.
     */
    public void setExcludes( String[] pExcludes )
    {
        excludes = pExcludes;
    }

    /**
     * Sets patterns of files, which are being included into the validation set.
     * @param pIncludes Patters of excluded files.
     */
    public void setIncludes( String[] pIncludes )
    {
        includes = pIncludes;
    }

    /**
     * Sets the schemas public ID. May be null, if the schema is loaded through its system ID or if the documents are
     * being validated for wellformedness only.
     * @param pPublicId The schemas public Id, if available, or null.
     */
    public void setPublicId( String pPublicId )
    {
        publicId = pPublicId;
    }

    /**
     * Sets the schema language. May be null, if the documents are being validated for wellformedness only, or if the
     * default schema language (W3C XML Schema) is being used. See
     * http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/validation/SchemaFactory.html for possible values.
     * @param pSchemaLanguage The schema language, if available, or null.
     */
    public void setSchemaLanguage( String pSchemaLanguage )
    {
        schemaLanguage = pSchemaLanguage;
    }

    /**
     * Sets, whether Maven's default excludes are being ignored. Defaults to false (Default excludes are being used).
     * @param pSkipDefaultExcludes Sets, whether to apply Maven's default ecxludes (false, default), or not (true).
     */
    public void setSkipDefaultExcludes( boolean pSkipDefaultExcludes )
    {
        skipDefaultExcludes = pSkipDefaultExcludes;
    }

    /**
     * Sets the schemas system ID. May be null, if the schema is loaded through its public ID or if the documents are
     * being validated for wellformedness only.
     * @param pSystemId The schemas system ID, if available, or null.
     */
    public void setSystemId( String pSystemId )
    {
        systemId = pSystemId;
    }

    /**
     * If the documents are being validated for wellformedness only: Sets, whether the parser should be validating. (In
     * other words: Whether documents must contain a document type or xml schema declaration.) The property is ignored
     * otherwise. The default value is false.
     * @param pValidating Whether documents are being validated (true), or not (false, default).
     */
    public void setValidating( boolean pValidating )
    {
        validating = pValidating;
    }
    
    /**
     * Returns, whether the validator should create xinclude aware XML parsers for reading XML documents. The default
     * value is false.
     * @return Whether XML parsers should be xinclude aware (true), or not (false, default).
     */    
    public boolean isXincludeAware()
    {
        return xincludeAware;
    }
    
    /**
     * Sets, whether the validator should create xinclude aware XML parsers for reading XML documents. The default value
     * is false.
     * @param pXIncludeAware Whether XML parsers should be xinclude aware (true), or not (false, default).
     */
    public void setXincludeAware(boolean pXIncludeAware)
    {
        xincludeAware = pXIncludeAware;
    }
}
