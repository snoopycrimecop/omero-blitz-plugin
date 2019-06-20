package org.openmicroscopy.blitz

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

abstract class AbstractGroovyTest extends AbstractTest {

    public static final String PROJECT_NAME = 'omero-blitz-plugin'

    File libsDir

    def setup() {
        libsDir = new File(projectDir, "libs")
        copyExampleJar(libsDir)

        setupBuildfile()
        writeSettingsFile()
    }

    @Override
    String getBuildFileName() {
        'build.gradle'
    }

    @Override
    String getSettingsFileName() {
        'settings.gradle'
    }

    protected void setupBuildfile() {
        buildFile << """
            plugins {
                id 'java'
                id 'org.openmicroscopy.api'
            }

            repositories {
                jcenter()
            }
            
            dependencies {
                implementation(fileTree(dir: 'libs', include: '*.jar'))
            }
        """
    }

    private void writeSettingsFile() {
        settingsFile << groovySettingsFile()
    }

    /**
     * Example jar contains various "resource" files, similar to "omero-model"
     * @param outputDir the directory to place the jar
     */
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

    static String groovySettingsFile() {
        """
            rootProject.name = '$PROJECT_NAME'
        """
    }

}
