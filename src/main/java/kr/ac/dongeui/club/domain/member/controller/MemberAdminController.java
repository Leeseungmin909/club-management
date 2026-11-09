package kr.ac.dongeui.club.domain.member.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.service.MemberAdminService;

/** 회원 관리: 목록 · 추방(SFR-08) · 관리자 지정/해제(SFR-10). /admin/* 라서 관리자만 들어온다. */
@WebServlet({"/admin/members", "/admin/members/role", "/admin/members/kick"})
public class MemberAdminController extends HttpServlet {

    private final MemberAdminService service = new MemberAdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getServletPath().equals("/admin/members")) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            req.setAttribute("members", service.list());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/members.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member me = (Member) req.getSession().getAttribute("me");
        String studentNo = req.getParameter("studentNo");
        String flash;
        try {
            switch (req.getServletPath()) {
                case "/admin/members/kick" -> {
                    service.kick(me, studentNo);
                    flash = "회원을 추방했어요.";
                }
                case "/admin/members/role" -> {
                    String role = req.getParameter("role");
                    String name = service.changeRole(studentNo, role);
                    flash = "ADMIN".equals(role) ? name + "님을 관리자로 지정했어요." : name + "님의 관리자 권한을 해제했어요.";
                }
                default -> {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            flash = e.getMessage();
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getSession().setAttribute("flash", flash);
        resp.sendRedirect(req.getContextPath() + "/admin/members");
    }
}
