// 공지 작성 · 수정: AI 다듬기 (SFR-30)
// 서버(data-url)가 AI API를 호출하고 다듬은 본문을 돌려준다. 실패하면 초안을 그대로 둔다 (NFR-18)
document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('aiBtn');
  const box = document.getElementById('content');
  const label = document.getElementById('aiLabel');
  const hint = document.getElementById('aiHint');

  btn.addEventListener('click', async () => {
    if (!box.value.trim()) { toast('내용에 초안을 먼저 입력해 주세요.'); return; }
    btn.disabled = true;
    label.textContent = 'AI가 다듬는 중…';
    try {
      const res = await fetch(btn.dataset.url, { method: 'POST', body: new URLSearchParams({ draft: box.value }) });
      if (res.status === 400) { toast(await res.text()); return; }  // 초안 문제 (비었거나 너무 김)
      if (!res.ok) throw new Error(res.status);
      box.value = await res.text();
      hint.textContent = '문장을 자연스럽게 정리했어요';
      ga('ai_polish');
    } catch {
      toast('AI 다듬기에 실패했어요. 초안은 그대로 유지돼요.');
    } finally {
      btn.disabled = false;
      label.textContent = 'AI로 다듬기';
    }
  });
});
