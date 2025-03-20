package org.cynic.ltg_export.function;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.cynic.ltg_export.domain.ApplicationException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ThrowingSupplierTest {
  @Test
  void withTryHandledWhenError() {
    ApplicationException exception = Instancio.create(ApplicationException.class);

    Assertions.assertThatThrownBy(
            () ->
                ThrowingSupplier.withTry(
                        () -> {
                          throw exception;
                        },
                        throwable -> exception)
                    .get())
        .asInstanceOf(InstanceOfAssertFactories.type(ApplicationException.class))
        .matches(it -> StringUtils.equals(it.getCode(), exception.getCode()))
        .extracting(ApplicationException::getValues)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsAllEntriesOf(exception.getValues());
  }

  @Test
  void withTryHandledWhenOk() {
    ApplicationException exception = Instancio.create(ApplicationException.class);

    Assertions.assertThat(ThrowingSupplier.withTry(() -> true, throwable -> exception).get())
        .isTrue();
  }
}
