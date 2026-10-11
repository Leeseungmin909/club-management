package kr.ac.dongeui.club.domain.notification.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.ac.dongeui.club.domain.notification.entity.Notification;
import kr.ac.dongeui.club.global.config.Db;

public class NotificationRepository {

    /** 알림 한 건 저장 (SFR-27). type: APPROVED · NOTICE · SCHEDULE */
    public void create(String receiverNo, String type, String message, String linkUrl) throws SQLException {
        execute("""
                INSERT INTO notification (receiver_no, type, message, link_url, created_at)
                VALUES (?, ?, ?, ?, NOW())
                """, receiverNo, type, message, linkUrl);
    }

    /** 활동 중인 모든 회원에게 같은 알림 저장 (새 공지 · 새 일정) */
    public void createForAllActive(String type, String message, String linkUrl) throws SQLException {
        execute("""
                INSERT INTO notification (receiver_no, type, message, link_url, created_at)
                SELECT student_no, ?, ?, ?, NOW() FROM member WHERE status = 'ACTIVE'
                """, type, message, linkUrl);
    }

    /** 최신순 목록 (offset 부터 limit 개) */
    public List<Notification> findPage(String receiverNo, int limit, int offset) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT * FROM notification WHERE receiver_no = ?
                     ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?
                     """)) {
            ps.setString(1, receiverNo);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> list = new ArrayList<>();
                while (rs.next()) list.add(new Notification(rs.getInt("id"), rs.getString("type"), rs.getString("message"),
                        rs.getString("link_url"), rs.getBoolean("is_read"), rs.getObject("created_at", LocalDateTime.class)));
                return list;
            }
        }
    }

    public int count(String receiverNo, boolean unreadOnly) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM notification WHERE receiver_no = ?"
                     + (unreadOnly ? " AND is_read = FALSE" : ""))) {
            ps.setString(1, receiverNo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** 내 알림 하나를 읽음 처리하고 이동할 주소를 돌려준다. 내 알림이 아니면 null */
    public String markReadAndGetLink(int id, String receiverNo) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT link_url FROM notification WHERE id = ? AND receiver_no = ?")) {
            ps.setInt(1, id);
            ps.setString(2, receiverNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                execute("UPDATE notification SET is_read = TRUE WHERE id = ?", id);
                return rs.getString(1);
            }
        }
    }

    public void markAllRead(String receiverNo) throws SQLException {
        execute("UPDATE notification SET is_read = TRUE WHERE receiver_no = ? AND is_read = FALSE", receiverNo);
    }

    private static void execute(String sql, Object... params) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        }
    }
}
