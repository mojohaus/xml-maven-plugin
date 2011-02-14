~~
~~
~~  Copyright 2005  The Apache Software Foundation
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

           -----------------------------------
           Using an alternative XSLT processor
           -----------------------------------

Using an alternative XSLT processor

  An alternative XSLT processor is important, if you want to use XSLT 2.0
  stylesheets or if you would like to work around a bug in your JRE's
  stylesheet processor. As of this writing, {{{http://saxon.sourceforge.net/}Saxon}}
  is the only XSLT 2.0 compliant processor, so we take this as an example.

  Changing the XSLT processor is as simple as specifying it as a dependency:

+----------------------------------------------------------------------------------
  <build>
    <plugins>
      ...
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>xml-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>transform</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <transformationSets>
            <transformationSet>
              <dir>src/main/xml</dir>
              <stylesheet>src/main/stylesheet.xsl</stylesheet>
            </transformationSet>
          </transformationSets>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>net.sf.saxon</groupId>
            <artifactId>saxon</artifactId>
            <version>8.7</version>
          </dependency>
        </dependencies>
      </plugin>
      ...
    </plugins>
  </build>
+----------------------------------------------------------------------------------

  If you would like to know, which XSLT processor and/or version you are actually
  using, add code like the following to your stylesheet:

+----------------------------------------------------------------------------------
  <xsl:comment>
    XSLT Version = <xsl:copy-of select="system-property('xsl:version')"/>
    XSLT Vendor = <xsl:copy-of select="system-property('xsl:vendor')"/>
    XSLT Vendor URL = <xsl:copy-of select="system-property('xsl:vendor-url')"/>
  </xsl:comment>
+----------------------------------------------------------------------------------
