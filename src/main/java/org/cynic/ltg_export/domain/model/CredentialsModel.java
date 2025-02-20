package org.cynic.ltg_export.domain.model;

import java.io.Serial;
import java.io.Serializable;

public record CredentialsModel(String username, String password) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}
