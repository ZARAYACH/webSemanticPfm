package com.websementic.fmp.borrow.controller;

import com.websementic.fmp.book.modal.Book;
import com.websementic.fmp.book.service.BookService;
import com.websementic.fmp.borrow.BorrowingMapper;
import com.websementic.fmp.borrow.modal.Borrowing;
import com.websementic.fmp.borrow.modal.dto.BorrowingDto;
import com.websementic.fmp.borrow.service.BorrowingService;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import com.websementic.fmp.user.model.User;
import com.websementic.fmp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingMapper borrowingMapper;
    private final BorrowingService borrowingService;
    private final UserService userService;
    private final BookService bookService;

    @GetMapping
    private List<BorrowingDto> listBorrowings() {
        return borrowingMapper.toBorrowingDto(borrowingService.list());
    }

    @GetMapping("/me")
    private List<BorrowingDto> listMyBorrowings(@AuthenticationPrincipal UserDetails userDetails) throws NotFoundException {
        User user = userService.findByEmail(userDetails.getUsername());
        return borrowingMapper.toBorrowingDto(borrowingService.list(user));
    }

    @GetMapping("/{id}")
    private BorrowingDto findBorrowingById(@PathVariable long id) throws NotFoundException {
        return borrowingMapper.toBorrowingDto(borrowingService.findById(id));
    }

    @PostMapping
    private BorrowingDto createBook(@RequestBody BorrowingDto.BorrowingPostDto borrowingDto) throws BadArgumentException {
        return borrowingMapper.toBorrowingDto(borrowingService.create(borrowingDto));
    }

    @PutMapping("/{id}")
    private BorrowingDto updateBorrowingDto(@PathVariable long id, @RequestBody BorrowingDto.BorrowingPostDto borrowingDto) throws NotFoundException, BadArgumentException {
        Borrowing borrowing = borrowingService.findById(id);
        return borrowingMapper.toBorrowingDto(borrowingService.update(borrowing, borrowingDto));
    }

    @DeleteMapping("/{id}")
    private Map<String, Boolean> deleteBorrowing(@PathVariable long id) throws NotFoundException {
        Borrowing borrowing = borrowingService.findById(id);
        borrowingService.delete(borrowing);
        return Collections.singletonMap("deleted", true);
    }

    @PostMapping("/borrow")
    private BorrowingDto borrowBook(@RequestBody BorrowingDto.BorrowingPostDto borrowingPostDto,
                                    @AuthenticationPrincipal UserDetails principal) throws NotFoundException, BadArgumentException {
        User user = userService.findByEmail(principal.getUsername());
        Book book = bookService.findById(borrowingPostDto.bookId());
        return borrowingMapper.toBorrowingDto(
                borrowingService.borrowBook(book, user, borrowingPostDto.borrowDate(), borrowingPostDto.dueDate()));
    }

    @PostMapping("/{id}/return")
    private BorrowingDto returnBook(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal) throws NotFoundException, BadArgumentException {
        User user = userService.findByEmail(principal.getUsername());
        Borrowing borrowing = borrowingService.findById(id);
        return borrowingMapper.toBorrowingDto(
                borrowingService.returnBook(borrowing, user, LocalDate.now()));
    }
}
