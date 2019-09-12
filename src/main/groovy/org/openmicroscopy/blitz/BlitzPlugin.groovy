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
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.openmicroscopy.blitz.extensions.IceExtension
import org.openmicroscopy.tasks.IceDocsTask

@CompileStatic
class BlitzPlugin implements Plugin<Project> {

    public static final String TASK_PROCESS_SLICE = "processSlice"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.plugins.apply(BlitzIcePlugin)

        project.plugins.apply(BlitzBasePlugin)

        // Sort out ordering of tasks
        registerProcessSliceTask()
        configureTaskOrdering()
    }

    TaskProvider<Copy> registerProcessSliceTask() {
        IceExtension ice = project.extensions.getByType(IceExtension)

        project.tasks.register(TASK_PROCESS_SLICE, Copy, new Action<Copy>() {
            @Override
            void execute(Copy task) {
                // Copy ice files generated in combinedToIce task to iceSrcDir
                CopySpec combinedToIceSpec = project.copySpec()
                combinedToIceSpec.from(project.tasks.named("combinedToIce"))
                combinedToIceSpec.into("omero/model")

                // Copy files located in project dir to iceSrcDir
                CopySpec mainSpec = project.copySpec()
                mainSpec.from(project.layout.projectDirectory.dir("src/main/slice"))

                task.into(ice.iceSrcDir)
                task.with(mainSpec)
                task.with(combinedToIceSpec)
            }
        })
    }

    void configureTaskOrdering() {
        // Ice docs task depends on all ice files being present
        project.tasks.withType(IceDocsTask).configureEach {
            it.dependsOn(project.tasks.named(TASK_PROCESS_SLICE))
        }
    }

}
