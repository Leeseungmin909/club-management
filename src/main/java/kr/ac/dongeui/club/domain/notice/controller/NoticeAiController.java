package kr.ac.dongeui.club.domain.notice.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.ac.dongeui.club.domain.notice.service.NoticeAiService;

/**
 * AI 공지 다듬기 (SFR-30). 공지 작성 화면의 "AI로 다듬기" 가 fetch 로 호출한다. /admin/* 라서 관리자만.
 * 성공 200 + 다듬은 본문, 잘못된 초안 400 + 안내, AI 장애 503 (화면은 초안을 그대로 둔다, NFR-18).
 */
@WebServlet("/admin/notices/ai")
public class NoticeAiController extends HttpServlet {

    private final NoticeAiService aiService = new NoticeAiService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");
        try {
            resp.getWriter().write(aiService.polish(req.getParameter("draft")));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(e.getMessage());
        } catch (Exception e) {  // 키 미설정 · 한도 초과 · 시간 초과 · 응답 이상
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log("AI 다듬기 실패", e);
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
