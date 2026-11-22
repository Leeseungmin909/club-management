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
import kr.ac.dongeui.club.domain.member.dto.OAuthProfile;
import kr.ac.dongeui.club.global.config.Config;

/**
 * 카카오 로그인 (OAuth 2.0). Client Secret 은 서버 설정 파일에만 있고 브라우저로 보내지 않는다 (NFR-09).
 * 이름 · 전화번호는 카카오 심사가 필요해 받지 않고 고유 ID 만 쓴다 (닉네임은 실명이 아니라서 쓰지 않음).
 */
public class KakaoOAuthClient {

    private static final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    /** 카카오 로그인 화면 주소. state 는 콜백에서 요청 위조를 확인하는 값 (NFR-05) */
    public String authorizeUrl(String state) {
        return "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + enc(Config.get("kakao.client.id"))
                + "&redirect_uri=" + enc(Config.get("kakao.callback.url"))
                + "&state=" + enc(state);
    }

    /** 콜백으로 받은 code 로 토큰을 받고, 그 토큰으로 고유 ID 를 가져온다. */
    public OAuthProfile fetchProfile(String code) throws IOException, InterruptedException {
        String form = "grant_type=authorization_code"
                + "&client_id=" + enc(Config.get("kakao.client.id"))
                + "&client_secret=" + enc(Config.get("kakao.client.secret"))
                + "&redirect_uri=" + enc(Config.get("kakao.callback.url"))  // 인가 요청 때와 같아야 한다
                + "&code=" + enc(code);
        JsonObject token = send(HttpRequest.newBuilder(URI.create("https://kauth.kakao.com/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(form)));
        if (!token.has("access_token")) throw new IOException("카카오 토큰 발급 실패: " + token);

        JsonObject me = send(HttpRequest.newBuilder(URI.create("https://kapi.kakao.com/v2/user/me"))
                .header("Authorization", "Bearer " + token.get("access_token").getAsString()));
        if (!me.has("id")) throw new IOException("카카오 프로필 조회 실패: " + me);
        return new OAuthProfile(OAuthProfile.Provider.KAKAO, me.get("id").getAsString(), null, null);
    }

    private static JsonObject send(HttpRequest.Builder req) throws IOException, InterruptedException {
        String body = http.send(req.timeout(Duration.ofSeconds(5)).build(), HttpResponse.BodyHandlers.ofString()).body();
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
