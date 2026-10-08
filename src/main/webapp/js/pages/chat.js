// 단체 채팅 (SFR-29). 새 메시지 SSE 수신은 기능 구현 때 여기에 추가한다
document.addEventListener('DOMContentLoaded', () => {
  const log = document.getElementById('chatLog');
  log.scrollTop = log.scrollHeight;

  // + 버튼으로 파일을 고르면 바로 전송
  const file = document.querySelector('#chatForm input[type=file]');
  file.addEventListener('change', () => { if (file.files.length) file.form.submit(); });
});
