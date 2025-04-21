package com.websementic.fmp.borrow.controller;

import com.websementic.fmp.borrow.BorrowingMapper;
import com.websementic.fmp.borrow.modal.Borrowing;
import com.websementic.fmp.borrow.modal.dto.BorrowingDto;
import com.websementic.fmp.borrow.service.BorrowingService;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingMapper borrowingMapper;
    private final BorrowingService borrowingService;

    @GetMapping
    private List<BorrowingDto> listBorrowings() {
        return borrowingMapper.toBorrowingDto(borrowingService.list());
    }

    @GetMapping("/{id}")
    private BorrowingDto findBorrowingById(@PathVariable long id) throws NotFoundException {
        return borrowingMapper.toBorrowingDto(borrowingService.findById(id));
    }

    @PostMapping
    private BorrowingDto createBook(@RequestBody BorrowingDto.PostDto borrowingDto) throws BadArgumentException {
        return borrowingMapper.toBorrowingDto(borrowingService.create(borrowingDto));
    }

    @PutMapping("/{id}")
    private BorrowingDto updateBorrowingDto(@PathVariable long id, @RequestBody BorrowingDto.PostDto borrowingDto) throws NotFoundException, BadArgumentException {
        Borrowing borrowing = borrowingService.findById(id);
        return borrowingMapper.toBorrowingDto(borrowingService.update(borrowing, borrowingDto));
    }

    @DeleteMapping("/{id}")
    private Map<String, Boolean> deleteBorrowing(@PathVariable long id) throws NotFoundException {
        Borrowing borrowing = borrowingService.findById(id);
        borrowingService.delete(borrowing);
        return Collections.singletonMap("deleted", true);
    }
}
