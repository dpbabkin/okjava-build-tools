package okjava.buildtool;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Project;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

//import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

//import static org.hamcrest.core.IsInstanceOf.any;

/**
 * @author Dmitry Babkin dpbabkin@gmail.com
 *         10/3/2016
 *         21:07.
 */
public class BuildConfigPluginTest {
    private static final String[] DEFAULT_CONFIGS = {"CommonConfig"};
    private static final String OKJAVA_EXT_NAME = "okjava";
    private static final String CONFIG_EXT_NAME = "configs";

    private final BuildConfigPlugin buildConfigPlugin = new BuildConfigPlugin();


    // Project project = ProjectBuilder.builder().build();
    // do not use ProjectBuilder as it creates files on file system.
    private final Project project = mock(Project.class);

    private final ExtensionContainer extensionContainer = mock(ExtensionContainer.class);
    private final ExtraPropertiesExtension extraPropertiesExtension = mock(ExtraPropertiesExtension.class);
    private final ScriptHandler scriptHandler = mock(ScriptHandler.class);

    private final ClassLoader classLoader = mock(ClassLoader.class);

    private final URL url1 = createURL("file1");
    private final URL url2 = createURL("file2");

    private final ExtensionContainer extensionContainerMethodClosure = mock(ExtensionContainer.class);
    private final ExtraPropertiesExtension extraPropertiesExtensionMethodClosure = mock(ExtraPropertiesExtension.class);


    private final ExtensionAwareMethodClosureImpl extensionAwareMethodClosureImpl = mock(ExtensionAwareMethodClosureImpl.class);

    private static URL createURL(String file) {
        try {
            return new URL("https://github.com/" + file);
        } catch (MalformedURLException e) {
            throw new Error("something went terribly wrong");
        }
    }

