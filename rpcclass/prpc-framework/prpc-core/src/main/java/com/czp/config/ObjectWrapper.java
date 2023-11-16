package com.czp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectWrapper<T> {
    private byte code;
    private String type;
    private T impl;
}
