package kr.ac.dongeui.club.demo;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
        "/notices", "/notices/*", "/schedules", "/schedules/*", "/chat", "/members", "/me", "/me/*",
        "/notifications", "/notifications/*", "/admin/*"})
public class DemoServlet extends HttpServlet {

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

    private static final List<Map<String, Object>> NOTICES = List.of(
            row("id", 1, "title", "10월 정기 출사 안내", "is_pinned", true, "view_count", 128, "writer_name", "김도윤",
                    "created_at", NOW.minusDays(1).withHour(10), "updated_at", null,
                    "content", "이번 정기 출사는 해운대에서 진행합니다.\n오후 2시까지 이벤트광장 앞으로 와 주세요.\n카메라가 없어도 휴대폰으로 참여할 수 있어요!"),
            row("id", 2, "title", "신입 부원 환영회 사진 공유", "is_pinned", false, "view_count", 96, "writer_name", "이나경",
                    "created_at", NOW.minusDays(5).withHour(20), "updated_at", null,
                    "content", "지난 환영회의 즐거운 순간들을 확인해 보세요. 사진은 단체 채팅방에 올려 두었습니다."),
            row("id", 3, "title", "2학기 회비 납부 안내", "is_pinned", false, "view_count", 74, "writer_name", "김도윤",
                    "created_at", NOW.minusDays(13).withHour(9), "updated_at", NOW.minusDays(12),
                    "content", "2학기 동아리 회비 납부 일정을 안내드립니다.\n금액: 20,000원\n기한: 이번 달 말까지"),
            row("id", 4, "title", "9월 사진 공모전 결과 발표", "is_pinned", false, "view_count", 157, "writer_name", "이나경",
                    "created_at", NOW.minusDays(20).withHour(18), "updated_at", null,
                    "content", "수상하신 모든 분께 축하의 마음을 전합니다. 수상작은 다음 전시회에 함께 걸립니다."));

    private static final List<Map<String, Object>> SCHEDULES = List.of(
            sched(1, "가을 출사 — 해운대", 3, 14, 0, "해운대해수욕장 이벤트광장", "부산 해운대구 우동", 35.1587, 129.1604,
                    "바다 풍경과 인물 사진을 함께 찍어 봐요. 보조배터리를 챙겨 주세요."),
            sched(2, "라이트룸 보정 세미나", 9, 18, 30, "동의대학교 정보공학관 302호", "부산 부산진구 엄광로 176", 35.1435, 129.0342,
                    "노트북에 라이트룸을 미리 설치해 오세요."),
            sched(3, "하반기 사진 전시회", 23, 11, 0, "부산시민공원 갤러리", "부산 부산진구 시민공원로 73", 35.1680, 129.0570, null),
            sched(4, "광안리 야경 출사", -12, 19, 0, "광안리해수욕장", "부산 수영구 광안해변로 219", 35.1532, 129.1186, null),
            sched(5, "신입 부원 환영회", -26, 18, 0, "동아리방", null, null, null, null));

    private static final List<Map<String, Object>> NOTIS = List.of(
            row("id", 1, "type", "APPROVED", "message", "이제 CPU의 모든 기능을 이용할 수 있어요.", "link_url", "/",
                    "is_read", false, "created_at", NOW.minusMinutes(1)),
            row("id", 2, "type", "NOTICE", "message", "10월 정기 출사 안내를 확인해 주세요.", "link_url", "/notices/view?id=1",
                    "is_read", false, "created_at", NOW.minusMinutes(10)),
            row("id", 3, "type", "SCHEDULE", "message", "가을 출사 — 해운대 일정이 추가되었어요.", "link_url", "/schedules/view?id=1",
                    "is_read", true, "created_at", NOW.minusDays(1)),
            row("id", 4, "type", "NOTICE", "message", "동아리방 이용 안내 (삭제된 공지)", "link_url", "/notices/view?id=99",
                    "is_read", true, "created_at", NOW.minusDays(4)));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath() + Objects.toString(req.getPathInfo(), "");
        req.setAttribute("bell", NOTIS.subList(0, 3));
        req.setAttribute("unread", NOTIS.stream().filter(n -> !(Boolean) n.get("is_read")).count());
        req.setAttribute("memberCount", MEMBERS.size());
        req.setAttribute("myRate", 86);
        req.setAttribute("pageNo", 1);
        req.setAttribute("totalPages", 1);

