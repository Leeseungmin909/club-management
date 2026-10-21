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

/** 동아리 탈퇴 (SFR-09): 탈퇴 처리 후 로그아웃 */
@WebServlet("/me/withdraw")
public class WithdrawController extends HttpServlet {

    private final MemberAdminService service = new MemberAdminService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member me = (Member) req.getSession().getAttribute("me");
        try {
            service.withdraw(me);
        } catch (IllegalArgumentException e) {   // 마지막 관리자
            req.getSession().setAttribute("flash", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/me");
            return;
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getSession().invalidate();
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
