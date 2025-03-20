package org.cynic.ltg_export.function;

import org.cynic.ltg_export.domain.ApplicationException;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
  static <T, E extends Throwable> Consumer<T> withTry(
      ThrowingConsumer<T, E> consumer, Function<Throwable, ApplicationException> exception) {
    return (it) -> {
      try {
        consumer.accept(it);
      } catch (Throwable e) {
        throw exception.apply(e);
      }
    };
  }

  void accept(T t) throws E;
}
