package org.cynic.ltg_export.domain;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serial;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;


public final class ApplicationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String code;
    private final transient Map<String, ?> values;

    public ApplicationException(String code, Throwable e, Map<String, ?> values) {
        super(code, e);

        this.code = code;
        this.values = ObjectUtils.clone(values);
    }

    @SafeVarargs
    public ApplicationException(String code, Throwable e, Map.Entry<String, ?>... values) {
        this(code,
            e,
            Arrays.stream(values)
                .collect(
                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                )
        );
    }

    @SafeVarargs
    public ApplicationException(final String code, final Map.Entry<String, ?>... values) {
        this(code, null, values);
    }

    public String getCode() {
        return code;
    }

    public Map<String, ?> getValues() {
        return ObjectUtils.cloneIfPossible(values);
    }

    @Override
    public String getMessage() {
        return new StringJoiner(",")
            .merge(new StringJoiner("=").add("code").add(code))
            .merge(new StringJoiner("=").add("values").add(ArrayUtils.toString(values)))
            .toString();
    }
}
