package kr.ac.dongeui.club.domain.club.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kr.ac.dongeui.club.domain.club.entity.Club;
import kr.ac.dongeui.club.global.config.Db;

public class ClubRepository {

    public Club find() throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT name, school, intro, image_path, banner_path FROM club WHERE id = 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? new Club(rs.getString("name"), rs.getString("school"), rs.getString("intro"),
                    rs.getString("image_path"), rs.getString("banner_path")) : null;
        }
    }

    /** 동아리 정보 수정 (SFR-05). 이미지 경로가 null 이면 기존 이미지를 그대로 둔다 */
    public void update(String name, String school, String intro, String imagePath, String bannerPath) throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("""
                     UPDATE club SET name = ?, school = ?, intro = ?,
                            image_path = COALESCE(?, image_path), banner_path = COALESCE(?, banner_path) WHERE id = 1
                     """)) {
            ps.setString(1, name);
            ps.setString(2, school);
            ps.setString(3, intro);
            ps.setString(4, imagePath);
            ps.setString(5, bannerPath);
            ps.executeUpdate();
        }
    }
}
