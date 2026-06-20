package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Application;
import model.CandidateStatus;
import util.DBConnection;

/**
 * ApplicationDAO — raw SQL for the applications junction table.
 * Same patterns as previous DAOs: PreparedStatement, try-with-resources,
 * RETURN_GENERATED_KEYS for new inserts.
 */
public class ApplicationDAO {

    public int insertApplication(Application app) throws SQLException {
        String sql = "INSERT INTO applications (candidate_id, job_id, status, applied_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, app.getCandidateId());
            ps.setInt(2, app.getJobId());
            ps.setString(3, app.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(app.getAppliedAt()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public Application getApplicationById(int id) throws SQLException {
        String sql = "SELECT * FROM applications WHERE id = ?";

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

    /** All applications for a given Job — this is what Phase 6's RankingService will consume. */
    public List<Application> getApplicationsByJob(int jobId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE job_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jobId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** All applications submitted by a given Candidate. */
    public List<Application> getApplicationsByCandidate(int candidateId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE candidate_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates ONLY the status column.
     * WHY A DEDICATED METHOD instead of a generic updateApplication(app):
     * Status is the ONE field that changes repeatedly through the pipeline's
     * lifetime. candidate_id/job_id/applied_at never change after creation.
     * A narrow method that touches exactly one column is clearer intent and
     * harder to misuse than a generic update that could accidentally overwrite
     * fields that should be immutable.
     */
    public void updateStatus(int applicationId, CandidateStatus newStatus) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setInt(2, applicationId);
            ps.executeUpdate();
        }
    }

    private Application mapRow(ResultSet rs) throws SQLException {
        return new Application(
            rs.getInt("id"),
            rs.getInt("candidate_id"),
            rs.getInt("job_id"),
            CandidateStatus.valueOf(rs.getString("status")),
            rs.getTimestamp("applied_at").toLocalDateTime() // Timestamp -> LocalDateTime
        );
    }
}