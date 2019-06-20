package org.openmicroscopy.blitz

import groovy.transform.CompileStatic
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.openmicroscopy.api.extensions.ApiExtension
import org.openmicroscopy.api.extensions.SplitExtension
import org.openmicroscopy.blitz.BlitzIcePlugin
import org.openmicroscopy.blitz.extensions.IceExtension
import org.openmicroscopy.dsl.extensions.DslExtension
import org.openmicroscopy.dsl.extensions.SingleFileConfig
import org.openmicroscopy.tasks.IcePythonTask

@CompileStatic
class BlitzIcePythonPlugin implements Plugin<Project> {

    public static final String TASK_ZIP_PYTHON = "zipPython"

    public static final String TASK_COMPILE_ICE_PYTHON = "compileIcePython"

    private Project project

    private IceExtension ice

    @Override
    void apply(Project project) {
        project.pluginManager.apply(BlitzIcePlugin)

        this.project = project
        this.ice = project.extensions.getByType(IceExtension)

        addPythonTaskGroup()
        addPythonConfigurations()
        addZipPythonTask()
    }

    TaskProvider<Task> addPythonTaskGroup() {
        def tasks = addIceOmeroTasks("pythonIceOmero", "pythonIceOmeroModel",
                "pythonIceOmeroCmd", "pythonIceOmeroApi")

        project.tasks.register(TASK_COMPILE_ICE_PYTHON) {
            it.setGroup("slice")
            it.setDescription("Runs all ice python tasks")
            it.dependsOn(tasks)
        }
    }

    List<TaskProvider<IcePythonTask>> addIceOmeroTasks(String... taskName) {
        taskName.collect {
            addIceOmeroTask(it)
        }
    }

    TaskProvider<IcePythonTask> addIceOmeroTask(String taskName) {
        String id = taskName.replace("pythonIce", "")
        String camel = id.substring(0, 1).toLowerCase() + id.substring(1)
        String dir = camel.replaceAll("([A-Z])", '/$1').toLowerCase()
        String dirAsPrefix = dir.replace("/", "_") + "_"
        project.tasks.register(taskName, IcePythonTask, new Action<IcePythonTask>() {
            @Override
            void execute(IcePythonTask task) {
                task.dependsOn(project.tasks.named(BlitzIcePlugin.TASK_PROCESS_SLICE))
                task.source = project.files(ice.iceSrcDir.dir(dir))
                task.includeDirs.add(ice.iceSrcDir)
                task.outputDir.set(ice.pythonOutputDir)
                task.prefix.set(dirAsPrefix)
            }
        })
    }

    void addPythonConfigurations() {
        DslExtension dsl = project.extensions.getByType(DslExtension)
        dsl.singleFile.create("objectFactoryRegistrar", new Action<SingleFileConfig>() {
            @Override
            void execute(SingleFileConfig singleFileConfig) {
                singleFileConfig.setTemplate("py_obj_reg.vm")
                singleFileConfig.setOutputFile(ice.pythonOutputDir.map { Directory dir ->
                    new File(dir.asFile, "omero/ObjectFactoryRegistrar.py")
                })
            }
        })

        ApiExtension api = project.extensions.getByType(ApiExtension)
        api.language.create("python", new Action<SplitExtension>() {
            @Override
            void execute(SplitExtension splitExtension) {
                splitExtension.setOutputDir(ice.pythonOutputDir.map { Directory dir ->
                    dir.asFile
                })
                splitExtension.rename({ String file ->
                    "omero_model_" + FilenameUtils.getBaseName(file)
                })
            }
        })

        project.tasks.named(TASK_COMPILE_ICE_PYTHON).configure {
            Provider<String> objectFactoryRegistrarName =
                    dsl.createTaskName("objectFactoryRegistrar")

            it.dependsOn(project.tasks.named("combinedToPython"),
                    project.tasks.named(objectFactoryRegistrarName.get()))
        }
    }

    TaskProvider<Zip> addZipPythonTask() {
        project.tasks.register(TASK_ZIP_PYTHON, Zip, new Action<Zip>() {
            @Override
            void execute(Zip zip) {
                zip.dependsOn(TASK_COMPILE_ICE_PYTHON)
                zip.archiveClassifier.set("python")
                zip.from ice.pythonOutputDir
            }
        })
    }

}
