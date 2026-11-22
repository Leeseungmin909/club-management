package kr.ac.dongeui.club.domain.dashboard.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.ac.dongeui.club.domain.dashboard.repository.DashboardRepository;

/** 관리자 대시보드 (SFR-26): 올해 1~12월 가입·탈퇴, 총 인원 누계, 평균 출석률 */
public class DashboardService {

    private final DashboardRepository repository = new DashboardRepository();

    /** {year, labels, joined, left, total (그래프, 아직 안 온 달은 null), members, joinedThisMonth, leftThisMonth, avgRate (null 이면 출석 데이터 없음)} */
    public Map<String, Object> load() throws SQLException {
        YearMonth now = YearMonth.now(), first = YearMonth.of(now.getYear(), 1);
        LocalDateTime from = first.atDay(1).atStartOfDay();
        Map<String, Integer> joinedBy = repository.countByMonth("approved_at", from);
        Map<String, Integer> leftBy = repository.countByMonth("left_at", from);

        List<String> labels = new ArrayList<>();
        Integer[] joined = new Integer[12], left = new Integer[12], total = new Integer[12];  // 다음 달부터는 null (그래프에 안 그림)
        int sum = repository.countMembersAt(from);
        for (int i = 0; i < 12; i++) {
            YearMonth ym = first.plusMonths(i);  // toString() = "2026-09", 쿼리의 DATE_FORMAT 과 같은 모양
            labels.add(ym.getMonthValue() + "월");
            if (ym.isAfter(now)) continue;
            joined[i] = joinedBy.getOrDefault(ym.toString(), 0);
            left[i] = leftBy.getOrDefault(ym.toString(), 0);
            total[i] = sum += joined[i] - left[i];
        }
        int cur = now.getMonthValue() - 1;

        Map<String, Object> d = new HashMap<>();  // avgRate 가 null 일 수 있어 Map.of 대신
        d.put("year", now.getYear());
        d.put("labels", labels);
        d.put("joined", joined);
        d.put("left", left);
        d.put("total", total);
        d.put("members", total[cur]);
        d.put("joinedThisMonth", joined[cur]);
        d.put("leftThisMonth", left[cur]);
        d.put("avgRate", repository.avgAttendanceRate());
        return d;
    }
}
