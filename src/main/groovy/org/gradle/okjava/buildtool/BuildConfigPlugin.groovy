package org.gradle.okjava.buildtool

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildConfigPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.ext.load = { gradleConfigName -> load(gradleConfigName, project); };
        load('BuildConfigPlugin', project);
    }

    private static void load(String gradleConfigName, Project project) {
        String urlToGradleConfigFile = project.buildscript.classLoader.getResource('gradle/' + gradleConfigName + '.gradle').toURI();
        println 'BuildConfigPlugin.loading `' + gradleConfigName + ' from file: ' + urlToGradleConfigFile;
        project.apply(from: urlToGradleConfigFile);
    }
}
