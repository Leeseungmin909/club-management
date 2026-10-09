package kr.ac.dongeui.club.domain.member.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
