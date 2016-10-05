package okjava.buildtool;

import static java.util.Collections.singletonMap;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.initialization.dsl.ScriptHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.InstanceOf;
import org.mockito.internal.matchers.VarargMatcher;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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

    private final URL url = createURL();

    private final ExtensionContainer extensionContainerCC = mock(ExtensionContainer.class);
    private final ExtraPropertiesExtension extraPropertiesExtensionCC = mock(ExtraPropertiesExtension.class);

    private final MethodClosure methodClosure = mock(MethodClosure.class);
    private final MyMethodClosure myMethodClosure = mock(MyMethodClosure.class);

    private static URL createURL() {
        try {
            return new URL("https://github.com");
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
        when(classLoader.getResource(eq("gradle/" + DEFAULT_CONFIGS[0] + ".gradle"))).thenReturn(url);

        //when(extensionContainer.create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), any(new Object[]{}.getClass()))).thenReturn(methodClosure);

        when(extensionContainer.create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), any(HasLoad.class), eq(HasLoad.FUNCTION_NAME))).thenReturn(myMethodClosure);
        //when(extensionContainer.create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), any(HasLoad.class))).thenReturn(methodClosure);


        when(myMethodClosure.getExtensions()).thenReturn(extensionContainerCC);
        when(extensionContainerCC.getExtraProperties()).thenReturn(extraPropertiesExtensionCC);


        ArgumentMatcher<Object[]> argumentMatcher = new ArgumentMatcher<Object[]>() {

            @Override
            public boolean matches(Object[] o) {
                return o.length == 2 && o[0] instanceof HasLoad && o[1].equals(HasLoad.FUNCTION_NAME);
            }
        };

        MM<Object> mm = new MM<>(new InstanceOf(HasLoad.class), new Equals(HasLoad.FUNCTION_NAME));

//        when(extensionContainer.create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class),
//            argThat(mm)))
//            .thenReturn(methodClosure);
        //argThat

        //doReturn(methodClosure).when(extensionContainer).create(eq(OKJAVA_EXT_NAME), eq(MethodClosure.class), any(HasLoad.class), eq(HasLoad.FUNCTION_NAME));
    }


    private static class MyMethodClosure extends MethodClosure implements ExtensionAware {

        public MyMethodClosure(Object owner, String method) {
            super(owner, method);
        }

        @Override
        public ExtensionContainer getExtensions() {
            throw new UnsupportedOperationException("not implemented");
        }
    }

    private static class MM<T> implements ArgumentMatcher<T[]>, VarargMatcher {

        private final List<ArgumentMatcher<T>> argumentMatchers;

        public MM(ArgumentMatcher<T>... argumentMatchers) {
            this.argumentMatchers = new ArrayList<>(Arrays.asList(argumentMatchers));
        }

        @Override
        public boolean matches(T[] o) {
            if (o.length != argumentMatchers.size()) {
                return false;
            }
            return IntStream.range(0, o.length).mapToObj(i -> argumentMatchers.get(i).matches(o[i])).allMatch(b -> b);
        }
    }

    @Test
    public void testProjectCreatedProject() {


        buildConfigPlugin.apply(project);

        //ArgumentCaptor<Map<String, ?>> argument = ArgumentCaptor.forClass(Map.class);
        verify(project).apply(eq(singletonMap("from", url)));
        //assertThat(singletonMap("from", url), is(argument.getValue()));

       // assertThat(argument.getValue(),hasEntry("from", url));
    }

    @Test
    @Ignore
    public void testProjectCreatedProject_OLD() {

        Project project = ProjectBuilder.builder().build();
        //project.getBuildscript().getDependencies().add("classpath",new File("d:\\Java\\cs\\build-tool\\build\\classes\\test\\"));
        assertThat(project, notNullValue());
        assertNotNull(project);
        //project.getBuildscript().getDependencies().

        project.getPluginManager().apply(JavaPlugin.class);

        File file = new File("test.gradle");
        assertThat(file.exists(), is(true));


        ConfigurableFileCollection collection = project.files("tt");
        project.apply(singletonMap("from", file.toURI()));


        project.getBuildscript().getDependencies().add("classpath", collection);
        //project.getBuildscript().getClassLoader().

        //project.getBuildscript().getDependencies().add("sas","dsd");
        //project.getBuildscript().getDependencies().add("classpath","com.google.guava:guava:+");

        project.getPluginManager().apply(BuildConfigPlugin.class);
        System.out.println("All OK");
        //assertEquals(project.ext.definedGretting, 'okjava build config loaded')
    }

//    @Test
//    public void testProjectCreatedProject2() {
//        Project project = ProjectBuilder.builder().build();
//
//        assertNotNull(project);
//        project.getPluginManager().apply(BuildConfigPlugin.class);
//        System.out.println("All OK");
//        //assertEquals(project.ext.definedGretting, 'okjava build config loaded')
//    }
}
