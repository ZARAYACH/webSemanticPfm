package com.websementic.fmp.borrow.service;

import com.websementic.fmp.book.modal.Book;
import com.websementic.fmp.book.repository.BookRepository;
import com.websementic.fmp.borrow.modal.Borrowing;
import com.websementic.fmp.borrow.modal.dto.BorrowingDto;
import com.websementic.fmp.borrow.repository.BorrowingRepository;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import com.websementic.fmp.user.model.User;
import com.websementic.fmp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<Borrowing> list() {
        return borrowingRepository.findAll();
    }

    public Borrowing findById(long id) throws NotFoundException {
        return borrowingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Borrowing #" + id + " Not found."));
    }

    public Borrowing create(BorrowingDto.PostDto borrowingDto) throws BadArgumentException {
        Borrowing borrowing = validateBorrowingDtoAndCreate(borrowingDto);
        return borrowingRepository.save(borrowing);
    }

    public Borrowing update(Borrowing borrowing, BorrowingDto.PostDto borrowingDto) throws BadArgumentException {
        Borrowing newBorrowing = validateBorrowingDtoAndCreate(borrowingDto);

        borrowing.setBook(newBorrowing.getBook());
        borrowing.setUser(newBorrowing.getUser());
        borrowing.setBorrowDate(newBorrowing.getBorrowDate());
        borrowing.setDueDate(newBorrowing.getDueDate());
        borrowing.setReturnDate(newBorrowing.getReturnDate());

        return borrowingRepository.save(borrowing);
    }

    public void delete(Borrowing borrowing) {
        borrowingRepository.delete(borrowing);
    }

    private Borrowing validateBorrowingDtoAndCreate(BorrowingDto.PostDto borrowingDto) throws BadArgumentException {
        try {
            Assert.isTrue(borrowingDto.dueDate().isAfter(LocalDate.now()), "Invalid due date");
            Assert.isTrue(borrowingDto.returnDate().isAfter(LocalDate.now()), "Invalid return date");
            User user = userRepository.findById(borrowingDto.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User #" + borrowingDto.userId() + " Not found."));
            Book book = bookRepository.findById(borrowingDto.bookId())
                    .orElseThrow(() -> new IllegalArgumentException("Book #" + borrowingDto.userId() + " Not found."));
            return new Borrowing(null, user, book, borrowingDto.borrowDate(), borrowingDto.dueDate(), borrowingDto.returnDate());
        } catch (IllegalArgumentException e) {
            throw new BadArgumentException(e);
        }
    }
}
