package org.codehaus.mojo.xml.transformer;

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

import org.codehaus.plexus.components.io.filemappers.FileMapper;


/**
 * An instance of this class is used to specify a set of files,
 * which are transformed by a common XSLT stylesheet.
 */
public class TransformationSet
{
    private String stylesheet;

    private File dir;

    private File outputDir;

    private boolean addedToClasspath;

    private String[] includes;

    private String[] excludes;

    private boolean skipDefaultExcludes;

    private String[] otherDepends;

    private NameValuePair[] parameters;

    private NameValuePair[] features;

    private NameValuePair[] attributes;

    private NameValuePair[] outputProperties;

    private FileMapper[] fileMappers;

    private boolean validating;

    /**
     * Sets patterns of files, which are being excluded from
     * the transformation set.
     */
    public void setExcludes( String[] pExcludes )
    {
        excludes = pExcludes;
    }

    /**
     * Sets patterns of files, which are being included into
     * the transformation set.
     */
    public void setIncludes( String[] pIncludes )
    {
        includes = pIncludes;
    }

    /**
     * Sets patterns of additional files, which are being considered
     * for the uptodate check.
     */
    public void setOtherDepends( String[] pOtherDepends )
    {
        otherDepends = pOtherDepends;
    }

    /**
     * Sets the stylesheet parameters.
     */
    public void setParameters( NameValuePair[] pParameters )
    {
        parameters = pParameters;
    }

    /**
     * Returns a directory, which is scanned
     * for files to transform.
     */
    public File getDir()
    {
        return dir;
    }

    /**
     * Returns patterns of files, which are being excluded from
     * the transformation set.
     */
    public String[] getExcludes()
    {
        return excludes;
    }

    /**
     * Returns patterns of files, which are being included into
     * the transformation set.
     */
    public String[] getIncludes()
    {
        return includes;
    }

    /**
     * Returns patterns of additional files, which are being considered
     * for the uptodate check.
     */
    public String[] getOtherDepends()
    {
        return otherDepends;
    }

    /**
     * Returns the output directory,
     * where the generated files are being placed. Defaults to
     * {project.build.directory}/generated-resources/xml/xslt.
     */
    public File getOutputDir()
    {
        return outputDir;
    }

    /**
     * Returns the stylesheet parameters.
     */
    public NameValuePair[] getParameters()
    {
        return parameters;
    }

    /**
     * Returns the XSLT stylesheet, which is being used to control
     * the transformation.
     */
    public String getStylesheet()
    {
        return stylesheet;
    }

    /**
     * Returns, whether the output directory is added to the classpath.
     * Defaults to false.
     */
    public boolean isAddedToClasspath()
    {
        return addedToClasspath;
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
     * Sets, whether the output directory is added to the classpath.
     * Defaults to false.
     */
    public void setAddedToClasspath( boolean pAddedToClasspath )
    {
        addedToClasspath = pAddedToClasspath;
    }

    /**
     * Sets the name of a directory, which is scanned
     * for files to transform.
     */
    public void setDir( File pDir )
    {
        dir = pDir;
    }

    /**
     * Sets the output directory,
     * where the generated files are being placed. Defaults to
     * {project.build.directory}/generated-resources/xml/xslt.
     */
    public void setOutputDir( File pOutputDir )
    {
        outputDir = pOutputDir;
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
     * Sets the XSLT stylesheet, which is being used to control
     * the transformation.
     */
    public void setStylesheet( String pStylesheet )
    {
        stylesheet = pStylesheet;
    }

    /**
     * Returns a set of file mappers, which are being used to
     * convert the generated files name.
     */
    public FileMapper[] getFileMappers()
    {
        return fileMappers;
    }

    /**
     * Sets a set of file mappers, which are being used to
     * convert the generated files name.
     */
    public void setFileMappers( FileMapper[] pFileMappers )
    {
        fileMappers = pFileMappers;
    }

    /**
     * Returns, whether the transformer should create validating XML parsers
     * for reading XML documents. The default value is false.
     */
    public boolean isValidating()
    {
        return validating;
    }

    /**
     * Sets, whether the transformer should create validating XML parsers
     * for reading XML documents. The default value is false.
     */
    public void setValidating( boolean pValidating )
    {
        validating = pValidating;
    }

    /**
     * Returns the transformers output properties.
     */
    public NameValuePair[] getOutputProperties()
    {
        return outputProperties;
    }

    /**
     * Sets the transformers output properties.
     */
    public void setOutputProperties( NameValuePair[] pOutputProperties )
    {
        outputProperties = pOutputProperties;
    }

    /**
     * Returns the features, which should be set on the transformer factory.
     */
    public NameValuePair[] getFeatures()
    {
        return features;
    }

    /**
     * Sets the features, which should be set on the transformer factory.
     */
    public void setFeatures( NameValuePair[] pFeatures )
    {
        features = pFeatures;
    }

    /**
     * Returns the attributes, which should be set on the transformer factory.
     */
    public NameValuePair[] getAttributes()
    {
        return attributes;
    }

    /**
     * Sets the attributes, which should be set on the transformer factory.
     */
    public void setAttributes( NameValuePair[] pAttributes )
    {
        attributes = pAttributes;
    }
}
