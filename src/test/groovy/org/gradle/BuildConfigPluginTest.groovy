package org.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class BuildConfigPluginTest {
    @Test
    public void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        println 'project: '+ project;
        //project.pluginManager.apply 'okjava.buildconfig'

        //assertEquals(project.ext.definedGretting, 'okjava build config loaded')
    }
}
