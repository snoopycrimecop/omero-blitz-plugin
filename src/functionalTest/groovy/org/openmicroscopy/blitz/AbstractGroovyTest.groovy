package org.openmicroscopy.blitz

class AbstractGroovyTest extends AbstractTest {

    public static final String PROJECT_NAME = 'omero-blitz-plugin'

    def setup() {
        setupBuildfile()
    }

    protected void setupBuildfile() {
        buildFile << """
            plugins {
                id 'org.openmicroscopy.api'
            }

            repositories {
                jcenter()
                maven { url 'https://artifacts.openmicroscopy.org/artifactory/maven' }
            }
        """
    }

    static String groovySettingsFile() {
        """
            rootProject.name = '$PROJECT_NAME'
        """
    }

    @Override
    String getBuildFileName() {
        'build.gradle'
    }

    @Override
    String getSettingsFileName() {
        'settings.gradle'
    }

}
