<%-- 로그인 (SFR-01) --%>
<!DOCTYPE html>
<html lang="ko">
<head>
<c:set var="title" value="로그인" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
</head>
<body class="auth">
<div class="auth-wrap">
    <div class="auth-box">
        <img class="logo" src="${ctx}/img/logo.svg" alt="모아">
        <p class="eyebrow" style="margin-top:28px">우리 동아리의 모든 순간</p>
        <h1>함께 모여,<br><em>더 가까워지는 곳</em></h1>
        <p class="lead">공지부터 일정, 출석까지.<br>동아리 생활을 모아에서 가볍게 시작하세요.</p>
        <c:if test="${not empty error}"><p class="alert">${fn:escapeXml(error)}</p></c:if>
        <a class="btn btn-naver" href="${ctx}/login/naver"><b>N</b>네이버로 시작하기</a>
        <p class="terms">로그인하면 서비스 이용약관 및 개인정보 처리방침에 동의하게 됩니다.</p>
    </div>
</div>
</body>
</html>
