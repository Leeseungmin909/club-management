package kr.ac.dongeui.club.domain.schedule.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.repository.MemberRepository;
import kr.ac.dongeui.club.domain.notification.service.NotificationService;
import kr.ac.dongeui.club.domain.schedule.repository.AttendanceRepository;
import kr.ac.dongeui.club.domain.schedule.repository.ScheduleRepository;

/** 일정 등록 · 조회 · 수정 · 삭제 (SFR-17~19)와 출석 체크 · 현황 (SFR-20~22) */
public class ScheduleService {

    public static final int PAGE_SIZE = 15;
    static final int MAX_CONTENT = 2000;
    private static final Logger log = Logger.getLogger(ScheduleService.class.getName());

    private final ScheduleRepository scheduleRepository = new ScheduleRepository();
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    private final MemberRepository memberRepository = new MemberRepository();
    private final NotificationService notificationService = new NotificationService();

    /** 등록 · 수정 폼에서 받은 값 (모두 문자열 그대로) */
    public record Form(String title, String date, String startTime, String endTime, String placeName,
                       String address, String latitude, String longitude, String content) {}

    public record ListView(List<Map<String, Object>> week, List<Map<String, Object>> upcoming,
                           List<Map<String, Object>> past, int page, int totalPages) {}

    public record Detail(Map<String, Object> schedule, String myStatus, List<Map<String, Object>> attendees,
                         List<Map<String, Object>> absentees, int noReply) {}

    public record MyAttendance(int total, int attend, int rate, List<Map<String, Object>> history) {}

    // ---------- 조회 ----------

