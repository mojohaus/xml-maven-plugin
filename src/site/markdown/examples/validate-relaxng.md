title: Checking whether XML files match a RelaxNG schema

<!--
Copyright 2005  The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Check whether XML files matching a RelaxNG schema

The following configuration snippet would check whether all files in the directory `src/main/relaxng` are matching the schema `src/main/schema.rng`. Note that an additional library is required to support RelaxNG validation: this example uses Jing, and names its `SchemaFactory` explicitly. To validate against a schema in RelaxNG compact syntax, use `com.thaiopensource.relaxng.jaxp.CompactSyntaxSchemaFactory` and the schema language `http://www.iana.org/assignments/media-types/application/relax-ng-compact-syntax`.

```xml
<build>
  <plugins>
    ...
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>xml-maven-plugin</artifactId>
      <dependencies>
        <dependency>
          <groupId>org.relaxng</groupId>
          <artifactId>jing</artifactId>
          <version>20241231</version>
        </dependency>
      </dependencies>
      <executions>
        <execution>
          <goals>
            <goal>validate</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <validationSets>
          <validationSet>
            <dir>src/main/relaxng</dir>
            <systemId>src/main/schema.rng</systemId>
            <schemaFactory>com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory</schemaFactory>
            <schemaLanguage>http://relaxng.org/ns/structure/1.0</schemaLanguage>
          </validationSet>
        </validationSets>
      </configuration>
    </plugin>
    ...
  </plugins>
</build>
```

