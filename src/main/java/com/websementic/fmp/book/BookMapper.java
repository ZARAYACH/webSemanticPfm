package com.websementic.fmp.book;

import com.websementic.fmp.CustomPage;
import com.websementic.fmp.book.modal.Book;
import com.websementic.fmp.book.modal.dto.BookDto;
import com.websementic.fmp.borrow.modal.Borrowing;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
@Component
public interface BookMapper {

    BookDto toBookDto(Book book);

    List<BookDto> toBookDto(List<Book> book);

    CustomPage<BookDto> toBookDto(Page<Book> bookPage);

    default Long map(Borrowing borrowing) {
        return borrowing.getId();
    }

}
