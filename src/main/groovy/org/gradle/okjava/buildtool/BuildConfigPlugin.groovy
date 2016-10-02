package org.gradle.okjava.buildtool

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildConfigPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.ext.load = { gradleConfigName -> load(gradleConfigName, project); };
        load('BuildConfigPlugin', project);
    }

    private static void load(String gradleConfigName, Project project) {
        URL url = project.buildscript.classLoader.getResource('gradle/' + gradleConfigName + '.gradle');
        if (url == null) {
            println('Can not resolve gradle config with name: ' + gradleConfigName);
            return;
        }
        String urlToGradleConfigFile = url.toURI();
        println 'BuildConfigPlugin.loading `' + gradleConfigName + ' from file: ' + urlToGradleConfigFile;
        project.apply(from: urlToGradleConfigFile);
    }
}
