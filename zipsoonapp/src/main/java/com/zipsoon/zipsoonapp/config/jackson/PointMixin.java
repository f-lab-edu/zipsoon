package com.zipsoon.zipsoonapp.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"firstPoint", "lastPoint"})
public abstract class PointMixin {
}
