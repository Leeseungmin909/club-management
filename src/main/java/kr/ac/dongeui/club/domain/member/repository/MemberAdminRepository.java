package kr.ac.dongeui.club.domain.member.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.ac.dongeui.club.global.config.Db;

/** 회원 목록 · 추방 · 탈퇴 · 관리자 지정 (SFR-07~10) */
public class MemberAdminRepository {

    /** 활동 중인 회원: 관리자 먼저, 이름순. 키: student_no, name, department, phone, role, profile_image */
    public List<Map<String, Object>> findActive() throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT m.student_no, m.name, d.name AS department, m.phone, m.role, m.profile_image
                     FROM member m JOIN department d ON d.id = m.department_id
                     WHERE m.status = 'ACTIVE' ORDER BY m.role = 'ADMIN' DESC, m.name, m.student_no
                     """);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                for (String col : new String[]{"student_no", "name", "department", "phone", "role", "profile_image"}) {
                    m.put(col, rs.getString(col));
                }
                list.add(m);
            }
            return list;
        }
    }

    /** 일반 회원 추방. 활동 중인 일반 회원이 아니면 false (관리자는 추방할 수 없다) */
    public boolean kick(String studentNo) throws SQLException {
        return execute("UPDATE member SET status = 'KICKED', left_at = NOW() WHERE student_no = ? AND status = 'ACTIVE' AND role = 'MEMBER'", studentNo);
    }

    /** 탈퇴: 전화번호를 지운다. 마지막 관리자는 탈퇴할 수 없어 false */
    public boolean withdraw(String studentNo) throws SQLException {
        return execute("UPDATE member SET status = 'WITHDRAWN', left_at = NOW(), phone = NULL WHERE student_no = ? AND status = 'ACTIVE' AND "
                + notLastAdmin(), studentNo);
    }

    public boolean promote(String studentNo) throws SQLException {
        return execute("UPDATE member SET role = 'ADMIN' WHERE student_no = ? AND status = 'ACTIVE' AND role = 'MEMBER'", studentNo);
    }

    /** 관리자 해제. 마지막 관리자면 false */
    public boolean demote(String studentNo) throws SQLException {
        return execute("UPDATE member SET role = 'MEMBER' WHERE student_no = ? AND status = 'ACTIVE' AND role = 'ADMIN' AND "
                + notLastAdmin(), studentNo);
    }

    /** 일반 회원이거나, 관리자가 두 명 이상일 때 참 (같은 테이블 서브쿼리는 한 단계 감싸야 MySQL 이 허용한다) */
    private static String notLastAdmin() {
        return "(role <> 'ADMIN' OR (SELECT n FROM (SELECT COUNT(*) AS n FROM member WHERE role = 'ADMIN' AND status = 'ACTIVE') t) > 1)";
    }

    private static boolean execute(String sql, String studentNo) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentNo);
            return ps.executeUpdate() == 1;
        }
    }
}
