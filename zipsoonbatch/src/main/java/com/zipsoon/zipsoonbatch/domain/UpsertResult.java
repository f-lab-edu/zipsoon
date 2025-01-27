package com.zipsoon.zipsoonbatch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertResult {
    private Long id;
    private String operation;   // INSERT or UPDATE

    public boolean isUpdate() {
        return "UPDATE".equals(operation);
    }

    public boolean isInsert() {
        return "INSERT".equals(operation);
    }
}