<%-- 안내 화면 (예: 삭제된 글입니다). 필요한 값: msgIcon, msgTitle, msgText, msgLink, msgLinkText --%>
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="center-msg">
    <div class="big-icon"><i data-lucide="${empty msgIcon ? 'info' : msgIcon}"></i></div>
    <h2>${msgTitle}</h2>
    <p>${msgText}</p>
    <a class="btn btn-primary" href="${empty msgLink ? ctx += '/' : msgLink}">${empty msgLinkText ? '홈으로' : msgLinkText}</a>
</div>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
