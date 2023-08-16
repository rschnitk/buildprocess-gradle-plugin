# NAPPL Gradle Plugins

[Confluence page](https://confluence.ceyoniq.com/x/QYDxBg)

## Usage

### `plugins` block:

```groovy
plugins {
  id "com.ceyoniq.gradle.buildprocess" version "$version"
}
```
or via the

### `buildscript` block:
```groovy
apply plugin: "com.ceyoniq.gradle.buildprocess"

buildscript {
  repositories {
    gradlePluginPortal()
  }

  dependencies {
    classpath "com.ceyoniq.gradle.buildprocess:nappl-gradle-plugins:$version"
  }
}
```
## Extensions

### `verInfo`
The verInfo extension reads the buildprocess/version file and detect some CI variables.

Configuration:
```groovy
tasks.register("buildVersion") {
    group = "Help"
    description = "Print build version."
    doLast {
        println "Build version:   " + version
        println "Full version:    " + verInfo_fullVersion
        println "Major-Minor:     " + verInfo_majorMinor
        println "Component:       " + verInfo_component
        println "Build timestamp: " + verInfo_buildTimestamp
    }
}
```

## Tasks

### `uploadBom`
The uploadBom task sends an POST request to an Dependency Track Service.

Configuration:
```groovy
tasks.named("uploadBom").configure {
    uri     = "http://001linuxserver01.ct.com:8888/api/v1/bom"
    bomFile = new File(cyclonedxBom.destination.get(), 'bom.json')

    apiKey      = providers.gradleProperty('dtrack.api.key').get();
    projectUUID = providers.gradleProperty('dtrack.project.id').get()
    
    dependsOn cyclonedxBom
}
```
