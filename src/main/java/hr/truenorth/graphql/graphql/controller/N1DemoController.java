package hr.truenorth.graphql.graphql.controller;

import hr.truenorth.graphql.graphql.model.Author;
import hr.truenorth.graphql.graphql.model.Comment;
import hr.truenorth.graphql.graphql.model.Disclosure;
import hr.truenorth.graphql.graphql.service.AuthorService;
import hr.truenorth.graphql.graphql.service.CommentService;
import hr.truenorth.graphql.graphql.service.DisclosureService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * N+1 Problem Demonstration Controller
 *
 * This controller intentionally does NOT use @BatchMapping to demonstrate
 * the N+1 problem. Use this for demo purposes to show the difference.
 *
 * Compare queries:
 * - disclosures { author { name } }        → Uses @BatchMapping (efficient)
 * - disclosuresNPlusOne { author { name } } → No batching (N+1 problem!)
 */
@Controller
public class N1DemoController {
    private final DisclosureService disclosureService;
    private final AuthorService authorService;
    private final CommentService commentService;

    public N1DemoController(DisclosureService disclosureService,
                            AuthorService authorService,
                            CommentService commentService) {
        this.disclosureService = disclosureService;
        this.authorService = authorService;
        this.commentService = commentService;
    }

    /**
     * Query WITHOUT batching - demonstrates N+1 problem
     * Each disclosure will trigger a separate database call for its author!
     */
    @QueryMapping
    public List<Disclosure> disclosuresNPlusOne() {
        return disclosureService.findAll();
    }

    /**
     * This resolver is called once PER disclosure (N+1 problem!)
     * If you have 100 disclosures, this method is called 100 times.
     */
    @SchemaMapping(typeName = "DisclosureNPlusOne", field = "author")
    public Author authorNPlusOne(Disclosure disclosure) {
        // This is called once per disclosure - causing N+1 problem!
        return authorService.findById(disclosure.getAuthorId())
                .orElse(null);
    }

    /**
     * This resolver is also called once PER disclosure
     */
    @SchemaMapping(typeName = "DisclosureNPlusOne", field = "comments")
    public List<Comment> commentsNPlusOne(Disclosure disclosure) {
        // Another N+1 problem - called once per disclosure
        return commentService.findByDisclosureId(disclosure.getId());
    }
}
