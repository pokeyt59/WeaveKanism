package mekanism.fabric_shim.common.util;

import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

/**
 * Same surface as net.neoforged.neoforge.common.util.RecipeMatcher: bipartite matching of inputs
 * to ingredient tests (fresh backtracking implementation).
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
    }

    /**
     * @return for each test index, the index of the input matched to it — or null if no complete
     * assignment exists. Requires inputs.size() == tests.size() to succeed.
     */
    @Nullable
    public static <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> tests) {
        int size = inputs.size();
        if (size != tests.size()) {
            return null;
        }
        int[] result = new int[size];
        boolean[] used = new boolean[size];
        return assign(inputs, tests, result, used, 0) ? result : null;
    }

    private static <T> boolean assign(List<T> inputs, List<? extends Predicate<T>> tests, int[] result, boolean[] used, int test) {
        if (test == tests.size()) {
            return true;
        }
        Predicate<T> predicate = tests.get(test);
        for (int input = 0; input < inputs.size(); input++) {
            if (!used[input] && predicate.test(inputs.get(input))) {
                used[input] = true;
                result[test] = input;
                if (assign(inputs, tests, result, used, test + 1)) {
                    return true;
                }
                used[input] = false;
            }
        }
        return false;
    }
}
