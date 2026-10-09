package kr.ac.dongeui.club.global.security;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import kr.ac.dongeui.club.domain.member.dto.NaverProfile;
import kr.ac.dongeui.club.global.config.Config;

/** 네이버 로그인 (OAuth 2.0). Client Secret 은 서버 설정 파일에만 있고 브라우저로 보내지 않는다 (NFR-09). */
public class NaverOAuthClient {

    private static final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    /** 네이버 로그인 화면 주소. state 는 콜백에서 요청 위조를 확인하는 값 (NFR-05) */
    public String authorizeUrl(String state) {
        return "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + enc(Config.get("naver.client.id"))
                + "&redirect_uri=" + enc(Config.get("naver.callback.url"))
                + "&state=" + enc(state);
    }

    /** 콜백으로 받은 code 로 토큰을 받고, 그 토큰으로 회원 프로필(고유 ID · 이름 · 휴대전화)을 가져온다. */
    public NaverProfile fetchProfile(String code, String state) throws IOException, InterruptedException {
        JsonObject token = getJson("https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                + "&client_id=" + enc(Config.get("naver.client.id"))
                + "&client_secret=" + enc(Config.get("naver.client.secret"))
                + "&code=" + enc(code) + "&state=" + enc(state), null);
        if (!token.has("access_token")) throw new IOException("네이버 토큰 발급 실패: " + token);

        JsonObject me = getJson("https://openapi.naver.com/v1/nid/me", token.get("access_token").getAsString());
        if (!"00".equals(me.get("resultcode").getAsString())) throw new IOException("네이버 프로필 조회 실패: " + me);
        JsonObject p = me.getAsJsonObject("response");
        return new NaverProfile(p.get("id").getAsString(), str(p, "name"), str(p, "mobile"));
    }

    private static JsonObject getJson(String url, String bearer) throws IOException, InterruptedException {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5));
        if (bearer != null) req.header("Authorization", "Bearer " + bearer);
        String body = http.send(req.build(), HttpResponse.BodyHandlers.ofString()).body();
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
