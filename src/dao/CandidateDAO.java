package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Candidate;
import util.DBConnection;

/**
 * CandidateDAO — handles SQL for the candidates table, and coordinates
 * with UserDAO to insert into the shared users table first.
 *
 * WHY insertCandidate DOES TWO INSERTS:
 * A Candidate spans 2 tables (users + candidates). The DAO layer hides that
 * complexity — the caller (service/main) just calls insertCandidate(candidate)
 * once and doesn't need to know 2 tables are involved underneath.
 */
public class CandidateDAO {

    private UserDAO userDAO = new UserDAO();

    public int insertCandidate(Candidate c) throws SQLException {
        // Step 1: insert shared fields into users, get back the new id
        int newId = userDAO.insertUser(c);

        // Step 2: insert candidate-only fields into candidates, using that same id
        String sql = "INSERT INTO candidates (id, skills, experience_years) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newId);
            ps.setString(2, c.getSkills());
            ps.setInt(3, c.getExperienceYears());

            ps.executeUpdate();
        }

        return newId;
    }

    /**
     * Fetches one candidate by id — joins users + candidates to build the full object.
     * WHY A JOIN: a Candidate's full data is split across 2 tables; we need both
     * halves to reconstruct one complete Java object.
     */
    public Candidate getCandidateById(int id) throws SQLException {
        String sql = "SELECT u.id, u.name, u.email, u.password, c.skills, c.experience_years " +
                     "FROM users u JOIN candidates c ON u.id = c.id WHERE u.id = ?";

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

    /** Fetches every candidate in the system. Used later for ranking (Phase 6). */
    public List<Candidate> getAllCandidates() throws SQLException {
        List<Candidate> list = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.password, c.skills, c.experience_years " +
                     "FROM users u JOIN candidates c ON u.id = c.id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateCandidate(Candidate c) throws SQLException {
        // Update users table (shared fields) and candidates table (specific fields) separately.
        String userSql = "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
        String candSql = "UPDATE candidates SET skills = ?, experience_years = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps1 = conn.prepareStatement(userSql)) {
                ps1.setString(1, c.getName());
                ps1.setString(2, c.getEmail());
                ps1.setString(3, c.getPassword());
                ps1.setInt(4, c.getId());
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(candSql)) {
                ps2.setString(1, c.getSkills());
                ps2.setInt(2, c.getExperienceYears());
                ps2.setInt(3, c.getId());
                ps2.executeUpdate();
            }
        }
    }

    public void deleteCandidate(int id) throws SQLException {
        // ON DELETE CASCADE on candidates.id means deleting from `users`
        // automatically deletes the matching `candidates` row too.
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Candidate mapRow(ResultSet rs) throws SQLException {
        return new Candidate(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("skills"),
            rs.getInt("experience_years")
        );
    }
}