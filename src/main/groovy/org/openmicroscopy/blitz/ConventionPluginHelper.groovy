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
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin

@CompileStatic
class ConventionPluginHelper {

    static Configuration getApiConfiguration(Project project) {
        project.configurations.findByName(JavaPlugin.API_CONFIGURATION_NAME)
    }

    static Configuration getCompileClasspathConfiguration(Project project) {
        project.configurations.findByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
    }

    static Configuration getRuntimeClasspathConfiguration(Project project) {
        project.configurations.findByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME)
    }

    static Configuration getImportDataFilesConfiguration(Project project) {
        project.configurations.findByName(ImportHelper.CONFIGURATION_NAME)
    }

}
