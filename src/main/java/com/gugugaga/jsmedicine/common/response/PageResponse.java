package com.gugugaga.jsmedicine.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private final List<T> records;
    private final long total;
    private final long page;
    private final long size;
}
