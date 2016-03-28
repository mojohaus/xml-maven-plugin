# MojoHaus XML Maven Plugin

This is the [xml-maven-plugin](http://www.mojohaus.org/xml-maven-plugin/).
 
[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/xml-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/xml-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cxml-maven-plugin)
[![Build Status](https://travis-ci.org/mojohaus/xml-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/xml-maven-plugin)

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
