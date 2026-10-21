package kr.ac.dongeui.club.domain.schedule.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import kr.ac.dongeui.club.global.config.Config;

/**
 * 일정 장소 검색 (SFR-23): 네이버 검색(지역) API 로 장소 이름 · 주소를 찾아 [{name, address, lat, lng}] JSON 으로 돌려준다.
 * 로그인용 네이버 앱(naver.client.id / secret)에서 "검색" API 를 켜 두면 같은 키를 쓴다. 키는 서버에만 둔다.
 */
@WebServlet("/admin/places/search")
public class PlaceSearchController extends HttpServlet {

    private static final Logger log = Logger.getLogger(PlaceSearchController.class.getName());
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q") == null ? "" : req.getParameter("q").strip();
        resp.setCharacterEncoding("UTF-8");
        if (q.isEmpty() || q.length() > 50) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "검색어를 50자 이하로 입력해 주세요.");
            return;
        }
        String id = Config.get("naver.client.id");
        String secret = Config.get("naver.client.secret");
        if (id.isEmpty() || secret.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "장소 검색 설정이 없어요.");
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://openapi.naver.com/v1/search/local.json?display=5&query="
                            + URLEncoder.encode(q, StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(5))
                    .header("X-Naver-Client-Id", id)
                    .header("X-Naver-Client-Secret", secret)
                    .GET().build();
            HttpResponse<String> res = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() != 200) {
                log.warning("네이버 장소 검색 실패: HTTP " + res.statusCode());
                resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "장소 검색에 실패했어요. 잠시 뒤 다시 시도해 주세요.");
                return;
            }
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(toPlaces(res.body()).toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "장소 검색에 실패했어요.");
        } catch (RuntimeException | IOException e) {
            log.log(Level.WARNING, "네이버 장소 검색 오류", e);
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "장소 검색에 실패했어요. 잠시 뒤 다시 시도해 주세요.");
        }
    }

    /** 네이버 응답 → 화면용 목록. mapx / mapy 는 경도 · 위도에 10^7 을 곱한 값이다 */
    static JsonArray toPlaces(String body) {
        JsonArray out = new JsonArray();
        for (JsonElement e : JsonParser.parseString(body).getAsJsonObject().getAsJsonArray("items")) {
            JsonObject item = e.getAsJsonObject();
            String address = text(item, "roadAddress").isEmpty() ? text(item, "address") : text(item, "roadAddress");
            JsonObject p = new JsonObject();
            p.addProperty("name", text(item, "title").replaceAll("<[^>]*>", ""));
            p.addProperty("address", address);
            p.addProperty("lat", new BigDecimal(text(item, "mapy")).movePointLeft(7).setScale(7, java.math.RoundingMode.HALF_UP));
            p.addProperty("lng", new BigDecimal(text(item, "mapx")).movePointLeft(7).setScale(7, java.math.RoundingMode.HALF_UP));
            out.add(p);
        }
        return out;
    }

    private static String text(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }
}
