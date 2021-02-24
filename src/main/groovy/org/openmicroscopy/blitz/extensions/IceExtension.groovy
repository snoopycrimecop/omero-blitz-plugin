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
