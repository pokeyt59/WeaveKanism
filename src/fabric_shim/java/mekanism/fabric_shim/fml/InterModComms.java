package mekanism.fabric_shim.fml;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Inter-mod message store (stand-in for FML's InterModComms; same surface). On this port the only
 * expected traffic is Mekanism messaging itself (module container registration) and its own
 * modules; messages to mods that are not loaded are simply never consumed.
 */
public final class InterModComms {

    private InterModComms() {
    }

    public record IMCMessage(String senderModId, String modId, String method, Supplier<?> messageSupplier) {

        /**
         * @deprecated Use {@link #messageSupplier()}.
         */
        @Deprecated
        @SuppressWarnings("unchecked")
        public <T> Supplier<T> getMessageSupplier() {
            return (Supplier<T>) messageSupplier;
        }
    }

    private static final Map<String, Queue<IMCMessage>> MESSAGES = new ConcurrentHashMap<>();

    public static boolean sendTo(String modId, String method, Supplier<?> thing) {
        return sendTo("mekanism", modId, method, thing);
    }

    public static boolean sendTo(String senderModId, String modId, String method, Supplier<?> thing) {
        MESSAGES.computeIfAbsent(modId, k -> new ConcurrentLinkedQueue<>()).add(new IMCMessage(senderModId, modId, method, thing));
        return true;
    }

    /**
     * Streams and consumes the messages sent to the given mod.
     */
    public static Stream<IMCMessage> getMessages(String modId) {
        return getMessages(modId, method -> true);
    }

    /**
     * Streams and consumes the messages sent to the given mod whose method matches the filter.
     */
    public static Stream<IMCMessage> getMessages(String modId, Predicate<String> methodFilter) {
        Queue<IMCMessage> queue = MESSAGES.get(modId);
        if (queue == null) {
            return Stream.empty();
        }
        Stream.Builder<IMCMessage> builder = Stream.builder();
        queue.removeIf(message -> {
            if (methodFilter.test(message.method())) {
                builder.add(message);
                return true;
            }
            return false;
        });
        return builder.build();
    }
}
