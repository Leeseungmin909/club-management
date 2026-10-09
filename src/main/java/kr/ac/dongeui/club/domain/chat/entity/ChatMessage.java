package kr.ac.dongeui.club.domain.chat.entity;

import java.time.LocalDateTime;

/** chat_message 한 줄 + 보낸 사람 이름 · 프로필. type: TEXT · IMAGE · VIDEO */
public class ChatMessage {

    private final int id;
    private final String senderNo;
    private final String senderName;
    private final String senderProfileImage;
    private final String type;
    private final String content;
    private final String filePath;
    private final String fileName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ChatMessage(int id, String senderNo, String senderName, String senderProfileImage, String type, String content,
                       String filePath, String fileName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.senderNo = senderNo;
        this.senderName = senderName;
        this.senderProfileImage = senderProfileImage;
        this.type = type;
        this.content = content;
        this.filePath = filePath;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public String getSenderNo() { return senderNo; }
    public String getSenderName() { return senderName; }
    public String getSenderProfileImage() { return senderProfileImage; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
