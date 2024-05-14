# Buildprocess gradle plugin

## Usage

### `plugins` block:

```groovy
plugins {
  id "io.github.rschnitk.buildprocess" version "$version"
}
```
or via the

### `buildscript` block:
```groovy
apply plugin: "io.github.rschnitk.buildprocess"

buildscript {
  repositories {
    gradlePluginPortal()
  }

  dependencies {
    classpath "io.github.rschnitk.buildprocess:buildprocess-gradle-plugin:$version"
  }
}
```
## Extensions

### `verInfo`
The verInfo extension reads the 'buildprocess/version.properties' file and detect some CI variables.
Supported CI Server: Gitlab, Jenkins and ABTS.

Configuration:
```groovy
tasks.register("buildVersion") {
    group = "Help"
    description = "Print build version."
    doLast {
        println "Build version:   " + project.verInfo.version
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
tasks.register("uploadBom", io.github.rschnitk.buildprocess.UploadBomTask) {
    uri         = "https://dependency-track-host.domain.com:8888/api/v1/bom"
    bomFile     = new File(cyclonedxBom.destination.get(), 'bom.json')

    apiKey      = providers.gradleProperty('dtrack.api.key').get()
    projectUUID = providers.gradleProperty('dtrack.project.id').get()
   
    dependsOn cyclonedxBom
}
```
