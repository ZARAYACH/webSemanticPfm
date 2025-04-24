package com.websementic.fmp.book.repository;

import com.websementic.fmp.book.modal.Book;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class BookSpecification implements Specification<Book> {

    private final Set<Long> ids;
    private final String searchTerm;

    @Override
    public Predicate toPredicate(Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (ids != null && !ids.isEmpty()) {
            predicates.add(root.get("id").in(ids));
        }

        if (StringUtils.isNotBlank(searchTerm)) {
            String searchPattern = "%" + searchTerm.toLowerCase().trim() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("author")), searchPattern),
                    cb.like(cb.lower(root.get("isbn")), searchPattern)));

        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
