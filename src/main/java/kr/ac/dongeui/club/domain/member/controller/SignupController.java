package kr.ac.dongeui.club.domain.member.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.dto.OAuthProfile;
import kr.ac.dongeui.club.domain.member.repository.DepartmentRepository;
import kr.ac.dongeui.club.domain.member.service.MemberService;

/** 동아리 가입 신청 · 승인 대기 화면 (SFR-02). 접근 조건은 AuthFilter 가 확인한다. */
@WebServlet({"/signup", "/pending"})
public class SignupController extends HttpServlet {

    private final MemberService memberService = new MemberService();
    private final DepartmentRepository departmentRepository = new DepartmentRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getServletPath().equals("/pending")) {
            req.setAttribute("applicant", req.getSession().getAttribute("me"));
            req.getRequestDispatcher("/WEB-INF/views/auth/pending.jsp").forward(req, resp);
        } else {
            showForm(req, resp, null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        OAuthProfile profile = (OAuthProfile) session.getAttribute("oauthProfile");
        try {
            session.setAttribute("me", memberService.signup(profile, req.getParameter("studentNo"), req.getParameter("department"),
                    req.getParameter("name"), req.getParameter("phone")));
            session.removeAttribute("oauthProfile");
            resp.sendRedirect(req.getContextPath() + "/pending");
        } catch (IllegalArgumentException e) {
            showForm(req, resp, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, String error) throws ServletException, IOException {
        try {
            req.setAttribute("departments", departmentRepository.findAll());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.setAttribute("profile", req.getSession().getAttribute("oauthProfile"));
        req.setAttribute("error", error);
        req.getRequestDispatcher("/WEB-INF/views/auth/signup.jsp").forward(req, resp);
    }
}
