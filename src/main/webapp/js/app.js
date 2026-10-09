// 모아 공통 스크립트: 아이콘, 모바일 메뉴, 모달, 확인 창, 이미지 미리보기, 알림 드롭다운 닫기
function toast(msg) {
  const t = Object.assign(document.createElement('div'), { className: 'toast', textContent: msg });
  document.body.append(t);
  setTimeout(() => t.remove(), 2600);
}

// 뒤로 가기로 브라우저가 저장해 둔 화면을 꺼내면 새로 받아 온다 (알림 읽음 표시 등 최신 상태)
addEventListener('pageshow', e => { if (e.persisted) location.reload(); });

document.addEventListener('DOMContentLoaded', () => {
  if (window.lucide) lucide.createIcons();

  document.querySelectorAll('[data-toggle-nav]').forEach(el =>
    el.addEventListener('click', () => document.body.classList.toggle('nav-open')));

  document.querySelectorAll('[data-open]').forEach(el =>
    el.addEventListener('click', () => document.getElementById(el.dataset.open).showModal()));
  document.querySelectorAll('[data-close]').forEach(el =>
    el.addEventListener('click', () => el.closest('dialog').close()));

  document.querySelectorAll('form[data-confirm]').forEach(f =>
    f.addEventListener('submit', e => { if (!confirm(f.dataset.confirm)) e.preventDefault(); }));

  // <input type="file" data-preview="박스id">: 고른 이미지를 박스에 미리 보여 주고, 같은 폼의 저장 버튼을 켠다
  document.querySelectorAll('input[type=file][data-preview]').forEach(input =>
    input.addEventListener('change', () => {
      const file = input.files[0], box = document.getElementById(input.dataset.preview);
      if (file) box.innerHTML = '<img alt="" src="' + URL.createObjectURL(file) + '">';
      const save = input.form && input.form.querySelector('[data-needs-file]');
      if (save) save.disabled = !file;
    }));

  document.addEventListener('click', e =>
    document.querySelectorAll('details.bell[open]').forEach(d => { if (!d.contains(e.target)) d.removeAttribute('open'); }));
});
