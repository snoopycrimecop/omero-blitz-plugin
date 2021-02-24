package org.openmicroscopy.blitz.tasks

import org.openmicroscopy.blitz.AbstractGroovyTest

class OfflineImportTest extends AbstractGroovyTest {

    def "can import ome.xml files from local jar"() {
        given:
        String outputDirName = "mappings"
        buildFile << """
            import org.openmicroscopy.blitz.tasks.ImportResourcesTask

            task simpleImport(type: ImportResourcesTask) {
                artifactName = "omero-model-example"
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

}
