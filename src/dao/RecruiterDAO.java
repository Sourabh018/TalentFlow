package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Recruiter;
import util.DBConnection;

/**
 * RecruiterDAO — mirrors CandidateDAO's pattern but for recruiters.
 * Same 2-table coordination via UserDAO.
 */
public class RecruiterDAO {

    private UserDAO userDAO = new UserDAO();

    public int insertRecruiter(Recruiter r) throws SQLException {
        int newId = userDAO.insertUser(r);

        String sql = "INSERT INTO recruiters (id, company_name) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newId);
            ps.setString(2, r.getCompanyName());
            ps.executeUpdate();
        }

        return newId;
    }

    public Recruiter getRecruiterById(int id) throws SQLException {
        String sql = "SELECT u.id, u.name, u.email, u.password, r.company_name " +
                     "FROM users u JOIN recruiters r ON u.id = r.id WHERE u.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Recruiter> getAllRecruiters() throws SQLException {
        List<Recruiter> list = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.password, r.company_name " +
                     "FROM users u JOIN recruiters r ON u.id = r.id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void deleteRecruiter(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Recruiter mapRow(ResultSet rs) throws SQLException {
        return new Recruiter(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("company_name")
        );
    }
}