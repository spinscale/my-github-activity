package io.firebolt;

import org.jdbi.v3.core.enums.EnumByName;

@EnumByName
public enum EventType {

    PushEvent,
    IssuesEvent("Issue"),
    IssueCommentEvent,
    CommitCommentEvent,
    PullRequestEvent,
    PullRequestReviewEvent("PR Review"),
    PullRequestReviewCommentEvent("PR Review Comment"),
    ReleaseEvent,
    WatchEvent,
    GollumEvent("Wiki"),
    CreateEvent,
    MemberEvent,
    DeleteEvent,
    ForkEvent,
    PublicEvent;

    private final String displayName;

    EventType() {
        // remove event, and add space after capital letters
        this.displayName = name().replaceFirst("Event$", "").replaceFirst("([a-z])([A-Z])", "$1 $2");
    }

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
