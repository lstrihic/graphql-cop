package hr.truenorth.graphql.graphql.controller;

import hr.truenorth.graphql.graphql.model.Author;
import hr.truenorth.graphql.graphql.model.Disclosure;
import hr.truenorth.graphql.graphql.service.AuthorService;
import hr.truenorth.graphql.graphql.service.DisclosureService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class AuthorController {
    private final AuthorService authorService;
    private final DisclosureService disclosureService;

    public AuthorController(AuthorService authorService, DisclosureService disclosureService) {
        this.authorService = authorService;
        this.disclosureService = disclosureService;
    }

    @QueryMapping
    public Author author(@Argument String id) {
        return authorService.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Author> authors() {
        return authorService.findAll();
    }

    @BatchMapping(typeName = "Author", field = "disclosures")
    public Map<Author, List<Disclosure>> disclosures(List<Author> authors) {
        List<String> authorIds = authors.stream()
                .map(Author::getId)
                .toList();

        Map<String, List<Disclosure>> disclosuresMap = disclosureService.findByAuthorIds(authorIds);

        return authors.stream()
                .collect(java.util.stream.Collectors.toMap(
                        author -> author,
                        author -> disclosuresMap.getOrDefault(author.getId(), List.of())
                ));
    }

    @SchemaMapping(typeName = "Author", field = "totalDisclosures")
    public int totalDisclosures(Author author) {
        return disclosureService.findByAuthorId(author.getId()).size();
    }
}
