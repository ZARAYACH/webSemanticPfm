package com.websementic.fmp.book.modal.dto;

import java.util.List;

public record BookDto(
        Long id,
        String title,
        String author,
        String isbn,
        int totalCopies,
        int availableCopies,
        List<Long> borrowingIds
) {
    public record PostDto(
            String title,
            String author,
            String isbn,
            int totalCopies
    ) {
    }
}