    @Before
    public void before() {
        when(project.getExtensions()).thenReturn(extensionContainer);

        when(extensionContainer.getExtraProperties()).thenReturn(extraPropertiesExtension);
        when(extraPropertiesExtension.has(eq(OKJAVA_EXT_NAME))).thenReturn(false);

        when(project.getBuildscript()).thenReturn(scriptHandler);
        when(scriptHandler.getClassLoader()).thenReturn(classLoader);
        when(classLoader.getResource(eq("gradle/" + DEFAULT_CONFIGS[0] + ".gradle"))).thenReturn(url1);

        when(extensionContainer.create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), any(HasLoad.class), eq(HasLoad.FUNCTION_NAME))).thenReturn(extensionAwareMethodClosureImpl);

        when(extensionAwareMethodClosureImpl.getExtensions()).thenReturn(extensionContainerMethodClosure);
        when(extensionContainerMethodClosure.getExtraProperties()).thenReturn(extraPropertiesExtensionMethodClosure);
    }


    private static class ExtensionAwareMethodClosureImpl extends MethodClosure implements ExtensionAware {

        public ExtensionAwareMethodClosureImpl(Object owner, String method) {
            super(owner, method);
        }

        @Override
        public ExtensionContainer getExtensions() {
            throw new UnsupportedOperationException("not implemented");
        }
    }


    @Test
    public void testProjectCreatedProject() {

        buildConfigPlugin.apply(project);

        verify(project).apply(singletonMap("from", url1));
        verify(extraPropertiesExtension).has(OKJAVA_EXT_NAME);
        verify(project, times(3)).getExtensions();
        verify(extensionContainer).create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), any(HasLoad.class), eq(HasLoad.FUNCTION_NAME));
        verify(extensionContainer, times(2)).getExtraProperties();
        verify(extraPropertiesExtension).set(OKJAVA_EXT_NAME, extensionAwareMethodClosureImpl);
        verify(extensionAwareMethodClosureImpl).getExtensions();
        verify(extensionContainerMethodClosure).getExtraProperties();
        verify(extraPropertiesExtensionMethodClosure).set(eq(CONFIG_EXT_NAME), eq(copyOf(DEFAULT_CONFIGS)));
    }

    @Test
    public void testProjectCreatedProjectWithZeroInitialModules() {

        when(extraPropertiesExtension.has(eq(OKJAVA_EXT_NAME))).thenReturn(true);

        when(extraPropertiesExtension.get(OKJAVA_EXT_NAME)).thenReturn(emptyList());

        buildConfigPlugin.apply(project);

        verify(project, never()).apply(anyMap());
        List<String> loadedConfigs = captureLoadedConfigs();
        assertThat(loadedConfigs, empty());
    }

    @Test
    public void testProjectCreatedProjectWIthThreeInitialModules() {

        List<String> modules = ImmutableList.of("moduleA", "moduleB", "moduleC");

        when(extraPropertiesExtension.has(eq(OKJAVA_EXT_NAME))).thenReturn(true);
        when(extraPropertiesExtension.get(OKJAVA_EXT_NAME)).thenReturn(modules);

        modules.forEach(moduleName -> when(classLoader.getResource(eq("gradle/" + moduleName + ".gradle"))).thenReturn(createURL(moduleName)));

        buildConfigPlugin.apply(project);

        List<Matcher<? super Map<? extends String, ?>>> matchers = modules
                                                                       .stream()
                                                                       .<Matcher<Map<? extends String, ?>>>map(m -> hasEntry(equalTo("from"), equalTo(createURL(m))))
                                                                       .collect(toList());

        List<Map<String, ?>> mapList = captureMap();
        assertThat(mapList, containsInAnyOrder(matchers));

        modules.stream().map(BuildConfigPluginTest::createURL).forEach(url -> verify(project).apply(singletonMap("from", url)));

        List<String> loadedConfigs = captureLoadedConfigs();
        assertThat(loadedConfigs, is(modules));
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsExceptionIfWrongTypeOfInitialConfiguration() {

        when(extraPropertiesExtension.has(eq(OKJAVA_EXT_NAME))).thenReturn(true);
        when(extraPropertiesExtension.get(OKJAVA_EXT_NAME)).thenReturn(new Object());

        buildConfigPlugin.apply(project);

    }


    @Test(expected = IllegalStateException.class)
    public void testThrowsExceptionWhenWrongTypeOfMethodClosureCreated() {

        when(extensionContainer.create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), any(HasLoad.class), eq(HasLoad.FUNCTION_NAME)))
            .thenReturn(new MethodClosure(new Object(), ""));

        buildConfigPlugin.apply(project);

    }


    @Test
    public void testProjectCreatedProjectAndLoadModuleAfter() {
        String moduleName = "moduleA";

        when(classLoader.getResource(eq("gradle/" + moduleName + ".gradle"))).thenReturn(url2);

        buildConfigPlugin.apply(project);

        List<String> loadedConfigs = captureLoadedConfigs();
        HasLoad      hasLoad       = captureHasLoad();

        assertThat(loadedConfigs, is(copyOf(DEFAULT_CONFIGS)));

        hasLoad.load(moduleName);
        assertThat(loadedConfigs, is(ImmutableList.of(DEFAULT_CONFIGS[0], moduleName)));
    }

    private HasLoad captureHasLoad() {
        ArgumentCaptor<HasLoad> hasLoadCaptor = ArgumentCaptor.forClass(HasLoad.class);
        verify(extensionContainer).create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), hasLoadCaptor.capture());
        return hasLoadCaptor.getAllValues().get(0);
    }

    private List<String> captureLoadedConfigs() {
        ArgumentCaptor<List<String>> loadedListCaptor = ArgumentCaptor.forClass(List.class);
        verify(extraPropertiesExtensionMethodClosure).set(eq(CONFIG_EXT_NAME), loadedListCaptor.capture());
        return loadedListCaptor.getValue();
    }


    private List<Map<String, ?>> captureMap() {
        ArgumentCaptor<Map<String, ?>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(project, atLeastOnce()).apply(mapCaptor.capture());
        return mapCaptor.getAllValues();
    }
}
