package org.openmicroscopy.blitz

import groovy.transform.CompileStatic
import ome.dsl.SemanticType
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.openmicroscopy.blitz.extensions.BlitzExtension
import org.openmicroscopy.blitz.tasks.ImportResourcesTask
import org.openmicroscopy.dsl.DslPlugin
import org.openmicroscopy.dsl.extensions.DslExtension
import org.openmicroscopy.dsl.extensions.MultiFileConfig
import org.openmicroscopy.dsl.tasks.GeneratorBaseTask

import static org.openmicroscopy.dsl.FileTypes.PATTERN_DB_TYPE
import static org.openmicroscopy.dsl.FileTypes.PATTERN_OME_XML

@CompileStatic
class BlitzDslPlugin implements Plugin<Project> {

    public static final String TASK_IMPORT_MAPPINGS = "importMappings"

    public static final String TASK_IMPORT_DATABASE_TYPES = "importDatabaseTypes"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.pluginManager.apply(DslPlugin)

        project.pluginManager.apply(BasePlugin)

        registerImportTask(TASK_IMPORT_MAPPINGS, "mappings", PATTERN_OME_XML)
        registerImportTask(TASK_IMPORT_DATABASE_TYPES, "databaseTypes", PATTERN_DB_TYPE)

        configureDslPlugin(project.extensions.getByType(BlitzExtension))
    }

    TaskProvider<ImportResourcesTask> registerImportTask(String taskName, String outputDir, String filePattern) {
        project.tasks.register(taskName, ImportResourcesTask, new Action<ImportResourcesTask>() {
            @Override
            void execute(ImportResourcesTask t) {
                t.setConfig(ImportHelper.getConfigurationForOmeroModel(project))
                t.setExtractDir(project.layout.buildDirectory.dir(outputDir))
                t.setPattern(filePattern)
            }
        })
    }

    /**
     * Creates a task that generated .combined files.
     * .ome.xml files and *-types.properties files are obtained from "omero-model.jar:
     * @param blitz
     */
    void configureDslPlugin(BlitzExtension blitz) {
        // Configure extensions of ome.dsl plugin
        DslExtension dsl = project.extensions.getByType(DslExtension)
        dsl.database.set(blitz.database)
        dsl.omeXmlFiles.from(project.tasks.named(TASK_IMPORT_MAPPINGS))
        dsl.databaseTypes.from(project.tasks.named(TASK_IMPORT_DATABASE_TYPES))

        // Add generateCombinedFilesTask
        dsl.multiFile.create("combined", new Action<MultiFileConfig>() {
            @Override
            void execute(MultiFileConfig mfc) {
                mfc.template = "combined.vm"
                mfc.outputDir = "combined"
                mfc.formatOutput = { SemanticType st -> "${st.getShortname()}I.combined" }
            }
        })

        // Ensure that each generateXXX task imports mappings and database types from omero-model
        // before running
        project.tasks.withType(GeneratorBaseTask).configureEach(new Action<GeneratorBaseTask>() {
            @Override
            void execute(GeneratorBaseTask t) {
                t.dependsOn(project.tasks.named(TASK_IMPORT_MAPPINGS), project.tasks.named(TASK_IMPORT_DATABASE_TYPES))
            }
        })
    }

}
