package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Job;
import util.DBConnection;

/**
 * JobDAO — handles SQL for the jobs table.
 * Simpler than Candidate/Recruiter DAOs since Job doesn't extend User —
 * it's a single-table entity, just one insert/select needed.
 */
public class JobDAO {

    public int insertJob(Job job) throws SQLException {
        String sql = "INSERT INTO jobs (recruiter_id, title, required_skills, salary, min_experience) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, job.getRecruiterId());
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getRequiredSkills());
            ps.setDouble(4, job.getSalary());
            ps.setInt(5, job.getMinExperience());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public Job getJobById(int id) throws SQLException {
        String sql = "SELECT * FROM jobs WHERE id = ?";

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

    public List<Job> getAllJobs() throws SQLException {
        List<Job> list = new ArrayList<>();
        String sql = "SELECT * FROM jobs";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void deleteJob(int id) throws SQLException {
        String sql = "DELETE FROM jobs WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Job mapRow(ResultSet rs) throws SQLException {
        return new Job(
            rs.getInt("id"),
            rs.getInt("recruiter_id"),
            rs.getString("title"),
            rs.getString("required_skills"),
            rs.getDouble("salary"),
            rs.getInt("min_experience")
        );
    }
}