package com.cs4135.elib.catalogue.application.usecases;

import com.cs4135.elib.catalogue.domain.Author;
import com.cs4135.elib.catalogue.domain.Book;
import com.cs4135.elib.catalogue.domain.BookCopy;
import com.cs4135.elib.catalogue.domain.ISBN;
import com.cs4135.elib.catalogue.domain.InventoryService;
import com.cs4135.elib.catalogue.domain.Category;
import com.cs4135.elib.catalogue.dto.BookAddedEvent;
import com.cs4135.elib.catalogue.infrastructure.AuthorRepository;
import com.cs4135.elib.catalogue.infrastructure.BookCopyRepository;
import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import com.cs4135.elib.catalogue.infrastructure.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddBookUseCase {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public Book execute(String rawIsbn, String title, String description,
                        Integer publicationYear, int copies,
                        List<String> authorNames, List<String> categoryNames) {

        ISBN isbn = new ISBN(rawIsbn);

        bookRepository.findByIsbnValue(isbn.getValue()).ifPresent(existing -> {
            throw new RuntimeException("A book with ISBN " + isbn.getValue() + " already exists");
        });

        Book book = new Book(isbn, title, description, publicationYear);

        List<Author> authors = resolveAuthors(authorNames != null ? authorNames : List.of());
        List<Category> categories = resolveCategories(categoryNames != null ? categoryNames : List.of());
        book.getAuthors().addAll(authors);
        book.getCategories().addAll(categories);

        bookRepository.save(book);

        for (int i = 0; i < copies; i++) {
            bookCopyRepository.save(new BookCopy(book));
        }

        inventoryService.updateAvailableCount(book.getId());

        eventPublisher.publishEvent(new BookAddedEvent(book.getId(), isbn.getValue(), title));

        return bookRepository.findById(book.getId()).orElseThrow();
    }

    public List<Book> findAll() {
        return bookRepository.findAllByDeletedAtIsNull();
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
