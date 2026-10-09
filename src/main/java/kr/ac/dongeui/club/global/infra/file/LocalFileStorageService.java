package kr.ac.dongeui.club.global.infra.file;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import kr.ac.dongeui.club.global.config.Config;

/**
 * 업로드 파일 저장 (NFR-14, 15). 웹앱 폴더 밖 upload.dir 에 UUID 파일명으로 저장하고 "/uploads/파일명" 을 돌려준다.
 * Tomcat 이 /uploads 를 업로드 폴더에 연결해 직접 내보낸다 (동영상 구간 이동 지원, NFR-16).
 */
public class LocalFileStorageService {

    public static final long MAX_BYTES = 100L * 1024 * 1024;
    public static final Set<String> IMAGES = Set.of("jpg", "png");
    public static final Set<String> CHAT_FILES = Set.of("jpg", "png", "gif", "mp4", "webm");

    private static final String URL_PREFIX = "/uploads/";

    /** 검사 후 저장. 잘못된 파일이면 화면에 보여 줄 문구로 IllegalArgumentException */
    public String store(Part part, Set<String> allowed) throws IOException {
        if (part == null || part.getSize() == 0) throw new IllegalArgumentException("파일을 선택해 주세요.");
        if (part.getSize() > MAX_BYTES) throw new IllegalArgumentException("파일은 100MB 이하만 올릴 수 있어요.");

        String ext = extension(part.getSubmittedFileName());
        if (!allowed.contains(ext) || !looksLike(part, ext)) {
            throw new IllegalArgumentException(String.join(", ", allowed).toUpperCase(Locale.ROOT) + " 파일만 올릴 수 있어요.");
        }

        Path dir = Path.of(Config.get("upload.dir"));
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "." + ext;
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, dir.resolve(name));
        }
        return URL_PREFIX + name;
    }

    /** store() 가 돌려준 주소의 파일 삭제. 업로드 폴더 밖은 건드리지 않는다 */
    public void delete(String url) throws IOException {
        if (url == null || !url.startsWith(URL_PREFIX)) return;
        Path dir = Path.of(Config.get("upload.dir"));
        Files.deleteIfExists(dir.resolve(Path.of(url).getFileName().toString()));
    }

    private static String extension(String fileName) {
        int dot = fileName == null ? -1 : fileName.lastIndexOf('.');
        String ext = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ext.equals("jpeg") ? "jpg" : ext;
    }

    /** 확장자만 바꾼 파일을 막기 위해 파일 앞부분(시그니처)을 확인한다 */
    private static boolean looksLike(Part part, String ext) throws IOException {
        byte[] h;
        try (InputStream in = part.getInputStream()) {
            h = in.readNBytes(12);
        }
        return switch (ext) {
            case "jpg" -> startsWith(h, 0, 0xFF, 0xD8, 0xFF);
            case "png" -> startsWith(h, 0, 0x89, 'P', 'N', 'G');
            case "gif" -> startsWith(h, 0, 'G', 'I', 'F', '8');
            case "mp4" -> startsWith(h, 4, 'f', 't', 'y', 'p');
            case "webm" -> startsWith(h, 0, 0x1A, 0x45, 0xDF, 0xA3);
            default -> false;
        };
    }

    private static boolean startsWith(byte[] data, int offset, int... sig) {
        if (data.length < offset + sig.length) return false;
        int[] head = new int[sig.length];
        for (int i = 0; i < sig.length; i++) head[i] = data[offset + i] & 0xFF;
        return Arrays.equals(head, sig);
    }
}
