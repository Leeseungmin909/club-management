// 관리자 대시보드 그래프 (SFR-26): 월별 신규 가입 · 탈퇴(막대) + 총 인원(꺾은선)
// 데이터는 <canvas data-chart='{"labels":[],"joined":[],"left":[],"total":[]}'> 로 받는다
document.addEventListener('DOMContentLoaded', () => {
  const canvas = document.getElementById('growth');
  const chart = JSON.parse(canvas.dataset.chart);
  new Chart(canvas, {
    data: {
      labels: chart.labels,
      datasets: [
        { type: 'line', label: '총 인원', data: chart.total, borderColor: '#6c4ee3', backgroundColor: '#fff',
          pointBorderWidth: 2, pointRadius: 4, tension: .3 },
        { type: 'bar', label: '신규 가입자', data: chart.joined, backgroundColor: '#e4ddfd', borderRadius: 4, barPercentage: .5 },
        { type: 'bar', label: '탈퇴자', data: chart.left, backgroundColor: '#fbd9da', borderRadius: 4, barPercentage: .5 }
      ]
    },
    options: {
      maintainAspectRatio: false,
      plugins: { legend: { position: 'top', align: 'end', labels: { boxWidth: 10, font: { size: 11 } } } },
      scales: { x: { grid: { display: false } }, y: { beginAtZero: true, ticks: { precision: 0 } } }
    }
  });
});
