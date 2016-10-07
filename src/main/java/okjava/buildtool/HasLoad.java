package okjava.buildtool;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * @author Dmitry Babkin dpbabkin@gmail.com
 *         10/3/2016
 *         20:35.
 */
@FunctionalInterface
interface HasLoad extends Function<Collection<String>, Collection<String>> {

    String FUNCTION_NAME = "load";

    Collection<String> load(Collection<String> configs);

    default Collection<String> load(String... configs) {
        return apply(Arrays.asList(configs));
    }

    @Override
    default Collection<String> apply(Collection<String> configs) {
        return load(configs);
    }
}
