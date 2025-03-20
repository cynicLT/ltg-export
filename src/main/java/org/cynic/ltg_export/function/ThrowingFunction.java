package org.cynic.ltg_export.function;


import java.util.function.Function;
import java.util.function.Supplier;
import org.cynic.ltg_export.domain.ApplicationException;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

    static <T, R, E extends Throwable> Function<T, R> withTry(ThrowingFunction<T, R, E> consumer,
        Function<Throwable, ApplicationException> exception) {
        return it -> {
            try {
                return consumer.apply(it);
            } catch (Throwable e) {
                throw exception.apply(e);
            }
        };
    }

    static <T, R, E extends Throwable> Function<T, R> withTry(ThrowingFunction<T, R, E> consumer,
        Supplier<R> defaultValue) {
        return it -> {
            try {
                return consumer.apply(it);
            } catch (Throwable e) {
                return defaultValue.get();
            }
        };
    }

    R apply(T t) throws E;
}
