package com.zipsoon.batch.domain.estate;

import java.io.Serial;
import java.io.Serializable;

public record DongCode(String code, String name) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}