package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import model.Interview;
import model.InterviewResult;
import util.DBConnection;

/**
 * InterviewDAO — raw SQL for the interviews table.
 * Same patterns as every prior DAO.
 */
public class InterviewDAO {

    public int insertInterview(Interview interview) throws SQLException {
        String sql = "INSERT INTO interviews (application_id, scheduled_at, result) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, interview.getApplicationId());
            ps.setTimestamp(2, Timestamp.valueOf(interview.getScheduledAt()));
            ps.setString(3, interview.getResult().name());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public Interview getInterviewByApplicationId(int applicationId) throws SQLException {
        String sql = "SELECT * FROM interviews WHERE application_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Narrow, single-purpose update — same reasoning as ApplicationDAO.updateStatus
     * in Phase 4: result is the one field that changes after creation, so we don't
     * risk a generic update accidentally touching scheduled_at or application_id.
     */
    public void updateResult(int interviewId, InterviewResult result) throws SQLException {
        String sql = "UPDATE interviews SET result = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, result.name());
            ps.setInt(2, interviewId);
            ps.executeUpdate();
        }
    }

    private Interview mapRow(ResultSet rs) throws SQLException {
        return new Interview(
            rs.getInt("id"),
            rs.getInt("application_id"),
            rs.getTimestamp("scheduled_at").toLocalDateTime(),
            InterviewResult.valueOf(rs.getString("result"))
        );
    }
}