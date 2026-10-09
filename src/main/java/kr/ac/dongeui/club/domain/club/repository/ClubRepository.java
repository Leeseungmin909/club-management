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
}
