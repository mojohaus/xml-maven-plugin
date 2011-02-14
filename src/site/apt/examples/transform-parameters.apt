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

           -----------------------------
           Setting stylesheet parameters
           -----------------------------

Setting stylesheet parameters

  The following configuration snippet would set the stylesheet parameter
  <<<MyParam>>> to <<<true>>>:

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
              <parameters>
                <parameter>
                  <name>MyParam</name>
                  <value>true</value>
                </parameter>
              </parameters>
            </transformationSet>
          </transformationSets>
        </configuration>
      </plugin>
      ...
    </plugins>
  </build>
+----------------------------------------------------------------------------------
