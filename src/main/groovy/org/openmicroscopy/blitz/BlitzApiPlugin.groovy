package org.openmicroscopy.blitz

import groovy.transform.CompileStatic
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.openmicroscopy.api.ApiPlugin
import org.openmicroscopy.api.extensions.ApiExtension
import org.openmicroscopy.api.extensions.SplitExtension
import org.openmicroscopy.api.tasks.SplitTask
import org.openmicroscopy.dsl.extensions.DslExtension

@CompileStatic
class BlitzApiPlugin implements Plugin<Project> {

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        project.pluginManager.apply(ApiPlugin)

        project.pluginManager.apply(BlitzDslPlugin)

        configureApiPlugin()
    }

    void configureApiPlugin() {
        TaskProvider<Task> genCombinedTask = getGenerateCombinedTask()

        // Configure API extension to rely on .combined files generated
        // from the BlitzDslPlugin task `generateCombined`
        ApiExtension api = project.extensions.getByType(ApiExtension)
        api.combinedFiles.from(genCombinedTask)

        // Results in task named 'combinedToIce'
        api.language.create("ice", new Action<SplitExtension>() {
            @Override
            void execute(SplitExtension splitExtension) {
                splitExtension.setOutputDir("slice/omero/model")
                splitExtension.rename { String fileName ->
                    String result = FilenameUtils.removeExtension(fileName)
                    int index = result.lastIndexOf("I")
                    if (index != -1) {
                        result = result.substring(0, index)
                    }
                    return result
                }
            }
        })

        // Set each API SplitTask to depend on the `generateCombined` task
        project.tasks.withType(SplitTask).configureEach(new Action<SplitTask>() {
            @Override
            void execute(SplitTask st) {
                st.dependsOn(genCombinedTask)
            }
        })
    }

    TaskProvider<Task> getGenerateCombinedTask() {
        DslExtension dsl = project.extensions.getByType(DslExtension)
        Provider<String> taskName = dsl.createTaskName("combined")
        project.tasks.named(taskName.get())
    }
}
