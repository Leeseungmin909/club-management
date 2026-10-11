package kr.ac.dongeui.club.domain.member.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import kr.ac.dongeui.club.domain.member.dto.OAuthProfile;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.service.MemberService;
import kr.ac.dongeui.club.global.security.KakaoOAuthClient;
import kr.ac.dongeui.club.global.security.NaverOAuthClient;

/** 소셜 로그인(네이버 · 카카오) · 로그아웃 (SFR-01, 03) */
@WebServlet({"/login", "/login/naver", "/login/naver/callback", "/login/kakao", "/login/kakao/callback", "/logout"})
public class AuthController extends HttpServlet {

    private final MemberService memberService = new MemberService();
    private final NaverOAuthClient naver = new NaverOAuthClient();
    private final KakaoOAuthClient kakao = new KakaoOAuthClient();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/login/naver", "/login/kakao" -> {
                String state = UUID.randomUUID().toString();
                req.getSession().setAttribute("oauthState", state);
                resp.sendRedirect(req.getServletPath().equals("/login/naver") ? naver.authorizeUrl(state) : kakao.authorizeUrl(state));
            }
            case "/login/naver/callback" -> callback(req, resp, OAuthProfile.Provider.NAVER);
            case "/login/kakao/callback" -> callback(req, resp, OAuthProfile.Provider.KAKAO);
            case "/logout" -> {
                req.getSession().invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            default -> {  // /login: 한 번만 보여 줄 오류 문구를 세션에서 꺼낸다
                HttpSession session = req.getSession();
                req.setAttribute("error", session.getAttribute("loginError"));
                session.removeAttribute("loginError");
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
            }
        }
    }

    private void callback(HttpServletRequest req, HttpServletResponse resp, OAuthProfile.Provider provider) throws IOException {
        HttpSession session = req.getSession();
        Object expected = session.getAttribute("oauthState");
        session.removeAttribute("oauthState");
        String state = req.getParameter("state"), code = req.getParameter("code");
        if (code == null || expected == null || !expected.equals(state)) {  // 취소 또는 요청 위조
            fail(req, resp, provider.getLabel() + " 로그인이 취소되었거나 올바르지 않은 요청이에요. 다시 시도해 주세요.");
            return;
        }

        OAuthProfile profile;
        Member member;
        try {
            profile = provider == OAuthProfile.Provider.NAVER ? naver.fetchProfile(code, state) : kakao.fetchProfile(code);
            member = memberService.findByOAuth(profile);
        } catch (Exception e) {  // 외부 API · DB 장애는 로그인 화면 안내로 끝낸다 (NFR-18)
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log(provider.getLabel() + " 로그인 실패", e);
            fail(req, resp, provider.getLabel() + " 로그인에 실패했어요. 잠시 후 다시 시도해 주세요.");
            return;
        }

        if (member != null && (member.getStatus() == Member.Status.WITHDRAWN || member.getStatus() == Member.Status.KICKED)) {
            fail(req, resp, "탈퇴했거나 이용이 제한된 계정이에요.");
            return;
        }
        req.changeSessionId();  // 로그인 직후 세션 ID 교체 (세션 고정 공격 방지)
        if (member == null) {   // 처음 온 사용자 → 가입 신청
            session.setAttribute("oauthProfile", profile);
            resp.sendRedirect(req.getContextPath() + "/signup");
        } else {                // 가입 대기면 AuthFilter 가 승인 대기 화면으로 보낸다
            session.setAttribute("me", member);
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    private static void fail(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        req.getSession().setAttribute("loginError", message);
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
