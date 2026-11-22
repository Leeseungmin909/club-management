package kr.ac.dongeui.club.domain.chat.service;

import com.google.gson.Gson;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import kr.ac.dongeui.club.domain.chat.entity.ChatMessage;
import kr.ac.dongeui.club.domain.chat.repository.ChatRepository;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.global.infra.file.LocalFileStorageService;

/** 동아리 단체 채팅 (SFR-29): 보내기 · 수정(본인) · 삭제(본인 · 관리자), 결과는 SSE 로 모두에게 */
public class ChatService {

    public static final int MAX_LENGTH = 1000;
    private static final Gson gson = new Gson();
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);

    private final ChatRepository chatRepository = new ChatRepository();
    private final LocalFileStorageService fileStorage = new LocalFileStorageService();

    /** 화면 처음 그릴 때 쓰는 전체 메시지 JSON */
    public String allJson() throws SQLException {
        return gson.toJson(chatRepository.findAfter(0).stream().map(ChatService::toMap).toList());
    }

    public List<ChatMessage> after(int id) throws SQLException {
        return chatRepository.findAfter(id);
    }

    /** 글자 또는 파일 하나를 보낸다. 잘못된 입력이면 IllegalArgumentException(화면에 보여 줄 문구) */
    public void send(Member me, String text, Part file) throws SQLException, IOException {
        int id;
        if (file != null && file.getSize() > 0) {
            String path = fileStorage.store(file, LocalFileStorageService.CHAT_FILES);
            String type = path.endsWith(".mp4") || path.endsWith(".webm") ? "VIDEO" : "IMAGE";
            try {
                id = chatRepository.insert(me.getStudentNo(), type, null, path, file.getSubmittedFileName());
            } catch (SQLException e) {
                fileStorage.delete(path);
                throw e;
            }
        } else {
            id = chatRepository.insert(me.getStudentNo(), "TEXT", checkText(text), null, null);
        }
        ChatBroadcaster.send("message", id, json(chatRepository.findById(id)));
    }

    /** 본인 글자 메시지만 수정 */
    public void edit(Member me, int id, String text) throws SQLException {
        ChatMessage m = chatRepository.findById(id);
        if (m == null) throw new IllegalArgumentException("이미 삭제된 메시지예요.");
        if (!m.getSenderNo().equals(me.getStudentNo()) || !m.getType().equals("TEXT")) {
            throw new SecurityException("본인이 보낸 글자 메시지만 수정할 수 있어요.");
        }
        chatRepository.updateContent(id, checkText(text));
        ChatBroadcaster.send("edit", null, json(chatRepository.findById(id)));
    }

    /** 본인 메시지 또는 관리자는 모든 메시지 삭제. 첨부 파일도 지운다 */
    public void delete(Member me, int id) throws SQLException, IOException {
        ChatMessage m = chatRepository.findById(id);
        if (m == null) return;  // 이미 삭제됨
        if (!m.getSenderNo().equals(me.getStudentNo()) && !me.isAdmin()) {
            throw new SecurityException("본인 메시지만 삭제할 수 있어요.");
        }
        chatRepository.delete(id);
        fileStorage.delete(m.getFilePath());
        ChatBroadcaster.send("delete", null, "{\"id\":" + id + "}");
    }

    public static String json(ChatMessage m) {
        return gson.toJson(toMap(m));
    }

    private static String checkText(String text) {
        String t = text == null ? "" : text.strip();
        if (t.isEmpty()) throw new IllegalArgumentException("메시지를 입력해 주세요.");
        if (t.length() > MAX_LENGTH) throw new IllegalArgumentException("메시지는 " + MAX_LENGTH + "자까지 보낼 수 있어요.");
        return t;
    }

    /** 브라우저(chat.js)가 그리는 데 필요한 값만. Gson 은 < > 를 이스케이프해 script 태그 안에 넣어도 안전하다 */
    private static Map<String, Object> toMap(ChatMessage m) {
        Map<String, Object> j = new LinkedHashMap<>();
        j.put("id", m.getId());
        j.put("senderNo", m.getSenderNo());
        j.put("senderName", m.getSenderName());
        j.put("senderProfileImage", m.getSenderProfileImage());
        j.put("type", m.getType());
        j.put("content", m.getContent());
        j.put("filePath", m.getFilePath());
        j.put("fileName", m.getFileName());
        j.put("day", DAY.format(m.getCreatedAt()));
        j.put("time", TIME.format(m.getCreatedAt()));
        j.put("edited", m.getUpdatedAt() != null);
        return j;
    }
}
