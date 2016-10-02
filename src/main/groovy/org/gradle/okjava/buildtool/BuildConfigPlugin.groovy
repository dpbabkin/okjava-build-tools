package org.gradle.okjava.buildtool

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildConfigPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.ext.load = {
            gradleConfigName ->
                String urlToGradleConfigFile = project.buildscript.classLoader.getResource('gradle/' + gradleConfigName + '.gradle').toURI();
                println 'BuildConfigPlugin.loading: ' + urlToGradleConfigFile;
                project.apply(from: urlToGradleConfigFile);
        }
        project.ext.load('BuildConfigPlugin')
    }

//    void load(String configName) {
//        String urlToGradleFile = project.buildscript.classLoader.getResource('gradle/' + configName + '.gradle').toURI();
//        println 'BuildConfigPlugin.loading `' + configName + ' from file: ' + urlToGradleFile;
//        project.apply(from: urlToGradleFile);
//    }
}
