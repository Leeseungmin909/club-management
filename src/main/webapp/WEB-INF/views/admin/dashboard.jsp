<%-- 관리자 대시보드 (SFR-26): 올해 1~12월 신규 가입·탈퇴(막대), 총 인원 누계(꺾은선), 전체 회원 수, 평균 출석률 --%>
<c:set var="pageCss" value="admin" />
<c:set var="pageJs" value="dashboard" />
<c:set var="nav" value="dashboard" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="dash-head">
    <div>
        <span class="badge purple">ADMIN</span>
        <h2>관리자 대시보드</h2>
        <p class="muted">동아리의 성장을 한눈에 확인하세요.</p>
    </div>
    <t:avatar name="${me.name}" src="${me.profileImage}" size="lg" ring="true" />
</div>

<div class="stats">
    <div class="stat">
        <span class="icon-tile"><i data-lucide="users"></i></span>
        <div><small>전체 회원</small><b>${dash.members}<small>명</small></b><span class="up" style="display:block">+${dash.joinedThisMonth} 이번 달</span></div>
    </div>
    <div class="stat">
        <span class="icon-tile green"><i data-lucide="check"></i></span>
        <div><small>평균 출석률</small><b><c:choose><c:when test="${empty dash.avgRate}">-</c:when><c:otherwise>${dash.avgRate}<small>%</small></c:otherwise></c:choose></b><span class="meta">${empty dash.avgRate ? '지난 일정이 아직 없어요' : '활동 중인 회원 평균'}</span></div>
    </div>
    <div class="stat">
        <span class="icon-tile red"><i data-lucide="log-out"></i></span>
        <div><small>이번 달 탈퇴자</small><b>${dash.leftThisMonth}<small>명</small></b><span class="down" style="display:block">탈퇴 · 추방 포함</span></div>
    </div>
</div>

<div class="chart-card">
    <header>
        <div><p class="meta">회원 성장</p><h4>월별 가입·탈퇴 및 총 인원</h4></div>
        <span class="badge">${dash.year}년</span>
    </header>
    <div class="chart-box"><canvas id="growth" data-chart="${fn:escapeXml(dashJson)}" aria-label="월별 가입·탈퇴 및 총 인원 그래프"></canvas></div>
</div>

<div class="sec-head">
    <div><p class="eyebrow">MANAGEMENT</p><h3>빠른 관리</h3></div>
</div>
<div class="list">
    <a href="${ctx}/admin/approvals">
        <span class="icon-tile"><i data-lucide="shield-check"></i></span>
        <span class="grow"><b class="title-sm">가입 승인</b><span class="meta">대기 ${pendingCount}명</span></span>
        <c:if test="${pendingCount > 0}"><span class="badge orange">${pendingCount}</span></c:if>
    </a>
    <a href="${ctx}/admin/members">
        <span class="icon-tile blue"><i data-lucide="users"></i></span>
        <span class="grow"><b class="title-sm">회원 관리</b><span class="meta">권한 및 추방</span></span>
        <i class="chev" data-lucide="chevron-right"></i>
    </a>
    <a href="${ctx}/admin/club">
        <span class="icon-tile gray"><i data-lucide="settings"></i></span>
        <span class="grow"><b class="title-sm">동아리 설정</b><span class="meta">프로필 편집</span></span>
        <i class="chev" data-lucide="chevron-right"></i>
    </a>
</div>

<%-- 그래프 라이브러리. 화면 스크립트(pages/dashboard.js)보다 먼저 실행되도록 defer 없이 둔다 --%>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
