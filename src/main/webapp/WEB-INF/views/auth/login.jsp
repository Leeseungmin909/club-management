<%-- 로그인 (SFR-01) --%>
<!DOCTYPE html>
<html lang="ko">
<head>
<c:set var="title" value="로그인" />
<c:set var="pageCss" value="auth" />
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
        <a class="btn btn-naver" href="${ctx}/login/naver" data-ga="login" data-ga-method="naver"><b>N</b>네이버로 시작하기</a>
        <a class="btn btn-kakao" href="${ctx}/login/kakao" data-ga="login" data-ga-method="kakao">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3C6.5 3 2 6.5 2 10.8c0 2.8 1.9 5.2 4.7 6.6l-1 3.6c-.1.3.3.6.6.4l4.3-2.8c.5.1 1 .1 1.4.1 5.5 0 10-3.5 10-7.9S17.5 3 12 3z"/></svg>카카오로 시작하기
        </a>
        <p class="terms">로그인하면 서비스 이용약관 및 개인정보 처리방침에 동의하게 됩니다.</p>
    </div>
</div>
</body>
</html>
