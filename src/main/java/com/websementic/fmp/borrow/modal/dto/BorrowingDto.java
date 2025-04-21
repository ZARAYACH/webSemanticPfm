package com.websementic.fmp.borrow.modal.dto;

import java.time.LocalDate;

public record BorrowingDto(
        Long id,
        Long userId,
        Long bookId,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        Boolean isReturned
) {
    public record PostDto(
            long userId,
            long bookId,
            LocalDate borrowDate,
            LocalDate dueDate,
            LocalDate returnDate) {
    }
}