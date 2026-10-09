<%-- 회원 관리 (SFR-08 추방, SFR-10 관리자 지정·해제) --%>
<c:set var="pageCss" value="admin" />
<c:set var="nav" value="manage" />
<c:set var="title" value="회원 관리" />
<c:set var="back" value="${ctx}/admin/dashboard" />
<c:set var="admin" value="${true}" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="note">
    <i data-lucide="shield-check" style="color:var(--primary)"></i>
    <span class="meta-2">관리자는 공지·일정·회원 정보를 관리할 수 있어요. 마지막 관리자는 해제할 수 없어요.</span>
</div>

<c:forEach items="${members}" var="m">
    <c:set var="isMe" value="${m.student_no == me.studentNo}" />
    <div class="mrow">
        <div class="mrow-top">
            <t:avatar name="${m.name}" src="${m.profile_image}" size="md" ring="${m.role == 'ADMIN'}" />
            <div class="grow">
                <span class="row title-sm">${fn:escapeXml(m.name)}<c:if test="${isMe}"><span class="badge">나</span></c:if></span>
                <span class="meta">${fn:escapeXml(m.student_no)} · ${fn:escapeXml(m.phone)}</span>
            </div>
            <span class="badge ${m.role == 'ADMIN' ? 'purple' : ''}">${m.role == 'ADMIN' ? '관리자' : '회원'}</span>
        </div>
        <div class="mrow-sub">
            <div class="grow"><b>역할 관리</b><span class="meta">${m.role == 'ADMIN' ? '공지와 일정을 관리할 수 있어요' : '일반회원 권한으로 이용 중'}</span></div>
            <form method="post" action="${ctx}/admin/members/role">
                <input type="hidden" name="studentNo" value="${fn:escapeXml(m.student_no)}">
                <input type="hidden" name="role" value="${m.role == 'ADMIN' ? 'MEMBER' : 'ADMIN'}">
                <button class="btn btn-sm" type="submit"><i data-lucide="shield-check"></i>${m.role == 'ADMIN' ? '관리자 해제' : '관리자로 지정'}</button>
            </form>
            <c:if test="${m.role != 'ADMIN' and not isMe}">
                <form method="post" action="${ctx}/admin/members/kick" data-confirm="${fn:escapeXml(m.name)}님을 추방할까요?">
                    <input type="hidden" name="studentNo" value="${fn:escapeXml(m.student_no)}">
                    <button class="btn btn-sm btn-danger" type="submit"><i data-lucide="user-x"></i>추방</button>
                </form>
            </c:if>
        </div>
    </div>
</c:forEach>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
