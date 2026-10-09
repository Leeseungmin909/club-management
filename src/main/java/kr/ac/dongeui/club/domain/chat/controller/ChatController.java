package kr.ac.dongeui.club.domain.chat.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.chat.service.ChatService;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.repository.MemberRepository;
import kr.ac.dongeui.club.global.infra.file.LocalFileStorageService;

/**
 * 단체 채팅 화면 (GET /chat) 과 보내기 · 수정 · 삭제 (chat.js 가 fetch / XHR 로 호출).
 * 성공은 204, 잘못된 입력은 400 + 안내 문구, 권한 없음은 403. 결과 화면 반영은 SSE 로 온다.
 */
@WebServlet({"/chat", "/chat/messages", "/chat/messages/edit", "/chat/messages/delete"})
@MultipartConfig(maxFileSize = LocalFileStorageService.MAX_BYTES, maxRequestSize = LocalFileStorageService.MAX_BYTES + 1024 * 1024)
public class ChatController extends HttpServlet {

    private final ChatService chatService = new ChatService();
    private final MemberRepository memberRepository = new MemberRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.getServletPath().equals("/chat")) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        try {
            req.setAttribute("messagesJson", chatService.allJson());
            req.setAttribute("memberCount", memberRepository.countActive());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/WEB-INF/views/chat.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member me = (Member) req.getSession().getAttribute("me");
        try {
            switch (req.getServletPath()) {
                case "/chat/messages" -> chatService.send(me, req.getParameter("content"),
                        req.getContentType() != null && req.getContentType().startsWith("multipart/") ? req.getPart("file") : null);
                case "/chat/messages/edit" -> chatService.edit(me, id(req), req.getParameter("content"));
                case "/chat/messages/delete" -> chatService.delete(me, id(req));
                default -> {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException e) {
            text(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {  // @MultipartConfig 용량 초과
            text(resp, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "파일은 100MB 이하만 올릴 수 있어요.");
        } catch (SecurityException e) {
            text(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private static int id(HttpServletRequest req) {
        try {
            return Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 메시지예요.");
        }
    }

    private static void text(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("text/plain; charset=UTF-8");
        resp.getWriter().write(message);
    }
}
