package kr.ac.dongeui.club.global.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** 서버의 config.properties (Git 밖) 를 읽는다. 위치는 환경변수 CLUBAPP_CONFIG 로 바꿀 수 있다. */
public final class Config {
    private static final Properties props = new Properties();

    static {
        String path = System.getenv().getOrDefault("CLUBAPP_CONFIG", "/var/clubapp/config.properties");
        try (Reader r = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            props.load(r);
        } catch (IOException e) {
            throw new IllegalStateException("설정 파일을 읽을 수 없음: " + path, e);
        }
    }

    private Config() {}

    public static String get(String key) {
        return props.getProperty(key, "");
    }
}
