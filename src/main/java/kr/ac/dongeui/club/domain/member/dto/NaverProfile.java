package kr.ac.dongeui.club.domain.member.dto;

import java.io.Serializable;

/** 네이버에서 받은 회원 정보. 가입 신청 전까지 세션 "naverProfile" 로 보관한다. */
public class NaverProfile implements Serializable {

    private final String id;
    private final String name;
    private final String phone;

    public NaverProfile(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
