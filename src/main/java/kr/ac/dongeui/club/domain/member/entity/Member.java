package kr.ac.dongeui.club.domain.member.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/** member 테이블 한 줄 + 학과 이름. 로그인 사용자는 세션 "me" 로 들고 다닌다. */
public class Member implements Serializable {

    public enum Role { ADMIN, MEMBER }

    public enum Status { PENDING, ACTIVE, WITHDRAWN, KICKED }

    private final String studentNo;
    private final String naverId;
    private final String name;
    private final int departmentId;
    private final String departmentName;
    private final String phone;
    private final String profileImage;
    private final Role role;
    private final Status status;
    private final LocalDateTime requestedAt;
    private final LocalDateTime approvedAt;
    private final LocalDateTime leftAt;

    public Member(String studentNo, String naverId, String name, int departmentId, String departmentName, String phone,
                  String profileImage, Role role, Status status,
                  LocalDateTime requestedAt, LocalDateTime approvedAt, LocalDateTime leftAt) {
        this.studentNo = studentNo;
        this.naverId = naverId;
        this.name = name;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.phone = phone;
        this.profileImage = profileImage;
        this.role = role;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.leftAt = leftAt;
    }

    public boolean isAdmin() { return role == Role.ADMIN; }

    public String getStudentNo() { return studentNo; }
    public String getNaverId() { return naverId; }
    public String getName() { return name; }
    public int getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getPhone() { return phone; }
    public String getProfileImage() { return profileImage; }
    public Role getRole() { return role; }
    public Status getStatus() { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getLeftAt() { return leftAt; }
}
