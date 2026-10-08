<%-- 일정 상세 (SFR-18, 19) + 출석 체크·변경 (SFR-20, 21) + 출석 현황 (SFR-22) + 지도 (SFR-24) --%>
<c:set var="pageCss" value="schedule" />
<c:set var="nav" value="schedules" />
<c:set var="title" value="일정" />
<c:set var="back" value="${ctx}/schedules" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<c:choose>
    <c:when test="${s.started}"><span class="badge">지난 일정</span></c:when>
    <c:otherwise><span class="badge purple">${s.dday == 0 ? 'D-DAY' : 'D-' += s.dday}</span></c:otherwise>
</c:choose>
<h2 class="detail-title">${fn:escapeXml(s.title)}</h2>

<div class="info-rows">
    <div><i data-lucide="calendar"></i><span><t:date value="${s.start_at}" pattern="yyyy. MM. dd (E) a h:mm" /><c:if test="${not empty s.end_at}"> ~ <t:date value="${s.end_at}" pattern="a h:mm" /></c:if></span></div>
    <div><i data-lucide="map-pin"></i><span><b>${fn:escapeXml(s.place_name)}</b><c:if test="${not empty s.address}"><br><span class="meta">${fn:escapeXml(s.address)}</span></c:if></span></div>
</div>

<c:if test="${not empty s.latitude}">
    <%-- 네이버 지도 (SFR-24, 박계령 담당): 이 div 에 지도와 마커를 그린다. 실패하면 아래 안내 문구가 그대로 남는다 --%>
    <div class="map" id="map" data-lat="${s.latitude}" data-lng="${s.longitude}">
        <span><i data-lucide="map"></i><br>네이버 지도 연동 예정<br><span class="small">${fn:escapeXml(s.place_name)}</span></span>
    </div>
</c:if>

<c:if test="${not empty s.content}"><div class="content">${fn:escapeXml(s.content)}</div></c:if>

<h3 style="font-size:16px;margin-top:8px">참석 여부</h3>
<form class="rsvp" method="post" action="${ctx}/schedules/attend">
    <input type="hidden" name="scheduleId" value="${s.id}">
    <button class="yes ${myStatus == 'ATTEND' ? 'on' : ''}" name="status" value="ATTEND" ${s.started ? 'disabled' : ''}>참석할게요</button>
    <button class="no ${myStatus == 'ABSENT' ? 'on' : ''}" name="status" value="ABSENT" ${s.started ? 'disabled' : ''}>참석이 어려워요</button>
</form>
<c:if test="${s.started}"><p class="hint" style="margin:-4px 0 12px">일정이 시작되어 참석 여부를 바꿀 수 없어요.</p></c:if>

<div class="counts">
    <details class="count">
        <summary><b>${fn:length(attendees)}</b>참석 <small>명단</small></summary>
        <ul><c:forEach items="${attendees}" var="m"><li><t:avatar name="${m.name}" src="${m.profile_image}" size="sm" />${fn:escapeXml(m.name)}</li></c:forEach></ul>
    </details>
    <details class="count">
        <summary><b>${fn:length(absentees)}</b>불참 <small>명단</small></summary>
        <ul><c:forEach items="${absentees}" var="m"><li><t:avatar name="${m.name}" src="${m.profile_image}" size="sm" />${fn:escapeXml(m.name)}</li></c:forEach></ul>
    </details>
    <div class="count"><b>${noReply}</b>미응답</div>
</div>

<c:if test="${me.role == 'ADMIN'}">
    <div class="detail-foot" style="margin-top:20px">
        <a class="btn btn-sm" href="${ctx}/admin/schedules/edit?id=${s.id}"><i data-lucide="pencil"></i>수정</a>
        <form method="post" action="${ctx}/admin/schedules/delete" data-confirm="이 일정을 삭제할까요? 출석 기록도 함께 삭제돼요.">
            <input type="hidden" name="id" value="${s.id}">
            <button class="btn btn-sm btn-danger" type="submit"><i data-lucide="trash-2"></i>삭제</button>
        </form>
    </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
