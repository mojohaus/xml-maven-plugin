# Schema Caching Example

This example demonstrates the schema caching feature of the XML Maven Plugin.

## What is Schema Caching?

When validating multiple XML files that reference the same DTD or XSD schemas from URLs,
the plugin can cache these schemas in memory to avoid redundant network requests during
the same plugin execution.

## How to Enable

Add the `enableSchemaCaching` parameter to your plugin configuration:

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>xml-maven-plugin</artifactId>
  <version>1.1.1-SNAPSHOT</version>
  <configuration>
    <!-- Enable schema caching -->
    <enableSchemaCaching>true</enableSchemaCaching>
    <validationSets>
      <validationSet>
        <dir>xml</dir>
      </validationSet>
    </validationSets>
  </configuration>
</plugin>
```

## Benefits

- **Performance**: Significantly reduces execution time when validating many files (e.g., 1200+ files)
- **Network Efficiency**: Avoids redundant network calls for the same schema
- **Reliability**: Reduces dependency on network stability during builds

## When to Use

Enable schema caching when:
- Validating many XML files in the same execution
- Using external schemas referenced by URL
- Experiencing slow validation due to network latency

## Default Behavior

By default, schema caching is **disabled** to maintain backward compatibility.
Set `enableSchemaCaching` to `true` to enable it.
