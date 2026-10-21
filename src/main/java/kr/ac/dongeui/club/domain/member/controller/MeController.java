package kr.ac.dongeui.club.domain.member.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.schedule.service.ScheduleService;

/** 내 정보 화면: 내 프로필 + 나의 출석률 · 최근 출석 기록 (SFR-11) */
@WebServlet("/me")
public class MeController extends HttpServlet {

    private final ScheduleService scheduleService = new ScheduleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member me = (Member) req.getSession().getAttribute("me");
        try {
            ScheduleService.MyAttendance a = scheduleService.myAttendance(me.getStudentNo());
            req.setAttribute("myRate", a.rate());
            req.setAttribute("myTotal", a.total());
            req.setAttribute("myAttend", a.attend());
            req.setAttribute("history", a.history());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/me.jsp").forward(req, resp);
    }
}
