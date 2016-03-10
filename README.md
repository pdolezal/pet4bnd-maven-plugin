# Package exports tracker #

This repository contains the source for the *Package exports tracker for bnd*, or *pet4bnd* for short. As the name suggests, this tool helps with maintenance package exports when using the [bnd](http://bnd.bndtools.org/). It can be used as a standalone tool together with the *bnd* or as a Maven plugin together with the [Maven Bundle Plugin](http://felix.apache.org/components/bundle-plugin/index.html).


## Motivation ##

OSGi depends on using [semantic versioning][versioning] and OSGi practices recommends applying the versioning on the package level when every package should maintain its own version independent on the bundle that contains the package. While this recommendation has many positive effects, it is difficult to achive. One of the problems is that OSGi versioning schema lacks the concept of snapshot versions, or some other concept of a temporary version number. Then applying semantic versioning properly and on the package level becomes a real challenge. This tool helps tracking the changes on the package level, so that a correct and suitable version number for each package can be resolved when building the bundle for both snapshot or release purposes.

[versioning]: https://www.osgi.org/wp-content/uploads/SemanticVersioning.pdf


## Background ##

The package exports description is usually contained in a POM file (when using the *Maven Bundle Plugin*) or in a *.bnd* file (when using the *bnd* directly). The package export directive is often generated according to common settings inheriting the version information from the bundle, which does not follow the idea of independent package versioning. Anyway, those ways are impractical when the package version should be always aligned with the package status while respecting the type of the build, i.e., relaxing the version change pace for snapshot builds.

Let's explain on an example what the relaxed version change means: A package has been released with version 1.0.0 and changed during subsequent development twice, with both changes implying a major version increment. Therefore, the package should get version 2.0.0 after the first change and 3.0.0 after the seconds change. But because it is not released after the first change (only snapshot builds contain it), the second change should not increment the version number again, otherwise the next release would publish it with the version of 3.0.0, instead of 2.0.0. So, when finally a release build is created, the package should have version 2.0.0 in the build despite of two changes occurred during the development. With the version number contained in a project description like a POM file, it is a bit tricky to achieve: how a developer would know that the package has been changed already since the previous release and how much? It should not be required to always dig in the package change history to find this out.

Therefore this tool maintains the package exports description in a relative form instead of using absolute version numbers; the relative form basically says *Package x.y.z underwent a minor change since the previous release*. The actual version numbers are generated for every build from the relative form. The developers still have to mark the changes, but in a simpler way. The separate relative form allows to keeps the actual version numbers as close to the expected release version as possible. It is merge-friendly and allows easy quick and dirty text manipulation as well (which might be a problem with POM or *.bnd* files).


## Quick introduction ##

This section provides a quick introduction focusing on the two most important things: how to describe package exports and how to use the description for building a bundle with Maven and the *Maven Bundle Plugin*.


### Package exports description ###

The package exports description for the *pet4bnd* can be found in the *exports.pet* file by default. The file could have content like this:

```
$bundle: 1.2.0

foo.bar: 1.1.0 < 2.0.0 @ minor
+ provides:=true

foo.baz: 1.1.1   @ major
foo.boo: $bundle @ minor
```

Even without telling much about the format, one could guess following:

* The format is line-oriented.
* There is some version for the bundle.
* Two packages, *foo.bar* and *foo.baz*, shall be exported.
* The packages have different versions: *foo.bar* has 1.1.0 and *foo.baz* has 1.1.1.
* The version of *foo.bar* must stay below 2.0.0 (perhaps version 2.0.0 has been released already).
* And there were some changes: *foo.bar* underwent a minor change, while *foo.baz* underwent a major change.
* Well, it seems that it is possible to inherit the version information like *foo.boo* does.
* Finally, there is a line related to *foo.bar*, which looks like an attribute list.

This guess would be quite correct. Names of the packages to export are separated with `:` (a colon) from the version information. Any additional attributes of an export should follow on the next line after `+` (a plus sign). Whitespace is not significant as well as comments; a comment starts with `#` (the hash sign) and ends with the line. A comment may appear on the line with the version information, but attributes can't contain any comments because the whole line should be used as the attribute list.

A package version description consists of several components in this order:

* The *version baseline* (just after the colon).
* The *version constraint* following `<` (the less-than sign).
* The *version change* following `@` (the at sign).

The baseline is mandatory, other parts are optional. The baseline is the version of the package in the previous release (version 0.0.0 shall be used for yet unreleased package). Baseline combined with the change information results in the actual package version; if no change information is present, the version baseline is considered fixed for the tool. If there is any constraint, the actual version must stay below. This provides a safety belt against releasing a package modification under an existing version (useful for branching projects).

What about the `$bundle` line? It similar to a package version, but it applies to the whole bundle. The change information for a bundle means the minimal version change to happpen (perhaps due to manually managed versions). A definition must contain `$bundle` and it may contain some package exports, but without duplications.

The `$bundle` definition is special because if concerns all packages. However, it is a special case of a more general construct: a *version group*. A version group allows to define a version for a group of packages at once, while any change of the group or any of the packages in the group affects the group's target version number. This feature can help when a group of packages should have the same version even if they have different change information. A version group definition looks similar to an export definition, just the name must start with `$` (a dollar) and it must not refer to another group like an export.


### Maven integration ###

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


## Function list ##

The tool can be used as a Maven plugin, which is described below, or as a standalone application that offers very similar options (run the *.jar* file with `--help` to get the details on how to use), except for the goals that update the POM file. The standalone version is useful for integrating with other tools, e.g., scripts.


### Generating the exports as a properties file: `export` ###

This goal generates a properties file with similar content that `generate` would produce, so that it can be integrated with tools like *Ant* easily. The default file format is the classical Java properties, however, when the file name has the *.xml* extension, the content is stored as an XML. The name of the file can be configured via the `pet4bnd.export` property. If the property is empty or missing, nothing is produced.


### Generating the exports for the *bnd*: `generate` ###

This goal generates the *.bnd* file that the *bnd* can use for making the complete manifest. The demonstration above shows how to configure and use the plugin for this goal.  If the output property `pet4bnd.output` is empty or missing, nothing is produced.


### Fixing the POM version for snapshots: `refresh` ###

This goal updates the version of the artifact recorded in the POM to the appropriate snapshot version as derived from the current baseline and constraints (unless constrained, the version is the next major version). Actually, setting the version is not needed always, but the operation is idempotent, so it does nothing if no change needed.


### Fixing the POM version for releases: `release` ###

This goal updates the version of the artifact recorded in the POM file to the final artifact version as derived from the current change records. Before releasing an artifact, this step should take part, so that the released version is aligned with the required version number updates. However, it is suitable for the release branch only


### Restoring the baseline: `restore` ###

This goal discards the change records and sets the new version baseline for each exported package in the package exports description file and for the bundle itself, which is useful after release from the given branch when the change records needs resetting for the next release cycle.


## Using the tool ##

Although the tool can be used in different ways, the foreseen usage assumes that releases are performed on a release branch which merges from a development branch (or in a more complex workflows, for each release a new release branch is forked from a development or integration branch). The release process consists of following steps then:

1. The source branch shall become frozen for a while.
2. The release branch is prepared (merging or forking).
3. The `release` goal is applied to fix the release version on the release branch.
4. Other similar workflow-specific steps shall proceed to finish the release (including the final build and tags).
5. When the release is successful, the `restore` and `refresh` goals shall be run on the source branch.
6. Committing the update from the previous step ends the source branch freeze.

Usually, the `restore` goal is coupled with the `refresh` goal and both are executed on the source branch, but it is possible to have a different scenario when the decoupling of both steps can be useful, e.g., the `restore` goal could be executed on the release branch instead to have the version information clean there, however, such a way is not convenient for a release branch that merges from the source branch because it creates conflicts on the package exports definition file.


## Requirements ##

For using as a standalone tool, JRE 8 or newer is sufficient. For using as a Maven plugin, Maven 3.3 or newer is needed additionally. For building the project, JDK 8 or newer and Maven 3.3 or newer are needed. For building an OSGi bundle, some other tools based on the *bnd* are needed as well (e.g., the *Maven Bundle Plugin*).


## Licensing ##

The project is licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). For previous versions of this repository the original or current license can be chosen, i.e., the current license applies as an option for all previously published content.

Contributions to the project are welcome and accepted if they can be incorporated without the need of changing the license or license conditions and terms.


[![Yetamine logo](http://petr.dolezal.matfyz.cz/files/Yetamine_small.svg "Our logo")](http://petr.dolezal.matfyz.cz/files/Yetamine_large.svg)
