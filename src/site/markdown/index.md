title: XML Maven Plugin

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

# The XML Maven Plugin

This Plugin is a collection of several XML related tasks.

## Goals Overview

The plugin offers the following goals:

- [xml:validate](./validate-mojo.html) validates XML files against a schema. If the files aren't matching a certain schema, one may simply check, whether they are well formed.
- [xml:transform](./transform-mojo.html) transforms XML files by applying XSLT stylesheets.

## Usage

Instructions on how to use the XML Maven Plugin can be found on the [usage page](./usage.html).

## Examples

To provide you with better understanding of some usages of the XML Maven Plugin, you can take a look into the following examples:

- Checking whether XML files are [well formed](./examples/validate-well-formedness.html)
- Checking whether XML files are [matching an XML schema](./examples/validate-schema.html)
- Checking whether XML files are [matching a RelaxNG schema](./examples/validate-relaxng.html)
- [Transforming](./examples/transform-basic.html) XML files by applying an XSLT stylesheet
- [Changing the extension](./examples/transform-change-extension.html) of a transformations result files.
- Using an [XSLT 2.0 compliant processor](./examples/transform-saxon.html).
- Setting [output properties](./examples/transform-outputProperties.html) on a transformer.
- Setting [stylesheet parameters](./examples/transform-parameters.html).
- Suppressing access to external resources by using a [catalog file](./examples/catalog.html).

