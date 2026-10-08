<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.sql.*, club.Db" %>
<%-- 배포 확인용 임시 페이지. 로그인 화면이 생기면 교체한다. --%>
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>모아</title></head>
<body>
<h1>모아 배포 확인</h1>
<%
    try (Connection c = Db.get();
         PreparedStatement ps = c.prepareStatement("SELECT name FROM club WHERE id = 1");
         ResultSet rs = ps.executeQuery()) {
        out.print(rs.next() ? "DB 연결 OK: " + rs.getString(1) : "DB 연결 OK, club 데이터 없음");
    } catch (Exception e) {
        out.print("DB 연결 실패: " + e.getMessage());
    }
%>
</body>
</html>
