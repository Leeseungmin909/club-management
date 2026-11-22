<%-- 일정 목록 (SFR-18): 이번 주 + 다가오는 일정 + 지난 일정(15개씩) --%>
<c:set var="pageCss" value="schedule" />
<c:set var="nav" value="schedules" />
<c:set var="title" value="일정" />
<c:if test="${me.role == 'ADMIN'}">
    <c:set var="headExtra"><a class="icon-btn" href="${ctx}/admin/schedules/write" title="일정 등록" aria-label="일정 등록"><i data-lucide="plus"></i></a></c:set>
</c:if>
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="week">
    <c:forEach items="${week}" var="d">
        <div class="${d.today ? 'on' : ''}${d.has ? ' has' : ''}">
            <t:date value="${d.date}" pattern="E" /><b><t:date value="${d.date}" pattern="d" /></b>
        </div>
    </c:forEach>
</div>

<div class="sec-head" style="margin-top:0">
    <div><p class="eyebrow">UPCOMING</p><h3>다가오는 일정</h3></div>
    <span class="meta">${fn:length(upcoming)}개</span>
</div>
<div class="cards">
<c:forEach items="${upcoming}" var="s">
    <a class="sched" href="${ctx}/schedules/view?id=${s.id}">
        <span class="datebox"><b><t:date value="${s.start_at}" pattern="dd" /></b><small><t:date value="${s.start_at}" pattern="M월" /></small></span>
        <span class="grow">
            <span class="title-sm"><span class="ellipsis">${fn:escapeXml(s.title)}</span><span class="badge purple">${s.dday == 0 ? 'D-DAY' : 'D-' += s.dday}</span></span>
            <span class="meta"><i data-lucide="clock"></i><t:date value="${s.start_at}" pattern="a h:mm" /></span>
            <c:if test="${not empty s.place_name}"><span class="meta"><i data-lucide="map-pin"></i>${fn:escapeXml(s.place_name)}</span></c:if>
        </span>
        <i class="chev" data-lucide="chevron-right" style="color:#c4c4d0"></i>
    </a>
</c:forEach>
</div>
<c:if test="${empty upcoming}"><p class="empty">다가오는 일정이 없어요.</p></c:if>

<p class="divider">지난 일정</p>
<div class="cards">
<c:forEach items="${past}" var="s">
    <a class="sched past" href="${ctx}/schedules/view?id=${s.id}">
        <span class="datebox"><b><t:date value="${s.start_at}" pattern="dd" /></b><small><t:date value="${s.start_at}" pattern="M월" /></small></span>
        <span class="grow">
            <span class="title-sm"><span class="ellipsis">${fn:escapeXml(s.title)}</span></span>
            <c:if test="${not empty s.place_name}"><span class="meta"><i data-lucide="map-pin"></i>${fn:escapeXml(s.place_name)}</span></c:if>
        </span>
        <span class="badge">완료</span>
    </a>
</c:forEach>
</div>
<c:if test="${empty past}"><p class="empty">지난 일정이 없어요.</p></c:if>
<c:url var="pageUrl" value="/schedules?page=" />
<t:pager current="${pageNo}" total="${totalPages}" url="${pageUrl}" />

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
