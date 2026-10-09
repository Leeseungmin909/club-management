package kr.ac.dongeui.club.domain.club.entity;

/** club 테이블 (id = 1 한 줄만 존재) */
public class Club {

    private final String name;
    private final String school;
    private final String intro;
    private final String imagePath;   // 대표(프로필) 이미지
    private final String bannerPath;  // 배경(커버) 이미지

    public Club(String name, String school, String intro, String imagePath, String bannerPath) {
        this.name = name;
        this.school = school;
        this.intro = intro;
        this.imagePath = imagePath;
        this.bannerPath = bannerPath;
    }

    public String getName() { return name; }
    public String getSchool() { return school; }
    public String getIntro() { return intro; }
    public String getImagePath() { return imagePath; }
    public String getBannerPath() { return bannerPath; }
}
