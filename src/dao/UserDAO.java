package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Role;
import model.User;
import util.DBConnection;

/**
 * UserDAO — handles raw SQL for the users table only.
 *
 * WHY THIS CLASS EXISTS SEPARATE FROM CandidateDAO/RecruiterDAO:
 * Inserting a Candidate is actually TWO inserts: one into `users` (shared fields),
 * one into `candidates` (candidate-only fields). UserDAO owns the first insert.
 * CandidateDAO/RecruiterDAO call into UserDAO rather than duplicating that SQL.
 *
 * REAL-WORLD ANALOGY:
 * Think of UserDAO like the front-desk registration step at a hospital —
 * everyone (patient or visitor) gets a base record (name, ID) created first,
 * regardless of which department (Candidate/Recruiter) they go to next.
 */
public class UserDAO {

    /**
     * Inserts a new row into `users` and returns the auto-generated id.
     * WHY PreparedStatement, NOT Statement + string-concatenated SQL:
     * String concatenation lets a user type `'; DROP TABLE users; --` as their name
     * and it would actually execute as SQL. PreparedStatement treats every "?" value
     * as DATA, never as executable SQL — this is the standard SQL injection defense.
     */
    public int insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

        // try-with-resources: Connection AND PreparedStatement both implement
        // AutoCloseable. Both get closed automatically, even on exception.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name()); // enum -> String for storage

            ps.executeUpdate();

            // RETURN_GENERATED_KEYS lets us pull back the auto-increment id
            // MySQL just assigned, so the Java object can be updated with its real id.
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1; // insert failed or no generated key returned
    }

    /**
     * Fetches a user row by id and maps it to a User object.
     * Used internally by CandidateDAO/RecruiterDAO when reconstructing full objects.
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

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

    /**
     * Maps one row of a ResultSet to a User object.
     * WHY A SEPARATE PRIVATE METHOD:
     * Every SELECT method (getUserById, getAllUsers, etc.) needs this exact
     * same mapping logic. Without this method, that mapping code gets copy-pasted
     * everywhere — and if we add a column later, we'd have to fix it in N places.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role")) // String -> enum
        );
    }
}