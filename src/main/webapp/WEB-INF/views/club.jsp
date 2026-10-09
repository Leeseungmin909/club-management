<%-- 동아리 소개 (모든 회원). 수정은 관리자 메뉴의 동아리 설정(/admin/club) --%>
<c:set var="pageCss" value="club" />
<c:set var="title" value="동아리 소개" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="club-cover">
    <c:if test="${not empty club.bannerPath}"><img src="${fn:escapeXml(club.bannerPath)}" alt=""></c:if>
    <div class="club-head">
        <t:avatar name="${club.name}" src="${club.imagePath}" size="xl" />
        <div>
            <h2>${fn:escapeXml(club.name)}</h2>
            <p class="row"><i data-lucide="school"></i>${fn:escapeXml(club.school)}</p>
        </div>
    </div>
</div>

<div class="sec-head"><div><p class="eyebrow">ABOUT</p><h3>동아리 소개</h3></div></div>
<div class="content">${empty club.intro ? '아직 소개글이 없어요.' : fn:escapeXml(club.intro)}</div>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
