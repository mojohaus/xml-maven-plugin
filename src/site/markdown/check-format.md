title: Checking the indentation of XML files

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Checking the indentation of XML files

The `xml:check-format` goal checks that the elements of XML files are indented by a fixed number of spaces. It runs in
the `validate` phase and, with its default settings, fails the build when it finds a violation:

```xml
<build>
  <plugins>
    ...
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>xml-maven-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>check-format</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    ...
  </plugins>
</build>
```

By default the goal checks the `*.xml` and `*.xsl` files in the project directory and the `*.xml` and `*.xsl` files
below `src`, and skips files and directories whose name starts with a dot. It expects an indentation of two spaces.
Every violation is logged with its file and line.

# Goal properties

The "xml:check-format" goal offers the following configurable properties:

|      Property Name      |                                                                                               Description                                                                                                |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| encoding                | The encoding of the files. Defaults to the source encoding of the project. Property: `xml.encoding`.                                                                                                     |
| failOnFormatViolation   | If `true`, violations are logged as errors and the build fails. If `false`, they are logged as warnings and the build goes on. Default is `true`. Property: `xml.failOnFormatViolation`.                 |
| formatFileSets          | The file sets to check, in addition to the default one. See the following section.                                                                                                                       |
| indentSize              | The number of spaces expected for indentation. Default is `2`. Property: `xml.indentSize`.                                                                                                               |
| useDefaultFormatFileSet | Whether to check the default file set, described above, in addition to `formatFileSets`. Set it to `false` to check only the files you name. Default is `true`. Property: `xml.useDefaultFormatFileSet`. |

The goal also accepts the [common goal properties](./common-properties.html).

# Format file sets

A `formatFileSet` is a Maven file set, which can set its own `encoding` and `indentSize`:

```xml
<configuration>
  <useDefaultFormatFileSet>false</useDefaultFormatFileSet>
  <formatFileSets>
    <formatFileSet>
      <directory>src/main/resources</directory>
      <includes>
        <include>**/*.xml</include>
      </includes>
      <indentSize>4</indentSize>
    </formatFileSet>
  </formatFileSets>
</configuration>
```

