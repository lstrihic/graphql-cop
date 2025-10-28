package hr.truenorth.graphql.graphql.model;

import java.time.LocalDate;

public class Disclosure {
    private String id;
    private String title;
    private String description;
    private LocalDate publishedDate;
    private Severity severity;
    private DisclosureStatus status;
    private String authorId;

    public Disclosure() {
    }

    public Disclosure(String id, String title, String description, LocalDate publishedDate,
                     Severity severity, DisclosureStatus status, String authorId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publishedDate = publishedDate;
        this.severity = severity;
        this.status = status;
        this.authorId = authorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public DisclosureStatus getStatus() {
        return status;
    }

    public void setStatus(DisclosureStatus status) {
        this.status = status;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}
