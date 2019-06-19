package org.openmicroscopy.blitz

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class AbstractBaseTest extends AbstractGroovyTest {

    File libsDir

    def setup() {
        libsDir = new File(projectDir, "src/libs")
        copyExampleJar(libsDir)
        writeSettingsFile()
    }

    private void writeSettingsFile() {
        settingsFile << groovySettingsFile()
    }

    private void copyExampleJar(File outputDir) {
        Path simple = getResource("/omero-model-example.jar")
        copyFile(simple, outputDir.toPath())
    }

    private void copyFile(Path fileToCopy, Path targetDir) {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir)
        }

        Path targetFile = targetDir.resolve(fileToCopy.getFileName())
        Files.copy(fileToCopy, targetFile, StandardCopyOption.REPLACE_EXISTING)
    }

    private Path getResource(String name) {
        Paths.get(Paths.getResource(name).toURI())
    }

}
