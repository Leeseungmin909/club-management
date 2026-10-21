package kr.ac.dongeui.club.domain.member.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.service.MemberAdminService;

/** 회원 목록 (SFR-07): 활동 중인 회원 모두. 전화번호는 화면에서 관리자에게만 보인다. */
@WebServlet("/members")
public class MemberListController extends HttpServlet {

    private final MemberAdminService service = new MemberAdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("members", service.list());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/member/list.jsp").forward(req, resp);
    }
}
