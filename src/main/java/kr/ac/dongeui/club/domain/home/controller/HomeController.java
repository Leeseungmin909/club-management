package kr.ac.dongeui.club.domain.home.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.service.MemberService;
import kr.ac.dongeui.club.domain.notice.service.NoticeService;
import kr.ac.dongeui.club.domain.schedule.service.ScheduleService;

/** 홈 화면: 인사말, 최근 공지, 다음 일정, 회원 수, 내 출석률을 각 도메인 서비스에서 모아 보여 준다. */
@WebServlet("")
public class HomeController extends HttpServlet {

    private final NoticeService noticeService = new NoticeService();
    private final ScheduleService scheduleService = new ScheduleService();
    private final MemberService memberService = new MemberService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int h = LocalTime.now().getHour();  // 요청할 때마다 현재 시각 기준
        req.setAttribute("greeting", h < 12 ? "좋은 아침이에요" : h < 18 ? "좋은 오후예요" : "좋은 저녁이에요");
        Member me = (Member) req.getSession().getAttribute("me");
        try {
            req.setAttribute("recentNotices", noticeService.recent(3));
            req.setAttribute("next", scheduleService.next());
            req.setAttribute("memberCount", memberService.countActive());
            req.setAttribute("myRate", scheduleService.myAttendance(me.getStudentNo()).rate());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
