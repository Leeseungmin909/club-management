<%-- 일정 등록 · 수정 (SFR-17, 19) + 장소 지도 검색 (SFR-23) --%>
<c:set var="pageCss" value="schedule" />
<c:set var="pageJs" value="schedule-map" />
<c:set var="nav" value="${empty s ? 'scheduleWrite' : 'schedules'}" />
<c:set var="title" value="${empty s ? '일정 등록' : '일정 수정'}" />
<c:set var="back" value="${empty s ? ctx += '/schedules' : ctx += '/schedules/view?id=' += s.id}" />
<c:set var="admin" value="${true}" />
<c:set var="panelClass" value="narrow" />
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<%-- 입력 오류로 돌아왔을 때(f)는 쓰던 값을 그대로, 아니면 저장된 일정(s)의 값을 보여 준다 --%>
<c:set var="vDate"><c:choose><c:when test="${not empty f}">${fn:escapeXml(f.date)}</c:when><c:otherwise><t:date value="${s.start_at}" pattern="yyyy-MM-dd" /></c:otherwise></c:choose></c:set>
<c:set var="vStart"><c:choose><c:when test="${not empty f}">${fn:escapeXml(f.startTime)}</c:when><c:otherwise><t:date value="${s.start_at}" pattern="HH:mm" /></c:otherwise></c:choose></c:set>
<c:set var="vEnd"><c:choose><c:when test="${not empty f}">${fn:escapeXml(f.endTime)}</c:when><c:otherwise><t:date value="${s.end_at}" pattern="HH:mm" /></c:otherwise></c:choose></c:set>
<form method="post" data-search-url="${ctx}/admin/places/search" action="${ctx}/admin/schedules/${empty s ? 'write' : 'edit'}" ${empty s ? 'data-ga="schedule_create"' : ''}>
    <c:if test="${not empty s}"><input type="hidden" name="id" value="${s.id}"></c:if>
    <c:if test="${not empty error}"><p class="alert">${fn:escapeXml(error)}</p></c:if>
    <label class="field">
        <span>제목</span>
        <input class="input" name="title" value="${fn:escapeXml(not empty f ? f.title : s.title)}" maxlength="100" required placeholder="예) 가을 정기 출사">
    </label>
    <label class="field">
        <span>날짜</span>
        <input class="input" type="date" name="date" value="${vDate}" required>
    </label>
    <div class="grid-2">
        <label class="field"><span>시작 시간</span><input class="input" type="time" name="startTime" value="${vStart}" required></label>
        <label class="field"><span>종료 시간</span><input class="input" type="time" name="endTime" value="${vEnd}"></label>
    </div>

    <div class="field">
        <span>장소</span>
        <%-- 네이버 지도 장소 검색 (SFR-23): 검색 결과 선택 · 지도 클릭으로 좌표를 latitude/longitude 에 넣는다 (schedule-map.js) --%>
        <div class="search" style="margin-bottom:8px"><i data-lucide="search"></i><input type="search" id="placeSearch" placeholder="장소 이름이나 주소로 검색"></div>
        <div class="map" id="map" data-key="${fn:escapeXml(Config.get('naver.map.client.id'))}"><span><i data-lucide="map"></i><br>검색하거나 지도를 눌러 장소를 골라요</span></div>
        <div class="grid-2">
            <input class="input" name="placeName" value="${fn:escapeXml(not empty f ? f.placeName : s.place_name)}" maxlength="100" placeholder="장소 이름">
            <input class="input" name="address" value="${fn:escapeXml(not empty f ? f.address : s.address)}" maxlength="200" placeholder="주소">
        </div>
        <input type="hidden" name="latitude" value="${not empty f ? fn:escapeXml(f.latitude) : s.latitude}">
        <input type="hidden" name="longitude" value="${not empty f ? fn:escapeXml(f.longitude) : s.longitude}">
    </div>

    <label class="field">
        <span>내용</span>
        <textarea class="input" name="content" rows="5" placeholder="준비물, 안내 사항 등">${fn:escapeXml(not empty f ? f.content : s.content)}</textarea>
    </label>
    <div class="btns">
        <a class="btn" href="${back}">취소</a>
        <button class="btn btn-primary" type="submit">${empty s ? '일정 등록' : '수정 완료'}</button>
    </div>
</form>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
