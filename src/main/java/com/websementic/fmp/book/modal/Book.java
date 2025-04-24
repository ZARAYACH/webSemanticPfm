package com.websementic.fmp.book.modal;

import com.websementic.fmp.borrow.modal.Borrowing;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String isbn;
    private int totalCopies;
    private int availableCopies;

    @OneToMany(mappedBy = "book")
    private List<Borrowing> borrowings = new ArrayList<>();
}
