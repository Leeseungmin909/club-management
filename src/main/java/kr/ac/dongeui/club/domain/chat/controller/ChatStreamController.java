package kr.ac.dongeui.club.domain.chat.controller;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.chat.entity.ChatMessage;
import kr.ac.dongeui.club.domain.chat.service.ChatBroadcaster;
import kr.ac.dongeui.club.domain.chat.service.ChatService;

/**
 * SSE 연결 (GET /chat/stream). 연결을 비동기로 열어 두고 새 메시지 · 수정 · 삭제를 흘려보낸다 (SFR-29, NFR-17).
 * 끊겼다 다시 붙으면 브라우저가 보낸 Last-Event-ID(처음엔 ?after=) 뒤의 메시지부터 보내 놓친 메시지를 채운다.
 * ponytail: 끊겨 있는 동안의 수정 · 삭제는 다시 보내지 않는다(새로고침하면 맞춰짐). 놓친 메시지 조회와 목록 등록 사이
 * 몇 ms 안에 온 메시지는 빠질 수 있다. 문제가 되면 등록을 먼저 하고 브라우저에서 id 순서로 끼워 넣는다.
 */
@WebServlet(urlPatterns = "/chat/stream", asyncSupported = true)
public class ChatStreamController extends HttpServlet {

    private final ChatService chatService = new ChatService();

    @Override
    public void init() {
        ChatBroadcaster.start();
    }

    @Override
    public void destroy() {
        ChatBroadcaster.stop();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");

        AsyncContext ctx = req.startAsync();
        ctx.setTimeout(0);  // 시간 제한 없음 (끊긴 연결은 heartbeat 가 정리)
        ctx.addListener(new AsyncListener() {
            public void onComplete(AsyncEvent e) { ChatBroadcaster.remove(ctx); }
            public void onTimeout(AsyncEvent e) { ChatBroadcaster.remove(ctx); }
            public void onError(AsyncEvent e) { ChatBroadcaster.remove(ctx); }
            public void onStartAsync(AsyncEvent e) { }
        });

        if (!ChatBroadcaster.write(ctx, "retry: 3000\n\n")) return;  // 끊기면 3초 뒤 다시 연결
        try {
            for (ChatMessage m : chatService.after(lastId(req))) {
                if (!ChatBroadcaster.write(ctx, "id: " + m.getId() + "\nevent: message\ndata: " + ChatService.json(m) + "\n\n")) return;
            }
        } catch (SQLException e) {
            log("놓친 채팅 메시지 조회 실패", e);
        }
        ChatBroadcaster.add(ctx);
    }

    private static int lastId(HttpServletRequest req) {
        String v = req.getHeader("Last-Event-ID");
        if (v == null) v = req.getParameter("after");
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;  // 기준이 없으면 지난 메시지는 보내지 않는다
        }
    }
}
