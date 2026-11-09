package kr.ac.dongeui.club.domain.schedule.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.ac.dongeui.club.global.config.Db;

/** 출석(참석 여부) 저장소 (SFR-20~22) */
public class AttendanceRepository {

    /** 처음 누르면 저장, 다시 누르면 바꾼다 (한 사람이 일정마다 한 줄) */
    public void save(int scheduleId, String studentNo, String status) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO attendance (schedule_id, student_no, status, updated_at) VALUES (?, ?, ?, NOW())
                     ON DUPLICATE KEY UPDATE status = VALUES(status), updated_at = NOW()
                     """)) {
            ps.setInt(1, scheduleId);
            ps.setString(2, studentNo);
            ps.setString(3, status);
            ps.executeUpdate();
        }
    }

    /** 내 응답(ATTEND / ABSENT). 아직 안 눌렀으면 null */
    public String findStatus(int scheduleId, String studentNo) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT status FROM attendance WHERE schedule_id = ? AND student_no = ?")) {
            ps.setInt(1, scheduleId);
            ps.setString(2, studentNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** 활동 중인 회원 중 해당 응답을 한 사람 (이름순). 키: name, profile_image */
    public List<Map<String, Object>> findMembers(int scheduleId, String status) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT m.name, m.profile_image FROM attendance a JOIN member m ON m.student_no = a.student_no
                     WHERE a.schedule_id = ? AND a.status = ? AND m.status = 'ACTIVE' ORDER BY m.name, m.student_no
                     """)) {
            ps.setInt(1, scheduleId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", rs.getString("name"));
                    m.put("profile_image", rs.getString("profile_image"));
                    list.add(m);
                }
                return list;
            }
        }
    }

    /** 내 출석률 계산용: {total, attend}. 가입(승인) 이후 이미 시작한 일정만 센다 */
    public int[] myStats(String studentNo, LocalDateTime now) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT COUNT(*), COALESCE(SUM(a.status = 'ATTEND'), 0)
                     FROM schedule s JOIN member m ON m.student_no = ?
                     LEFT JOIN attendance a ON a.schedule_id = s.id AND a.student_no = m.student_no
                     WHERE s.start_at <= ? AND s.start_at >= COALESCE(m.approved_at, m.requested_at)
                     """)) {
            ps.setString(1, studentNo);
            ps.setObject(2, now);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[]{rs.getInt(1), rs.getInt(2)};
            }
        }
    }

    /** 내 최근 응답 기록. 키: schedule_id, title, start_at, status */
    public List<Map<String, Object>> myHistory(String studentNo, int limit) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT s.id, s.title, s.start_at, a.status FROM attendance a JOIN schedule s ON s.id = a.schedule_id
                     WHERE a.student_no = ? ORDER BY s.start_at DESC, s.id DESC LIMIT ?
                     """)) {
            ps.setString(1, studentNo);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("schedule_id", rs.getInt("id"));
                    m.put("title", rs.getString("title"));
                    m.put("start_at", rs.getObject("start_at", LocalDateTime.class));
                    m.put("status", rs.getString("status"));
                    list.add(m);
                }
                return list;
            }
        }
    }
}
