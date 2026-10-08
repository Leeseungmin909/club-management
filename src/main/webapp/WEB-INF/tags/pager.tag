<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="empty" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="current" required="true" type="java.lang.Integer" %>
<%@ attribute name="total" required="true" type="java.lang.Integer" %>
<%@ attribute name="url" required="true" %>
<%-- 페이지 번호 (목록 15개씩, NFR-20). url 은 "page=" 로 끝나는 주소 --%>
<c:if test="${total > 1}">
    <nav class="pager" aria-label="페이지">
        <c:if test="${current > 1}"><a href="${url}${current - 1}" aria-label="이전 페이지"><i data-lucide="chevron-left"></i></a></c:if>
        <c:forEach begin="1" end="${total}" var="p">
            <c:choose>
                <c:when test="${p == current}"><span class="on" aria-current="page">${p}</span></c:when>
                <c:otherwise><a href="${url}${p}">${p}</a></c:otherwise>
            </c:choose>
        </c:forEach>
        <c:if test="${current < total}"><a href="${url}${current + 1}" aria-label="다음 페이지"><i data-lucide="chevron-right"></i></a></c:if>
    </nav>
</c:if>
