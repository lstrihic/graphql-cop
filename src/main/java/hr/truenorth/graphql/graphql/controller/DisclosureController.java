package hr.truenorth.graphql.graphql.controller;

import hr.truenorth.graphql.graphql.input.CommentInput;
import hr.truenorth.graphql.graphql.input.CreateDisclosureInput;
import hr.truenorth.graphql.graphql.input.UpdateDisclosureInput;
import hr.truenorth.graphql.graphql.model.Author;
import hr.truenorth.graphql.graphql.model.Comment;
import hr.truenorth.graphql.graphql.model.Disclosure;
import hr.truenorth.graphql.graphql.service.AuthorService;
import hr.truenorth.graphql.graphql.service.CommentService;
import hr.truenorth.graphql.graphql.service.DisclosureService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Controller
public class DisclosureController {
    private final DisclosureService disclosureService;
    private final AuthorService authorService;
    private final CommentService commentService;

    public DisclosureController(DisclosureService disclosureService,
                               AuthorService authorService,
                               CommentService commentService) {
        this.disclosureService = disclosureService;
        this.authorService = authorService;
        this.commentService = commentService;
    }

    @QueryMapping
    public Disclosure disclosure(@Argument String id) {
        return disclosureService.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Disclosure> disclosures() {
        return disclosureService.findAll();
    }

    @BatchMapping(typeName = "Disclosure", field = "author")
    public Map<Disclosure, Author> author(List<Disclosure> disclosures) {
        List<String> authorIds = disclosures.stream()
                .map(Disclosure::getAuthorId)
                .distinct()
                .toList();

        List<Author> authors = authorService.findByIds(authorIds);

        Map<String, Author> authorMap = authors.stream()
                .collect(java.util.stream.Collectors.toMap(Author::getId, author -> author));

        return disclosures.stream()
                .collect(java.util.stream.Collectors.toMap(
                        disclosure -> disclosure,
                        disclosure -> authorMap.get(disclosure.getAuthorId())
                ));
    }

    @BatchMapping(typeName = "Disclosure", field = "comments")
    public Map<Disclosure, List<Comment>> comments(List<Disclosure> disclosures) {
        List<String> disclosureIds = disclosures.stream()
                .map(Disclosure::getId)
                .toList();

        Map<String, List<Comment>> commentsMap = commentService.findByDisclosureIds(disclosureIds);

        return disclosures.stream()
                .collect(java.util.stream.Collectors.toMap(
                        disclosure -> disclosure,
                        disclosure -> commentsMap.getOrDefault(disclosure.getId(), List.of())
                ));
    }

    @SchemaMapping(typeName = "Disclosure", field = "commentsCount")
    public int commentsCount(Disclosure disclosure) {
        return commentService.findByDisclosureId(disclosure.getId()).size();
    }

    @SchemaMapping(typeName = "Disclosure", field = "isRecent")
    public boolean isRecent(Disclosure disclosure) {
        long daysSincePublished = ChronoUnit.DAYS.between(disclosure.getPublishedDate(), LocalDate.now());
        return daysSincePublished <= 30;
    }

    @MutationMapping
    public Disclosure createDisclosure(@Argument CreateDisclosureInput input) {
        return disclosureService.create(
                input.getTitle(),
                input.getDescription(),
                input.getSeverity(),
                input.getAuthorId()
        );
    }

    @MutationMapping
    public Disclosure updateDisclosure(@Argument String id, @Argument UpdateDisclosureInput input) {
        return disclosureService.update(
                id,
                input.getTitle(),
                input.getDescription(),
                input.getSeverity(),
                input.getStatus()
        ).orElseThrow(() -> new RuntimeException("Disclosure not found: " + id));
    }

    @MutationMapping
    public Comment addComment(@Argument String disclosureId, @Argument CommentInput input) {
        if (!disclosureService.findById(disclosureId).isPresent()) {
            throw new RuntimeException("Disclosure not found: " + disclosureId);
        }

        return commentService.create(
                disclosureId,
                input.getContent(),
                input.getAuthorId()
        );
    }
}
