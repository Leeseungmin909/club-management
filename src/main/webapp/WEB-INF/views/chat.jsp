<%-- 동아리 단체 채팅 (SFR-29): 글자 · 이미지 · 동영상. 새 메시지는 SSE로 받는다 (기능 구현 시 연결) --%>
<c:set var="nav" value="chat" />
<c:set var="title" value="${fn:escapeXml(club.name)} 단체채팅" />
<c:set var="panelClass" value="chat" />
<c:set var="headExtra"><span class="head-count">${memberCount}명</span></c:set>
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="chat-log" id="chatLog">
    <c:set var="lastDay" value="" />
    <c:forEach items="${messages}" var="m">
        <c:set var="day"><t:date value="${m.created_at}" pattern="yyyy년 M월 d일 EEEE" /></c:set>
        <c:if test="${day != lastDay}"><p class="day-sep">${day}</p><c:set var="lastDay" value="${day}" /></c:if>
        <c:set var="mine" value="${m.sender_no == me.student_no}" />
        <div class="msg ${mine ? 'mine' : ''}">
            <c:if test="${not mine}"><t:avatar name="${m.name}" src="${m.profile_image}" /></c:if>
            <div>
                <c:if test="${not mine}"><p class="who">${fn:escapeXml(m.name)}</p></c:if>
                <div class="msg-row">
                    <c:choose>
                        <c:when test="${m.message_type == 'IMAGE'}">
                            <a class="bubble media" href="${fn:escapeXml(m.file_path)}" target="_blank" rel="noopener"><img src="${fn:escapeXml(m.file_path)}" alt="${fn:escapeXml(m.file_name)}" loading="lazy"></a>
                        </c:when>
                        <c:when test="${m.message_type == 'VIDEO'}">
                            <div class="bubble media"><video src="${fn:escapeXml(m.file_path)}" controls preload="metadata"></video></div>
                        </c:when>
                        <c:otherwise><div class="bubble">${fn:escapeXml(m.content)}</div></c:otherwise>
                    </c:choose>
                    <time><t:date value="${m.created_at}" pattern="a h:mm" /></time>
                </div>
            </div>
        </div>
    </c:forEach>
    <c:if test="${empty messages}"><p class="empty">첫 메시지를 보내 보세요.</p></c:if>
</div>

<form class="chat-input" id="chatForm" method="post" action="${ctx}/chat" enctype="multipart/form-data">
    <label class="round" title="이미지 · 동영상 보내기" aria-label="이미지 · 동영상 보내기">
        <i data-lucide="plus"></i>
        <input type="file" name="file" accept=".jpg,.jpeg,.png,.gif,.mp4,.webm" hidden onchange="this.form.submit()">
    </label>
    <input type="text" name="content" maxlength="1000" placeholder="메시지를 입력하세요" autocomplete="off" aria-label="메시지">
    <button class="round send" type="submit" aria-label="보내기"><i data-lucide="send"></i></button>
</form>

<script>
const log = document.getElementById('chatLog');
log.scrollTop = log.scrollHeight;
</script>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
