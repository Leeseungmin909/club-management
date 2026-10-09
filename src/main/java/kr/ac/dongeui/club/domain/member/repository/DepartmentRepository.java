package kr.ac.dongeui.club.domain.member.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import kr.ac.dongeui.club.domain.member.entity.Department;
import kr.ac.dongeui.club.global.config.Db;

public class DepartmentRepository {

    public List<Department> findAll() throws SQLException {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement("SELECT id, name FROM department ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            List<Department> list = new ArrayList<>();
            while (rs.next()) list.add(new Department(rs.getInt("id"), rs.getString("name")));
            return list;
        }
    }

    /** 학과 이름으로 번호 찾기. 없으면 null */
    public Integer findIdByName(String name) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("SELECT id FROM department WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
