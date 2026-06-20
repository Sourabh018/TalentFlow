package model;

/**
 * Recruiter — a User who posts jobs and reviews candidates.
 *
 * WHY A SEPARATE CLASS INSTEAD OF A "isRecruiter" BOOLEAN ON USER:
 * A boolean flag approach means User would need to carry candidate-only fields
 * (skills, experience) AND recruiter-only fields (companyName) all in one class,
 * mostly unused depending on which type the row actually is. Subclassing keeps
 * each type's data honest — a Recruiter object literally cannot have a "skills" field
 * because it doesn't exist on this class.
 *
 * REAL-WORLD ANALOGY:
 * Same office building, different badge. A Recruiter's "extra folder" contains
 * company name and which jobs they've posted — not skills or experience.
 */
public class Recruiter extends User {

    private String companyName;

    public Recruiter() {
        super();
    }

    public Recruiter(String name, String email, String password, String companyName) {
        super(name, email, password, Role.RECRUITER);
        this.companyName = companyName;
    }

    public Recruiter(int id, String name, String email, String password, String companyName) {
        super(id, name, email, password, Role.RECRUITER);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "Recruiter{id=" + id + ", name='" + name + "', email='" + email +
               "', companyName='" + companyName + "}";
    }
}