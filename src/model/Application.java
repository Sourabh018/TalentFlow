package model;

import java.time.LocalDateTime;

/**
 * Application — the JUNCTION entity linking a Candidate to a Job.
 *
 * WHY THIS NEEDS ITS OWN CLASS (not just a Candidate-Job pairing in a list):
 * A Candidate can apply to MANY Jobs, and a Job can receive applications from
 * MANY Candidates — that's a many-to-many relationship. You CANNOT represent
 * many-to-many directly with foreign keys on either Candidate or Job alone
 * (a Job can't store "candidate_id" as a single column if 50 candidates apply).
 * The fix is a JUNCTION TABLE/entity that sits in between, holding both
 * foreign keys PLUS relationship-specific data (status, applied date) that
 * belongs to neither Candidate nor Job individually — it belongs to the
 * RELATIONSHIP between them.
 *
 * REAL-WORLD ANALOGY:
 * Think of enrollment records at a university — a Student can enroll in many
 * Courses, a Course has many Students. The "Enrollment" record (not Student,
 * not Course) is what holds the grade and enrollment date, because that data
 * is about THIS SPECIFIC pairing, not about the student or course alone.
 */
public class Application {

    private int id;
    private int candidateId;
    private int jobId;
    private CandidateStatus status;
    private LocalDateTime appliedAt;

    public Application() {
    }

    // No-id constructor — used when a candidate NEWLY applies to a job
    public Application(int candidateId, int jobId) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.status = CandidateStatus.APPLIED; // every new application starts here, no exceptions
        this.appliedAt = LocalDateTime.now();
    }

    // Full constructor — used when reconstructing an existing Application from the DB
    public Application(int id, int candidateId, int jobId, CandidateStatus status, LocalDateTime appliedAt) {
        this.id = id;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    @Override
    public String toString() {
        return "Application{id=" + id + ", candidateId=" + candidateId + ", jobId=" + jobId +
               ", status=" + status + ", appliedAt=" + appliedAt + "}";
    }
}