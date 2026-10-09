package kr.ac.dongeui.club.domain.member.entity;

/** department 테이블 한 줄 (가입 시 검색해서 선택) */
public class Department {

    private final int id;
    private final String name;

    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}
