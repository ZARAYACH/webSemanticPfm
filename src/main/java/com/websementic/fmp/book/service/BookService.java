package com.websementic.fmp.book.service;

import com.websementic.fmp.book.modal.Book;
import com.websementic.fmp.book.modal.dto.BookDto;
import com.websementic.fmp.book.repository.BookRepository;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> list() {
        return bookRepository.findAll();
    }

    public Book findById(long id) throws NotFoundException {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book #" + id + "Not found."));
    }

    public Book create(BookDto.PostDto bookDto) throws BadArgumentException {
        Book book = validatePostBookDtoAndCreate(bookDto);
        return bookRepository.save(book);
    }

    public Book update(Book book, BookDto.PostDto bookDto) throws BadArgumentException {
        Book newBook = validatePostBookDtoAndCreate(bookDto);

        book.setIsbn(newBook.getIsbn());
        book.setAuthor(newBook.getAuthor());
        book.setTitle(newBook.getTitle());

        return bookRepository.save(book);
    }

    public void delete(@NotNull Book book) {
        bookRepository.delete(book);
    }

    private Book validatePostBookDtoAndCreate(BookDto.PostDto bookDto) throws BadArgumentException {
        try {
            Assert.hasText(bookDto.title(), "Title is required");
            Assert.hasText(bookDto.author(), "Author is required");
            Assert.hasText(bookDto.isbn(), "Isbn is required");
            Assert.isTrue(bookDto.totalCopies() > 0, "Total copies should be bigger than 0");

            return new Book(null,
                    bookDto.title(),
                    bookDto.author(),
                    bookDto.isbn(),
                    bookDto.totalCopies(),
                    bookDto.totalCopies(),
                    Collections.emptyList());
        } catch (IllegalArgumentException e) {
            throw new BadArgumentException(e);
        }

    }
}
