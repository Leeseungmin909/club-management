package kr.ac.dongeui.club.domain.notification.service;

import java.sql.SQLException;
import java.util.List;
import kr.ac.dongeui.club.domain.notification.entity.Notification;
import kr.ac.dongeui.club.domain.notification.repository.NotificationRepository;

/** 알림 생성 · 조회 (SFR-27, 28) */
public class NotificationService {

    public static final int PAGE_SIZE = 15;
    private static final int BELL_SIZE = 5;

    private final NotificationRepository notificationRepository = new NotificationRepository();

    /**
     * 활동 중인 전체 회원에게 알림 (새 공지 · 새 일정 등록 직후 호출).
     * 예) notifyAllActive("NOTICE", "10월 정기 출사 안내가 등록되었어요.", "/notices/view?id=3")
     */
    public void notifyAllActive(String type, String message, String linkUrl) throws SQLException {
        notificationRepository.createForAllActive(type, message, linkUrl);
    }

    /** 종 아이콘 드롭다운용 최근 알림 */
    public List<Notification> recent(String studentNo) throws SQLException {
        return notificationRepository.findPage(studentNo, BELL_SIZE, 0);
    }

    public int unreadCount(String studentNo) throws SQLException {
        return notificationRepository.count(studentNo, true);
    }

    public int totalPages(String studentNo) throws SQLException {
        return Math.max(1, (notificationRepository.count(studentNo, false) + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    public List<Notification> page(String studentNo, int page) throws SQLException {
        return notificationRepository.findPage(studentNo, PAGE_SIZE, (page - 1) * PAGE_SIZE);
    }

    /** 읽음 처리 후 이동할 주소. 내 알림이 아니거나 사이트 밖 주소면 알림 목록으로 */
    public String open(int id, String studentNo) throws SQLException {
        String link = notificationRepository.markReadAndGetLink(id, studentNo);
        boolean internal = link != null && link.startsWith("/") && !link.startsWith("//") && !link.contains("\\");
        return internal ? link : "/notifications";
    }

    public void readAll(String studentNo) throws SQLException {
        notificationRepository.markAllRead(studentNo);
    }
}
