package com.websementic.fmp.borrow.modal.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BorrowingDto(
        @NotNull Long id,
        @NotNull Long userId,
        @NotNull Long bookId,
        String bookISBN,
        String bookTitle,
        String bookAuthor,
        @NotNull LocalDate borrowDate,
        @NotNull LocalDate dueDate,
        LocalDate returnDate,
        boolean returned
) {
    public record BorrowingPostDto(
            long userId,
            long bookId,
            LocalDate borrowDate,
            LocalDate dueDate,
            LocalDate returnDate) {
    }
}