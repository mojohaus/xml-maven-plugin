title: XML Maven Plugin FAQ

<!--
Copyright 2006 The Apache Software Foundation.

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

# XML Maven Plugin FAQ

**General**

1. [What goals does the XML Maven Plugin offer?](#Goals)
2. [How do I tell the plugin to use my proxy server?](#Proxy%20Settings)
3. [I have a stylesheet (a schema, another resource), which should be loaded through the class loader.](#ClassLoader%20Resources)
4. [The plugin doesn't work in offline mode. It always tries to resolve external resources and fails, if the network is not available.](#Catalogs)

**Transformation**

1. [How do I use an XSLT stylesheet for transforming files?](#basic-use)
2. [How do I transform the names of the generated files?](#name%20transformation)
3. [How do I perform other kinds of name transformations?](#other%20name%20transformation)
4. [How can I use XSLT 2.0 in my stylesheets?](#xslt2)
5. [How do I use another XSLT processor, for example Saxon?](#saxon)
6. [How do I set a transformers output properties?](#outputProperties)
7. [How do I set stylesheet parameters?](#parameters)

**Validation**

1. [How do I check files for well formedness?](#well-formedness)
2. [How do I make sure, that files are matching an XML schema?](#schema)

## General

<a id="Goals"></a>

### What goals does the XML Maven Plugin offer?

The plugin offers the [xml:validate](validation.html) goal for [validating](validation.html) XML files and the [xml:transform](transformation.html) goal for [transforming](transformation.html) XML files.

<a id="Proxy Settings"></a>

### How do I tell the plugin to use my proxy server?

Use the standard Maven proxy settings. See the [Mini guide on proxy configuration](http://maven.apache.org/guides/mini/guide-proxies.html) for details.

<a id="ClassLoader Resources"></a>

### I have a stylesheet (a schema, another resource), which should be loaded through the class loader.

The plugin attempts to resolve resources through the class loader automatically. Alternatively, you can enforce using the class loader, if you specify your resource with an URI like "resource:org/foo/stylesheet.xsl".

<a id="Catalogs"></a>

### The plugin doesn't work in offline mode. It always tries to resolve external resources and fails, if the network is not available.

You've got to download the resources in question once and store them as part of the project. A catalog file will replace the external resource with your local copy. See [this example](examples/catalog.html) for details.

## Transformation

<a id="basic-use"></a>

### How do I use an XSLT stylesheet for transforming files?

See [this example](examples/transform-basic.html).

<a id="name transformation"></a>

### How do I transform the names of the generated files?

Use a [File Mapper](http://plexus.codehaus.org/plexus-components/plexus-io/filemappers.html). See [this example](examples/transform-change-extension.html).

<a id="other name transformation"></a>

### How do I perform other kinds of name transformations?

Check the list of available file mappers at [File Mapper](http://plexus.codehaus.org/plexus-components/plexus-io/filemappers.html). Perhaps, there is one which suits your needs.

If not, you need to implement your own. If you feel that it is sufficiently generic, then you might consider to submit it as a contribution to the Plexus IO component.

See [this example](examples/transform-change-extension.html) for how to configure the transformer to map names.

<a id="xslt2"></a>

### How can I use XSLT 2.0 in my stylesheets?

You need to use an XSLT 2.0 complicant stylesheet processor like Saxon. See [this example](examples/transform-saxon.html).

<a id="saxon"></a>

### How do I use another XSLT processor, for example Saxon?

You need to specify your favorite XSLT processor as a dependency. See [this example](examples/transform-saxon.html).

<a id="outputProperties"></a>

### How do I set a transformers output properties?

Use the element `outputProperties` in the transformation set. See [this example](examples/transform-outputProperties.html) for details.

<a id="parameters"></a>

### How do I set stylesheet parameters?

Use the element `parameters` in the transformation set. See [this example](examples/transform-parameters.html) for details.

## Validation

<a id="well-formedness"></a>

### How do I check files for well formedness?

See [this example](examples/validate-well-formedness.html).

<a id="schema"></a>

### How do I make sure, that files are matching an XML schema?

See [this example](examples/validate-schema.html).
