package hr.truenorth.graphql.graphql.controller;

import hr.truenorth.graphql.graphql.model.Author;
import hr.truenorth.graphql.graphql.model.Comment;
import hr.truenorth.graphql.graphql.service.AuthorService;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Map;

@Controller
public class CommentController {
    private final AuthorService authorService;

    public CommentController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @BatchMapping(typeName = "Comment", field = "author")
    public Map<Comment, Author> author(List<Comment> comments) {
        List<String> authorIds = comments.stream()
                .map(Comment::getAuthorId)
                .distinct()
                .toList();

        List<Author> authors = authorService.findByIds(authorIds);

        Map<String, Author> authorMap = authors.stream()
                .collect(Collectors.toMap(Author::getId, author -> author));

        return comments.stream()
                .collect(Collectors.toMap(
                        comment -> comment,
                        comment -> authorMap.get(comment.getAuthorId())
                ));
    }
}
