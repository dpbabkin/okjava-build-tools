package org.gradle.okjava.buildtool

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildConfigPlugin implements Plugin<Project> {
    void apply(Project project) {
        String urlToGradleFile = project.buildscript.classLoader.getResource('gradle/BuildConfigPlugin.gradle').toURI();
        println 'urlToGradleFile: ' + urlToGradleFile;
        project.apply(from: urlToGradleFile);
    }
}
