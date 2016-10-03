//
//package okjava.buildtool;
//
//import static java.util.Collections.unmodifiableList;
//import static java.util.Objects.requireNonNull;
//
//import org.gradle.api.Project;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//
//class Extension {
//
//    private final List<String> configs = new CopyOnWriteArrayList<>();
//    private final List<String> unmodifiableListConfigs = unmodifiableList(configs);
//    private final Project project;
//
//    static Extension create(Project project) {
//        return new Extension(null, project);
//    }
//
//    private Extension(Void _void, Project project) {
//        this.project = requireNonNull(project);
//    }
//
//    public List<String> getConfigs() {
//        return unmodifiableListConfigs;
//    }
//
//    private Extension addConfigs(List<String> configs) {
//        //System.out.println("adding config:=" + config + " to project:=" + this.project);
//        this.configs.addAll(configs);
//        return this;
//    }
//
//    private Extension addConfigs(String... configs) {
//        this.addConfigs(Arrays.asList(configs));
//        return this;
//    }
//
//    public List<String> load(Collection<String> gradleConfigNames) {
//        gradleConfigNames.forEach(this::loadNative);
//        return getConfigs();
//    }
//
//    public List<String> load(String... gradleConfigNames) {
//        return load(Arrays.asList(gradleConfigNames));
//    }
//
//    private void loadNative(String gradleConfigName) {
//        if (configs.contains(gradleConfigName)) {
//            System.out.println(BuildConfigPlugin.TO_STRING+": skipping... config:=" + gradleConfigName + " already loaded by project:=" + project);
//            return;
//        }
//        BuildConfigPlugin.load(gradleConfigName, project);
//        addConfigs(gradleConfigName);
//    }
//
//    @Override
//    public String toString() {
//        return "BuildConfigPlugin.Extension{" + "project=" + project + ", configs=" + configs + '}';
//    }
//}
