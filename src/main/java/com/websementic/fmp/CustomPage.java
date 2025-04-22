package com.websementic.fmp;

import java.util.List;

public record CustomPage<T>(

        List<T> content,
        int number,
        int size,
        int totalPages,
        long totalElements,
        int numberOfElements
) {
}