    public ListView list(int page) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        int totalPast = scheduleRepository.countPast(now);
        int totalPages = Math.max(1, (totalPast + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(1, page), totalPages);
        Set<LocalDate> busy = scheduleRepository.datesWithSchedule(now.toLocalDate(), 7);
        List<Map<String, Object>> week = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = now.toLocalDate().plusDays(i);
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", d);
            day.put("today", i == 0);
            day.put("has", busy.contains(d));
            week.add(day);
        }
        return new ListView(week, scheduleRepository.findUpcoming(now, 100),
                scheduleRepository.findPast(now, PAGE_SIZE, (current - 1) * PAGE_SIZE), current, totalPages);
    }

    /** 홈 화면의 "다가오는 일정" (없으면 null) */
    public Map<String, Object> next() throws SQLException {
        List<Map<String, Object>> list = scheduleRepository.findUpcoming(LocalDateTime.now(), 1);
        return list.isEmpty() ? null : list.get(0);
    }

    public Map<String, Object> find(int id) throws SQLException {
        return scheduleRepository.find(id, LocalDateTime.now());
    }

    /** 상세 + 출석 현황. 삭제된 일정이면 null */
    public Detail detail(int id, String studentNo) throws SQLException {
        Map<String, Object> s = find(id);
        if (s == null) return null;
        List<Map<String, Object>> yes = attendanceRepository.findMembers(id, "ATTEND");
        List<Map<String, Object>> no = attendanceRepository.findMembers(id, "ABSENT");
        int noReply = Math.max(0, memberRepository.countActive() - yes.size() - no.size());
        return new Detail(s, attendanceRepository.findStatus(id, studentNo), yes, no, noReply);
    }

    public int memberCount() throws SQLException {
        return memberRepository.countActive();
    }

    /** 내 출석률 + 최근 응답 기록 (내 정보 · 홈 화면) */
    public MyAttendance myAttendance(String studentNo) throws SQLException {
        int[] stats = attendanceRepository.myStats(studentNo, LocalDateTime.now());
        int rate = stats[0] == 0 ? 0 : Math.round(stats[1] * 100f / stats[0]);
        return new MyAttendance(stats[0], stats[1], rate, attendanceRepository.myHistory(studentNo, 10));
    }

    // ---------- 출석 체크 (SFR-20, 21) ----------

    /** 참석 여부 저장 · 변경. 삭제된 일정이면 false, 이미 시작했거나 값이 이상하면 IllegalArgumentException */
    public boolean attend(int scheduleId, String studentNo, String status) throws SQLException {
        if (!"ATTEND".equals(status) && !"ABSENT".equals(status)) throw new IllegalArgumentException("잘못된 요청이에요.");
        Map<String, Object> s = find(scheduleId);
        if (s == null) return false;
        if ((Boolean) s.get("started")) throw new IllegalArgumentException("일정이 시작되어 참석 여부를 바꿀 수 없어요.");
        attendanceRepository.save(scheduleId, studentNo, status);
        return true;
    }

    // ---------- 등록 · 수정 · 삭제 (SFR-17, 19) ----------

    /** 일정 등록 후 활동 중인 회원 모두에게 알림. 입력이 잘못되면 IllegalArgumentException(안내 문구) */
    public int write(Member writer, Form f) throws SQLException {
        Parsed p = parse(f);
        int id = scheduleRepository.insert(writer.getStudentNo(), p.title, p.content, p.start, p.end,
                p.placeName, p.address, p.lat, p.lng);
        try {
            notificationService.notifyAllActive("SCHEDULE", p.title + " 일정이 등록되었어요.", "/schedules/view?id=" + id);
        } catch (SQLException e) {
            log.log(Level.WARNING, "일정 알림 저장 실패 (일정 " + id + "는 등록됨)", e);
        }
        return id;
    }

    /** 수정. 이미 삭제된 일정이면 false */
    public boolean edit(int id, Form f) throws SQLException {
        Parsed p = parse(f);
        return scheduleRepository.update(id, p.title, p.content, p.start, p.end, p.placeName, p.address, p.lat, p.lng);
    }

    public boolean delete(int id) throws SQLException {
        return scheduleRepository.delete(id);
    }

    // ---------- 입력 검사 ----------

    private record Parsed(String title, String content, LocalDateTime start, LocalDateTime end,
                          String placeName, String address, BigDecimal lat, BigDecimal lng) {}

    private static Parsed parse(Form f) {
        String title = strip(f.title());
        if (title.isEmpty()) throw new IllegalArgumentException("제목을 입력해 주세요.");
        if (title.length() > 100) throw new IllegalArgumentException("제목은 100자 이하로 입력해 주세요.");

        LocalDate date;
        LocalTime startTime;
        LocalTime endTime = null;
        try {
            date = LocalDate.parse(strip(f.date()));
            startTime = LocalTime.parse(strip(f.startTime()));
            if (!strip(f.endTime()).isEmpty()) endTime = LocalTime.parse(strip(f.endTime()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜와 시간을 올바르게 입력해 주세요.");
        }
        if (endTime != null && !endTime.isAfter(startTime)) throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 해요.");

        String placeName = emptyToNull(strip(f.placeName()));
        String address = emptyToNull(strip(f.address()));
        if (placeName != null && placeName.length() > 100) throw new IllegalArgumentException("장소 이름은 100자 이하로 입력해 주세요.");
        if (address != null && address.length() > 200) throw new IllegalArgumentException("주소는 200자 이하로 입력해 주세요.");
        String content = emptyToNull(strip(f.content()));
        if (content != null && content.length() > MAX_CONTENT) throw new IllegalArgumentException("내용은 " + MAX_CONTENT + "자 이하로 입력해 주세요.");

        BigDecimal lat = null;
        BigDecimal lng = null;
        String latText = strip(f.latitude());
        String lngText = strip(f.longitude());
        if (!latText.isEmpty() || !lngText.isEmpty()) {   // 지도에서 고른 좌표 (SFR-23): 둘 다 있어야 한다
            try {
                lat = new BigDecimal(latText);
                lng = new BigDecimal(lngText);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("장소 위치가 올바르지 않아요. 장소를 다시 선택해 주세요.");
            }
            if (lat.abs().compareTo(BigDecimal.valueOf(90)) > 0 || lng.abs().compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new IllegalArgumentException("장소 위치가 올바르지 않아요. 장소를 다시 선택해 주세요.");
            }
        }
        return new Parsed(title, content, date.atTime(startTime), endTime == null ? null : date.atTime(endTime),
                placeName, address, lat, lng);
    }

    private static String strip(String s) {
        return s == null ? "" : s.strip();
    }

    private static String emptyToNull(String s) {
        return s.isEmpty() ? null : s;
    }
}
