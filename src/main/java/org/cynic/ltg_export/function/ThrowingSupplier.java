package org.cynic.ltg_export.function;


import java.util.function.Function;
import java.util.function.Supplier;
import org.cynic.ltg_export.domain.ApplicationException;

@FunctionalInterface
public interface ThrowingSupplier<R, E extends Throwable> {

    static <R, E extends Throwable> Supplier<R> withTry(ThrowingSupplier<R, E> supplier,
        Function<Throwable, ApplicationException> exception) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable e) {
                throw exception.apply(e);
            }
        };
    }

    R get() throws E;
}
