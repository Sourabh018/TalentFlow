package service;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import dao.ApplicationDAO;
import dao.CandidateDAO;
import dao.JobDAO;
import dao.ResumeDAO;
import model.Application;
import model.Candidate;
import model.Job;
import model.Resume;

/**
 * RankingService — sorts all applications for a given Job, best candidate first.
 *
 * WHY THIS IS ITS OWN SERVICE, NOT A METHOD INSIDE ApplicationService:
 * ApplicationService owns the PIPELINE (applying, status transitions) — that's
 * a state-machine concern. Ranking is a completely different concern: "given a
 * snapshot of applications, in what order should a recruiter review them."
 * Mixing the two would mean ApplicationService has two unrelated reasons to
 * change (a new pipeline rule vs a new ranking rule) — Single Responsibility again.
 *
 * WHY RankingResult IS A SEPARATE INNER CLASS (see below), NOT JUST SORTING
 * List<Candidate> DIRECTLY:
 * A ranking needs to display the SCORE alongside each candidate (a recruiter
 * wants to see "Rahul — 70/100", not just a sorted list of names with the
 * number hidden). Candidate itself doesn't have a score field (and shouldn't —
 * score is specific to ONE job, not a permanent property of the candidate).
 * So we pair Candidate + score + experience together just for this result.
 *
 * REAL-WORLD ANALOGY:
 * Like a leaderboard screen after a coding contest — it doesn't just show
 * names sorted by some hidden internal number, it explicitly displays
 * "Name — Score — Tiebreaker stat" so a viewer can SEE why the order is what it is.
 */
public class RankingService {

    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private CandidateDAO candidateDAO = new CandidateDAO();
    private ResumeDAO resumeDAO = new ResumeDAO();
    private JobDAO jobDAO = new JobDAO();
    private ScoringService scoringService = new ScoringService();

    /**
     * RankedCandidate — a small data holder pairing a Candidate with their
     * computed score and experience FOR ONE SPECIFIC JOB.
     * Declared as a static nested class because it has no meaning outside
     * the context of "a ranking result produced by RankingService."
     */
    public static class RankedCandidate {
        private Candidate candidate;
        private int matchScore;
        private int experienceYears;

        public RankedCandidate(Candidate candidate, int matchScore, int experienceYears) {
            this.candidate = candidate;
            this.matchScore = matchScore;
            this.experienceYears = experienceYears;
        }

        public Candidate getCandidate() {
            return candidate;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public int getExperienceYears() {
            return experienceYears;
        }

        @Override
        public String toString() {
            return candidate.getName() + " — score: " + matchScore + "/100, experience: " + experienceYears + "yrs";
        }
    }

    /**
     * Returns every candidate who applied to jobId, ranked best-to-worst.
     */
    public List<RankedCandidate> rankCandidatesForJob(int jobId) throws SQLException {
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("No job found with id: " + jobId);
        }

        List<Application> applications = applicationDAO.getApplicationsByJob(jobId);

        return applications.stream()
            .map(app -> buildRankedCandidate(app, job))
            .filter(rc -> rc != null) // skip any candidate whose resume/data couldn't be loaded
            .sorted(
                // Comparator.comparing(...).reversed() = descending by matchScore,
                // .thenComparing(...).reversed() = within equal matchScore, descending by experience.
                // WHY thenComparing AND NOT a second separate sort: a second separate sort would
                // destroy the first sort's ordering. thenComparing only breaks TIES from the
                // first comparator — it doesn't override cases where matchScore already differs.
                Comparator.comparingInt(RankedCandidate::getMatchScore).reversed()
                    .thenComparing(Comparator.comparingInt(RankedCandidate::getExperienceYears).reversed())
            )
            .collect(Collectors.toList());
    }

    /** Builds one RankedCandidate by pulling together Candidate + Resume + computing score. Returns null if data is missing. */
    private RankedCandidate buildRankedCandidate(Application app, Job job) {
        try {
            Candidate candidate = candidateDAO.getCandidateById(app.getCandidateId());
            Resume resume = resumeDAO.getResumeByCandidateId(app.getCandidateId());

            if (candidate == null || resume == null) {
                return null; // candidate has no resume on file yet — can't score them
            }

            int score = scoringService.computeMatchScore(resume.getSkillScores(), job.getRequiredSkills());
            return new RankedCandidate(candidate, score, candidate.getExperienceYears());

        } catch (SQLException e) {
            return null;
        }
    }
}