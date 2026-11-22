<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty" import="java.time.*" %>
<%@ attribute name="value" required="true" type="java.time.LocalDateTime" %>
<%-- "방금 전 / 10분 전 / 3시간 전 / 어제 / 5일 전 / 2025. 05. 14" --%>
<%
    long m = Duration.between(value, LocalDateTime.now()).toMinutes();
    String s = m < 1 ? "방금 전" : m < 60 ? m + "분 전" : m < 1440 ? (m / 60) + "시간 전"
            : m < 2880 ? "어제" : m < 10080 ? (m / 1440) + "일 전"
            : java.time.format.DateTimeFormatter.ofPattern("yyyy. MM. dd").format(value);
%><%= s %>
