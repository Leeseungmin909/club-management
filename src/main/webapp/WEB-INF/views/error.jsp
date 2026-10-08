<%-- 403 · 404 · 500 공통 에러 화면 (web.xml error-page) --%>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<c:set var="title" value="${code == 403 ? '접근 권한 없음' : code == 404 ? '페이지 없음' : '오류'}" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
</head>
<body class="auth">
<div class="auth-wrap">
    <div class="auth-box center-msg">
        <div class="big-icon"><i data-lucide="${code == 403 ? 'lock' : code == 404 ? 'search-x' : 'triangle-alert'}"></i></div>
        <h2>${code == 403 ? '접근 권한이 없어요' : code == 404 ? '페이지를 찾을 수 없어요' : '잠시 문제가 생겼어요'}</h2>
        <p>${code == 403 ? '관리자만 이용할 수 있는 화면이에요.' : code == 404 ? '주소가 바뀌었거나 없는 페이지예요.' : '잠시 후 다시 시도해 주세요.'}</p>
        <a class="btn btn-primary" href="${ctx}/">홈으로</a>
    </div>
</div>
</body>
</html>
