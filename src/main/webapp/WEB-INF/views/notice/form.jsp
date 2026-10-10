<%-- 공지 작성 · 수정 (SFR-13, 15) + AI 다듬기 (SFR-30) --%>
<c:set var="pageCss" value="notice" />
<c:set var="pageJs" value="notice-form" />
<c:set var="nav" value="${empty notice ? 'noticeWrite' : 'notices'}" />
<c:set var="title" value="${empty notice ? '공지 작성' : '공지 수정'}" />
<c:set var="back" value="${empty notice ? ctx += '/notices' : ctx += '/notices/view?id=' += notice.id}" />
<c:set var="admin" value="${true}" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<form method="post" action="${ctx}/admin/notices/${empty notice ? 'write' : 'edit'}" ${empty notice ? 'data-ga="notice_create"' : ''}>
    <c:if test="${not empty notice}"><input type="hidden" name="id" value="${notice.id}"></c:if>
    <c:if test="${not empty error}"><p class="alert">${fn:escapeXml(error)}</p></c:if>
    <label class="field">
        <span>제목</span>
        <input class="input" name="title" value="${fn:escapeXml(notice.title)}" maxlength="100" required placeholder="공지 제목">
    </label>
    <label class="field">
        <span>내용</span>
        <textarea class="input" id="content" name="content" rows="8" required placeholder="공지 내용을 입력하세요">${fn:escapeXml(notice.content)}</textarea>
    </label>
    <button class="ai-btn" type="button" id="aiBtn" data-url="${ctx}/admin/notices/ai">
        <i data-lucide="sparkles"></i><span id="aiLabel">AI로 다듬기</span><small id="aiHint">초안을 더 명확하고 친근하게</small>
    </button>
    <label class="check" style="margin-bottom:28px">
        <input type="checkbox" name="pinned" value="true" ${notice.is_pinned ? 'checked' : ''}>상단에 중요 공지로 고정
    </label>
    <div class="btns">
        <a class="btn" href="${back}">취소</a>
        <button class="btn btn-primary" type="submit">${empty notice ? '공지 등록' : '수정 완료'}</button>
    </div>
</form>


<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
