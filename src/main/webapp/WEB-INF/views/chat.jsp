<%-- 동아리 단체 채팅 (SFR-29). 메시지는 chat.js 가 그린다: 처음엔 아래 JSON, 이후엔 SSE(/chat/stream) --%>
<c:set var="pageCss" value="chat" />
<c:set var="pageJs" value="chat" />
<c:set var="nav" value="chat" />
<c:set var="title" value="${fn:escapeXml(club.name)} 단체채팅" />
<c:set var="panelClass" value="chat" />
<c:set var="headExtra"><span class="head-count">${memberCount}명</span></c:set>
<%@ include file="/WEB-INF/views/layout/top.jspf" %>

<div class="chat-log" id="chatLog" data-ctx="${ctx}" data-me="${fn:escapeXml(me.studentNo)}" data-admin="${me.admin}">
    <p class="empty" id="chatEmpty">첫 메시지를 보내 보세요.</p>
</div>
<%-- 지난 메시지 전체 (Gson 이 < > 를 < 로 바꿔 두어 script 안에 넣어도 안전) --%>
<script type="application/json" id="chatData">${messagesJson}</script>

<div class="upload-progress hidden" id="uploadProgress" role="status">
    <span id="uploadLabel">파일 올리는 중…</span><b id="uploadPercent">0%</b>
    <div class="bar"><i id="uploadBar"></i></div>
</div>
<form class="chat-input" id="chatForm" autocomplete="off">
    <label class="round" title="이미지 · 동영상 보내기" aria-label="이미지 · 동영상 보내기">
        <i data-lucide="plus"></i>
        <input type="file" id="chatFile" accept=".jpg,.jpeg,.png,.gif,.mp4,.webm" hidden>
    </label>
    <input type="text" id="chatText" maxlength="1000" placeholder="메시지를 입력하세요" aria-label="메시지">
    <button class="round send" type="submit" aria-label="보내기"><i data-lucide="send"></i></button>
</form>

<%-- 파일을 고르면 바로 보내지 않고 미리보기로 한 번 확인한다 --%>
<dialog id="fileDlg">
    <div class="dlg-head">파일 보내기<button class="icon-btn" type="button" data-close aria-label="닫기"><i data-lucide="x"></i></button></div>
    <div class="dlg-body">
        <div class="file-preview" id="filePreview"></div>
        <p class="meta" id="fileInfo"></p>
    </div>
    <div class="dlg-foot">
        <button class="btn" type="button" data-close>취소</button>
        <button class="btn btn-primary" type="button" id="fileSend"><i data-lucide="send"></i>보내기</button>
    </div>
</dialog>

<%@ include file="/WEB-INF/views/layout/bottom.jspf" %>
