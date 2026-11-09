package kr.ac.dongeui.club.demo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.notice.service.NoticeService;
import kr.ac.dongeui.club.domain.schedule.service.ScheduleService;
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
@WebServlet(urlPatterns = {"", "/deleted"})
public class DemoServlet extends HttpServlet {

    private final NoticeService noticeService = new NoticeService();
    private final ScheduleService scheduleService = new ScheduleService();

    private static final LocalDateTime NOW = LocalDateTime.now().withSecond(0).withNano(0);
    private static final LocalDateTime TODAY_AT = NOW.toLocalDate().atStartOfDay();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath() + Objects.toString(req.getPathInfo(), "");
        req.setAttribute("myRate", 86);
        req.setAttribute("pageNo", 1);
        req.setAttribute("totalPages", 1);

        String view;
        switch (path) {
            case "/" -> {
                int h = NOW.getHour();
                req.setAttribute("greeting", h < 12 ? "좋은 아침이에요" : h < 18 ? "좋은 오후예요" : "좋은 저녁이에요");
                try {
                    req.setAttribute("recentNotices", noticeService.recent(3));
                    req.setAttribute("next", scheduleService.next());
                    req.setAttribute("memberCount", scheduleService.memberCount());
                    Member me = (Member) req.getSession().getAttribute("me");
                    req.setAttribute("myRate", scheduleService.myAttendance(me.getStudentNo()).rate());
                } catch (SQLException e) {
                    throw new ServletException(e);
                }
                view = "home";
            }
            case "/deleted" -> { deleted(req, "/", null); view = "message"; }
            default -> { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    /** 폼 제출은 저장하지 않고 다음 화면으로만 이동한다. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getServletPath() + Objects.toString(req.getPathInfo(), "");
        String to = null;
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


    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }
}
