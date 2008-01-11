package org.codehaus.mojo.xml;

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

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;


/**
 * Implementation of {@link LSInput}, for use by the {@link Resolver}.
 */
class LSInputImpl
    implements LSInput
{
    private String baseURI, encoding, systemId, publicId, stringData;

    private InputStream byteStream;

    private Reader characterStream;

    private boolean certifiedText;

    public String getBaseURI()
    {
        return baseURI;
    }

    public InputStream getByteStream()
    {
        return byteStream;
    }

    public boolean getCertifiedText()
    {
        return certifiedText;
    }

    public Reader getCharacterStream()
    {
        return characterStream;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public String getPublicId()
    {
        return publicId;
    }

    public String getStringData()
    {
        return stringData;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public void setBaseURI( String pBaseURI )
    {
        baseURI = pBaseURI;
    }

    public void setByteStream( InputStream pByteStream )
    {
        byteStream = pByteStream;
    }

    public void setCertifiedText( boolean pCertifiedText )
    {
        certifiedText = pCertifiedText;
    }

    public void setCharacterStream( Reader pCharacterStream )
    {
        characterStream = pCharacterStream;
    }

    public void setEncoding( String pEncoding )
    {
        encoding = pEncoding;
    }

    public void setPublicId( String pPublicId )
    {
        publicId = pPublicId;
    }

    public void setStringData( String pStringData )
    {
        stringData = pStringData;
    }

    public void setSystemId( String pSystemId )
    {
        systemId = pSystemId;
    }
}
