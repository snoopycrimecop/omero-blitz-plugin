package org.openmicroscopy.blitz.extensions

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty

@CompileStatic
class IceExtension {

    /**
     * The directory from which slice2(java, python) will be obtain it's sources
     */
    final DirectoryProperty iceSrcDir

    final DirectoryProperty outputDir

    final DirectoryProperty docsOutputDir

    final DirectoryProperty pythonOutputDir

    IceExtension(Project project) {
        this.iceSrcDir = project.objects.directoryProperty()
        this.outputDir = project.objects.directoryProperty()
        this.docsOutputDir = project.objects.directoryProperty()
        this.pythonOutputDir = project.objects.directoryProperty()

        // Default ice source directory is 'build/src/ice' and contains
        // ice files located in 'src/main/slice' + 'build/generated/sources/api/slice'.
        // See registerProcessSliceTask
        this.iceSrcDir.convention(project.layout.buildDirectory.dir("src/ice"))
        this.docsOutputDir.convention(project.layout.buildDirectory.dir("docs/icedoc"))
        this.pythonOutputDir.convention(project.layout.buildDirectory.dir("toArchive/python"))
    }

}
