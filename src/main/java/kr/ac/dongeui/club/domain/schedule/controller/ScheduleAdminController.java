package kr.ac.dongeui.club.domain.schedule.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.schedule.service.ScheduleService;

/** 일정 등록 · 수정 · 삭제 (SFR-17, 19). /admin/* 라서 AuthFilter 가 관리자만 들여보낸다. */
@WebServlet({"/admin/schedules/write", "/admin/schedules/edit", "/admin/schedules/delete"})
public class ScheduleAdminController extends HttpServlet {

    private static final String FORM = "/WEB-INF/views/schedule/form.jsp";
    private final ScheduleService scheduleService = new ScheduleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String path = req.getServletPath();
            if (path.equals("/admin/schedules/delete")) {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            if (path.equals("/admin/schedules/edit")) {
                Map<String, Object> s = scheduleService.find(ScheduleController.parseInt(req.getParameter("id"), -1));
                if (s == null) {
                    ScheduleController.showDeleted(req, resp);
                    return;
                }
                req.setAttribute("s", s);
            }
            req.getRequestDispatcher(FORM).forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        int id = ScheduleController.parseInt(req.getParameter("id"), -1);
        ScheduleService.Form form = new ScheduleService.Form(req.getParameter("title"), req.getParameter("date"),
                req.getParameter("startTime"), req.getParameter("endTime"), req.getParameter("placeName"),
                req.getParameter("address"), req.getParameter("latitude"), req.getParameter("longitude"),
                req.getParameter("content"));
        try {
            switch (path) {
                case "/admin/schedules/write" -> {
                    Member me = (Member) req.getSession().getAttribute("me");
                    int newId = scheduleService.write(me, form);
                    req.getSession().setAttribute("flash", "일정을 등록했어요.");
                    resp.sendRedirect(req.getContextPath() + "/schedules/view?id=" + newId);
                }
                case "/admin/schedules/edit" -> {
                    if (!scheduleService.edit(id, form)) {
                        ScheduleController.showDeleted(req, resp);
                        return;
                    }
                    req.getSession().setAttribute("flash", "일정을 수정했어요.");
                    resp.sendRedirect(req.getContextPath() + "/schedules/view?id=" + id);
                }
                case "/admin/schedules/delete" -> {
                    scheduleService.delete(id);   // 이미 지워졌어도 목록으로 돌려보낸다
                    req.getSession().setAttribute("flash", "일정을 삭제했어요.");
                    resp.sendRedirect(req.getContextPath() + "/schedules");
                }
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {   // 입력 오류: 쓰던 내용을 그대로 두고 안내
            req.setAttribute("error", e.getMessage());
            req.setAttribute("f", form);
            if (path.equals("/admin/schedules/edit")) req.setAttribute("s", Map.of("id", id));
            req.getRequestDispatcher(FORM).forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
