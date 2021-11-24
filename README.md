# AMPT

Aquatic Mammal Photogrammetry Tool

# Build Requirements

* Python
    * Python 3.7+
    * virtualenv 20.8.1+
* [Fiji][fiji]
* Maven

# Installation

AMPT is built using Maven.

## Command Line Build

The simplest approach is to invoke Maven from the directory containing `pom.xml`. Assuming that
Maven, virtualenv, and make are installed run the following.

### MacOS X

```
> export JAVA_HOME='/Applications/Fiji.app/java/macosx/adoptopenjdk-8.jdk/jre/Contents/Home'
> mvn package -P uberjar 
> cp -f target/AMPT-0.3.0-all.jar /Applications/Fiji.app/plugins/jars`
```



[fiji]: <https://fiji.sc/>  "Fiji Home"