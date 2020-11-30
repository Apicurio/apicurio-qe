# Apicurito tests



## Overriding options

The Cucumber runtime parses command line options to know what features to run, where the glue code lives, what plugins to use etc.
When you use the JUnit runner, these options are generated from the `@CucumberOptions` annotation on your test.

Sometimes it can be useful to override these options without changing or recompiling the JUnit class. This can be done with the
`cucumber.options` system property. The general form is:

Using Maven:

    mvn -Dcucumber.options="..." test


Let's look at some things you can do with `cucumber.options`. Try this:

    -Dcucumber.options="--help"

That should list all the available options.

*IMPORTANT*

When you override options with `-Dcucumber.options`, you will completely override whatever options are hard-coded in
your `@CucumberOptions` or in the script calling `cucumber.api.cli.Main`. There is one exception to this rule, and that
is the `--plugin` option. This will not _override_, but _add_ a plugin. The reason for this is to make it easier
for 3rd party tools (such as [Cucumber Pro](https://cucumber.pro/)) to automatically configure additional plugins by appending arguments to a `cucumber.properties`
file.

### Run a subset of Features or Scenarios

Specify a particular scenario by *line* (and use the pretty plugin, which prints the scenario back)

    -Dcucumber.options="classpath:skeleton/belly.feature:4 --plugin pretty"

This works because Maven puts `./src/test/resources` on your `classpath`.
You can also specify files to run by filesystem path:

    -Dcucumber.options="src/test/resources/skeleton/belly.feature:4 --plugin pretty"

You can also specify what to run by *tag*:

    -Dcucumber.options="--tags @bar --plugin pretty"

### Specify a different formatter:

For example a JUnit formatter:

    -Dcucumber.options="--plugin junit:target/cucumber-junit-report.xml"







## PROPERTIES
Inside file ``test.properties``:

```
apicurito.config.openshift.url=https://{minishift ip}:8443
apicurito.config.openshift.namespace=apicurito
apicurito.config.openshift.namespace.cleanup.after=false
apicurito.config.openshift.route.suffix={minishift ip}.nip.io
apicurito.config.ui.url=https://{namespace}.{minishift ip}.nip.io
apicurito.config.openshift.reinstall=true
apicurito.config.ui.username=username
apicurito.config.ui.password=password
```
