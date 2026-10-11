package kr.ac.dongeui.club.domain.notice.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import kr.ac.dongeui.club.global.config.Config;

/**
 * AI 공지 작성 보조 (SFR-30): 관리자가 쓴 초안을 공지 문체로 다듬는다. Gemini API (무료 등급) 를 서버에서 호출한다.
 * API 키는 config.properties 의 ai.api.key 에만 있고 브라우저로 보내지 않는다 (NFR-09).
 */
public class NoticeAiService {

    public static final int MAX_DRAFT = 2000;
    private static final String DEFAULT_MODEL = "gemini-3.5-flash-lite";
    private static final String INSTRUCTION = """
            너는 대학 동아리 관리자가 공지를 쓰는 것을 돕는다. 관리자가 쓴 초안을 회원들에게 보낼 공지 문체로 다듬어라.
            규칙:
            1. 날짜, 시간, 장소, 금액, 이름, 준비물 같은 사실은 그대로 두고, 초안에 없는 내용은 지어내지 마라.
            2. 정중하고 친근한 존댓말로, 가장 중요한 내용이 먼저 오게 정리하라.
            3. 필요하면 줄바꿈과 "-" 목록만 쓰고, 마크다운 제목이나 굵은 글씨는 쓰지 마라.
            4. 다듬은 공지 본문만 출력하고 설명이나 인사말을 덧붙이지 마라.
            """;

    private static final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final Gson gson = new Gson();

    /** 다듬은 본문. 초안이 잘못되면 IllegalArgumentException(안내 문구), AI 호출이 실패하면 IOException */
    public String polish(String draft) throws IOException, InterruptedException {
        String text = draft == null ? "" : draft.strip();
        if (text.isEmpty()) throw new IllegalArgumentException("내용에 초안을 먼저 입력해 주세요.");
        if (text.length() > MAX_DRAFT) throw new IllegalArgumentException("초안은 " + MAX_DRAFT + "자까지 다듬을 수 있어요.");

        String key = Config.get("ai.api.key");
        if (key.isBlank()) throw new IOException("ai.api.key 가 설정되지 않음");
        String model = Config.get("ai.model").isBlank() ? DEFAULT_MODEL : Config.get("ai.model");

        String body = gson.toJson(Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", INSTRUCTION))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", text)))),
                "generationConfig", Map.of("temperature", 0.4, "maxOutputTokens", 2048)));
        HttpRequest req = HttpRequest.newBuilder(URI.create(
                        "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("x-goog-api-key", key)  // 주소(로그에 남을 수 있음)가 아닌 헤더로 보낸다
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Gemini " + res.statusCode() + ": " + res.body());

        // candidates[0].content.parts[*].text 를 이어 붙인다
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        JsonArray candidates = json.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) throw new IOException("Gemini 응답에 결과 없음: " + res.body());
        JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
        StringBuilder out = new StringBuilder();
        if (content != null && content.has("parts")) {
            for (JsonElement part : content.getAsJsonArray("parts")) {
                JsonObject p = part.getAsJsonObject();
                if (p.has("text") && !p.has("thought")) out.append(p.get("text").getAsString());
            }
        }
        if (out.toString().isBlank()) throw new IOException("Gemini 응답이 비어 있음: " + res.body());
        return out.toString().strip();
    }
}
