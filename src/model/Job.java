package model;

/**
 * Job — a job posting created by a Recruiter.
 *
 * WHY THIS DOESN'T EXTEND USER:
 * A Job isn't a person — it's a posting. It HAS-A relationship to a Recruiter
 * (a job belongs to the recruiter who posted it) rather than IS-A relationship.
 * This is the difference between inheritance (Candidate IS-A User) and
 * association (Job HAS-A recruiterId).
 *
 * REAL-WORLD ANALOGY:
 * A Job posting is like a notice pinned on a board by an HR person — the notice
 * itself isn't a person, it just references WHO posted it (recruiterId).
 */
public class Job {

    private int id;
    private int recruiterId;       // foreign key — links this job to the Recruiter who posted it
    private String title;
    private String requiredSkills; // comma-separated for now, e.g. "Java,SQL,Spring" — scored against in Phase 5
    private double salary;
    private int minExperience;

    public Job() {
    }

    // No-id constructor — used when a recruiter creates a NEW job posting
    public Job(int recruiterId, String title, String requiredSkills, double salary, int minExperience) {
        this.recruiterId = recruiterId;
        this.title = title;
        this.requiredSkills = requiredSkills;
        this.salary = salary;
        this.minExperience = minExperience;
    }

    // Full constructor — used when reading an existing job back from the DB
    public Job(int id, int recruiterId, String title, String requiredSkills, double salary, int minExperience) {
        this.id = id;
        this.recruiterId = recruiterId;
        this.title = title;
        this.requiredSkills = requiredSkills;
        this.salary = salary;
        this.minExperience = minExperience;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(int recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public int getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(int minExperience) {
        this.minExperience = minExperience;
    }

    @Override
    public String toString() {
        return "Job{id=" + id + ", recruiterId=" + recruiterId + ", title='" + title +
               "', requiredSkills='" + requiredSkills + "', salary=" + salary +
               ", minExperience=" + minExperience + "}";
    }
}