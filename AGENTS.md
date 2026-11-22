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
- The site is built from `src/site`; the goal pages come from the Javadoc of the mojos.

## Build gates

- On a local build Spotless applies the formatting; it also reformats Markdown, so run
  `mvn spotless:apply` after editing the README or this file.
- The parent, `mojo-parent`, sets `maven.compiler.release` to 8, so main code must use the Java 8
  API.
- The plugin compiles against the Maven 3.9.x line (`mavenVersion`) and declares a literal
  `<prerequisites><maven>`; keep the two apart, so raising `mavenVersion` does not raise the
  prerequisite.

## Unit tests

- Unit tests use JUnit 5. `ValidateMojoTest`, `TransformMojoTest` and `CheckFormatMojoTest` extend
  `AbstractXmlMojoTestCase`, which carries `@ProjectMojoTest` and builds the mojo from a project
  with `ProjectMojos.lookup(goal, pom)`. The projects are the directories under `src/test` (such as
  `it1` to `it20`, `check-format-pass`, `skip-missing-validate`, `xinclude-xsd`); the test copies
  them to `target/test-fixtures`, so the files the mojos generate stay out of `src/test`, which the
  source release archives. A new case needs a directory and a test method.

## Integration tests

- Only what needs a real Maven build is an integration test. They live in `src/it`, one directory
  per test, and run with maven-invoker-plugin in the `run-its` profile. The plugin is installed
  into `target/local-repo` first, and `src/it/settings.xml` points the forked builds at it. A test
  passes when the build succeeds; `invoker.properties` sets another `invoker.buildResult`, the goals
  (`gh11` runs `test`) or a `invoker.java.version` such as `21+`, as the RELAX NG tests do.
- Run a subset:

  ```shell
  mvn -B -P run-its verify -Dinvoker.test=gh11,it22-rnc-valid
  ```

  The tests have no `setup-*` projects.

- They run in parallel, one per CPU core (`invoker.parallelThreads` 1C), so a test must not depend
  on another test's output.

- The forked builds get `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1 -XX:+UseSerialGC`, so every JVM
  prints a `Picked up JAVA_TOOL_OPTIONS` line on stderr.

## Dependencies

- Deliberate pins in `.github/dependabot.yml`: `maven-compat`, `maven-core` and `maven-plugin-api`
  stay below 3.10.0, because the plugin baseline is the Maven 3.9.x line, and `plexus-xml` stays
  below 4.0.0, because 4.x depends on the Maven 4 XML API.

## Continuous integration and pull requests

- `.github/workflows/maven.yml` calls the MojoHaus `maven-verify.yml` workflow in `mojohaus/.github`.
  Branches in this repository build under the push event, so look
  at the push run for a pull request from a same-repository branch.
- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- The goal pages are generated from the Javadoc of the mojos: describe each `@Parameter` with its
  default, its `property` name and, for a new one, `@since`. Keep the class Javadoc of a mojo
  accurate, because it is the goal's description in `plugin-info.html`.
- `src/site/markdown/index.md` lists every goal. `validation.md`, `transformation.md` and
  `check-format.md` describe one goal each, with a table of its properties and of the elements of
  its sets; `common-properties.md` holds the properties all goals share. A new parameter goes into
  the table of its page.
- `src/site/markdown/usage.md` and `faq.md` give the general setup and the recurring questions, and
  the pages under `examples/` show one configuration each. Add an example for behaviour that needs
  more than a table row.
- Keep the `src/site/site.xml` menu in sync when a page is added, and never rename a published page
  or change a heading: both are URLs that readers link to.
- Write site pages as plain Markdown, and show the plugin without a version, or with
  `<version><!-- latest release --></version>` where a version is needed, and never a snapshot
  version. A page that needs Velocity (`#set`, `#parse`, `$h2`) would be named `.md.vm`.
- `README.md` states what the plugin does and its Maven and JDK floor; update it when those
  change. Update this file when a change adds a convention, build gate or fixture.

