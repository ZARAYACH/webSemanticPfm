package com.websementic.fmp.book.controller;

import com.websementic.fmp.book.BookMapper;
import com.websementic.fmp.book.modal.Book;
import com.websementic.fmp.book.modal.dto.BookDto;
import com.websementic.fmp.book.service.BookService;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookMapper bookMapper;
    private final BookService bookService;

    @GetMapping
    private List<BookDto> listBooks() {
        return bookMapper.toBookDto(bookService.list());
    }

    @GetMapping("/{id}")
    private BookDto findBookById(@PathVariable long id) throws NotFoundException {
        return bookMapper.toBookDto(bookService.findById(id));
    }

    @PostMapping
    private BookDto createBook(@RequestBody BookDto.PostDto bookDto) throws BadArgumentException {
        return bookMapper.toBookDto(bookService.create(bookDto));
    }

    @PutMapping("/{id}")
    private BookDto updateBook(@PathVariable long id, @RequestBody BookDto.PostDto bookDto) throws NotFoundException, BadArgumentException {
        Book book = bookService.findById(id);
        return bookMapper.toBookDto(bookService.update(book, bookDto));
    }

    @DeleteMapping("/{id}")
    private Map<String, Boolean> deleteBook(@PathVariable long id) throws NotFoundException {
        Book book = bookService.findById(id);
        bookService.delete(book);
        return Collections.singletonMap("deleted", true);
    }
}
