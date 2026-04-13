package com.cs4135.elib.catalogue.application.usecases;

import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import com.cs4135.elib.catalogue.domain.Book;
import com.cs4135.elib.catalogue.domain.BookSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchBooksUseCase {

    @Autowired
    private BookSearchService bookSearchService;

    public List<Book> execute(String title, String authorName, String categoryName, AvailabilityStatus availability) {
        return bookSearchService.search(title, authorName, categoryName, availability);
    }
}
