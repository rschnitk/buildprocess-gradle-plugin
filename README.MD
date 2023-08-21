# Buildprocess gradle plugin

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
        println "Build version:   " + project.version
        println "Major-Minor:     " + project.verInfo.majorMinor
        println "Component:       " + project.verInfo.component
        println "Full version:    " + project.verInfo.fullVersion
        println "releaseDate:     " + project.verInfo.releaseDate
        println "Rev/BuildID:     " + project.verInfo.buildID
        println "branchName:      " + project.verInfo.branchName
        println "commitID:        " + project.verInfo.commitID
    }
}
```

## Tasks

### `uploadBom`
The uploadBom task sends an POST request to an Dependency Track Service.

Configuration:
```groovy
tasks.register("uploadBom", com.ceyoniq.gradle.buildprocess.UploadBomTask) {
    uri         = "http://dependency-track-host.domain.com:8888/api/v1/bom"
    bomFile     = new File(cyclonedxBom.destination.get(), 'bom.json')

    apiKey      = providers.gradleProperty('dtrack.api.key').get();
    projectUUID = providers.gradleProperty('dtrack.project.id').get()
   
    dependsOn cyclonedxBom
}
```
