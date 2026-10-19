package kr.ac.dongeui.club.domain.notice.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import kr.ac.dongeui.club.global.config.Db;

/**
 * 공지사항 저장소 (SFR-13~16).
 * 화면(JSP)이 DB 컬럼 이름 그대로 값을 읽으므로 한 줄을 Map(id, title, content, is_pinned, view_count,
 * writer_name, created_at, updated_at)으로 돌려준다.
 */
public class NoticeRepository {

    private static final String SELECT = """
            SELECT n.id, n.title, n.content, n.is_pinned, n.view_count, n.created_at, n.updated_at,
                   COALESCE(m.name, '알 수 없음') AS writer_name
            FROM notice n LEFT JOIN member m ON m.student_no = n.writer_no
            """;

    /** 제목에 q 가 포함된 공지 수. q 가 비어 있으면 전체 */
    public int count(String q) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM notice" + where(q))) {
            bind(ps, q, 1);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** 상단 고정 공지가 먼저, 그다음 최신순 (offset 부터 limit 개) */
    public List<Map<String, Object>> findPage(String q, int limit, int offset) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(SELECT + where(q).replace("title", "n.title")
                     + " ORDER BY n.is_pinned DESC, n.created_at DESC, n.id DESC LIMIT ? OFFSET ?")) {
            int next = bind(ps, q, 1);
            ps.setInt(next, limit);
            ps.setInt(next + 1, offset);
            return rows(ps);
        }
    }

    /** 홈 화면용 최근 공지 (고정 공지 우선) */
    public List<Map<String, Object>> findRecent(int limit) throws SQLException {
        return findPage("", limit, 0);
    }

    public Map<String, Object> find(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE n.id = ?")) {
            ps.setInt(1, id);
            List<Map<String, Object>> list = rows(ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    public void increaseViewCount(int id) throws SQLException {
        execute("UPDATE notice SET view_count = view_count + 1 WHERE id = ?", id);
    }

    /** 공지 저장. 만들어진 공지 번호를 돌려준다 */
    public int insert(String writerNo, String title, String content, boolean pinned) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     INSERT INTO notice (writer_no, title, content, is_pinned, created_at) VALUES (?, ?, ?, ?, NOW())
                     """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, writerNo);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setBoolean(4, pinned);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** 제목 · 본문 · 고정 여부 수정. 이미 삭제된 공지면 false */
    public boolean update(int id, String title, String content, boolean pinned) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     UPDATE notice SET title = ?, content = ?, is_pinned = ?, updated_at = NOW() WHERE id = ?
                     """)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setBoolean(3, pinned);
            ps.setInt(4, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM notice WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static String where(String q) {
        return q == null || q.isEmpty() ? "" : " WHERE title LIKE ? ESCAPE '\\\\'";
    }

    /** where() 에서 ? 를 썼으면 값을 넣고, 다음 ? 번호를 돌려준다. LIKE 특수문자(% _ \)는 글자 그대로 찾도록 막는다 */
    private static int bind(PreparedStatement ps, String q, int index) throws SQLException {
        if (q == null || q.isEmpty()) return index;
        String escaped = q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        ps.setString(index, "%" + escaped + "%");
        return index + 1;
    }

    private static List<Map<String, Object>> rows(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("title", rs.getString("title"));
                m.put("content", rs.getString("content"));
                m.put("is_pinned", rs.getBoolean("is_pinned"));
                m.put("view_count", rs.getInt("view_count"));
                m.put("writer_name", rs.getString("writer_name"));
                m.put("created_at", rs.getObject("created_at", LocalDateTime.class));
                m.put("updated_at", rs.getObject("updated_at", LocalDateTime.class));
                list.add(m);
            }
            return list;
        }
    }

    private static void execute(String sql, int id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
