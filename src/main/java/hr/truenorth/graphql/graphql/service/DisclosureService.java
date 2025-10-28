package hr.truenorth.graphql.graphql.service;

import hr.truenorth.graphql.graphql.model.Disclosure;
import hr.truenorth.graphql.graphql.model.DisclosureStatus;
import hr.truenorth.graphql.graphql.model.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DisclosureService {
    private static final Logger logger = LoggerFactory.getLogger(DisclosureService.class);
    private final Map<String, Disclosure> disclosures = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(4);

    public DisclosureService() {
        initializeSampleData();
    }

    public Optional<Disclosure> findById(String id) {
        logger.info("DisclosureService.findById called for id: {}", id);
        return Optional.ofNullable(disclosures.get(id));
    }

    public List<Disclosure> findAll() {
        logger.info("DisclosureService.findAll called");
        return List.copyOf(disclosures.values());
    }

    public List<Disclosure> findByAuthorId(String authorId) {
        logger.info("DisclosureService.findByAuthorId called for author: {}", authorId);
        simulateLatency();
        return disclosures.values().stream()
                .filter(d -> d.getAuthorId().equals(authorId))
                .toList();
    }

    public Map<String, List<Disclosure>> findByAuthorIds(List<String> authorIds) {
        logger.info("DisclosureService.findByAuthorIds called for {} authors (BATCHED)", authorIds.size());
        simulateLatency();

        Map<String, List<Disclosure>> result = new ConcurrentHashMap<>();
        for (String authorId : authorIds) {
            List<Disclosure> authorDisclosures = disclosures.values().stream()
                    .filter(d -> d.getAuthorId().equals(authorId))
                    .toList();
            result.put(authorId, authorDisclosures);
        }
        return result;
    }

    public List<Disclosure> search(String keyword, int limit, int offset) {
        logger.info("DisclosureService.search called with keyword: {}, limit: {}, offset: {}", keyword, limit, offset);
        return disclosures.values().stream()
                .filter(d -> keyword == null || d.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .skip(offset)
                .limit(limit)
                .toList();
    }

    public Disclosure create(String title, String description, Severity severity, String authorId) {
        String id = String.valueOf(idCounter.getAndIncrement());
        Disclosure disclosure = new Disclosure(id, title, description, LocalDate.now(),
                severity, DisclosureStatus.DRAFT, authorId);
        disclosures.put(id, disclosure);
        logger.info("Created disclosure: {}", id);
        return disclosure;
    }

    public Optional<Disclosure> update(String id, String title, String description,
                                      Severity severity, DisclosureStatus status) {
        Disclosure existing = disclosures.get(id);
        if (existing == null) {
            return Optional.empty();
        }

        if (title != null) existing.setTitle(title);
        if (description != null) existing.setDescription(description);
        if (severity != null) existing.setSeverity(severity);
        if (status != null) existing.setStatus(status);

        logger.info("Updated disclosure: {}", id);
        return Optional.of(existing);
    }

    private void simulateLatency() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeSampleData() {
        disclosures.put("1", new Disclosure(
                "1",
                "Security Vulnerability in Authentication Module",
                "Critical vulnerability allowing unauthorized access through JWT token manipulation",
                LocalDate.of(2025, 1, 15),
                Severity.CRITICAL,
                DisclosureStatus.PUBLISHED,
                "A1"
        ));

        disclosures.put("2", new Disclosure(
                "2",
                "SQL Injection Risk in User Query",
                "Potential SQL injection vulnerability in user search functionality",
                LocalDate.of(2025, 2, 10),
                Severity.HIGH,
                DisclosureStatus.PUBLISHED,
                "A2"
        ));

        disclosures.put("3", new Disclosure(
                "3",
                "Performance Issue with Database Queries",
                "Optimization needed for large dataset queries",
                LocalDate.of(2025, 3, 5),
                Severity.MEDIUM,
                DisclosureStatus.DRAFT,
                "A1"
        ));
    }
}
