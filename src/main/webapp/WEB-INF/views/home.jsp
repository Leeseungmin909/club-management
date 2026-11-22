<%-- 홈: 다가오는 일정, 최근 공지, 바로가기 --%>
<c:set var="pageCss" value="home" />
<c:set var="nav" value="home" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="greet">
    <div>
        <p>${greeting},</p>
        <h2>${fn:escapeXml(fn:length(me.name) > 2 ? fn:substring(me.name, 1, fn:length(me.name)) : me.name)}님 <em>반가워요!</em></h2>
    </div>
    <t:avatar name="${me.name}" src="${me.profileImage}" size="lg" ring="true" />
</div>

<c:choose>
    <c:when test="${not empty next}">
        <a class="hero" href="${ctx}/schedules/view?id=${next.id}">
            <p class="hero-top"><span class="pill">${next.dday == 0 ? 'D-DAY' : 'D-' += next.dday}</span>다가오는 일정</p>
            <div class="hero-row">
                <span class="hero-day"><t:date value="${next.start_at}" pattern="d" /></span>
                <span class="hero-sub"><t:date value="${next.start_at}" pattern="M월" /><br><t:date value="${next.start_at}" pattern="EEEE" /></span>
                <div class="grow">
                    <h4 class="ellipsis">${fn:escapeXml(next.title)}</h4>
                    <span class="meta"><i data-lucide="clock"></i><t:date value="${next.start_at}" pattern="a h:mm" /></span>
                    <c:if test="${not empty next.place_name}"><span class="meta"><i data-lucide="map-pin"></i>${fn:escapeXml(next.place_name)}</span></c:if>
                </div>
            </div>
            <span class="go"><i data-lucide="chevron-right"></i></span>
        </a>
    </c:when>
    <c:otherwise>
        <div class="hero empty-hero"><p>다가오는 일정이 없어요.</p></div>
    </c:otherwise>
</c:choose>

<div class="sec-head">
    <div><p class="eyebrow">NOTICE</p><h3>최근 공지</h3></div>
    <a href="${ctx}/notices">전체보기<i data-lucide="chevron-right"></i></a>
</div>
<div class="list">
    <c:forEach items="${recentNotices}" var="n">
        <a href="${ctx}/notices/view?id=${n.id}">
            <span class="icon-tile sm"><i data-lucide="file-text"></i></span>
            <span class="grow">
                <span class="row title-sm">
                    <c:if test="${n.is_pinned}"><span class="badge purple">필독</span></c:if>
                    <span class="ellipsis">${fn:escapeXml(n.title)}</span>
                </span>
                <span class="meta"><t:date value="${n.created_at}" pattern="MM. dd" /> · ${fn:escapeXml(n.writer_name)}</span>
            </span>
            <i class="chev" data-lucide="chevron-right"></i>
        </a>
    </c:forEach>
    <c:if test="${empty recentNotices}"><p class="empty">등록된 공지가 없어요.</p></c:if>
</div>

<div class="grid-2" style="margin-top:12px">
    <a class="tile" href="${ctx}/members">
        <span class="icon-tile"><i data-lucide="users"></i></span>
        <span><b>회원 목록</b><small class="meta">${memberCount}명의 동료</small></span>
    </a>
    <a class="tile" href="${ctx}/me">
        <span class="icon-tile green"><i data-lucide="check"></i></span>
        <span><b>나의 출석</b><small class="meta">참석률 ${myRate}%</small></span>
    </a>
</div>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
