package okjava.buildtool;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author Dmitry Babkin dpbabkin@gmail.com
 *         10/3/2016
 *         20:35.
 */
public class BuildConfigPlugin implements Plugin<Project> {

    private static final String[] DEFAULT_CONFIGS = {"CommonConfig"};
    private static final String OKJAVA_EXT_NAME = "okjava";
    private static final String CONFIG_EXT_NAME = "configs";
    private static final String LOG_EXT_NAME = "log";
    private static final String LOG_FUNCTION_NAME = LOG_EXT_NAME;
    private static final String TO_STRING = OKJAVA_EXT_NAME + ".BuildConfigPlugin";
    private static final String THIS_LOG_PREFIX = TO_STRING + ": ";
    private static final String PATH_PREFIX = "gradle/";
    private static final String PATH_SUFFIX = ".gradle";


    public void apply(Project project) {
        project.getLogger().info(THIS_LOG_PREFIX + "initializing");

        List<String> loaded  = new CopyOnWriteArrayList<>();
        HasLoad      hasLoad = configs -> checkAndLoad(configs, project, loaded);

        Collection<String> initialConfigs = getInitialConfigs(project);

        MethodClosure okjavaMethodClosure = project.getExtensions().create(OKJAVA_EXT_NAME, MethodClosure.class, hasLoad, HasLoad.FUNCTION_NAME);
        project.getExtensions().getExtraProperties().set(OKJAVA_EXT_NAME, okjavaMethodClosure);

        if (!(okjavaMethodClosure instanceof ExtensionAware)) { //todo check do I need that
            String message = "inconsistent library behaviour. must return 'ExtensionAware' type, but returns ";
            message += okjavaMethodClosure == null ? "null" : "class:=" + okjavaMethodClosure.getClass() + " object:=" + okjavaMethodClosure;
            throw new IllegalStateException(THIS_LOG_PREFIX + message);

        }
        ((ExtensionAware) okjavaMethodClosure).getExtensions().getExtraProperties().set(CONFIG_EXT_NAME, unmodifiableList(loaded));

        Consumer<String> logger = message -> project.getLogger().info(THIS_LOG_PREFIX + message);
        ((ExtensionAware) okjavaMethodClosure).getExtensions().create(LOG_EXT_NAME, MethodClosure.class, logger, "accept");

        hasLoad.load(initialConfigs);

        project.getLogger().info(THIS_LOG_PREFIX + "initialized");
    }

    private static String getPath(String gradleConfigName) {
        return PATH_PREFIX + gradleConfigName + PATH_SUFFIX;
    }


    private static URL getURL(String gradleConfigName, Project project) {
        URL configUrl = project.getBuildscript().getClassLoader().getResource(getPath(gradleConfigName));
        if (configUrl == null) {
            throw new IllegalArgumentException(THIS_LOG_PREFIX + "Can not resolve gradle config with name:=" + gradleConfigName);
        }
        return configUrl;
    }

    private static void load(String gradleConfigName, Project project) {
        URL configUrl = getURL(gradleConfigName, project);
        project.getLogger().info(THIS_LOG_PREFIX + "loading `{} from file:={}", gradleConfigName, configUrl);
        project.apply(singletonMap("from", configUrl));
    }


    private static List<String> checkAndLoad(Collection<String> configs, Project project, List<String> loadedConfigs) {
        configs.forEach(config -> checkAndLoad(config, project, loadedConfigs));
        return loadedConfigs;
    }


    private static void checkAndLoad(String gradleConfigName, Project project, List<String> loadedConfigs) {
        if (loadedConfigs.contains(gradleConfigName)) {
            project.getLogger().info(THIS_LOG_PREFIX + "skipping... config:={} already loaded by project:={}", gradleConfigName, project);
            return;
        }
        load(gradleConfigName, project);
        loadedConfigs.add(gradleConfigName);
    }


    private static Collection<String> getInitialConfigs(Project project) {
        if (project.getExtensions().getExtraProperties().has(OKJAVA_EXT_NAME)) {
            Object object = project.getExtensions().getExtraProperties().get(OKJAVA_EXT_NAME);
            if (object instanceof Collection<?>) {
                return ((Collection<?>) object).stream().map(Object::toString).collect(toList());
            } else {
                throw new IllegalStateException(THIS_LOG_PREFIX + "object in ext." + OKJAVA_EXT_NAME + " has wrong type:=" + object.getClass() + " object:=" + object);
            }
        }
        return asList(DEFAULT_CONFIGS);
    }


    @Override
    public String toString() {
        return TO_STRING;
    }
}
