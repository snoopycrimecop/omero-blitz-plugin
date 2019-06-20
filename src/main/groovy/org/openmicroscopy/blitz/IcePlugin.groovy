package org.openmicroscopy.blitz

import com.zeroc.gradle.icebuilder.slice.SlicePlugin
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.openmicroscopy.blitz.extensions.IceExtension
import org.openmicroscopy.tasks.IceDocsTask

import javax.inject.Inject

@CompileStatic
class IcePlugin implements Plugin<Project> {

    public static final String EXTENSION_ICE = "ice"

    public static final String TASK_ZIP_ICEDOC = "zipIcedoc"

    public static final String TASK_COMPILE_ICEDOC = "compileIcedoc"

    private Project project

    private IceExtension ice

    private final ProjectLayout layout

    @Inject
    IcePlugin(ProjectLayout layout) {
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
            void execute(IceDocsTask t) {
                t.source = ice.iceSrcDir
                t.includeDirs.add(ice.iceSrcDir)
                t.outputDir.set(ice.docsOutputDir)
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
