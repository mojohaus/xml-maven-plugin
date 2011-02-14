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

           -------------------------------------------
           Change the extension of the generated files
           -------------------------------------------

Change the extension of the generated files

  The following configuration snippet would transform all files
  in the directory <<<src/main/xml>>> by applying the stylesheet
  <<<src/main/stylesheet.xsl>>>. The created files would have
  the extension <<<.fo>>>, rather than <<<.xml>>>.

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
              <fileMappers>
                <fileMapper implementation="org.codehaus.plexus.components.io.filemappers.FileExtensionMapper">
                  <targetExtension>.fo</targetExtension>
                </fileMapper>
              </fileMappers>
            </transformationSet>
          </transformationSets>
        </configuration>
      </plugin>
      ...
    </plugins>
  </build>
+----------------------------------------------------------------------------------

  Btw, the same could be achieved the much more powerful and generic RegExpFileMapper,
  which you find explained in more detail in {{{./transform-change-regexp.html}another
  example}}.
