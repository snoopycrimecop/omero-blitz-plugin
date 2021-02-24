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

import com.zeroc.gradle.icebuilder.slice.SlicePlugin
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.util.PatternFilterable
import org.openmicroscopy.blitz.extensions.IceExtension
import org.openmicroscopy.tasks.IceDocsTask

import javax.inject.Inject

@CompileStatic
class BlitzIcePlugin implements Plugin<Project> {

    public static final String EXTENSION_ICE = "ice"

    public static final String TASK_ZIP_ICEDOC = "zipIcedoc"

    public static final String TASK_COMPILE_ICEDOC = "compileIcedoc"

    private Project project

    private IceExtension ice

    private final ProjectLayout layout

    @Inject
    BlitzIcePlugin(ProjectLayout layout) {
        this.layout = layout
    }

    @Override
    void apply(Project project) {
        // Apply the zeroc plugin
        project.pluginManager.apply(SlicePlugin)

        this.project = project

        // Create ice extension
        this.ice = project.extensions.create(EXTENSION_ICE, IceExtension, project)

        // Register our tasks
        registerIceDocsTask()
        registerZipIceDocs()
    }

    TaskProvider<IceDocsTask> registerIceDocsTask() {
        project.tasks.register(TASK_COMPILE_ICEDOC, IceDocsTask, new Action<IceDocsTask>() {
            @Override
            void execute(IceDocsTask task) {
                task.source(project.fileTree(ice.iceSrcDir).matching { PatternFilterable filterable ->
                    filterable.include "**/*.ice"
                })
                task.includeDirs.add(ice.iceSrcDir)
                task.outputDir.set(ice.docsOutputDir)
            }
        })
    }

    TaskProvider<Zip> registerZipIceDocs() {
        project.tasks.register(TASK_ZIP_ICEDOC, Zip, new Action<Zip>() {
            @Override
            void execute(Zip zip) {
                zip.archiveClassifier.set("icedoc")
                zip.from(project.tasks.named(TASK_COMPILE_ICEDOC))
            }
        })
    }

}
