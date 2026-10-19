package kr.ac.dongeui.club.demo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.notice.service.NoticeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 화면 확인용 임시 서블릿. 예시 데이터를 DB 컬럼 이름 그대로 넣어 JSP를 보여 준다.
 * 기능을 구현하면 해당 주소를 실제 Controller 가 맡고, 모두 바뀌면 이 파일을 삭제한다.
 * 로그인 · 가입 · 권한 검사는 실제 코드(AuthController, SignupController, AuthFilter)가 처리한다.
 */
@WebServlet(urlPatterns = {"", "/deleted",
        "/schedules", "/schedules/*", "/members", "/me", "/me/*",
        "/admin/*"})
public class DemoServlet extends HttpServlet {

    private final NoticeService noticeService = new NoticeService();

    private static final LocalDateTime NOW = LocalDateTime.now().withSecond(0).withNano(0);
    private static final LocalDateTime TODAY_AT = NOW.toLocalDate().atStartOfDay();

    private static final List<Map<String, Object>> MEMBERS = List.of(
            member("20201001", "김도윤", "컴퓨터소프트웨어공학과", "010-1234-0001", "ADMIN", 400),
            member("20211002", "이나경", "디자인조형학과", "010-1234-0002", "ADMIN", 300),
            member("20221003", "김민지", "시각디자인학과", "010-1234-0003", "MEMBER", 200),
            member("20221004", "박서준", "경영학과", "010-1234-0004", "MEMBER", 150),
            member("20231005", "정하은", "영화학과", "010-1234-0005", "MEMBER", 90),
            member("20231006", "최유진", "신문방송학과", "010-1234-0006", "MEMBER", 40),
            member("20241007", "한지민", "인공지능학과", "010-1234-0007", "MEMBER", 12));

    private static final List<Map<String, Object>> SCHEDULES = List.of(
            sched(1, "가을 출사 — 해운대", 3, 14, 0, "해운대해수욕장 이벤트광장", "부산 해운대구 우동", 35.1587, 129.1604,
                    "바다 풍경과 인물 사진을 함께 찍어 봐요. 보조배터리를 챙겨 주세요."),
            sched(2, "라이트룸 보정 세미나", 9, 18, 30, "동의대학교 정보공학관 302호", "부산 부산진구 엄광로 176", 35.1435, 129.0342,
                    "노트북에 라이트룸을 미리 설치해 오세요."),
            sched(3, "하반기 사진 전시회", 23, 11, 0, "부산시민공원 갤러리", "부산 부산진구 시민공원로 73", 35.1680, 129.0570, null),
            sched(4, "광안리 야경 출사", -12, 19, 0, "광안리해수욕장", "부산 수영구 광안해변로 219", 35.1532, 129.1186, null),
            sched(5, "신입 부원 환영회", -26, 18, 0, "동아리방", null, null, null, null));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath() + Objects.toString(req.getPathInfo(), "");
        req.setAttribute("memberCount", MEMBERS.size());
        req.setAttribute("myRate", 86);
        req.setAttribute("pageNo", 1);
        req.setAttribute("totalPages", 1);

