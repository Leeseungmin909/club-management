<%-- 일정 등록 · 수정 (SFR-17, 19) + 장소 지도 검색 (SFR-23) --%>
<c:set var="nav" value="${empty s ? 'scheduleWrite' : 'schedules'}" />
<c:set var="title" value="${empty s ? '일정 등록' : '일정 수정'}" />
<c:set var="back" value="${empty s ? ctx += '/schedules' : ctx += '/schedules/view?id=' += s.id}" />
<c:set var="admin" value="${true}" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<form method="post" action="${ctx}/admin/schedules/${empty s ? 'write' : 'edit'}">
    <c:if test="${not empty s}"><input type="hidden" name="id" value="${s.id}"></c:if>
    <c:if test="${not empty error}"><p class="alert">${fn:escapeXml(error)}</p></c:if>
    <label class="field">
        <span>제목</span>
        <input class="input" name="title" value="${fn:escapeXml(s.title)}" maxlength="100" required placeholder="예) 가을 정기 출사">
    </label>
    <label class="field">
        <span>날짜</span>
        <input class="input" type="date" name="date" value="<t:date value='${s.start_at}' pattern='yyyy-MM-dd' />" required>
    </label>
    <div class="grid-2">
        <label class="field"><span>시작 시간</span><input class="input" type="time" name="startTime" value="<t:date value='${s.start_at}' pattern='HH:mm' />" required></label>
        <label class="field"><span>종료 시간</span><input class="input" type="time" name="endTime" value="<t:date value='${s.end_at}' pattern='HH:mm' />"></label>
    </div>

    <div class="field">
        <span>장소</span>
        <%-- 네이버 지도 장소 검색 (SFR-23, 박계령 담당): 검색 · 지도 클릭으로 좌표를 latitude/longitude 에 넣는다 --%>
        <div class="search" style="margin-bottom:8px"><i data-lucide="search"></i><input type="search" id="placeSearch" placeholder="장소 이름이나 주소로 검색"></div>
        <div class="map" id="map"><span><i data-lucide="map"></i><br>네이버 지도 연동 예정</span></div>
        <div class="grid-2">
            <input class="input" name="placeName" value="${fn:escapeXml(s.place_name)}" maxlength="100" placeholder="장소 이름">
            <input class="input" name="address" value="${fn:escapeXml(s.address)}" maxlength="200" placeholder="주소">
        </div>
        <input type="hidden" name="latitude" value="${s.latitude}">
        <input type="hidden" name="longitude" value="${s.longitude}">
    </div>

    <label class="field">
        <span>내용</span>
        <textarea class="input" name="content" rows="5" placeholder="준비물, 안내 사항 등">${fn:escapeXml(s.content)}</textarea>
    </label>
    <div class="btns">
        <a class="btn" href="${back}">취소</a>
        <button class="btn btn-primary" type="submit">${empty s ? '일정 등록' : '수정 완료'}</button>
    </div>
</form>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
