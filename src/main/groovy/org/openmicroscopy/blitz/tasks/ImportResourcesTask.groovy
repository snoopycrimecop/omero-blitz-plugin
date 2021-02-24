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
package org.openmicroscopy.blitz.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.Factory

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@CompileStatic
class ImportResourcesTask extends DefaultTask {

    @OutputDirectory
    final DirectoryProperty extractDir = project.objects.directoryProperty()

    @Input
    final Property<String> pattern = project.objects.property(String)

    @Input
    final Property<String> artifactName = project.objects.property(String)

    private final PatternSet patternSet

    private Configuration config

    ImportResourcesTask() {
        patternSet = getPatternSetFactory().create()
        artifactName.convention("omero-model")
    }

    @Inject
    protected Factory<PatternSet> getPatternSetFactory() {
        throw new UnsupportedOperationException()
    }

    @TaskAction
    void apply() {
        final String artifactName = artifactName.get()

        File artifact = config.files.find {
            it.name.contains(artifactName)
        }
        if (!artifact) {
            throw new GradleException("$artifactName artifact not found")
        }

        // Set our pattern set
        patternSet.include(pattern.get())

        // obtain file tree for jar file
        FileTree fileTree = project.zipTree(artifact).matching(patternSet)

        // Copy each file matching pattern to our extract directory
        fileTree.files.each { File src ->
            Path file = src.toPath()
            Path to = extractDir.asFile.get().toPath()

            // Copy each file to output location
            Files.copy(src.toPath(), to.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    @InputFiles
    Configuration getConfig() {
        this.config
    }

    void setConfig(Configuration config) {
        this.config = config

        // Set this task to depend on the configuration
        this.dependsOn(config)
    }

    void setExtractDir(File dir) {
        this.extractDir.set(dir)
    }

    void setExtractDir(Directory dir) {
        this.extractDir.set(dir)
    }

    void setExtractDir(Provider<? extends Directory> dir) {
        this.extractDir.set(dir)
    }

    void setPattern(String pattern) {
        this.pattern.set(pattern)
    }

    void setPattern(Provider<? extends String> pattern) {
        this.pattern.set(pattern)
    }

    void setArtifactName(String name) {
        this.artifactName.set(name)
    }

}
