package com.websementic.fmp.borrow;


import com.websementic.fmp.borrow.modal.Borrowing;
import com.websementic.fmp.borrow.modal.dto.BorrowingDto;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
@Component
public interface BorrowingMapper {
    BorrowingDto toBorrowingDto(Borrowing borrowing);

    List<BorrowingDto> toBorrowingDto(List<Borrowing> borrowings);

}
