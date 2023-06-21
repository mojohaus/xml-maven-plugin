# MojoHaus XML Maven Plugin

This is the [xml-maven-plugin](https://www.mojohaus.org/xml-maven-plugin/).
 
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/xml-maven-plugin.svg?label=License)](https://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/xml-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/xml-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/xml-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/xml-maven-plugin/actions/workflows/maven.yml)

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn site
mvn scm-publish:publish-scm
```
