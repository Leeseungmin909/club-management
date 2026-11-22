package kr.ac.dongeui.club.domain.notice.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import kr.ac.dongeui.club.domain.notice.service.NoticeService;

/** 공지 목록 · 검색 · 상세 (SFR-14, 16). 모든 회원이 볼 수 있다. */
@WebServlet({"/notices", "/notices/view"})
public class NoticeController extends HttpServlet {

    private static final int MAX_QUERY = 100;
    private final NoticeService noticeService = new NoticeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getServletPath().equals("/notices/view")) view(req, resp);
            else list(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
        String q = req.getParameter("q") == null ? "" : req.getParameter("q").strip();
        if (q.length() > MAX_QUERY) q = q.substring(0, MAX_QUERY);
        NoticeService.Page p = noticeService.list(q, parseInt(req.getParameter("page"), 1));
        req.setAttribute("q", q);
        req.setAttribute("notices", p.items());
        req.setAttribute("total", p.total());
        req.setAttribute("pageNo", p.page());
        req.setAttribute("totalPages", p.totalPages());
        req.getRequestDispatcher("/WEB-INF/views/notice/list.jsp").forward(req, resp);
    }

    private void view(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
        int id = parseInt(req.getParameter("id"), -1);
        Map<String, Object> notice = id < 0 ? null : noticeService.view(id, firstView(req.getSession(), id));
        if (notice == null) {   // 삭제된 공지 (알림 · 즐겨찾기로 들어온 경우)
            showDeleted(req, resp);
            return;
        }
        req.setAttribute("notice", notice);
        req.getRequestDispatcher("/WEB-INF/views/notice/view.jsp").forward(req, resp);
    }

    /** 같은 로그인 동안 같은 공지를 새로고침해도 조회수는 한 번만 올린다 */
    @SuppressWarnings("unchecked")
    private static boolean firstView(HttpSession session, int id) {
        Set<Integer> viewed = (Set<Integer>) session.getAttribute("viewedNotices");
        if (viewed == null) {
            viewed = new HashSet<>();
            session.setAttribute("viewedNotices", viewed);
        }
        return viewed.add(id);
    }

    static void showDeleted(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("msgIcon", "file-x");
        req.setAttribute("msgTitle", "삭제된 글입니다");
        req.setAttribute("msgText", "작성자가 삭제했거나 더 이상 볼 수 없는 글이에요.");
        req.setAttribute("msgLink", req.getContextPath() + "/notices");
        req.setAttribute("msgLinkText", "공지 목록으로");
        req.getRequestDispatcher("/WEB-INF/views/message.jsp").forward(req, resp);
    }

    static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.strip());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
