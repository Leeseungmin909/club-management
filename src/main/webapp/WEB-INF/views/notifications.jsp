<%-- 알림 목록 (SFR-28): 최신순 15개씩 --%>
<c:set var="title" value="알림" />
<c:set var="back" value="${ctx}/" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="list-head">
    <span>안 읽은 알림 <b>${unread}</b></span>
    <form method="post" action="${ctx}/notifications/read-all"><button class="btn btn-sm" type="submit"><i data-lucide="check-check"></i>모두 읽음</button></form>
</div>
<div class="list">
    <c:forEach items="${notifications}" var="n">
        <%@ include file="/WEB-INF/views/layout/noti-item.jspf" %>
    </c:forEach>
    <c:if test="${empty notifications}"><p class="empty">받은 알림이 없어요.</p></c:if>
</div>
<c:url var="pageUrl" value="/notifications?page=" />
<t:pager current="${pageNo}" total="${totalPages}" url="${pageUrl}" />

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
