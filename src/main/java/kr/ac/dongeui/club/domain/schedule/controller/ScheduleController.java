package kr.ac.dongeui.club.domain.schedule.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.schedule.service.ScheduleService;

/** 일정 목록 · 상세 · 참석 여부 체크 (SFR-18, 20~22). 모든 회원이 쓸 수 있다. */
@WebServlet({"/schedules", "/schedules/view", "/schedules/attend"})
public class ScheduleController extends HttpServlet {

    private final ScheduleService scheduleService = new ScheduleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getServletPath().equals("/schedules/view")) view(req, resp);
            else if (req.getServletPath().equals("/schedules")) list(req, resp);
            else resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getServletPath().equals("/schedules/attend")) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        Member me = (Member) req.getSession().getAttribute("me");
        int id = parseInt(req.getParameter("scheduleId"), -1);
        String status = req.getParameter("status");
        try {
            if (!scheduleService.attend(id, me.getStudentNo(), status)) {
                showDeleted(req, resp);
                return;
            }
            req.getSession().setAttribute("flash", "ATTEND".equals(status) ? "참석으로 응답했어요." : "불참으로 응답했어요.");
        } catch (IllegalArgumentException e) {
            req.getSession().setAttribute("flash", e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/schedules/view?id=" + id);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
        ScheduleService.ListView v = scheduleService.list(parseInt(req.getParameter("page"), 1));
        req.setAttribute("week", v.week());
        req.setAttribute("upcoming", v.upcoming());
        req.setAttribute("past", v.past());
        req.setAttribute("pageNo", v.page());
        req.setAttribute("totalPages", v.totalPages());
        req.getRequestDispatcher("/WEB-INF/views/schedule/list.jsp").forward(req, resp);
    }

    private void view(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ServletException, IOException {
        Member me = (Member) req.getSession().getAttribute("me");
        ScheduleService.Detail d = scheduleService.detail(parseInt(req.getParameter("id"), -1), me.getStudentNo());
        if (d == null) {   // 삭제된 일정 (알림으로 들어온 경우 등)
            showDeleted(req, resp);
            return;
        }
        req.setAttribute("s", d.schedule());
        req.setAttribute("myStatus", d.myStatus());
        req.setAttribute("attendees", d.attendees());
        req.setAttribute("absentees", d.absentees());
        req.setAttribute("noReply", d.noReply());
        req.getRequestDispatcher("/WEB-INF/views/schedule/view.jsp").forward(req, resp);
    }

    static void showDeleted(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("msgIcon", "file-x");
        req.setAttribute("msgTitle", "삭제된 일정입니다");
        req.setAttribute("msgText", "관리자가 삭제했거나 더 이상 볼 수 없는 일정이에요.");
        req.setAttribute("msgLink", req.getContextPath() + "/schedules");
        req.setAttribute("msgLinkText", "일정 목록으로");
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
