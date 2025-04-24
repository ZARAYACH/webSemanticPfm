package com.websementic.fmp.book.controller;

import com.websementic.fmp.CustomPage;
import com.websementic.fmp.book.BookMapper;
import com.websementic.fmp.book.modal.Book;
import com.websementic.fmp.book.modal.dto.BookDto;
import com.websementic.fmp.book.service.BookService;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookMapper bookMapper;
    private final BookService bookService;

    @GetMapping
    private CustomPage<BookDto> listBooks(@RequestParam(required = false) Set<Long> ids,
                                          @RequestParam(required = false) String search,
                                          @RequestParam(required = false) Integer pageNumber,
                                          @RequestParam(required = false) Integer pageSize) {
        Pageable pageable = Pageable.unpaged();

        if (pageSize != null && pageNumber != null) pageable = PageRequest.of(pageNumber, pageSize);

        return bookMapper.toBookDto(bookService.list(ids, search, pageable));
    }

    @GetMapping("/{id}")
    private BookDto findBookById(@PathVariable long id) throws NotFoundException {
        return bookMapper.toBookDto(bookService.findById(id));
    }

    @PostMapping
    private BookDto createBook(@RequestBody BookDto.BookPostDto bookDto) throws BadArgumentException {
        return bookMapper.toBookDto(bookService.create(bookDto));
    }

    @PutMapping("/{id}")
    private BookDto updateBook(@PathVariable long id, @RequestBody BookDto.BookPostDto bookDto) throws NotFoundException, BadArgumentException {
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
