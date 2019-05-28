/*
 * -----------------------------------------------------------------------------
 *  Copyright (C) 2019 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * ------------------------------------------------------------------------------
 */
package org.openmicroscopy.blitz

import groovy.transform.CompileStatic
import ome.dsl.SemanticType
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskProvider
import org.openmicroscopy.api.ApiPlugin
import org.openmicroscopy.api.extensions.ApiExtension
import org.openmicroscopy.api.tasks.SplitTask
import org.openmicroscopy.blitz.extensions.BlitzExtension
import org.openmicroscopy.blitz.tasks.ImportResourcesTask
import org.openmicroscopy.dsl.DslPlugin
import org.openmicroscopy.dsl.DslPluginBase
import org.openmicroscopy.dsl.extensions.DslExtension
import org.openmicroscopy.dsl.extensions.MultiFileConfig
import org.openmicroscopy.dsl.tasks.FilesGeneratorTask
import org.openmicroscopy.dsl.tasks.GeneratorBaseTask

import static org.openmicroscopy.dsl.FileTypes.PATTERN_DB_TYPE
import static org.openmicroscopy.dsl.FileTypes.PATTERN_OME_XML

@CompileStatic
class BlitzPlugin implements Plugin<Project> {

    private static final Logger Log = Logging.getLogger(BlitzPlugin)

    public static final String EXTENSION_BLITZ = "blitz"

    public static final String TASK_IMPORT_MAPPINGS = "importMappings"

    public static final String TASK_IMPORT_DATABASE_TYPES = "importDatabaseTypes"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.pluginManager.apply(DslPlugin)
        project.pluginManager.apply(ApiPlugin)

        BlitzExtension blitz =
                project.extensions.create(EXTENSION_BLITZ, BlitzExtension, project)

        registerImportMappings()
        registerImportDbTypes()

        configureDslPlugin(blitz)
        configureApiPlugin(blitz)
    }

    TaskProvider<ImportResourcesTask> registerImportMappings() {
        project.tasks.register(TASK_IMPORT_MAPPINGS, ImportResourcesTask, new Action<ImportResourcesTask>() {
            @Override
            void execute(ImportResourcesTask t) {
                t.setConfig(ImportHelper.getConfigurationForOmeroModel(project))
                t.setExtractDir(project.layout.buildDirectory.dir("mappings"))
                t.setPattern(PATTERN_OME_XML)
            }
        })
    }

    TaskProvider<ImportResourcesTask> registerImportDbTypes() {
        project.tasks.register(TASK_IMPORT_DATABASE_TYPES, ImportResourcesTask, new Action<ImportResourcesTask>() {
            @Override
            void execute(ImportResourcesTask t) {
                t.setConfig(ImportHelper.getConfigurationForOmeroModel(project))
                t.setExtractDir(project.layout.buildDirectory.dir("databaseTypes"))
                t.setPattern(PATTERN_DB_TYPE)
            }
        })
    }

    void configureDslPlugin(BlitzExtension blitz) {
        DslExtension dsl = project.extensions.getByType(DslExtension)

        // Configure extensions of ome.dsl plugin
        dsl.database.set(blitz.database)
        dsl.outputDir.set(blitz.outputDir)
        dsl.omeXmlFiles.from(project.tasks.named(TASK_IMPORT_MAPPINGS))
        dsl.databaseTypes.from(project.tasks.named(TASK_IMPORT_DATABASE_TYPES))

        // Add generateCombinedFilesTask
        dsl.multiFile.create("combined", new Action<MultiFileConfig>() {
            @Override
            void execute(MultiFileConfig mfc) {
                mfc.template = "combined.vm"
                mfc.outputDir = "${project.buildDir}/${blitz.database.get()}/combined"
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

    void configureApiPlugin(BlitzExtension blitz) {
        TaskProvider<FilesGeneratorTask> generateCombinedTask = getGenerateCombinedTask(blitz)

        ApiExtension api = project.extensions.getByType(ApiExtension)
        api.outputDir.set(blitz.outputDir)
        api.combinedFiles.from(generateCombinedTask)

        project.tasks.withType(SplitTask).configureEach(new Action<SplitTask>() {
            @Override
            void execute(SplitTask st) {
                st.dependsOn(generateCombinedTask)
            }
        })
    }

    TaskProvider<FilesGeneratorTask> getGenerateCombinedTask(BlitzExtension blitz) {
        String taskName = DslPluginBase.makeDslTaskName("combined", blitz.database.get())
        project.tasks.named(taskName, FilesGeneratorTask)
    }

}
