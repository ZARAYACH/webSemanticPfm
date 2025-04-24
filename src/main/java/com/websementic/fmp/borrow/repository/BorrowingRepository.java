package com.websementic.fmp.borrow.repository;

import com.websementic.fmp.borrow.modal.Borrowing;
import com.websementic.fmp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findAllByUser(User user);
}
