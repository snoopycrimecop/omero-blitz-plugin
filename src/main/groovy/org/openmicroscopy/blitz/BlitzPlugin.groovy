package org.openmicroscopy.blitz

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class BlitzPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply(BlitzDslPlugin)
        project.plugins.apply(BlitzApiPlugin)
        project.plugins.apply(BlitzIcePlugin)
    }

}
