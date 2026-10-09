package kr.ac.dongeui.club.domain.member.service;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import kr.ac.dongeui.club.domain.member.dto.NaverProfile;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.repository.DepartmentRepository;
import kr.ac.dongeui.club.domain.member.repository.MemberRepository;
import kr.ac.dongeui.club.domain.notification.repository.NotificationRepository;
import kr.ac.dongeui.club.global.infra.file.LocalFileStorageService;

/** 로그인 · 가입 신청 · 가입 승인 · 프로필 이미지 (SFR-01, 02, 06, 12) */
public class MemberService {

    private final MemberRepository memberRepository = new MemberRepository();
    private final DepartmentRepository departmentRepository = new DepartmentRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final LocalFileStorageService fileStorage = new LocalFileStorageService();

    public Member findByNaverId(String naverId) throws SQLException {
        return memberRepository.findByNaverId(naverId);
    }

    public Member findByStudentNo(String studentNo) throws SQLException {
        return memberRepository.findByStudentNo(studentNo);
    }

    public List<Member> listPending() throws SQLException {
        return memberRepository.findPending();
    }

    /** 가입 승인 + 해당 회원에게 승인 알림 (SFR-06, 27). 이미 처리된 신청이면 null */
    public Member approve(String studentNo) throws SQLException {
        Member member = memberRepository.findByStudentNo(studentNo);
        if (member == null || !memberRepository.approve(studentNo)) return null;
        // ponytail: 승인과 알림 저장을 트랜잭션으로 묶지 않음. 알림 저장만 실패하면 알림이 빠질 수 있다
        notificationRepository.create(studentNo, "APPROVED", "이제 동아리의 모든 기능을 이용할 수 있어요.", "/");
        return member;
    }

    /** 가입 거절: 신청 삭제 (SFR-06). 이미 처리된 신청이면 null */
    public Member reject(String studentNo) throws SQLException {
        Member member = memberRepository.findByStudentNo(studentNo);
        return member != null && memberRepository.deletePending(studentNo) ? member : null;
    }

    /** 프로필 이미지 변경 (SFR-12): 새 파일 저장 → DB 경로 변경 → 예전 파일 삭제 */
    public void changeProfileImage(Member me, Part photo) throws SQLException, IOException {
        String url = fileStorage.store(photo, LocalFileStorageService.IMAGES);
        try {
            memberRepository.updateProfileImage(me.getStudentNo(), url);
        } catch (SQLException e) {
            fileStorage.delete(url);  // DB 저장 실패 시 방금 올린 파일 정리
            throw e;
        }
        fileStorage.delete(me.getProfileImage());
    }

    /**
     * 가입 신청을 가입 대기 상태로 저장하고 저장된 회원을 돌려준다.
     * 입력이 잘못되면 화면에 보여 줄 문구로 IllegalArgumentException 을 던진다.
     */
    public Member signup(NaverProfile profile, String studentNo, String departmentName) throws SQLException {
        studentNo = studentNo == null ? "" : studentNo.trim();
        if (!studentNo.matches("\\d{8}")) throw new IllegalArgumentException("학번은 숫자 8자리로 입력해 주세요.");

        Integer departmentId = departmentName == null ? null : departmentRepository.findIdByName(departmentName.trim());
        if (departmentId == null) throw new IllegalArgumentException("학과는 목록에서 검색해 선택해 주세요.");

        if (memberRepository.findByStudentNo(studentNo) != null) throw new IllegalArgumentException("이미 등록된 학번입니다.");
        if (memberRepository.findByNaverId(profile.getId()) != null) throw new IllegalArgumentException("이미 가입 신청한 네이버 계정입니다.");

        try {
            memberRepository.insertPending(studentNo, profile.getId(), profile.getName(), departmentId, profile.getPhone());
        } catch (SQLIntegrityConstraintViolationException e) {  // 위 확인과 저장 사이에 같은 학번이 먼저 들어온 경우
            throw new IllegalArgumentException("이미 등록된 학번입니다.");
        }
        return memberRepository.findByStudentNo(studentNo);
    }
}
