package kr.ac.dongeui.club.domain.member.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.global.config.Db;

public class MemberRepository {

    private static final String SELECT = """
            SELECT m.*, d.name AS department_name
            FROM member m JOIN department d ON d.id = m.department_id
            """;

    public Member findByNaverId(String naverId) throws SQLException {
        return findOne(SELECT + "WHERE m.naver_id = ?", naverId);
    }

    public Member findByStudentNo(String studentNo) throws SQLException {
        return findOne(SELECT + "WHERE m.student_no = ?", studentNo);
    }

    public int countActive() throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM member WHERE status = 'ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** 가입 대기 목록 (신청 순서대로) */
    public List<Member> findPending() throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE m.status = 'PENDING' ORDER BY m.requested_at");
             ResultSet rs = ps.executeQuery()) {
            List<Member> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    /** 가입 승인. 가입 대기 상태일 때만 바뀌므로 두 번 눌러도 한 번만 처리된다. */
    public boolean approve(String studentNo) throws SQLException {
        return update("UPDATE member SET status = 'ACTIVE', approved_at = NOW() WHERE student_no = ? AND status = 'PENDING'", studentNo);
    }

    public void updateProfileImage(String studentNo, String url) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("UPDATE member SET profile_image = ? WHERE student_no = ?")) {
            ps.setString(1, url);
            ps.setString(2, studentNo);
            ps.executeUpdate();
        }
    }

    /** 가입 거절: 신청 줄을 지운다 (다시 로그인하면 가입 신청부터) */
    public boolean deletePending(String studentNo) throws SQLException {
        return update("DELETE FROM member WHERE student_no = ? AND status = 'PENDING'", studentNo);
    }

    /** 가입 신청: 가입 대기(PENDING) 상태로 저장 */
    public void insertPending(String studentNo, String naverId, String name, int departmentId, String phone) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO member (student_no, naver_id, name, department_id, phone, requested_at)
                     VALUES (?, ?, ?, ?, ?, NOW())
                     """)) {
            ps.setString(1, studentNo);
            ps.setString(2, naverId);
            ps.setString(3, name);
            ps.setInt(4, departmentId);
            ps.setString(5, phone);
            ps.executeUpdate();
        }
    }

    private static boolean update(String sql, String param) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);
            return ps.executeUpdate() == 1;
        }
    }

    private Member findOne(String sql, String param) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private static Member map(ResultSet rs) throws SQLException {
        return new Member(
                rs.getString("student_no"), rs.getString("naver_id"), rs.getString("name"),
                rs.getInt("department_id"), rs.getString("department_name"), rs.getString("phone"),
                rs.getString("profile_image"),
                Member.Role.valueOf(rs.getString("role")), Member.Status.valueOf(rs.getString("status")),
                rs.getObject("requested_at", LocalDateTime.class), rs.getObject("approved_at", LocalDateTime.class),
                rs.getObject("left_at", LocalDateTime.class));
    }
}
