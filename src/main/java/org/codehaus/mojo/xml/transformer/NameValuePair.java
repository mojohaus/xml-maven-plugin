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


/**
 * An instance of this class is used to specify an output property.
 */
public class NameValuePair
{
    private String name, value;

    /**
     * Returns the parameter name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the parameter name.
     */
    public void setName( String pName )
    {
        name = pName;
    }

    /**
     * Returns the parameter value.
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Sets the parameter value.
     */
    public void setValue( String pValue )
    {
        value = pValue;
    }
}
