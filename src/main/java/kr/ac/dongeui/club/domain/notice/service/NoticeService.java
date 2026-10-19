package kr.ac.dongeui.club.domain.notice.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.notice.repository.NoticeRepository;
import kr.ac.dongeui.club.domain.notification.service.NotificationService;

/** 공지사항 작성 · 조회 · 수정 · 삭제 · 검색 (SFR-13~16) */
public class NoticeService {

    public static final int PAGE_SIZE = 15;   // NFR-20
    static final int MAX_TITLE = 100;
    static final int MAX_CONTENT = 5000;
    private static final Logger log = Logger.getLogger(NoticeService.class.getName());

    private final NoticeRepository noticeRepository = new NoticeRepository();
    private final NotificationService notificationService = new NotificationService();

    /** 목록 한 쪽. 쪽 번호가 범위를 벗어나면 가까운 쪽으로 맞춘다 */
    public record Page(List<Map<String, Object>> items, int total, int page, int totalPages) {}

    public Page list(String q, int page) throws SQLException {
        String keyword = q == null ? "" : q.strip();
        int total = noticeRepository.count(keyword);
        int totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(1, page), totalPages);
        return new Page(noticeRepository.findPage(keyword, PAGE_SIZE, (current - 1) * PAGE_SIZE), total, current, totalPages);
    }

    public List<Map<String, Object>> recent(int limit) throws SQLException {
        return noticeRepository.findRecent(limit);
    }

    public Map<String, Object> find(int id) throws SQLException {
        return noticeRepository.find(id);
    }

    /** 상세 보기: countView 가 true 면 조회수를 1 올린 뒤의 공지를 돌려준다. 삭제된 공지면 null */
    public Map<String, Object> view(int id, boolean countView) throws SQLException {
        if (countView) noticeRepository.increaseViewCount(id);
        return noticeRepository.find(id);
    }

    /** 공지 등록 후 활동 중인 회원 모두에게 알림. 잘못된 입력이면 IllegalArgumentException(안내 문구) */
    public int write(Member writer, String title, String content, boolean pinned) throws SQLException {
        String t = checkTitle(title);
        String c = checkContent(content);
        int id = noticeRepository.insert(writer.getStudentNo(), t, c, pinned);
        try {
            notificationService.notifyAllActive("NOTICE", t + " 공지가 등록되었어요.", "/notices/view?id=" + id);
        } catch (SQLException e) {
            log.log(Level.WARNING, "공지 알림 저장 실패 (공지 " + id + "는 등록됨)", e);   // 공지는 이미 저장됐으니 다시 등록하게 만들지 않는다
        }
        return id;
    }

    /** 수정. 이미 삭제된 공지면 false */
    public boolean edit(int id, String title, String content, boolean pinned) throws SQLException {
        return noticeRepository.update(id, checkTitle(title), checkContent(content), pinned);
    }

    public boolean delete(int id) throws SQLException {
        return noticeRepository.delete(id);
    }

    private static String checkTitle(String title) {
        String t = title == null ? "" : title.strip();
        if (t.isEmpty()) throw new IllegalArgumentException("제목을 입력해 주세요.");
        if (t.length() > MAX_TITLE) throw new IllegalArgumentException("제목은 " + MAX_TITLE + "자 이하로 입력해 주세요.");
        return t;
    }

    private static String checkContent(String content) {
        String c = content == null ? "" : content.strip();
        if (c.isEmpty()) throw new IllegalArgumentException("내용을 입력해 주세요.");
        if (c.length() > MAX_CONTENT) throw new IllegalArgumentException("내용은 " + MAX_CONTENT + "자 이하로 입력해 주세요.");
        return c;
    }
}
