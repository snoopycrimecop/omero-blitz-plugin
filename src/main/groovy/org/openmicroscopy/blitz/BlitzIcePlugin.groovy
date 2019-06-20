package org.openmicroscopy.blitz


import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.openmicroscopy.blitz.extensions.IceExtension

@CompileStatic
class BlitzIcePlugin implements Plugin<Project> {

    public static final String TASK_PROCESS_SLICE = "processSlice"

    public static final String TASK_COMPILE_ICEDOC = "compileIcedoc"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        // Apply the zeroc plugin
        project.pluginManager.apply(IcePlugin)

        project.pluginManager.apply(BlitzApiPlugin)

        // Sort out ordering of tasks
        registerProcessSliceTask()
        configureTaskOrdering()
    }

    TaskProvider<Copy> registerProcessSliceTask() {
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

                IceExtension ice = project.extensions.getByType(IceExtension,)
                task.into(ice.iceSrcDir)
                task.with(mainSpec)
                task.with(combinedToIceSpec)
            }
        })
    }

    void configureTaskOrdering() {
        TaskProvider<Task> processSlice = project.tasks.named(TASK_PROCESS_SLICE)

        // Ice docs task depends on all ice files being present
        project.tasks.named(TASK_COMPILE_ICEDOC).configure {
            it.dependsOn(processSlice)
        }
    }

}
