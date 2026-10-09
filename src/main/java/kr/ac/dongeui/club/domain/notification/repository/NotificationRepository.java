package kr.ac.dongeui.club.domain.notification.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import kr.ac.dongeui.club.global.config.Db;

public class NotificationRepository {

    /** 알림 한 건 저장 (SFR-27). type: APPROVED · NOTICE · SCHEDULE */
    public void create(String receiverNo, String type, String message, String linkUrl) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO notification (receiver_no, type, message, link_url, created_at)
                     VALUES (?, ?, ?, ?, NOW())
                     """)) {
            ps.setString(1, receiverNo);
            ps.setString(2, type);
            ps.setString(3, message);
            ps.setString(4, linkUrl);
            ps.executeUpdate();
        }
    }
}
