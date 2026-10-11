package kr.ac.dongeui.club.domain.notification.entity;

import java.time.LocalDateTime;

/** notification 테이블 한 줄. type: APPROVED · NOTICE · SCHEDULE */
public class Notification {

    private final int id;
    private final String type;
    private final String message;
    private final String linkUrl;
    private final boolean read;
    private final LocalDateTime createdAt;

    public Notification(int id, String type, String message, String linkUrl, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.linkUrl = linkUrl;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getLinkUrl() { return linkUrl; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
