package com.websementic.fmp.borrow.repository;

import com.websementic.fmp.borrow.modal.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
}
