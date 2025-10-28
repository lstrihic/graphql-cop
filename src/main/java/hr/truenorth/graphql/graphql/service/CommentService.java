package hr.truenorth.graphql.graphql.service;

import hr.truenorth.graphql.graphql.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private final Map<String, Comment> comments = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(4);

    public CommentService() {
        initializeSampleData();
    }

    public Optional<Comment> findById(String id) {
        logger.info("CommentService.findById called for id: {}", id);
        return Optional.ofNullable(comments.get(id));
    }

    public List<Comment> findByDisclosureId(String disclosureId) {
        logger.info("CommentService.findByDisclosureId called for disclosure: {}", disclosureId);
        simulateLatency();
        return comments.values().stream()
                .filter(comment -> comment.getDisclosureId().equals(disclosureId))
                .toList();
    }

    public Map<String, List<Comment>> findByDisclosureIds(List<String> disclosureIds) {
        logger.info("CommentService.findByDisclosureIds called for {} disclosures (BATCHED)", disclosureIds.size());
        simulateLatency();

        Map<String, List<Comment>> result = new ConcurrentHashMap<>();
        for (String disclosureId : disclosureIds) {
            List<Comment> disclosureComments = comments.values().stream()
                    .filter(comment -> comment.getDisclosureId().equals(disclosureId))
                    .toList();
            result.put(disclosureId, disclosureComments);
        }
        return result;
    }

    public Comment create(String disclosureId, String content, String authorId) {
        String id = "C" + idCounter.getAndIncrement();
        Comment comment = new Comment(id, content, LocalDateTime.now(), authorId, disclosureId);
        comments.put(id, comment);
        logger.info("Created comment: {}", id);
        return comment;
    }

    private void simulateLatency() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeSampleData() {
        comments.put("C1", new Comment("C1", "This is a critical issue that needs immediate attention!",
                LocalDateTime.now().minusDays(2), "A2", "1"));
        comments.put("C2", new Comment("C2", "Has this been fixed in the latest version?",
                LocalDateTime.now().minusDays(1), "A3", "1"));
        comments.put("C3", new Comment("C3", "We need to implement proper input validation.",
                LocalDateTime.now().minusHours(5), "A1", "2"));
    }
}
