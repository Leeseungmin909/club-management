package kr.ac.dongeui.club.domain.member.dto;

import java.io.Serializable;

/**
 * 소셜 로그인(네이버 · 카카오)에서 받은 회원 정보. 가입 신청 전까지 세션 "oauthProfile" 로 보관한다.
 * 네이버는 이름 · 휴대전화를 주고, 카카오는 고유 ID 만 쓴다 (이름 · 전화번호는 가입 신청 화면에서 입력).
 */
public class OAuthProfile implements Serializable {

    public enum Provider {
        NAVER("네이버"), KAKAO("카카오");

        private final String label;
        Provider(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final Provider provider;
    private final String id;
    private final String name;
    private final String phone;

    public OAuthProfile(Provider provider, String id, String name, String phone) {
        this.provider = provider;
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public Provider getProvider() { return provider; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
