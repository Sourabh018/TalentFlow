package main;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import model.Application;
import model.Candidate;
import model.CandidateStatus;
import model.Interview;
import model.InterviewResult;
import model.Job;
import model.Recruiter;
import model.Resume;
import dao.CandidateDAO;
import dao.RecruiterDAO;
import dao.ResumeDAO;
import service.ApplicationService;
import service.CandidateService;
import service.InterviewService;
import service.InvalidDataException;
import service.JobService;
import service.RankingService;
import service.ScoringService;

/**
 * App — Phase 8 console driver.
 *
 * WHAT CHANGED FROM PHASES 0-7:
 * App.java is no longer a list of hardcoded checkpoint calls. It is now a
 * menu loop that calls the EXACT SAME services every checkpoint already used
 * (CandidateService, JobService, ApplicationService, ScoringService via
 * RankingService, InterviewService). No new business logic is added here —
 * this file is pure orchestration: read input, call a service, print output.
 *
 * WHY NO NEW SERVICE/DAO CLASSES WERE ADDED:
 * Two real gaps exist in the current layer (no email-lookup DAO method, no
 * "list pending interviews" query) but adding either would mean touching the
 * DB-access layer mid-phase, which Phase 8's brief explicitly forbids
 * ("orchestration only, no new business logic"). Both gaps are worked around
 * here using only methods that already exist:
 *   - "Login" is a linear scan over getAllCandidates()/getAllRecruiters()
 *     matching email+password in Java, NOT an indexed DB lookup. This is a
 *     known limitation, not a real authentication system (Phase 9 is where
 *     real auth would go, if ever).
 *   - Recording an interview result requires you to already know the
 *     interview's id, because no service method exists to look up "pending
 *     interviews for this application." We print the id right after
 *     scheduling so you can re-enter it later. Clunky, but honest to what
 *     the service layer currently supports.
 *
 * REAL-WORLD ANALOGY:
 * Think of this class as a bank branch's front counter clerk — they don't
 * know how interest is calculated or how a loan gets approved (that's the
 * service layer's job), they just take your request, hand it to the right
 * back-office department, and read the result back to you.
 */
public class App {

    private static final Scanner scanner = new Scanner(System.in);

    // Services — same instances reused for the whole session.
    private static final CandidateService candidateService = new CandidateService();
    private static final JobService jobService = new JobService();
    private static final ApplicationService applicationService = new ApplicationService();
    private static final ScoringService scoringService = new ScoringService();
    private static final RankingService rankingService = new RankingService();
    private static final InterviewService interviewService = new InterviewService();

    // DAOs called directly only where no service method exists for the read
    // we need (email-lookup, resume insert convenience). This is the one
    // place we step around the service layer, and it's confined to this file.
    private static final CandidateDAO candidateDAO = new CandidateDAO();
    private static final RecruiterDAO recruiterDAO = new RecruiterDAO();
    private static final ResumeDAO resumeDAO = new ResumeDAO();

