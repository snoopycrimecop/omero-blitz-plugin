package org.openmicroscopy.blitz.tasks

import org.openmicroscopy.blitz.AbstractGroovyTest

class OnlineImportTest extends AbstractGroovyTest {

    def "can import ome.xml files from hosted jar"() {
        given:
        String outputDirName = "mappings"
        buildFile << """
            import org.openmicroscopy.blitz.tasks.ImportResourcesTask

            task simpleImport(type: ImportResourcesTask) {
                artifactName = "omero-model"
                config = configurations.runtimeClasspath
                extractDir = file("\$projectDir/$outputDirName")
                pattern = "**/*ome.xml"
            }
        """

        when:
        build("simpleImport")

        then:
        File result = file(outputDirName)
        result.listFiles().length > 0
    }

    @Override
    protected void setupBuildfile() {
        buildFile << """
            plugins {
                id 'java'
                id 'org.openmicroscopy.api'
            }

            repositories {
                jcenter()
                maven { url 'https://artifacts.openmicroscopy.org/artifactory/maven' }
                maven { url 'https://artifacts.unidata.ucar.edu/repository/unidata-all/' }
            }
            
            dependencies {
                implementation('org.openmicroscopy:omero-model:5.5.+')
            }
        """
    }
}
