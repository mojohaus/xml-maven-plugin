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

           -----------------------------------------------------
           Transforming XML files by applying an XSLT stylesheet
           -----------------------------------------------------

Transforming XML files by applying an XSLT stylesheet

  The following configuration snippet would transform all files
  in the directory <<<src/main/xml>>> by applying the stylesheet
  <<<src/main/stylesheet.xsl>>>.

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
      </plugin>
      ...
    </plugins>
  </build>
+----------------------------------------------------------------------------------
