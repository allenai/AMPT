# Aquatic Mammal Photogrammetry Tool

The Aquatic Mammal Photogrammetry Tool (AMPT) is an ImageJ plug-in that simplifies the process of measuring aquatic
mammals from aerial imagery. The tool may be configured to support different guided measurements.

_**As of December 15, 2022 AMPT is no longer supported. If you are interested in taking over the development and support of AMPT, please contact us at ampt-dev&lt;AT&gt;allenai.org.**_

## Features

* Measurements can be initially generated by external tools.
* Measurements are configurable.
* Guides are configurable.
* Measurements are reviewable.
* Configurable subset of measurement data can be exported.

Please see the [documentation][docs] for more details on how to use the tool.

## Installing

The easy approach to installing AMPT is to use the [update site functionality][update site] built into Fiji/ImageJ. AMPT is available
through the update manager in Fiji/ImageJ at https://sites.imagej.net/AMPT/ with the name AMPT.

## Contributing
If you would like to contribute to AMPT's development, please feel free to open a pull request for consideration.

## Building

AMPT is a Java plugin and consequently requires a some effort to build. Unless you are customizing AMPT, please use the
Fiji/ImageJ update site.

### Requirements

* Python
    * [Python][python] 3.7+
    * [Virtualenv][virtualenv] 20.8.1+
* Java JDK 1.7+
* [Fiji][fiji]
* [Maven][maven] 3.8.4+

### Compiling

AMPT is built using Maven.

## Command Line Build

The simplest approach is to invoke Maven from the directory containing `pom.xml`.

### MacOS X

#### Environment

ImageJ/Fiji requires Java 1.8, which is bundled with the Fiji install for Mac OS X.

We need two environment variables, `FIJI_HOME` and `JAVA_HOME`

```zsh
export FIJI_HOME=/Applications/Fiji.app
export JAVA_HOME=`${FIJI_HOME}/Contents/MacOS/ImageJ-macosx --print-java-home`
```

Compile:

```zsh
mvn package
```

Optional Local Install :
The local deploy script moves the plugin jarfile

```
./local_deploy.zsh
```

[fiji]: <https://fiji.sc/>  "Fiji Home"

[docs]: <https://allenai.github.io/AMPT/> "Documentation"

[maven]: <https://maven.apache.org/> "Maven Homepage"

[python]: <https://www.python.org/> "Python Homepage"

[virtualenv]: <https://virtualenv.pypa.io/en/latest/> "Virtualenv Homepage"
[update site]: https://imagej.net/update-sites/following "Fiji/ImageJ Update Site"