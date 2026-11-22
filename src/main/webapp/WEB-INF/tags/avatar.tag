<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="src" %>
<%@ attribute name="size" %>
<%@ attribute name="ring" type="java.lang.Boolean" %>
<%-- 프로필 이미지. 이미지가 없으면 글자로 표시 (SFR-12):
     한글 3~4자 이름은 성을 뺀 2글자(이승민 → 승민), 그 외는 앞 3글자(CPU → CPU) --%>
<c:set var="initials" value="${name.matches('[가-힣]{3,4}') ? fn:substring(name, 1, 3) : fn:substring(name, 0, 3)}" />
<c:choose>
    <c:when test="${not empty src}"><img class="avatar ${size}${ring ? ' ring' : ''}" src="${fn:escapeXml(src)}" alt=""></c:when>
    <c:otherwise><span class="avatar ${size}${ring ? ' ring' : ''}" aria-hidden="true">${fn:escapeXml(initials)}</span></c:otherwise>
</c:choose>
