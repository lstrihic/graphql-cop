package hr.truenorth.graphql.graphql.service;

import hr.truenorth.graphql.graphql.model.Author;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthorService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);
    private final Map<String, Author> authors = new ConcurrentHashMap<>();

    public AuthorService() {
        initializeSampleData();
    }

    public Optional<Author> findById(String id) {
        logger.info("AuthorService.findById called for id: {}", id);
        simulateLatency();
        return Optional.ofNullable(authors.get(id));
    }

    public List<Author> findAll() {
        logger.info("AuthorService.findAll called");
        simulateLatency();
        return List.copyOf(authors.values());
    }

    public List<Author> findByIds(List<String> ids) {
        logger.info("AuthorService.findByIds called for {} authors (BATCHED)", ids.size());
        simulateLatency();
        return ids.stream()
                .map(authors::get)
                .filter(author -> author != null)
                .toList();
    }

    private void simulateLatency() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeSampleData() {
        authors.put("A1", new Author("A1", "John Doe", "john.doe@example.com"));
        authors.put("A2", new Author("A2", "Jane Smith", "jane.smith@example.com"));
        authors.put("A3", new Author("A3", "Bob Johnson", "bob.johnson@example.com"));
    }
}
