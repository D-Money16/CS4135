package com.cs4135.elib.catalogue.domain;

import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookSearchService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> search(String title, String authorName, String categoryName, AvailabilityStatus availability) {
        return bookRepository.searchBooks(title, authorName, categoryName, availability);
    }
}
