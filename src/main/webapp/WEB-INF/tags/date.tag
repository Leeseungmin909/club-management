<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty" import="java.time.format.DateTimeFormatter, java.time.temporal.TemporalAccessor, java.util.Locale" %>
<%@ attribute name="value" required="true" type="java.lang.Object" %>
<%@ attribute name="pattern" required="true" %>
<%-- LocalDateTime 출력. 예: <t:date value="${n.created_at}" pattern="yyyy. MM. dd" /> --%>
<%= value == null ? "" : DateTimeFormatter.ofPattern(pattern, Locale.KOREAN).format((TemporalAccessor) value) %>
