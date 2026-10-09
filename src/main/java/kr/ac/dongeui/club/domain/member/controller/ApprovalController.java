package kr.ac.dongeui.club.domain.member.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.service.MemberService;

/** 가입 승인 · 거절 (SFR-06). /admin/* 라서 AuthFilter 가 관리자만 들여보낸다. */
@WebServlet({"/admin/approvals", "/admin/approvals/*"})
public class ApprovalController extends HttpServlet {

    private final MemberService memberService = new MemberService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("pending", memberService.listPending());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/approvals.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getPathInfo();
        if (!"/approve".equals(action) && !"/reject".equals(action)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String studentNo = req.getParameter("studentNo");
        boolean approve = action.equals("/approve");
        try {
            Member m = approve ? memberService.approve(studentNo) : memberService.reject(studentNo);
            req.getSession().setAttribute("flash", m == null ? "이미 처리된 신청이에요."
                    : m.getName() + "님의 가입을 " + (approve ? "승인했어요." : "거절했어요."));
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/approvals");
    }
}
