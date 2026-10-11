package kr.ac.dongeui.club.domain.notification.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.notification.service.NotificationService;

/** 알림 목록 · 알림 열기(읽음 처리 후 이동) · 모두 읽음 (SFR-28) */
@WebServlet({"/notifications", "/notifications/open", "/notifications/read-all"})
public class NotificationController extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String me = ((Member) req.getSession().getAttribute("me")).getStudentNo();
        try {
            if (req.getServletPath().equals("/notifications/open")) {
                resp.sendRedirect(req.getContextPath() + notificationService.open(parseInt(req.getParameter("id"), 0), me));
                return;
            }
            int totalPages = notificationService.totalPages(me);
            int page = Math.min(Math.max(parseInt(req.getParameter("page"), 1), 1), totalPages);
            req.setAttribute("notifications", notificationService.page(me, page));
            req.setAttribute("pageNo", page);
            req.setAttribute("totalPages", totalPages);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getServletPath().equals("/notifications/read-all")) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            notificationService.readAll(((Member) req.getSession().getAttribute("me")).getStudentNo());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getSession().setAttribute("flash", "모든 알림을 읽음으로 표시했어요.");
        resp.sendRedirect(req.getContextPath() + "/notifications");
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
