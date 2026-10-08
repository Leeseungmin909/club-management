<%-- 승인 대기 (SFR-02, 04) --%>
<!DOCTYPE html>
<html lang="ko">
<head>
<c:set var="title" value="승인 대기" />
<c:set var="pageCss" value="auth" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
</head>
<body class="auth">
<header class="topbar">
    <a class="brand" href="${ctx}/pending"><img class="logo sm" src="${ctx}/img/logo.svg" alt="">모아</a>
    <a class="icon-btn" href="${ctx}/logout" title="로그아웃" aria-label="로그아웃"><i data-lucide="log-in"></i></a>
</header>
<div class="auth-wrap">
    <div class="auth-box center">
        <div class="orbit"><span class="icon-tile"><i data-lucide="clock"></i></span></div>
        <p style="margin-top:24px"><span class="badge orange">가입 승인 대기</span></p>
        <h2>관리자 승인을<br>기다리고 있어요</h2>
        <p class="lead" style="margin-bottom:0">${fn:escapeXml(club.name)} 관리자가 가입 정보를 확인 중이에요.<br>승인이 완료되면 모든 기능을 이용할 수 있어요.</p>
        <div class="kv">
            <div><span>가입 동아리</span><b>${fn:escapeXml(club.name)}</b></div>
            <div><span>신청자</span><b>${fn:escapeXml(applicant.name)}</b></div>
            <div><span>신청일</span><b><t:date value="${applicant.requested_at}" pattern="yyyy. MM. dd" /></b></div>
        </div>
        <p class="muted small row" style="justify-content:center"><i data-lucide="bell"></i>승인되면 알림으로 알려드릴게요</p>
        <%-- 화면 확인용(DemoServlet). 실제로는 승인 후 다시 로그인하면 홈으로 이동 --%>
        <p style="margin-top:18px"><a class="link-btn" href="${ctx}/">승인 완료 화면 미리보기 ›</a></p>
    </div>
</div>
</body>
</html>
