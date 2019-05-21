### OMERO blitz Gradle plugin

The _omero-blitz-plugin_ is a gradle plugin that provides users and projects the ability to generate/compile the necessary files
required files to use _omero-blitz_

From a high level, blitz-plugin consists of the following tasks/stages:

1. Import `.ome.xml` map files from `org.openmicroscopy:omero-model` (`omero-model.jar`) resources
2. Import `-types.properties` files from `org.openmicroscopy:omero-model` (`omero-model.jar`) resources

### Usage

Include the following at the top of your _build.gradle_ file:

```groovy
plugins {
    id "org.openmicroscopy.blitz" version "1.0.0"
}
```


### Gradle Tasks

| Task name      | Depends On     |
| -------------- | -------------- |
| importMappings |                |
