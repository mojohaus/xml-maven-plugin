# Agent guide for xml-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with three goals in `org.codehaus.mojo.xml`: `validate` (`ValidateMojo`, bound to
  `test`), `transform` (`TransformMojo`, bound to `generate-resources`) and `check-format`
  (`CheckFormatMojo`, bound to `validate`). They share `AbstractXmlMojo`; `Resolver` is the
  catalog-based entity, schema and URI resolver.
- `validation`, `transformer` and `format` hold the parameter types and the work of each goal,
  such as `ValidationSet`, `TransformationSet` and `IndentCheckSaxHandler`.
- The site is built from `src/site`.

## Build gates

- On a local build Spotless applies the formatting; it also reformats Markdown, so run
  `mvn spotless:apply` after editing the README or this file.
- The parent, `mojo-parent`, sets `maven.compiler.release` to 8, so main code must use the Java 8
  API.
- The plugin compiles against Maven 3.9.16 (`mavenVersion`) and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite.

## Unit tests

- JUnit 5 only. `ValidateMojoTest` and `TransformMojoTest` extend `AbstractXmlMojoTestCase`, which
  carries `@ProjectMojoTest` and builds the mojo from `src/test/<case>/pom.xml` with
  `ProjectMojos.lookup(goal, pom)`. A case is a directory (`src/test/it1` to `it20`,
  `src/test/multimodule`, `xinclude-xsd`, `xinclude-xsl`) that the test names in `runTest`, so a new
  case needs a directory and a test method.

## Integration tests

- They live in `src/it`, one directory per test, and run with maven-invoker-plugin in the `run-its`
  profile. The plugin is installed into `target/local-repo` first, and `src/it/settings.xml` points
  the forked builds at it. Each `invoker.properties` names the goal, such as
  `invoker.goals = xml:check-format`.
- Run a subset:

  ```shell
  mvn -B -P run-its verify -Dinvoker.test=check-format-pass,it1
  ```

  The tests have no `setup-*` projects.

- They run in parallel, one per CPU core (`invoker.parallelThreads` 1C), so a test must not depend
  on another test's output.

- The forked builds get `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1 -XX:+UseSerialGC`, so every JVM
  prints a `Picked up JAVA_TOOL_OPTIONS` line on stderr.

- Most tests pass when the build succeeds; only `check-format-pass`, `check-format-fail` and
  `xinclude-xsl` have a `verify.groovy`. Write it as `assert` statements: Invoker fails a script
  whose last expression is anything other than `true` or `null`, so a trailing `list.each { }`
  fails the test; use a `for` loop.

## Dependencies

- Deliberate pins in `.github/dependabot.yml`: `maven-compat`, `maven-core` and `maven-plugin-api`
  stay below 3.10.0, because the plugin baseline is the Maven 3.9.x line, and `plexus-xml` stays
  below 4.0.0, because 4.x depends on the Maven 4 XML API.

## Continuous integration and pull requests

- `.github/workflows/maven.yml` calls the shared `maven-verify.yml@v5` workflow from
  `apache/maven-gh-actions-shared`. Branches in this repository build under the push event, so look
  at the push run for a pull request from a same-repository branch.
- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

