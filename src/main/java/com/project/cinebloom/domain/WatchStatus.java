package com.project.cinebloom.domain;

public enum WatchStatus {
    WANT_TO_WATCH("Want to Watch"),
    WATCHING("Watching"),
    WATCHED("Watched");

    private final String description;

    WatchStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
