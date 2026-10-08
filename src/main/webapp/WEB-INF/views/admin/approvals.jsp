<%-- 가입 승인 · 거절 (SFR-06) --%>
<c:set var="pageCss" value="admin" />
<c:set var="nav" value="approvals" />
<c:set var="title" value="가입 승인" />
<c:set var="back" value="${ctx}/admin/dashboard" />
<c:set var="admin" value="${true}" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="note">
    <span class="icon-tile sm"><i data-lucide="shield-check"></i></span>
    <div><b>승인 대기 ${fn:length(pending)}명</b><span class="meta">가입 정보를 확인하고 승인해 주세요.</span></div>
</div>

<c:forEach items="${pending}" var="p">
    <div class="apply">
        <div class="apply-top">
            <t:avatar name="${p.name}" size="lg" />
            <div class="grow">
                <b>${fn:escapeXml(p.name)}</b>
                <span class="meta-2">${fn:escapeXml(p.student_no)} · ${fn:escapeXml(p.department)}</span>
                <span class="meta">신청일 <t:date value="${p.requested_at}" pattern="yyyy. MM. dd" /></span>
            </div>
        </div>
        <div class="btns">
            <form method="post" action="${ctx}/admin/approvals/reject" data-confirm="${fn:escapeXml(p.name)}님의 가입 신청을 거절할까요?">
                <input type="hidden" name="studentNo" value="${fn:escapeXml(p.student_no)}">
                <button class="btn" type="submit"><i data-lucide="x"></i>거절</button>
            </form>
            <form method="post" action="${ctx}/admin/approvals/approve">
                <input type="hidden" name="studentNo" value="${fn:escapeXml(p.student_no)}">
                <button class="btn btn-primary" type="submit"><i data-lucide="check"></i>승인</button>
            </form>
        </div>
    </div>
</c:forEach>
<c:if test="${empty pending}"><p class="empty">승인을 기다리는 신청이 없어요.</p></c:if>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
