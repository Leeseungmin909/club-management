// 단체 채팅 (SFR-29): 메시지 그리기 · SSE 실시간 수신 · 보내기(글자 fetch, 파일 XHR + 진행률) · 수정 · 삭제
document.addEventListener('DOMContentLoaded', () => {
  const log = document.getElementById('chatLog');
  const { ctx, me } = log.dataset;
  const isAdmin = log.dataset.admin === 'true';
  let lastId = 0, lastDay = '';

  // ── 그리기 ─────────────────────────────────────────
  const el = (tag, cls, text) => {
    const e = document.createElement(tag);
    if (cls) e.className = cls;
    if (text != null) e.textContent = text;  // 항상 글자로 넣어 XSS 방지 (NFR-08)
    return e;
  };

  // avatar.tag 와 같은 규칙: 한글 3~4자 이름은 성을 뺀 2글자, 그 외 앞 3글자
  const avatar = m => {
    if (m.senderProfileImage) {
      const img = el('img', 'avatar');
      img.src = m.senderProfileImage;
      img.alt = '';
      return img;
    }
    const n = m.senderName || '';
    return el('span', 'avatar', /^[가-힣]{3,4}$/.test(n) ? n.slice(1, 3) : n.slice(0, 3));
  };

  const bubble = m => {
    if (m.type === 'IMAGE') {
      const a = el('a', 'bubble media');
      a.href = m.filePath; a.target = '_blank'; a.rel = 'noopener';
      const img = el('img'); img.src = m.filePath; img.alt = m.fileName || ''; img.loading = 'lazy';
      img.addEventListener('load', keepBottom);
      a.append(img);
      return a;
    }
    if (m.type === 'VIDEO') {
      const box = el('div', 'bubble media');
      const v = el('video'); v.src = m.filePath; v.controls = true; v.preload = 'metadata';
      box.append(v);
      return box;
    }
    return el('div', 'bubble', m.content);
  };

  const render = m => {
    if (log.querySelector(`[data-id="${m.id}"]`)) return;  // 재연결 등으로 같은 메시지가 다시 와도 한 번만
    document.getElementById('chatEmpty')?.remove();
    if (m.day !== lastDay) { log.append(el('p', 'day-sep', m.day)); lastDay = m.day; }

    const mine = m.senderNo === me;
    const row = el('div', 'msg' + (mine ? ' mine' : ''));
    row.dataset.id = m.id;
    if (!mine) row.append(avatar(m));

    const body = el('div');
    if (!mine) body.append(el('p', 'who', m.senderName));
    const line = el('div', 'msg-row');
    const meta = el('div', 'msg-meta');
    const time = el('time', null, m.time + (m.edited ? ' (수정됨)' : ''));
    meta.append(time);

    const actions = el('span', 'msg-actions');
    if (mine && m.type === 'TEXT') actions.append(action('pencil', '수정', () => edit(m.id, row)));
    if (mine || isAdmin) actions.append(action('trash-2', '삭제', () => remove(m.id)));
    if (actions.childElementCount) meta.prepend(actions);

    line.append(bubble(m), meta);
    body.append(line);
    row.append(body);
    log.append(row);
    lastId = Math.max(lastId, m.id);
  };
  const icons = () => { if (window.lucide) lucide.createIcons(); };

  const action = (icon, label, onClick) => {
    const b = el('button', 'msg-action');
    b.type = 'button';
    b.title = label;
    b.setAttribute('aria-label', label);
    b.innerHTML = `<i data-lucide="${icon}"></i>`;
    b.addEventListener('click', onClick);
    return b;
  };

  const applyEdit = m => {
    const row = log.querySelector(`[data-id="${m.id}"]`);
    if (!row) return;
    row.querySelector('.bubble').textContent = m.content;
    row.querySelector('time').textContent = m.time + ' (수정됨)';
  };

  const nearBottom = () => log.scrollHeight - log.scrollTop - log.clientHeight < 120;
  function keepBottom() { if (stick) log.scrollTop = log.scrollHeight; }
  let stick = true;
  log.addEventListener('scroll', () => { stick = nearBottom(); });

  JSON.parse(document.getElementById('chatData').textContent).forEach(render);
  icons();
  log.scrollTop = log.scrollHeight;

  // ── 실시간 수신 (SSE). 끊기면 브라우저가 3초 뒤 자동으로 다시 연결하고, 놓친 메시지는 서버가 채운다 ──
  const es = new EventSource(`${ctx}/chat/stream?after=${lastId}`);
  es.addEventListener('message', e => {
    const m = JSON.parse(e.data);
    const follow = stick || m.senderNo === me;
    render(m);
    icons();
    if (follow) log.scrollTop = log.scrollHeight;
  });
  es.addEventListener('edit', e => applyEdit(JSON.parse(e.data)));
  es.addEventListener('delete', e => log.querySelector(`[data-id="${JSON.parse(e.data).id}"]`)?.remove());

  // ── 보내기 · 수정 · 삭제 (결과는 SSE 로 돌아와 화면에 반영) ──
  const post = async (path, params) => {
    const res = await fetch(ctx + path, { method: 'POST', body: new URLSearchParams(params) });
    if (!res.ok) toast(res.status === 400 || res.status === 403 ? await res.text() : '잠시 후 다시 시도해 주세요.');
    return res.ok;
  };

  const form = document.getElementById('chatForm');
  const text = document.getElementById('chatText');
  form.addEventListener('submit', async e => {
    e.preventDefault();
    if (!text.value.trim()) return;
    if (await post('/chat/messages', { content: text.value })) {
      text.value = '';
      ga('chat_send', { type: 'text' });
    }
    text.focus();
  });

  function edit(id, row) {
    const current = row.querySelector('.bubble').textContent;
    const next = prompt('메시지 수정', current);
    if (next != null && next.trim() && next !== current) post('/chat/messages/edit', { id, content: next });
  }

  function remove(id) {
    if (confirm('이 메시지를 삭제할까요? 모든 회원의 채팅방에서 사라져요.')) post('/chat/messages/delete', { id });
  }

  // 파일: 고르면 미리보기 창으로 확인 → "보내기" 를 눌러야 올린다
  const file = document.getElementById('chatFile');
  const progress = document.getElementById('uploadProgress');
  const dlg = document.getElementById('fileDlg');
  const preview = document.getElementById('filePreview');
  let previewUrl = null;

  file.addEventListener('change', () => {
    const f = file.files[0];
    if (!f) return;
    previewUrl = URL.createObjectURL(f);
    const media = el(f.type.startsWith('video/') ? 'video' : 'img');
    media.src = previewUrl;
    if (media.tagName === 'VIDEO') media.controls = true;
    preview.replaceChildren(media);
    document.getElementById('fileInfo').textContent = `${f.name} · ${(f.size / 1024 / 1024).toFixed(1)}MB`;
    dlg.returnValue = '';
    dlg.showModal();
  });
  document.getElementById('fileSend').addEventListener('click', () => {
    upload(file.files[0]);
    dlg.close('send');
  });
  dlg.addEventListener('close', () => {  // 보내기 · 취소 · 닫기 모두 여기서 정리
    preview.replaceChildren();
    URL.revokeObjectURL(previewUrl);
    if (dlg.returnValue !== 'send') file.value = '';
  });

  // XMLHttpRequest 로 보내야 업로드 진행률을 알 수 있다 (fetch 는 업로드 진행률 미지원)
  function upload(f) {
    const data = new FormData();
    data.append('file', f);
    const xhr = new XMLHttpRequest();
    xhr.open('POST', ctx + '/chat/messages');
    document.getElementById('uploadLabel').textContent = `${f.name} 올리는 중…`;
    progress.classList.remove('hidden');
    xhr.upload.addEventListener('progress', e => {
      if (!e.lengthComputable) return;
      const p = Math.round(e.loaded / e.total * 100);
      document.getElementById('uploadPercent').textContent = p + '%';
      document.getElementById('uploadBar').style.width = p + '%';
    });
    xhr.addEventListener('loadend', () => {
      progress.classList.add('hidden');
      document.getElementById('uploadBar').style.width = '0';
      document.getElementById('uploadPercent').textContent = '0%';
      file.value = '';
      if (xhr.status === 204) ga('chat_send', { type: f.type.startsWith('video/') ? 'video' : 'image' });
      else toast(xhr.status === 400 || xhr.status === 413 ? xhr.responseText || '파일을 올리지 못했어요.' : '파일을 올리지 못했어요.');
    });
    xhr.send(data);
  }
});
