<%-- 공지 상세 (SFR-14, 15) --%>
<c:set var="nav" value="notices" />
<c:set var="title" value="공지사항" />
<c:set var="back" value="${ctx}/notices" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<c:if test="${notice.is_pinned}"><span class="badge purple"><i data-lucide="pin"></i>상단 고정</span></c:if>
<h2 class="detail-title">${fn:escapeXml(notice.title)}</h2>
<p class="row meta">
    ${fn:escapeXml(notice.writer_name)} · <t:date value="${notice.created_at}" pattern="yyyy. MM. dd HH:mm" />
    <c:if test="${not empty notice.updated_at}">(수정됨)</c:if>
    · <i data-lucide="eye" style="width:13px;height:13px"></i>${notice.view_count}
</p>
<hr style="border:0;border-top:1px solid var(--line);margin:18px 0">
<div class="content">${fn:escapeXml(notice.content)}</div>

<c:if test="${me.role == 'ADMIN'}">
    <div class="detail-foot">
        <a class="btn btn-sm" href="${ctx}/admin/notices/edit?id=${notice.id}"><i data-lucide="pencil"></i>수정</a>
        <form method="post" action="${ctx}/admin/notices/delete" data-confirm="이 공지를 삭제할까요?">
            <input type="hidden" name="id" value="${notice.id}">
            <button class="btn btn-sm btn-danger" type="submit"><i data-lucide="trash-2"></i>삭제</button>
        </form>
    </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
