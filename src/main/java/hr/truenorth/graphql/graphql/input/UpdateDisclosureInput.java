package hr.truenorth.graphql.graphql.input;

import hr.truenorth.graphql.graphql.model.DisclosureStatus;
import hr.truenorth.graphql.graphql.model.Severity;

public class UpdateDisclosureInput {
    private String title;
    private String description;
    private Severity severity;
    private DisclosureStatus status;

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
}
