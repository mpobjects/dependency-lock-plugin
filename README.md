[![Build Status](https://travis-ci.org/mpobjects/dependency-lock-plugin.svg?branch=master)](https://travis-ci.org/mpobjects/dependency-lock-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/com.mpobjects.maven/dependency-lock-plugin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mpobjects.maven%22%20AND%20a:%22dependency-lock-plugin%22)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.mpobjects.maven/dependency-lock-plugin.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/mpobjects/maven/dependency-lock-plugin/)
[![License](https://img.shields.io/github/license/mpobjects/dependency-lock-plugin.svg)](https://github.com/mpobjects/dependency-lock-plugin/blob/master/LICENSE)

# dependency-lock-plugin

Maven Plugin which produces a POM where all dependencies have been locked down via `<dependencyManagement>` entries. The output POM is the original POM with additional entries to the `<dependencyManagement>`.
This mostly affects cases of version ranges, especially in transtivie dependencies. It was inspired by the [BOM builder](https://github.com/jboss/bom-builder-maven-plugin) plugin.


# Usage

```xml
<project>
  [...]
  <build>
    <plugins>
      <plugin>
      	<groupId>com.mpobjects.maven</groupId>
        <artifactId>dependency-lock-plugin</artifactId>
        <version>${dependency-lock-plugin.version}</version>
        <executions>
          <execution>
            <id>lock-dependencies</id>
            <goals>
              <goal>lock</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
  [...]
</project>
```

More information is available in the [plugin documentation](https://mpobjects.github.io/dependency-lock-plugin/).
