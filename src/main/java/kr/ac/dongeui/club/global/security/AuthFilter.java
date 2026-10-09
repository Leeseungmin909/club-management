package kr.ac.dongeui.club.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import kr.ac.dongeui.club.domain.club.repository.ClubRepository;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.service.MemberService;
import kr.ac.dongeui.club.domain.notification.service.NotificationService;

/**
 * 모든 요청의 로그인 · 가입 상태 · 역할 확인 (SFR-04, NFR-06).
 * 미로그인 → 로그인, 가입 신청 전 → 가입 신청, 가입 대기 → 승인 대기, /admin/* 는 관리자만.
 * 회원 정보는 요청마다 DB에서 다시 읽어 승인 · 추방 · 권한 변경이 바로 반영되게 한다.
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    private static final Set<String> PUBLIC = Set.of("/login", "/login/naver", "/login/naver/callback", "/logout");

    private final MemberService memberService = new MemberService();
    private final ClubRepository clubRepository = new ClubRepository();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/img/") || PUBLIC.contains(path)) {
            chain.doFilter(req, resp);
            return;
        }

        // 화면은 브라우저에 저장하지 않는다: 뒤로 가기에도 최신 상태(알림 읽음 등)를 받고, 로그아웃 후 이전 화면도 안 보이게 (SFR-03)
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession();
        Member me = (Member) session.getAttribute("me");
        if (me == null) {
            if (path.equals("/signup") && session.getAttribute("naverProfile") != null) chain.doFilter(req, resp);
            else redirect(req, resp, "/login");
            return;
        }

        try {
            me = memberService.findByStudentNo(me.getStudentNo());
            if (me == null || me.getStatus() == Member.Status.WITHDRAWN || me.getStatus() == Member.Status.KICKED) {
                session.invalidate();  // 거절(신청 삭제) · 탈퇴 · 추방
                redirect(req, resp, "/login");
                return;
            }
            session.setAttribute("me", me);
            req.setAttribute("club", clubRepository.find());  // 사이드바 동아리 카드
            if (me.getStatus() == Member.Status.ACTIVE) {     // 종 아이콘: 안 읽은 수 · 최근 알림 (SFR-28)
                req.setAttribute("unread", notificationService.unreadCount(me.getStudentNo()));
                req.setAttribute("bell", notificationService.recent(me.getStudentNo()));
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        if (me.getStatus() == Member.Status.PENDING) {
            if (path.equals("/pending")) chain.doFilter(req, resp);
            else redirect(req, resp, "/pending");
        } else if (path.equals("/pending") || path.equals("/signup")) {
            redirect(req, resp, "/");
        } else if (path.startsWith("/admin/") && !me.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            chain.doFilter(req, resp);
        }
    }

    private static void redirect(HttpServletRequest req, HttpServletResponse resp, String to) throws IOException {
        resp.sendRedirect(req.getContextPath() + to);
    }
}
