package org.gradle.okjava.buildtool

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildConfigPlugin implements Plugin<Project> {

    String[] configs = ['CommonConfig'];

    void apply(Project project) {
        project.ext.definedGretting = 'okjava build config plugin loaded';
        println project.ext.definedGretting;
        project.ext.load = { gradleConfigName -> load(gradleConfigName, project) };
        configs.each { load(it, project) }
    }

    private static void load(String gradleConfigName, Project project) {
        URL url = project.buildscript.classLoader.getResource('gradle/' + gradleConfigName + '.gradle');
        if (url == null) {
            println('BuildConfigPlugin: Can not resolve gradle config with name: ' + gradleConfigName);
            return;
        }
        String urlToGradleConfigFile = url.toURI();
        println 'BuildConfigPlugin.loading `' + gradleConfigName + ' from file: ' + urlToGradleConfigFile;
        project.apply(from: urlToGradleConfigFile);
    }
}
