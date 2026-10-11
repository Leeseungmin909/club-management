package kr.ac.dongeui.club.domain.dashboard.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import kr.ac.dongeui.club.global.config.Db;

/** 대시보드 집계 (SFR-26) */
public class DashboardRepository {

    /** t 시점에 활동 중이던 인원: 그 전에 승인됐고 그때까지 나가지 않음 */
    public int countMembersAt(LocalDateTime t) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM member WHERE approved_at < ? AND (left_at IS NULL OR left_at >= ?)")) {
            ps.setObject(1, t);
            ps.setObject(2, t);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** from 이후 월별 건수 {"2026-09": 3}. column 은 approved_at(가입) 또는 left_at(탈퇴·추방), 코드 상수만 넘긴다 */
    public Map<String, Integer> countByMonth(String column, LocalDateTime from) throws SQLException {
        String sql = "SELECT DATE_FORMAT(" + column + ", '%Y-%m') ym, COUNT(*) FROM member WHERE " + column + " >= ? GROUP BY ym";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, from);
            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Integer> map = new HashMap<>();
                while (rs.next()) map.put(rs.getString(1), rs.getInt(2));
                return map;
            }
        }
    }

    /**
     * 평균 출석률(%). 활동 중인 회원마다 가입 후 이미 시작한 일정만 센다 (가입 전 일정으로 신입 출석률이 깎이지 않게).
     * 응답이 없으면 불참으로 본다. 대상 일정이 없으면 null.
     */
    public Integer avgAttendanceRate() throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     SELECT ROUND(100 * SUM(a.status <=> 'ATTEND') / COUNT(*))
                     FROM member m
                     JOIN schedule s ON s.start_at >= m.approved_at AND s.start_at <= NOW()
                     LEFT JOIN attendance a ON a.schedule_id = s.id AND a.student_no = m.student_no
                     WHERE m.status = 'ACTIVE'
                     """);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            BigDecimal rate = rs.getBigDecimal(1);
            return rate == null ? null : rate.intValue();
        }
    }
}
