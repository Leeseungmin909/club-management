<%-- 관리자 대시보드 (SFR-26): 월별 신규 가입·탈퇴(막대), 총 인원 누계(꺾은선), 전체 회원 수, 평균 출석률 --%>
<c:set var="nav" value="dashboard" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="dash-head">
    <div>
        <span class="badge purple">ADMIN</span>
        <h2>관리자 대시보드</h2>
        <p class="muted">동아리의 성장을 한눈에 확인하세요.</p>
    </div>
    <t:avatar name="${me.name}" src="${me.profile_image}" size="lg" ring="true" />
</div>

<div class="stats">
    <div class="stat">
        <span class="icon-tile"><i data-lucide="users"></i></span>
        <div><small>전체 회원</small><b>${stats.total}<small>명</small></b><span class="up" style="display:block">+${stats.joinedThisMonth} 이번 달</span></div>
    </div>
    <div class="stat">
        <span class="icon-tile green"><i data-lucide="check"></i></span>
        <div><small>평균 출석률</small><b>${stats.avgRate}<small>%</small></b><span class="meta">활동 중인 회원 평균</span></div>
    </div>
    <div class="stat">
        <span class="icon-tile red"><i data-lucide="log-out"></i></span>
        <div><small>이번 달 탈퇴자</small><b>${stats.leftThisMonth}<small>명</small></b><span class="down" style="display:block">탈퇴 · 추방 포함</span></div>
    </div>
</div>

<div class="chart-card">
    <header>
        <div><p class="meta">회원 성장</p><h4>월별 가입·탈퇴 및 총 인원</h4></div>
        <span class="badge">최근 ${fn:length(chart.labels)}개월</span>
    </header>
    <div class="chart-box"><canvas id="growth" aria-label="월별 가입·탈퇴 및 총 인원 그래프"></canvas></div>
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

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
<script>
const chart = ${chartJson};
new Chart(document.getElementById('growth'), {
  data: {
    labels: chart.labels,
    datasets: [
      { type: 'line', label: '총 인원', data: chart.total, borderColor: '#6c4ee3', backgroundColor: '#fff',
        pointBorderWidth: 2, pointRadius: 4, tension: .3, yAxisID: 'y' },
      { type: 'bar', label: '신규 가입자', data: chart.joined, backgroundColor: '#e4ddfd', borderRadius: 4, barPercentage: .5 },
      { type: 'bar', label: '탈퇴자', data: chart.left, backgroundColor: '#fbd9da', borderRadius: 4, barPercentage: .5 }
    ]
  },
  options: {
    maintainAspectRatio: false,
    plugins: { legend: { position: 'top', align: 'end', labels: { boxWidth: 10, font: { size: 11 } } } },
    scales: { x: { grid: { display: false } }, y: { beginAtZero: true, ticks: { precision: 0 } } }
  }
});
</script>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
