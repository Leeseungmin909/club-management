<%-- 회원 목록 (SFR-07): 관리자 먼저, 전화번호는 관리자에게만 --%>
<c:set var="nav" value="members" />
<c:set var="title" value="회원 목록" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="list-head"><span>활동 중인 회원 <b>${fn:length(members)}</b>명</span></div>
<div class="list">
    <c:forEach items="${members}" var="m">
        <div>
            <t:avatar name="${m.name}" src="${m.profile_image}" size="md" />
            <span class="grow">
                <span class="row title-sm">${fn:escapeXml(m.name)}<c:if test="${m.student_no == me.studentNo}"><span class="badge">나</span></c:if></span>
                <span class="meta">${fn:escapeXml(m.student_no)} · ${fn:escapeXml(m.department)}</span>
                <c:if test="${me.role == 'ADMIN'}"><span class="meta">${fn:escapeXml(m.phone)}</span></c:if>
            </span>
            <span class="badge ${m.role == 'ADMIN' ? 'purple' : ''}">${m.role == 'ADMIN' ? '관리자' : '회원'}</span>
        </div>
    </c:forEach>
</div>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
