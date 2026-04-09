package com.cs4135.elib.catalogue.application.usecases;

import com.cs4135.elib.catalogue.domain.Author;
import com.cs4135.elib.catalogue.domain.Book;
import com.cs4135.elib.catalogue.domain.Category;
import com.cs4135.elib.catalogue.infrastructure.AuthorRepository;
import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import com.cs4135.elib.catalogue.infrastructure.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UpdateBookUseCase {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Book execute(UUID bookId, String title, String description,
                        Integer publicationYear, List<String> authorNames,
                        List<String> categoryNames) {

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        if (book.isDeleted()) {
            throw new IllegalStateException("Cannot update an archived book: " + bookId);
        }

        if (title != null && !title.isBlank()) {
            book.setTitle(title);
        }
        if (description != null) {
            book.setDescription(description);
        }
        if (publicationYear != null) {
            book.setPublicationYear(publicationYear);
        }
        if (authorNames != null) {
            List<Author> authors = resolveAuthors(authorNames);
            book.getAuthors().clear();
            book.getAuthors().addAll(authors);
        }
        if (categoryNames != null) {
            List<Category> categories = resolveCategories(categoryNames);
            book.getCategories().clear();
            book.getCategories().addAll(categories);
        }

        return bookRepository.save(book);
    }

    private List<Author> resolveAuthors(List<String> names) {
        List<Author> authors = new ArrayList<>();
        for (String name : names) {
            Author author = authorRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> authorRepository.save(new Author(name)));
            authors.add(author);
        }
        return authors;
    }

    private List<Category> resolveCategories(List<String> names) {
        List<Category> categories = new ArrayList<>();
        for (String name : names) {
            Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name)));
            categories.add(category);
        }
        return categories;
    }
}
