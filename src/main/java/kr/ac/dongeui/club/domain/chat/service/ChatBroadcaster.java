package kr.ac.dongeui.club.domain.chat.service;

import jakarta.servlet.AsyncContext;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 로 연결된 브라우저 목록과 방송 (NFR-17).
 * ponytail: 서버 한 대 · 메모리 보관. 서버를 여러 대로 늘리면 Redis pub/sub 같은 공용 채널이 필요하다.
 */
public final class ChatBroadcaster {

    private static final Set<AsyncContext> clients = ConcurrentHashMap.newKeySet();
    private static ScheduledExecutorService heartbeat;

    private ChatBroadcaster() {}

    public static void add(AsyncContext ctx) {
        clients.add(ctx);
    }

    public static void remove(AsyncContext ctx) {
        clients.remove(ctx);
    }

    /** event: message(새 메시지) · edit(수정) · delete(삭제). id 는 새 메시지만 붙여 재연결 시 이어 받기에 쓴다 */
    public static void send(String event, Integer id, String json) {
        String frame = (id != null ? "id: " + id + "\n" : "") + "event: " + event + "\ndata: " + json + "\n\n";
        clients.forEach(ctx -> write(ctx, frame));
    }

    /** 한 브라우저에만 보내기 (재연결 시 놓친 메시지) */
    public static boolean write(AsyncContext ctx, String frame) {
        try {
            PrintWriter out = ctx.getResponse().getWriter();
            synchronized (ctx) {
                out.write(frame);
                out.flush();
            }
            if (!out.checkError()) return true;
        } catch (Exception ignored) {
            // 아래에서 정리
        }
        clients.remove(ctx);  // 창을 닫는 등 끊긴 연결
        try { ctx.complete(); } catch (Exception ignored) { }
        return false;
    }

    /** 25초마다 빈 주석을 보내 연결을 유지하고, 끊긴 연결을 정리한다 */
    public static synchronized void start() {
        if (heartbeat != null) return;
        heartbeat = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chat-sse-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeat.scheduleAtFixedRate(() -> clients.forEach(ctx -> write(ctx, ": ping\n\n")), 25, 25, TimeUnit.SECONDS);
    }

    public static synchronized void stop() {
        if (heartbeat != null) heartbeat.shutdownNow();
        heartbeat = null;
        clients.forEach(ctx -> { try { ctx.complete(); } catch (Exception ignored) { } });
        clients.clear();
    }
}