        String view;
        switch (path) {
            case "/notifications/open" -> {
                String id = req.getParameter("id");
                String link = NOTIS.stream().filter(n -> n.get("id").toString().equals(id))
                        .map(n -> (String) n.get("link_url")).findFirst().orElse("/notifications");
                resp.sendRedirect(req.getContextPath() + link);
                return;
            }
            case "/" -> {
                int h = NOW.getHour();
                req.setAttribute("greeting", h < 12 ? "좋은 아침이에요" : h < 18 ? "좋은 오후예요" : "좋은 저녁이에요");
                req.setAttribute("next", SCHEDULES.get(0));
                req.setAttribute("recentNotices", NOTICES.subList(0, 3));
                view = "home";
            }
            case "/notices" -> {
                String q = Objects.toString(req.getParameter("q"), "").trim();
                List<Map<String, Object>> list = NOTICES.stream().filter(n -> ((String) n.get("title")).contains(q)).toList();
                req.setAttribute("q", q);
                req.setAttribute("notices", list);
                req.setAttribute("total", list.size());
                view = "notice/list";
            }
            case "/notices/view" -> {
                Map<String, Object> n = find(NOTICES, req.getParameter("id"));
                if (n == null) { deleted(req, "/notices", "공지 목록으로"); view = "message"; }
                else { req.setAttribute("notice", n); view = "notice/view"; }
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
            case "/chat" -> {
                req.setAttribute("messages", List.of(
                        chat("20211002", "TEXT", "이번 주 출사 날씨가 정말 좋대요!", null, NOW.minusDays(1).withHour(13).withMinute(32)),
                        chat("20201001", "TEXT", "맞아요. 해운대 이벤트광장 앞에서 2시에 만나요.", null, NOW.minusDays(1).withHour(13).withMinute(34)),
                        chat("20221003", "TEXT", "저도 참석할게요! 카메라 배터리 넉넉히 챙겨갈게요.", null, NOW.minusDays(1).withHour(13).withMinute(36)),
                        chat("20231005", "IMAGE", null, "/img/demo-photo.svg", NOW.minusDays(1).withHour(13).withMinute(39)),
                        chat("20231005", "TEXT", "지난번 광안리에서 찍은 사진이에요. 출사 끝나고 다 같이 저녁 먹을 사람도 있나요?", null, NOW.minusDays(1).withHour(13).withMinute(40)),
                        chat("20221004", "TEXT", "좋아요 🙌", null, NOW.withHour(9).withMinute(5))));
                view = "chat";
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
            case "/notifications" -> { req.setAttribute("notifications", NOTIS); view = "notifications"; }
            case "/deleted" -> { deleted(req, "/", null); view = "message"; }
            case "/admin/dashboard" -> {
                List<String> labels = new ArrayList<>();
                for (int i = 6; i >= 0; i--) labels.add(YearMonth.from(NOW).minusMonths(i).getMonthValue() + "월");
                int[] joined = {4, 3, 5, 6, 8, 4, 3}, left = {0, 1, 0, 1, 2, 1, 2}, total = new int[7];
                for (int i = 0, sum = 10; i < 7; i++) total[i] = sum += joined[i] - left[i];
                Map<String, Object> chart = row("labels", labels, "joined", joined, "left", left, "total", total);
                req.setAttribute("chart", chart);
                req.setAttribute("chartJson", new Gson().toJson(chart));
                req.setAttribute("stats", row("total", total[6], "joinedThisMonth", joined[6], "avgRate", 78, "leftThisMonth", left[6]));
                req.setAttribute("pendingCount", 3);
                view = "admin/dashboard";
            }
            case "/admin/members" -> { req.setAttribute("members", MEMBERS); view = "admin/members"; }
            case "/admin/club" -> view = "admin/club";
            case "/admin/notices/write" -> view = "notice/form";
            case "/admin/notices/edit" -> { req.setAttribute("notice", find(NOTICES, req.getParameter("id"))); view = "notice/form"; }
            case "/admin/schedules/write" -> view = "schedule/form";
            case "/admin/schedules/edit" -> { req.setAttribute("s", find(SCHEDULES, req.getParameter("id"))); view = "schedule/form"; }
            default -> { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    /** 폼 제출은 저장하지 않고 다음 화면으로만 이동한다. AI 다듬기는 아직 연결 전이라 실패로 응답한다. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath() + Objects.toString(req.getPathInfo(), "");
        if (path.equals("/admin/notices/ai")) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        String to = path.startsWith("/admin/notices") ? "/notices"
                : path.startsWith("/admin/schedules") ? "/schedules"
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

    private static Map<String, Object> chat(String senderNo, String type, String content, String file, LocalDateTime at) {
        Map<String, Object> sender = MEMBERS.stream().filter(m -> m.get("student_no").equals(senderNo)).findFirst().orElseThrow();
        return row("sender_no", senderNo, "name", sender.get("name"), "profile_image", null, "message_type", type,
                "content", content, "file_path", file, "file_name", file == null ? null : "photo.jpg", "created_at", at);
    }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }
}
