# MojoHaus XML Maven Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/xml-maven-plugin.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/xml-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/xml-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/xml-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/xml-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

The [XML Maven Plugin](https://www.mojohaus.org/xml-maven-plugin/) validates XML files against a
schema, transforms them with XSLT stylesheets, and checks their indentation. It needs Maven 3.9.0
or later and JDK 8 or later. The goals are `xml:validate`, `xml:transform` and `xml:check-format`;
bind them to a build in the POM, or run one from the command line:

```shell
mvn xml:check-format
```

The [project site](https://www.mojohaus.org/xml-maven-plugin/) has the
[goals](https://www.mojohaus.org/xml-maven-plugin/plugin-info.html) and examples for validation,
transformation, catalogs and alternative XSLT processors. See [AGENTS.md](AGENTS.md) for the build,
the tests and the pull request conventions.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn site
mvn scm-publish:publish-scm
```

