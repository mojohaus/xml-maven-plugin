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

           -----------------------------------------------
           Use regular expressions to transform file names
           -----------------------------------------------

Use regular expressions to transform file names

  A particularly powerfule file mapper deserves more detailed
  description: It's the regular expression file mapper.

  This file mapper uses regular expressions to identify certain
  pieces in the input file name. This piece, or these pieces
  are replaced with a replacement pattern.

  Let's first design a configuration snippet, which would
  transform all files in the directory <<<src/main/xml>>> by
  applying the stylesheet <<<src/main/stylesheet.xsl>>>. The
  created files would have the extension <<<.fo>>>, rather than
  <<<.xml>>>. (We could very well achieve the same by using
  the <<<FileExtensionMapper>>>, as demonstrated in
  {{{./transform-change-extension.html}this example}}.)

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
                <fileMapper implementation="org.codehaus.plexus.components.io.filemappers.RegExpFileMapper">
                  <pattern>\.gif$</pattern>
                  <replacement>.jpg</replacement>
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

  Let's have a look at some other examples. We omit most of the details,
  except for the pattern and the replacement, which are the important parts:

+----------------------------------------------------------------------------------
    <fileMapper implementation="org.codehaus.plexus.components.io.filemappers.RegExpFileMapper">
      <pattern>^(.*)\.gif$</pattern>
      <replacement>$1.jpg</replacement>
    </fileMapper>
+----------------------------------------------------------------------------------

  This example is identical with the previous. However, it demonstrates
  the use of <capture groups>. The contents of <<<(.*)>>> are captured and
  inserted by using the group term <<<$1>>>. You might have multiple groups
  and use the terms <<<$2>>>, <<<$3>>>, and so on.

+----------------------------------------------------------------------------------
    <fileMapper implementation="org.codehaus.plexus.components.io.filemappers.RegExpFileMapper">
      <pattern>a</pattern>
      <replacement>b</replacement>
    </fileMapper>
+----------------------------------------------------------------------------------

  This example replaces the <<first>> a with a b. Nothing special here, except
  that you might like to replace <<all>> a's instead. Here's how to do it:

+----------------------------------------------------------------------------------
    <fileMapper implementation="org.codehaus.plexus.components.io.filemappers.RegExpFileMapper">
      <pattern>a</pattern>
      <replacement>b</replacement>
      <replaceAll>true</replaceAll>
    </fileMapper>
+----------------------------------------------------------------------------------
