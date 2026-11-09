package kr.ac.dongeui.club.domain.schedule.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.ac.dongeui.club.global.config.Db;

/**
 * 일정 저장소 (SFR-17~19). JSP 가 DB 컬럼 이름으로 값을 읽으므로 한 줄을 Map 으로 돌려준다.
 * 키: id, title, content, start_at, end_at, place_name, address, latitude, longitude, dday, started
 */
public class ScheduleRepository {

    private static final String SELECT = """
            SELECT id, title, content, start_at, end_at, place_name, address, latitude, longitude FROM schedule
            """;

    /** 아직 시작하지 않은 일정 (가까운 순) */
    public List<Map<String, Object>> findUpcoming(LocalDateTime now, int limit) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE start_at > ? ORDER BY start_at, id LIMIT ?")) {
            ps.setObject(1, now);
            ps.setInt(2, limit);
            return rows(ps, now);
        }
    }

    public int countPast(LocalDateTime now) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM schedule WHERE start_at <= ?")) {
            ps.setObject(1, now);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** 이미 시작한 일정 (최근 순) */
    public List<Map<String, Object>> findPast(LocalDateTime now, int limit, int offset) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE start_at <= ? ORDER BY start_at DESC, id DESC LIMIT ? OFFSET ?")) {
            ps.setObject(1, now);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            return rows(ps, now);
        }
    }

    /** 오늘부터 days 일 중 일정이 있는 날짜 */
    public Set<LocalDate> datesWithSchedule(LocalDate from, int days) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT DISTINCT DATE(start_at) FROM schedule WHERE start_at >= ? AND start_at < ?")) {
            ps.setObject(1, from.atStartOfDay());
            ps.setObject(2, from.plusDays(days).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                Set<LocalDate> set = new HashSet<>();
                while (rs.next()) set.add(rs.getObject(1, LocalDate.class));
                return set;
            }
        }
    }

    public Map<String, Object> find(int id, LocalDateTime now) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(SELECT + " WHERE id = ?")) {
            ps.setInt(1, id);
            List<Map<String, Object>> list = rows(ps, now);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    public int insert(String writerNo, String title, String content, LocalDateTime start, LocalDateTime end,
                      String placeName, String address, BigDecimal lat, BigDecimal lng) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO schedule (writer_no, title, content, start_at, end_at, place_name, address, latitude, longitude, created_at)
                     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                     """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, writerNo);
            bind(ps, 2, title, content, start, end, placeName, address, lat, lng);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** 이미 삭제된 일정이면 false */
    public boolean update(int id, String title, String content, LocalDateTime start, LocalDateTime end,
                          String placeName, String address, BigDecimal lat, BigDecimal lng) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     UPDATE schedule SET title = ?, content = ?, start_at = ?, end_at = ?, place_name = ?, address = ?,
                            latitude = ?, longitude = ?, updated_at = NOW() WHERE id = ?
                     """)) {
            bind(ps, 1, title, content, start, end, placeName, address, lat, lng);
            ps.setInt(9, id);
            return ps.executeUpdate() == 1;
        }
    }

    /** 출석 기록까지 함께 삭제 (한 번에 처리) */
    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement a = c.prepareStatement("DELETE FROM attendance WHERE schedule_id = ?");
                 PreparedStatement s = c.prepareStatement("DELETE FROM schedule WHERE id = ?")) {
                a.setInt(1, id);
                a.executeUpdate();
                s.setInt(1, id);
                boolean deleted = s.executeUpdate() == 1;
                c.commit();
                return deleted;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static void bind(PreparedStatement ps, int i, String title, String content, LocalDateTime start, LocalDateTime end,
                             String placeName, String address, BigDecimal lat, BigDecimal lng) throws SQLException {
        ps.setString(i, title);
        ps.setString(i + 1, content);
        ps.setObject(i + 2, start);
        ps.setObject(i + 3, end);
        ps.setString(i + 4, placeName);
        ps.setString(i + 5, address);
        ps.setBigDecimal(i + 6, lat);
        ps.setBigDecimal(i + 7, lng);
    }

    private static List<Map<String, Object>> rows(PreparedStatement ps, LocalDateTime now) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                LocalDateTime start = rs.getObject("start_at", LocalDateTime.class);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("title", rs.getString("title"));
                m.put("content", rs.getString("content"));
                m.put("start_at", start);
                m.put("end_at", rs.getObject("end_at", LocalDateTime.class));
                m.put("place_name", rs.getString("place_name"));
                m.put("address", rs.getString("address"));
                m.put("latitude", rs.getBigDecimal("latitude"));
                m.put("longitude", rs.getBigDecimal("longitude"));
                m.put("dday", ChronoUnit.DAYS.between(now.toLocalDate(), start.toLocalDate()));
                m.put("started", !start.isAfter(now));
                list.add(m);
            }
            return list;
        }
    }
}
