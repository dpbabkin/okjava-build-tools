package okjava.buildtool;

import static java.util.Collections.unmodifiableList;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Dmitry Babkin dpbabkin@gmail.com
 *         10/3/2016
 *         20:35.
 */
public class BuildConfigPlugin implements Plugin<Project> {

    private static final String[] DEFAULT_CONFIGS = {"CommonConfig"};
    private static final String TO_STRING = "okjava.BuildConfigPlugin:";

    private static final String EXT_NAME = "okjava";
    private static final String CONFIG_EXT_NAME = "configs";

    private static void checkAndLoad(String gradleConfigName, Project project) {
        URL configUrl = project.getBuildscript().getClassLoader().getResource("gradle/" + gradleConfigName + ".gradle");
        if (configUrl == null) {
            throw new IllegalArgumentException(TO_STRING + " Can not resolve gradle config with name:=" + gradleConfigName);
        }
        System.out.println(TO_STRING + " loading `" + gradleConfigName + " from file:=" + configUrl);
        project.apply(Collections.singletonMap("from", configUrl));
    }

    private static List<String> checkAndLoad(Collection<String> configs, Project project, List<String> loaded) {
        configs.forEach(config -> checkAndLoad(config, project, loaded));
        return loaded;
    }

    private static void checkAndLoad(String gradleConfigName, Project project, List<String> loaded) {
        if (loaded.contains(gradleConfigName)) {
            System.out.println(TO_STRING + ": skipping... config:=" + gradleConfigName + " already loaded by project:=" + project);
            return;
        }
        checkAndLoad(gradleConfigName, project);
        loaded.add(gradleConfigName);
    }

    public void apply(Project project) {

        System.out.println(TO_STRING + " initializing");

        List<String> loaded = new CopyOnWriteArrayList<>();
        HasLoad hasLoad = configs -> checkAndLoad(configs, project, loaded);

        Collection<String> initialConfigs = getInitialConfigs(project);

        MethodClosure methodClosure = project.getExtensions().create(EXT_NAME, MethodClosure.class, hasLoad, HasLoad.FUNCTION_NAME);
        project.getExtensions().getExtraProperties().set(EXT_NAME, methodClosure);

        ((ExtensionAware) methodClosure).getExtensions().getExtraProperties().set(CONFIG_EXT_NAME, unmodifiableList(loaded));

        hasLoad.load(initialConfigs);
        System.out.println(TO_STRING + " initialized");
    }

    private Collection<String> getInitialConfigs(Project project) {
        if (project.getExtensions().getExtraProperties().has(EXT_NAME)) {
            Object object = project.getExtensions().getExtraProperties().get(EXT_NAME);
            if (object instanceof Collection<?>) {
                return ((Collection<?>) object).stream().map(Object::toString).collect(Collectors.toList());
            } else {
                throw new IllegalStateException(TO_STRING + " object in ext." + EXT_NAME + " has wrong type:=" + object.getClass() + " object:=" + object);
            }
        }
        return Arrays.asList(DEFAULT_CONFIGS);
    }

    @Override
    public String toString() {
        return TO_STRING;
    }
}
