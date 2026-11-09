package kr.ac.dongeui.club.domain.member.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import kr.ac.dongeui.club.domain.member.entity.Member;
import kr.ac.dongeui.club.domain.member.repository.MemberAdminRepository;

/** 회원 목록 · 추방 · 탈퇴 · 관리자 지정 (SFR-07~10). 안내 문구는 IllegalArgumentException 으로 돌려준다. */
public class MemberAdminService {

    private final MemberAdminRepository repository = new MemberAdminRepository();
    private final MemberService memberService = new MemberService();

    public List<Map<String, Object>> list() throws SQLException {
        return repository.findActive();
    }

    public void kick(Member admin, String studentNo) throws SQLException {
        if (admin.getStudentNo().equals(studentNo)) throw new IllegalArgumentException("나 자신은 추방할 수 없어요.");
        Member target = memberService.findByStudentNo(studentNo);
        if (target == null || target.getStatus() != Member.Status.ACTIVE) throw new IllegalArgumentException("이미 활동 중인 회원이 아니에요.");
        if (target.isAdmin()) throw new IllegalArgumentException("관리자는 추방할 수 없어요. 먼저 관리자를 해제해 주세요.");
        if (!repository.kick(studentNo)) throw new IllegalArgumentException("추방하지 못했어요. 다시 확인해 주세요.");
    }

    /** 관리자 지정(ADMIN) · 해제(MEMBER). 이름을 돌려준다 */
    public String changeRole(String studentNo, String role) throws SQLException {
        Member target = memberService.findByStudentNo(studentNo);
        if (target == null || target.getStatus() != Member.Status.ACTIVE) throw new IllegalArgumentException("활동 중인 회원이 아니에요.");
        if ("ADMIN".equals(role)) {
            if (target.isAdmin() || !repository.promote(studentNo)) throw new IllegalArgumentException("이미 관리자예요.");
        } else if ("MEMBER".equals(role)) {
            if (!repository.demote(studentNo)) {
                throw new IllegalArgumentException(target.isAdmin() ? "마지막 관리자는 해제할 수 없어요." : "이미 일반 회원이에요.");
            }
        } else {
            throw new IllegalArgumentException("잘못된 요청이에요.");
        }
        return target.getName();
    }

    /** 동아리 탈퇴. 마지막 관리자는 먼저 다른 관리자를 지정해야 한다 */
    public void withdraw(Member me) throws SQLException {
        if (!repository.withdraw(me.getStudentNo())) {
            throw new IllegalArgumentException("마지막 관리자는 탈퇴할 수 없어요. 다른 회원을 관리자로 지정한 뒤 탈퇴해 주세요.");
        }
    }
}
