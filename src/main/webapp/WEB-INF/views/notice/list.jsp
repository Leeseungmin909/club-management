<%-- 공지 목록 · 검색 (SFR-14, 16) --%>
<c:set var="pageCss" value="notice" />
<c:set var="nav" value="notices" />
<c:set var="title" value="공지사항" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<form class="search" method="get" action="${ctx}/notices" role="search">
    <i data-lucide="search"></i>
    <input type="search" name="q" value="${fn:escapeXml(q)}" placeholder="공지 제목을 검색하세요" aria-label="공지 제목 검색">
</form>

<div class="list-head">
    <span>${empty q ? '전체' : '검색 결과'} <b>${total}</b></span>
    <c:if test="${me.role == 'ADMIN'}"><a class="btn btn-sm" href="${ctx}/admin/notices/write"><i data-lucide="pencil"></i>공지 작성</a></c:if>
</div>

<div class="cards">
<c:forEach items="${notices}" var="n">
    <a class="card ${n.is_pinned ? 'pinned' : ''}" href="${ctx}/notices/view?id=${n.id}">
        <span class="row meta">
            <c:if test="${n.is_pinned}"><span class="badge purple"><i data-lucide="pin"></i>상단 고정</span></c:if>
            <t:date value="${n.created_at}" pattern="yyyy. MM. dd" />
        </span>
        <h4>${fn:escapeXml(n.title)}</h4>
        <p class="ellipsis">${fn:escapeXml(n.content)}</p>
        <span class="views"><i data-lucide="eye"></i>${n.view_count}</span>
    </a>
</c:forEach>
</div>
<c:if test="${empty notices}">
    <p class="empty">${empty q ? '등록된 공지가 없어요.' : '검색 결과가 없습니다.'}</p>
</c:if>

<c:choose>
    <c:when test="${empty q}"><c:url var="pageUrl" value="/notices?page=" /></c:when>
    <c:otherwise><c:url var="pageUrl" value="/notices"><c:param name="q" value="${q}" /></c:url><c:set var="pageUrl" value="${pageUrl}&page=" /></c:otherwise>
</c:choose>
<t:pager current="${pageNo}" total="${totalPages}" url="${pageUrl}" />

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
