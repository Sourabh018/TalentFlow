package model;

/**
 * Candidate — a User who applies to jobs.
 *
 * WHY "extends User":
 * A Candidate IS-A User (it has name/email/password/role) PLUS extra fields
 * specific to job-seeking (skills, experience, resume link later).
 * Inheritance avoids re-declaring name/email/password here.
 *
 * REAL-WORLD ANALOGY:
 * A Candidate is like a "Job Applicant" — same person-level info as anyone else,
 * but with an extra folder attached: experience, skills, resume.
 */
public class Candidate extends User {

    private String skills;       // comma-separated for now, e.g. "Java,SQL,React" — Phase 5 turns this into structured scoring
    private int experienceYears;

    public Candidate() {
        super();
    }

    // No-id constructor — used when a NEW candidate signs up
    public Candidate(String name, String email, String password, String skills, int experienceYears) {
        super(name, email, password, Role.CANDIDATE);
        this.skills = skills;
        this.experienceYears = experienceYears;
    }

    // Full constructor — used when reading an existing candidate back from the DB
    public Candidate(int id, String name, String email, String password, String skills, int experienceYears) {
        super(id, name, email, password, Role.CANDIDATE);
        this.skills = skills;
        this.experienceYears = experienceYears;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    @Override
    public String toString() {
        return "Candidate{id=" + id + ", name='" + name + "', email='" + email +
               "', skills='" + skills + "', experienceYears=" + experienceYears + "}";
    }
}