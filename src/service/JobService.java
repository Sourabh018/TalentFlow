package service;

import java.sql.SQLException;
import java.util.List;
import dao.JobDAO;
import model.Job;
import util.ValidationUtil;

/**
 * JobService — same role as CandidateService, but for Job rules.
 * Mirrors the same pattern: validate() before any DAO call.
 */
public class JobService {

    private JobDAO jobDAO = new JobDAO();

    public int postJob(Job job) throws InvalidDataException, SQLException {
        validate(job);
        return jobDAO.insertJob(job);
    }

    public Job getJobById(int id) throws SQLException {
        return jobDAO.getJobById(id);
    }

    public List<Job> getAllJobs() throws SQLException {
        return jobDAO.getAllJobs();
    }

    public void deleteJob(int id) throws SQLException {
        jobDAO.deleteJob(id);
    }

    private void validate(Job job) throws InvalidDataException {
        if (!ValidationUtil.isNonEmpty(job.getTitle())) {
            throw new InvalidDataException("Job title cannot be empty.");
        }
        if (!ValidationUtil.isNonEmpty(job.getRequiredSkills())) {
            throw new InvalidDataException("Job must list required skills.");
        }
        if (!ValidationUtil.isPositive(job.getSalary())) {
            throw new InvalidDataException("Salary must be greater than 0.");
        }
        if (!ValidationUtil.isNonNegative(job.getMinExperience())) {
            throw new InvalidDataException("Minimum experience cannot be negative.");
        }
    }
}