package kr.ac.dongeui.club.domain.chat.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.ac.dongeui.club.domain.chat.entity.ChatMessage;
import kr.ac.dongeui.club.global.config.Db;

public class ChatRepository {

    private static final String SELECT = """
            SELECT c.*, m.name AS sender_name, m.profile_image AS sender_profile_image
            FROM chat_message c JOIN member m ON m.student_no = c.sender_no
            """;

    /** id 보다 뒤에 온 메시지 (0 이면 전체), 오래된 순 */
    public List<ChatMessage> findAfter(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE c.id > ? ORDER BY c.id")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<ChatMessage> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public ChatMessage findById(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE c.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** 저장하고 새 번호를 돌려준다 */
    public int insert(String senderNo, String type, String content, String filePath, String fileName) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO chat_message (sender_no, message_type, content, file_path, file_name, created_at)
                     VALUES (?, ?, ?, ?, ?, NOW())
                     """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, senderNo);
            ps.setString(2, type);
            ps.setString(3, content);
            ps.setString(4, filePath);
            ps.setString(5, fileName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void updateContent(int id, String content) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("UPDATE chat_message SET content = ?, updated_at = NOW() WHERE id = ?")) {
            ps.setString(1, content);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM chat_message WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static ChatMessage map(ResultSet rs) throws SQLException {
        return new ChatMessage(rs.getInt("id"), rs.getString("sender_no"), rs.getString("sender_name"),
                rs.getString("sender_profile_image"), rs.getString("message_type"), rs.getString("content"),
                rs.getString("file_path"), rs.getString("file_name"),
                rs.getObject("created_at", LocalDateTime.class), rs.getObject("updated_at", LocalDateTime.class));
    }
}
