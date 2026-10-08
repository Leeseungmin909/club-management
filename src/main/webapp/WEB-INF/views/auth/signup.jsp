<%-- 동아리 가입 신청 (SFR-02). Figma에 없어 승인 대기 화면 스타일로 만듦 --%>
<!DOCTYPE html>
<html lang="ko">
<head>
<c:set var="title" value="가입 신청" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
</head>
<body class="auth">
<header class="topbar">
    <a class="brand" href="${ctx}/login"><img class="logo sm" src="${ctx}/img/logo.svg" alt="">모아</a>
    <a class="icon-btn" href="${ctx}/logout" title="나가기" aria-label="나가기"><i data-lucide="log-in"></i></a>
</header>
<div class="auth-wrap">
    <div class="auth-box center">
        <span class="badge purple">처음 오셨네요</span>
        <h2>동아리 가입 신청</h2>
        <p class="lead" style="margin-bottom:22px">학번과 학과를 입력하면 가입 신청이 완료돼요.<br>관리자가 승인하면 모든 기능을 이용할 수 있어요.</p>

        <form class="form-card" method="post" action="${ctx}/signup">
            <c:if test="${not empty error}"><p class="alert">${fn:escapeXml(error)}</p></c:if>
            <div class="grid-2">
                <label class="field"><span>이름</span><input class="input" value="${fn:escapeXml(naver.name)}" readonly></label>
                <label class="field"><span>전화번호</span><input class="input" value="${fn:escapeXml(naver.phone)}" readonly></label>
            </div>
            <label class="field">
                <span>학번</span>
                <input class="input" name="studentNo" value="${fn:escapeXml(param.studentNo)}" required
                       inputmode="numeric" pattern="[0-9]{8}" maxlength="8" placeholder="숫자 8자리" title="학번은 숫자 8자리로 입력해 주세요">
            </label>
            <label class="field">
                <span>학과</span>
                <input class="input" name="department" value="${fn:escapeXml(param.department)}" list="departments" required
                       autocomplete="off" placeholder="학과 이름을 입력해 검색하세요">
                <datalist id="departments">
                    <c:forEach items="${departments}" var="d"><option value="${fn:escapeXml(d.name)}"></option></c:forEach>
                </datalist>
                <p class="hint">이름과 전화번호는 네이버 계정 정보를 사용해요.</p>
            </label>
            <button class="btn btn-primary btn-block" type="submit">가입 신청하기</button>
        </form>
    </div>
</div>
</body>
</html>
