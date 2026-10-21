// 일정 지도 (SFR-23, 24): 네이버 지도(JS)로 장소를 보여 주고, 등록·수정 화면에서는 장소 검색 · 지도 클릭으로 좌표를 고른다.
// 지도 키가 없거나 불러오지 못해도 장소 이름·주소 입력과 검색 결과 선택은 그대로 동작한다.
document.addEventListener('DOMContentLoaded', () => {
  const box = document.getElementById('map');
  if (!box) return;
  const form = box.closest('form');
  const latInput = form && form.elements.latitude;
  const lngInput = form && form.elements.longitude;
  let map = null, marker = null;

  const lat0 = parseFloat(box.dataset.lat || (latInput && latInput.value));
  const lng0 = parseFloat(box.dataset.lng || (lngInput && lngInput.value));
  const hasPoint = !isNaN(lat0) && !isNaN(lng0);

  function loadNaver(key) {
    return new Promise((resolve, reject) => {
      if (window.naver && naver.maps) return resolve();
      if (!key) return reject(new Error('no key'));
      window.navermap_authFailure = () => reject(new Error('auth'));
      const s = document.createElement('script');
      s.src = 'https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=' + encodeURIComponent(key);
      s.onload = resolve;
      s.onerror = () => reject(new Error('load'));
      document.head.appendChild(s);
    });
  }

  function show(lat, lng) {
    if (!window.naver || !naver.maps) return;
    const pos = new naver.maps.LatLng(lat, lng);
    if (!map) {
      box.innerHTML = '';
      box.style.display = 'block';
      map = new naver.maps.Map(box, { center: pos, zoom: 16 });
      marker = new naver.maps.Marker({ position: pos, map });
      if (form) naver.maps.Event.addListener(map, 'click', e => setPoint(e.coord.lat(), e.coord.lng()));
    } else {
      map.setCenter(pos);
      marker.setPosition(pos);
    }
  }

  function setPoint(lat, lng) {
    latInput.value = lat.toFixed(7);
    lngInput.value = lng.toFixed(7);
    show(lat, lng);
  }

  loadNaver(box.dataset.key).then(() => {
    if (hasPoint) show(lat0, lng0);
    else if (form) { box.innerHTML = ''; box.style.display = 'block'; map = new naver.maps.Map(box, { center: new naver.maps.LatLng(35.1435, 129.0342), zoom: 14 }); marker = new naver.maps.Marker({ position: map.getCenter(), map }); marker.setVisible(false);
      naver.maps.Event.addListener(map, 'click', e => { marker.setVisible(true); setPoint(e.coord.lat(), e.coord.lng()); }); }
  }).catch(() => { /* 지도를 못 불러오면 안내 문구가 그대로 남는다 */ });

  if (!form) return;

  // ----- 장소 검색 (등록 · 수정 화면) -----
  const search = document.getElementById('placeSearch');
  const list = document.createElement('ul');
  list.className = 'place-results';
  search.closest('.search').after(list);

  async function run() {
    const q = search.value.trim();
    list.innerHTML = '';
    if (!q) return;
    try {
      const res = await fetch(form.dataset.searchUrl + '?q=' + encodeURIComponent(q));
      if (!res.ok) { toast(res.status === 503 ? '장소 검색이 아직 설정되지 않았어요. 이름과 주소를 직접 입력해 주세요.' : '장소 검색에 실패했어요.'); return; }
      const places = await res.json();
      if (!places.length) { const li = document.createElement('li'); li.className = 'none'; li.textContent = '검색 결과가 없어요.'; list.append(li); return; }
      places.forEach(p => {
        const li = document.createElement('li');
        const b = document.createElement('b'); b.textContent = p.name;
        const s = document.createElement('small'); s.textContent = p.address;
        li.append(b, s);
        li.addEventListener('click', () => {
          form.elements.placeName.value = p.name;
          form.elements.address.value = p.address;
          if (marker) marker.setVisible(true);
          latInput.value = p.lat; lngInput.value = p.lng;
          show(p.lat, p.lng);
          if (!map) box.innerHTML = '<span><i data-lucide="map-pin"></i><br>' + '위치를 선택했어요</span>';
          list.innerHTML = '';
        });
        list.append(li);
      });
    } catch { toast('장소 검색에 실패했어요.'); }
  }

  search.addEventListener('keydown', e => { if (e.key === 'Enter') { e.preventDefault(); run(); } });
  search.addEventListener('search', run);
});
