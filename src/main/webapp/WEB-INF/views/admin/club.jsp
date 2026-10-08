<%-- 동아리 정보 수정 (SFR-05) --%>
<c:set var="nav" value="club" />
<c:set var="title" value="동아리 설정" />
<c:set var="back" value="${ctx}/admin/dashboard" />
<c:set var="admin" value="${true}" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<form method="post" action="${ctx}/admin/club" enctype="multipart/form-data">
    <c:if test="${not empty error}"><p class="alert">${fn:escapeXml(error)}</p></c:if>
    <div class="field">
        <span>대표 이미지</span>
        <div class="drop square" id="clubPreview">
            <c:choose>
                <c:when test="${not empty club.image_path}"><img src="${fn:escapeXml(club.image_path)}" alt=""></c:when>
                <c:otherwise><span><i data-lucide="image"></i><br>대표 이미지를 올려 주세요</span></c:otherwise>
            </c:choose>
        </div>
        <label class="file-btn"><i data-lucide="upload"></i>이미지 선택
            <input type="file" name="image" accept=".jpg,.jpeg,.png" data-preview="clubPreview">
        </label>
        <p class="hint">JPG, PNG 파일을 올릴 수 있어요.</p>
    </div>
    <label class="field"><span>동아리 이름</span><input class="input" name="name" value="${fn:escapeXml(club.name)}" maxlength="50" required></label>
    <label class="field"><span>학교</span><input class="input" name="school" value="${fn:escapeXml(club.school)}" maxlength="50"></label>
    <label class="field"><span>소개</span><textarea class="input" name="intro" rows="5" maxlength="500">${fn:escapeXml(club.intro)}</textarea></label>
    <p class="hint" style="margin:-8px 0 24px">저장하면 사이드바의 동아리 카드에 바로 반영돼요.</p>
    <div class="btns">
        <a class="btn" href="${ctx}/admin/dashboard">취소</a>
        <button class="btn btn-primary" type="submit">저장</button>
    </div>
</form>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