    public static void main(String[] args) {
        System.out.println("=== TalentFlow ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Recruiter\n2. Candidate\n3. Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    recruiterFlow();
                    break;
                case "2":
                    candidateFlow();
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        System.out.println("Goodbye.");
        scanner.close();
    }

    // ---------------------------------------------------------------
    // RECRUITER FLOW
    // ---------------------------------------------------------------

    private static void recruiterFlow() {
        System.out.println("\n-- Recruiter --\n1. Register\n2. Login\n3. Back");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        Recruiter recruiter = null;

        if (choice.equals("1")) {
            recruiter = registerRecruiter();
        } else if (choice.equals("2")) {
            recruiter = loginRecruiter();
        } else {
            return;
        }

        if (recruiter == null) {
            return; // registration/login failed, message already printed
        }

        boolean inSession = true;
        while (inSession) {
            System.out.println("\n-- Recruiter Menu (" + recruiter.getName() + ") --");
            System.out.println("1. Post a job");
            System.out.println("2. View ranked candidates for a job");
            System.out.println("3. Advance an application's status");
            System.out.println("4. Schedule next interview for a job");
            System.out.println("5. Record an interview result");
            System.out.println("6. Logout");
            System.out.print("Choose: ");
            String c = scanner.nextLine().trim();

            try {
                switch (c) {
                    case "1": postJob(recruiter); break;
                    case "2": viewRankedCandidates(); break;
                    case "3": advanceApplicationStatus(); break;
                    case "4": scheduleNextInterview(); break;
                    case "5": recordInterviewResult(); break;
                    case "6": inSession = false; break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (InvalidDataException e) {
                System.out.println("Invalid data: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static Recruiter registerRecruiter() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Company name: ");
        String company = scanner.nextLine().trim();

        Recruiter r = new Recruiter(name, email, password, company);
        try {
            int id = recruiterDAO.insertRecruiter(r);
            r.setId(id);
            System.out.println("Registered. Your recruiter id is " + id);
            return r;
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Linear scan over getAllRecruiters() matching email+password.
     * NOT a real auth system — see class-level comment.
     */
    private static Recruiter loginRecruiter() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            List<Recruiter> all = recruiterDAO.getAllRecruiters();
            for (Recruiter r : all) {
                if (r.getEmail().equalsIgnoreCase(email) && r.getPassword().equals(password)) {
                    System.out.println("Welcome back, " + r.getName());
                    return r;
                }
            }
            System.out.println("No matching recruiter found.");
            return null;
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private static void postJob(Recruiter recruiter) throws InvalidDataException, SQLException {
        System.out.print("Job title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Required skills (comma-separated, e.g. Java,SQL,Spring): ");
        String skills = scanner.nextLine().trim();
        System.out.print("Salary: ");
        double salary = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Minimum experience (years): ");
        int minExp = Integer.parseInt(scanner.nextLine().trim());

        Job job = new Job(recruiter.getId(), title, skills, salary, minExp);
        int id = jobService.postJob(job);
        System.out.println("Job posted. Job id = " + id);
    }

    private static void viewRankedCandidates() throws SQLException {
        System.out.print("Job id: ");
        int jobId = Integer.parseInt(scanner.nextLine().trim());

        List<RankingService.RankedCandidate> ranked = rankingService.rankCandidatesForJob(jobId);
        if (ranked.isEmpty()) {
            System.out.println("No scorable applications for this job yet.");
            return;
        }
        int rank = 1;
        for (RankingService.RankedCandidate rc : ranked) {
            System.out.println(rank + ". " + rc);
            rank++;
        }
    }

    private static void advanceApplicationStatus() throws InvalidDataException, SQLException {
        System.out.print("Application id: ");
        int appId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("New status (SHORTLISTED, INTERVIEW, SELECTED, REJECTED): ");
        String statusStr = scanner.nextLine().trim().toUpperCase();

        CandidateStatus newStatus;
        try {
            newStatus = CandidateStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Not a valid status: " + statusStr);
            return;
        }

        applicationService.advanceStatus(appId, newStatus);
        System.out.println("Status updated.");
    }

    private static void scheduleNextInterview() throws SQLException {
        System.out.print("Job id: ");
        int jobId = Integer.parseInt(scanner.nextLine().trim());

        Queue<Application> queue = interviewService.buildInterviewQueue(jobId);
        if (queue.isEmpty()) {
            System.out.println("No applications currently at INTERVIEW status for this job.");
            return;
        }

        System.out.print("Schedule for how many minutes from now? ");
        int minutes = Integer.parseInt(scanner.nextLine().trim());
        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(minutes);

        // Peek which application is about to be pulled, so we can report its id.
        Application next = queue.peek();
        int interviewId = interviewService.scheduleNext(queue, scheduledAt);

        if (interviewId == -1) {
            System.out.println("Queue was empty.");
            return;
        }

        System.out.println("Scheduled interview id " + interviewId +
            " for application id " + next.getId() + " at " + scheduledAt);
        System.out.println("Remember these two ids — you'll need them to record the result later.");
    }

    private static void recordInterviewResult() throws InvalidDataException, SQLException {
        System.out.print("Interview id: ");
        int interviewId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Application id: ");
        int applicationId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Result (PASSED, FAILED): ");
        String resultStr = scanner.nextLine().trim().toUpperCase();

        InterviewResult result;
        try {
            result = InterviewResult.valueOf(resultStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Not a valid result: " + resultStr);
            return;
        }

        interviewService.recordResult(interviewId, applicationId, result, applicationService);
        System.out.println("Result recorded. Application status auto-advanced.");
    }

    // ---------------------------------------------------------------
    // CANDIDATE FLOW
    // ---------------------------------------------------------------

    private static void candidateFlow() {
        System.out.println("\n-- Candidate --\n1. Register\n2. Login\n3. Back");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        Candidate candidate = null;

        if (choice.equals("1")) {
            candidate = registerCandidate();
        } else if (choice.equals("2")) {
            candidate = loginCandidate();
        } else {
            return;
        }

        if (candidate == null) {
            return;
        }

        boolean inSession = true;
        while (inSession) {
            System.out.println("\n-- Candidate Menu (" + candidate.getName() + ") --");
            System.out.println("1. Browse jobs");
            System.out.println("2. Apply to a job");
            System.out.println("3. Enter/update my skill map (resume)");
            System.out.println("4. Check my application statuses");
            System.out.println("5. Logout");
            System.out.print("Choose: ");
            String c = scanner.nextLine().trim();

            try {
                switch (c) {
                    case "1": browseJobs(); break;
                    case "2": applyToJob(candidate); break;
                    case "3": enterResume(candidate); break;
                    case "4": checkMyApplications(candidate); break;
                    case "5": inSession = false; break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static Candidate registerCandidate() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Skills (comma-separated, just for display — NOT used in scoring): ");
        String skills = scanner.nextLine().trim();
        System.out.print("Experience (years): ");
        int exp = Integer.parseInt(scanner.nextLine().trim());

        Candidate c = new Candidate(name, email, password, skills, exp);
        try {
            int id = candidateService.registerCandidate(c);
            c.setId(id);
            System.out.println("Registered. Your candidate id is " + id);
            System.out.println("Note: skill match SCORING comes from the resume skill map " +
                "(menu option 3), not from the comma-separated skills above.");
            return c;
        } catch (InvalidDataException | SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Linear scan over getAllCandidates() matching email+password.
     * NOT a real auth system — see class-level comment.
     */
    private static Candidate loginCandidate() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            List<Candidate> all = candidateDAO.getAllCandidates();
            for (Candidate c : all) {
                if (c.getEmail().equalsIgnoreCase(email) && c.getPassword().equals(password)) {
                    System.out.println("Welcome back, " + c.getName());
                    return c;
                }
            }
            System.out.println("No matching candidate found.");
            return null;
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private static void browseJobs() throws SQLException {
        List<Job> jobs = jobService.getAllJobs();
        if (jobs.isEmpty()) {
            System.out.println("No jobs posted yet.");
            return;
        }
        for (Job j : jobs) {
            System.out.println(j);
        }
    }

    private static void applyToJob(Candidate candidate) throws SQLException {
        System.out.print("Job id to apply to: ");
        int jobId = Integer.parseInt(scanner.nextLine().trim());

        Job job = jobService.getJobById(jobId);
        if (job == null) {
            System.out.println("No job with that id.");
            return;
        }

        int appId = applicationService.apply(candidate.getId(), jobId);
        System.out.println("Applied. Application id = " + appId + " (status: APPLIED)");
    }

    /**
     * Manual skill-map entry, looped until the candidate types "done".
     * This is what the original brief loosely calls "upload resume" — there
     * is no file parsing here (that's optional Phase 9). Calling it "upload"
     * to anyone outside this conversation would be inaccurate.
     */
    private static void enterResume(Candidate candidate) throws SQLException {
        Map<String, Integer> skillScores = new HashMap<>();
        System.out.println("Enter skills one at a time. Type 'done' as the skill name to finish.");

        while (true) {
            System.out.print("Skill name (or 'done'): ");
            String skill = scanner.nextLine().trim();
            if (skill.equalsIgnoreCase("done")) {
                break;
            }
            System.out.print("Proficiency (1-10) for " + skill + ": ");
            int prof = Integer.parseInt(scanner.nextLine().trim());
            skillScores.put(skill, prof);
        }

        if (skillScores.isEmpty()) {
            System.out.println("No skills entered, nothing saved.");
            return;
        }

        Resume existing = resumeDAO.getResumeByCandidateId(candidate.getId());
        if (existing != null) {
            // No updateResume() method exists on ResumeDAO — only insert and read.
            // Flagging this rather than silently working around it: re-running
            // this menu option for a candidate who already has a resume on file
            // will currently throw a duplicate-key SQLException, because the
            // resumes table has one row per candidate_id. Fixing this properly
            // means adding ResumeDAO.updateResume() — that's new DAO behavior,
            // which Phase 8's brief says not to add. Flagging it instead of
            // pretending it works.
            System.out.println("You already have a resume on file. Updating an existing " +
                "resume isn't supported yet (ResumeDAO has no update method) — this would fail.");
            return;
        }

        Resume resume = new Resume(candidate.getId(), skillScores);
        int id = resumeDAO.insertResume(resume);
        System.out.println("Resume saved (id=" + id + "). Skill map: " + skillScores);
    }

    private static void checkMyApplications(Candidate candidate) throws SQLException {
        List<Application> apps = applicationService.getApplicationsByCandidate(candidate.getId());
        if (apps.isEmpty()) {
            System.out.println("You haven't applied to any jobs yet.");
            return;
        }
        for (Application a : apps) {
            System.out.println(a);
        }
    }
}