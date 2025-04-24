package com.websementic.fmp.book.modal.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record BookDto(
        @NotNull Long id,
        @NotNull String title,
        @NotNull String author,
        @NotNull String isbn,
        int totalCopies,
        int availableCopies,
        List<Long> borrowingIds,
        @NotNull LocalDateTime createdAt,
        @NotNull LocalDateTime updatedAt
) {
    public record BookPostDto(
            @NotNull String title,
            @NotNull String author,
            @NotNull String isbn,
            int totalCopies
    ) {
    }
}
