package com.czp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeeperNode {
    private String nodePath;
    private Byte[] data;
}