        String view;
        switch (path) {
            case "/" -> {
                int h = NOW.getHour();
                req.setAttribute("greeting", h < 12 ? "좋은 아침이에요" : h < 18 ? "좋은 오후예요" : "좋은 저녁이에요");
                req.setAttribute("next", SCHEDULES.get(0));
                try {
                    req.setAttribute("recentNotices", noticeService.recent(3));
                } catch (SQLException e) {
                    throw new ServletException(e);
                }
                view = "home";
            }
            case "/schedules" -> {
                LocalDate today = NOW.toLocalDate();
                List<Map<String, Object>> week = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    LocalDate d = today.plusDays(i);
                    week.add(row("date", d, "today", i == 0,
                            "has", SCHEDULES.stream().anyMatch(s -> ((LocalDateTime) s.get("start_at")).toLocalDate().equals(d))));
                }
                req.setAttribute("week", week);
                req.setAttribute("upcoming", SCHEDULES.stream().filter(s -> !(Boolean) s.get("started")).toList());
                req.setAttribute("past", SCHEDULES.stream().filter(s -> (Boolean) s.get("started")).toList());
                view = "schedule/list";
            }
            case "/schedules/view" -> {
                Map<String, Object> s = find(SCHEDULES, req.getParameter("id"));
                if (s == null) { deleted(req, "/schedules", "일정 목록으로"); view = "message"; }
                else {
                    req.setAttribute("s", s);
                    req.setAttribute("myStatus", "1".equals(req.getParameter("id")) ? "ATTEND" : null);
                    req.setAttribute("attendees", MEMBERS.subList(0, 4));
                    req.setAttribute("absentees", MEMBERS.subList(4, 5));
                    req.setAttribute("noReply", MEMBERS.size() - 5);
                    view = "schedule/view";
                }
            }
            case "/members" -> { req.setAttribute("members", MEMBERS); view = "member/list"; }
            case "/me" -> {
                req.setAttribute("myTotal", 7);
                req.setAttribute("myAttend", 6);
                req.setAttribute("history", List.of(
                        row("schedule_id", 4, "title", "광안리 야경 출사", "start_at", SCHEDULES.get(3).get("start_at"), "status", "ATTEND"),
                        row("schedule_id", 5, "title", "신입 부원 환영회", "start_at", SCHEDULES.get(4).get("start_at"), "status", "ATTEND"),
                        row("schedule_id", 99, "title", "벚꽃 출사", "start_at", NOW.minusMonths(6), "status", "ABSENT")));
                view = "me";
            }
            case "/deleted" -> { deleted(req, "/", null); view = "message"; }
            case "/admin/members" -> { req.setAttribute("members", MEMBERS); view = "admin/members"; }
            case "/admin/club" -> view = "admin/club";
            case "/admin/schedules/write" -> view = "schedule/form";
            case "/admin/schedules/edit" -> { req.setAttribute("s", find(SCHEDULES, req.getParameter("id"))); view = "schedule/form"; }
            default -> { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    /** 폼 제출은 저장하지 않고 다음 화면으로만 이동한다. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath() + Objects.toString(req.getPathInfo(), "");
        String to = path.startsWith("/admin/schedules") ? "/schedules"
                : null;
        String referer = req.getHeader("Referer");
        resp.sendRedirect(to != null ? req.getContextPath() + to : referer != null ? referer : req.getContextPath() + "/");
    }

    private static void deleted(HttpServletRequest req, String link, String linkText) {
        req.setAttribute("msgIcon", "file-x");
        req.setAttribute("msgTitle", "삭제된 글입니다");
        req.setAttribute("msgText", "작성자가 삭제했거나 더 이상 볼 수 없는 글이에요.");
        req.setAttribute("msgLink", req.getContextPath() + link);
        req.setAttribute("msgLinkText", linkText);
    }

    private static Map<String, Object> find(List<Map<String, Object>> rows, String id) {
        return rows.stream().filter(r -> r.get("id").toString().equals(id)).findFirst().orElse(null);
    }

    private static Map<String, Object> member(String no, String name, String dept, String phone, String role, int daysAgo) {
        return row("student_no", no, "name", name, "department", dept, "phone", phone, "role", role,
                "profile_image", null, "approved_at", NOW.minusDays(daysAgo));
    }

    private static Map<String, Object> sched(int id, String title, int days, int hour, int min, String place, String address,
                                             Double lat, Double lng, String content) {
        LocalDateTime start = TODAY_AT.plusDays(days).withHour(hour).withMinute(min);
        return row("id", id, "title", title, "content", content, "start_at", start, "end_at", start.plusHours(3),
                "place_name", place, "address", address, "latitude", lat, "longitude", lng,
                "dday", ChronoUnit.DAYS.between(NOW.toLocalDate(), start.toLocalDate()), "started", start.isBefore(NOW));
    }


    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }
}
