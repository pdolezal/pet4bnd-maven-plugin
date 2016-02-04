# Package exports tracker #

This plugin helps with maintenance package exports when using the [Maven Bundle Plugin](http://felix.apache.org/components/bundle-plugin/index.html).


## Maven integration ##

What about a POM file must specify to employ this tool? It should contain these elements (or inherit similar content):

```{xml}
<properties>
    <!-- The convenience property to set the file with exports for the bnd -->
    <pet4bnd.output>${project.build.directory}/exports.bnd</pet4bnd.output>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.felix</groupId>
            <artifactId>maven-bundle-plugin</artifactId>
            <extensions>true</extensions>
            <configuration>
                <instructions>
                    <!-- Use the exports provided by the pet4bnd -->
                    <_include>${exports.file}</_include>
                </instructions>
            </configuration>
        </plugin>
        <plugin>
            <groupId>net.yetamine</groupId>
            <artifactId>pet4bnd-maven-plugin</artifactId>
            <!--
                Unfortunately the goal must be bound explicitly. So we have to
                specify the executions element with all that goal stuff within.
            -->
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

This is a bit mouthful (blame Maven for that), but nothing mysterious happens here. The *pet4bnd* plugin just generates a *.bnd* file with the appropriate `Package-Export` directive (during the `prepare-package` phase), so the *Maven Bundle Plugin* must just be configured to use the generated *.bnd* file to employ the directive. The `pet4bnd.output` property, which the *pet4bnd* uses by default, is the most convenient place to share the file name for both plugins. The *pet4bnd* uses the `pet4bnd.source` property for finding the package exports description file or assumes `${project.basedir}/exports.pet` if the property is not specified.


## Goals ###

For a quick overview, the usual Maven `help` goal for this plugin is available. Use it for the quick and up-to-date reference.


### Generating the exports as a properties file: `export` ###

This goal generates a properties file with similar content that `generate` would produce, so that it can be integrated with tools like *Ant* easily. The default file format is the classical Java properties, however, when the file name has the *.xml* extension, the content is stored as an XML. The name of the file can be configured via the `pet4bnd.export` property (default: `${project.build.directory}/exports.properties`).


### Generating the exports for the *bnd*: `generate` ###

This goal generates the *.bnd* file that the *bnd* can use for making the complete manifest. The demonstration above shows how to configure and use the plugin for this goal.


### Fixing the POM version for snapshots: `refresh` ###

This goal updates the version of the artifact recorded in the POM to the appropriate snapshot version as derived from the current baseline and constraints (unless constrained, the version is the next major version). Actually, setting the version is not needed always, but the operation is idempotent, so it does nothing if no change needed.


### Fixing the POM version for releases: `release` ###

This goal updates the version of the artifact recorded in the POM file to the final artifact version as derived from the current change records. Before releasing an artifact, this step should take part, so that the released version is aligned with the required version number updates. However, it is suitable for the release branch only


### Restoring the baseline: `restore` ###

This goal discards the change records and sets the new version baseline for each exported package in the package exports description file and for the bundle itself, which is useful after release from the given branch when the change records needs resetting for the next release cycle.


## Requirements ##

* JDK/JRE 8 or newer.
* Maven 3.3 or newer.

Technically, the JDK is required for building this project only, but it is quite pointless using this plugin without actually building an OSGi bundle, which requires a JDK installation anyway. For building an OSGi bundle, some other tools based on the *bnd* are needed as well (e.g., the *Maven Bundle Plugin*).
