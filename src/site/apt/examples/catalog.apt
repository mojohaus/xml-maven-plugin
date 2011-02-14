~~
~~
~~  Copyright 2011  The Apache Software Foundation
~~
~~  Licensed under the Apache License, Version 2.0 (the "License");
~~  you may not use this file except in compliance with the License.
~~  You may obtain a copy of the License at
~~
~~  http://www.apache.org/licenses/LICENSE-2.0
~~
~~  Unless required by applicable law or agreed to in writing, software
~~  distributed under the License is distributed on an "AS IS" BASIS,
~~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~~  See the License for the specific language governing permissions and
~~  limitations under the License.
~~
~~
    -------------
    Catalog files
    -------------

Catalog files

    It is not unusual that XML parsers or stylesheet processors try to access
    external resources. For example if you specify an external schema, the XML
    parser might try to load the external schema in order to validate your
    XML file:

+--------------------------------------------------------------------------
  <xsl:stylesheet version="2.0"
       xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.w3.org/1999/XSL/Transform http://www.w3.org/2007/schema-for-xslt20.xsd">
    ...
  </xsl:stylesheet>
+--------------------------------------------------------------------------

    In this example the XML parser would likely attempt to load an XML schema
    from <<<http://www.w3.org/2007/schema-for-xslt20.xsd>>>.

    Another example might be the use of a DTD:

+--------------------------------------------------------------------------
  <!DOCTYPE doc PUBLIC "-COUNTER-" "http://www.example.com/sample.dtd">
  <doc><header>A sample document.</header></doc>
+--------------------------------------------------------------------------

    In this case, the XML parser would likely attempt to connect to
    <<<http://www.example.com/sample.dtd>>>.

    This is all very well, if you are connected to the Internet, with
    suitably configured proxy servers. But if that is not the case,
    then you will likely encounter trouble.

    The solution is using local copies of these external resources
    and a catalog file. This is best explained by an example catalog
    file:

+--------------------------------------------------------------------------
  <catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
    <public publicId="--COUNTER--" uri="./sample.dtd"/>
    <system systemId="http://www.w3.org/2007/schema-for-xslt20.xsd" uri="./schema-for-xslt20.xsd"/>
  </catalog>
+--------------------------------------------------------------------------

    The example shows to things:

    * If the XML parser is searching for a file with a <<public id>> of
      <<<--COUNTER-->>>, then the catalog advises to use the local
      file <<<./doc-schema.xsd>>> instead.

    * If the XML parser or stylesheet processor is searching for a file
      with the <<system id>> <<<http://www.w3.org/2007/schema-for-xslt20.xsd>>>,
      then the catalog requests to use the local file <<<./schema-for-xslt20.xsd>>>
      instead.

    It doesn't matter whether you use the public id or the system id. The important
    thing to know is what the respective public id and the system id are. In
    our first example, we had a public id of <<<http://www.w3.org/1999/XSL/Transform>>>
    (the namespace URI) and a system id of <<<http://www.w3.org/2007/schema-for-xslt20.xsd>>>
    (the URL from which to actually load the schema). In the second example, the
    public id was <<<--COUNTER-->> and the system id <<<http://www.example.com/sample.dtd>>>.

    In other words, we might as well rewrite our catalog file like this:

+--------------------------------------------------------------------------
  <catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
    <public publicId="http://www.w3.org/1999/XSL/Transform" uri="./schema-for-xslt20.xsd"/>
    <system systemId="http://www.example.com/sample.dtd" uri="./sample.dtd"/>
  </catalog>
+--------------------------------------------------------------------------

    We might as well use only public id's or system id's.

How to use a catalog file

    To use a catalog file, you must specify it in your POM.

+--------------------------------------------------------------------------
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>xml-maven-plugin</artifactId>
        <version>1.0</version>
        <configuration>
          <validationSets>
            <validationSet>
              <dir>src/main/xml</dir>
              <includes>
                <include>doc1.xml</include>
              </includes>
            </validationSet>
          </validationSets>
          <catalogs>
            <catalog>src/main/xml/catalog.xml</catalog>
          </catalogs>
        </configuration>
      </plugin>
+--------------------------------------------------------------------------

    In this example, the catalog file would be stored in <<<src/main/xml/catalog.xml>>>.
    To use a catalog file for transformation, just use the same <<<catalogs>>> element
    in your <<<configuration>>> section.

Relative paths in the catalog file

    An important point to make: How are relative paths in the catalog file interpreted?
    Suggest that the catalog file is stored in <<<src/main/xml/catalog.xml>>> and the
    catalog file contains the following entry:

+--------------------------------------------------------------------------
    <public publicId="--COUNTER--" uri="./sample.dtd"/>
+--------------------------------------------------------------------------

    The answer is that the URI is interpreted relative to the catalog files. In other
    words, the <<<sample.dtd>>> is stored in <<<src/main/xml/sample.dtd>>> and not
    in <<<${basedir}/sample.dtd>>>.
 