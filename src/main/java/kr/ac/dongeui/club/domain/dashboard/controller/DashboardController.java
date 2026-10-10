package kr.ac.dongeui.club.domain.dashboard.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import kr.ac.dongeui.club.domain.dashboard.service.DashboardService;
import kr.ac.dongeui.club.domain.member.service.MemberService;

/** 관리자 대시보드 (SFR-26). /admin/* 라서 관리자만. 그래프 데이터는 페이지에 JSON 으로 함께 내려 보낸다. */
@WebServlet("/admin/dashboard")
public class DashboardController extends HttpServlet {

    private final DashboardService dashboardService = new DashboardService();
    private final MemberService memberService = new MemberService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> dash = dashboardService.load();
            req.setAttribute("dash", dash);
            req.setAttribute("dashJson", gson.toJson(dash));
            req.setAttribute("pendingCount", memberService.listPending().size());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
