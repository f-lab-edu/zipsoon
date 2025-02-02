package com.zipsoon.batch.domain;

import java.io.Serializable;

public record DongCode(String code, String name) implements Serializable {
    private static final long serialVersionUID = 1L;
}