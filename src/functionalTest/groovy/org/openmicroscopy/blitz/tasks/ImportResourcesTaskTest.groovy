package org.openmicroscopy.blitz.tasks

import org.openmicroscopy.blitz.AbstractGroovyTest

class ImportResourcesTaskTest extends AbstractGroovyTest {

    def ""() {
        given:
        buildFile << """
            dependencies {
                implementation("org.openmicroscopy:omero-model:5.5.0")
            }   
    
            import org.openmicroscopy.blitz.tasks.ImportResourcesTask

            task simpleImport(type: ImportResourcesTask) {
                language = "python"
                outputDir = file("$outputDir")
                source = file("$combinedDir")
            }
        """


    }


}
