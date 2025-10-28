package hr.truenorth.graphql.graphql.model;

import java.time.LocalDateTime;

public class Comment {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private String authorId;
    private String disclosureId;

    public Comment() {
    }

    public Comment(String id, String content, LocalDateTime createdAt, String authorId, String disclosureId) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.authorId = authorId;
        this.disclosureId = disclosureId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getDisclosureId() {
        return disclosureId;
    }

    public void setDisclosureId(String disclosureId) {
        this.disclosureId = disclosureId;
    }
}
