<%-- 내 정보 (SFR-11) + 프로필 이미지 변경 (SFR-12) + 동아리 탈퇴 (SFR-09) --%>
<c:set var="pageCss" value="member" />
<c:set var="nav" value="me" />
<c:set var="title" value="내 정보" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="profile-card">
    <span class="avatar-wrap">
        <t:avatar name="${me.name}" src="${me.profileImage}" size="xl" ring="true" />
        <button class="cam" type="button" data-open="photoDlg" aria-label="사진 변경"><i data-lucide="camera"></i></button>
    </span>
    <button class="link-btn" type="button" data-open="photoDlg">사진 변경</button>
    <h2>${fn:escapeXml(me.name)} <span class="badge ${me.role == 'ADMIN' ? 'purple' : ''}">${me.role == 'ADMIN' ? '관리자' : '회원'}</span></h2>
    <p class="meta">${fn:escapeXml(me.departmentName)} · ${fn:escapeXml(me.studentNo)}</p>
    <div class="info3">
        <div><small>전화번호</small><b>${fn:escapeXml(me.phone)}</b></div>
        <div><small>역할</small><b>${me.role == 'ADMIN' ? '관리자' : '회원'}</b></div>
        <div><small>가입일</small><b><t:date value="${me.approvedAt}" pattern="yyyy. MM. dd" /></b></div>
    </div>
</div>

<div class="rate">
    <div style="display:flex;justify-content:space-between;align-items:flex-end">
        <b>나의 출석률</b><span class="big">${myRate}<small>%</small></span>
    </div>
    <div class="bar"><i style="width:${myRate}%"></i></div>
    <p class="meta">총 ${myTotal}번의 일정 중 ${myAttend}번 참석했어요.</p>
</div>

<div class="sec-head">
    <div><p class="eyebrow">HISTORY</p><h3>최근 출석 기록</h3></div>
    <a href="${ctx}/schedules">일정 보기<i data-lucide="chevron-right"></i></a>
</div>
<div class="list">
    <c:forEach items="${history}" var="h">
        <a href="${ctx}/schedules/view?id=${h.schedule_id}">
            <span class="icon-tile sm ${h.status == 'ATTEND' ? 'green' : 'red'}"><i data-lucide="${h.status == 'ATTEND' ? 'check' : 'x'}"></i></span>
            <span class="grow"><span class="title-sm ellipsis">${fn:escapeXml(h.title)}</span><span class="meta"><t:date value="${h.start_at}" pattern="yyyy. MM. dd" /></span></span>
            <span class="badge ${h.status == 'ATTEND' ? 'green' : 'red'}">${h.status == 'ATTEND' ? '참석' : '불참'}</span>
        </a>
    </c:forEach>
    <c:if test="${empty history}"><p class="empty">아직 응답한 일정이 없어요.</p></c:if>
</div>

<form method="post" action="${ctx}/me/withdraw" style="margin-top:28px;text-align:center"
      data-confirm="정말 동아리에서 탈퇴할까요? 탈퇴하면 다시 로그인할 수 없어요.">
    <button class="btn btn-danger-soft" type="submit"><i data-lucide="log-out"></i>동아리 탈퇴</button>
</form>

<dialog id="photoDlg">
    <form method="post" action="${ctx}/me/photo" enctype="multipart/form-data">
        <div class="dlg-head">프로필 사진 변경<button class="icon-btn" type="button" data-close aria-label="닫기"><i data-lucide="x"></i></button></div>
        <div class="dlg-body">
            <div class="drop" id="photoPreview"><span><i data-lucide="camera"></i><br>선택한 사진이 여기에<br>표시돼요</span></div>
            <label class="file-btn"><i data-lucide="image"></i>파일 선택
                <input type="file" name="photo" accept=".jpg,.jpeg,.png" data-preview="photoPreview" required>
            </label>
            <p class="hint" style="text-align:center">JPG, PNG 파일을 선택할 수 있어요.</p>
        </div>
        <div class="dlg-foot">
            <button class="btn" type="button" data-close>취소</button>
            <button class="btn btn-primary" type="submit" data-needs-file disabled>저장</button>
        </div>
    </form>
</dialog>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
