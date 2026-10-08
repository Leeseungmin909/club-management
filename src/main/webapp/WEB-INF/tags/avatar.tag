<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="src" %>
<%@ attribute name="size" %>
<%@ attribute name="ring" type="java.lang.Boolean" %>
<%-- 프로필 이미지. 이미지가 없으면 이름 앞 글자(성 제외 2글자)로 표시 (SFR-12) --%>
<c:choose>
    <c:when test="${not empty src}"><img class="avatar ${size}${ring ? ' ring' : ''}" src="${fn:escapeXml(src)}" alt=""></c:when>
    <c:otherwise><span class="avatar ${size}${ring ? ' ring' : ''}" aria-hidden="true">${fn:escapeXml(fn:length(name) > 2 ? fn:substring(name, 1, 3) : name)}</span></c:otherwise>
</c:choose>
