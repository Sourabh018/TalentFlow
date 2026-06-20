package model;

/**
 * User — the base class for anyone who logs into TalentFlow.
 *
 * WHY THIS CLASS EXISTS:
 * Candidate and Recruiter share common fields (id, name, email, password, role).
 * Without this base class, we'd duplicate those 5 fields + their getters/setters
 * in BOTH Candidate and Recruiter — copy-paste duplication, and a bug fixed in one
 * place wouldn't be fixed in the other.
 *
 * REAL-WORLD ANALOGY:
 * Think of "User" like an "Employee" base record at a company — every employee
 * (manager or intern) has a name, ID, email. What makes them DIFFERENT
 * (a manager has direct reports, an intern has a mentor) is added in the
 * subclass, not duplicated at the base.
 *
 * WHY 3 CONSTRUCTORS:
 * 1. No-arg: required by some frameworks/reflection patterns, and useful when
 *    you build the object piece-by-piece via setters.
 * 2. No-id: used when CREATING a new user — the DB will assign the id (auto-increment),
 *    so we don't have one yet at object-creation time.
 * 3. Full (with id): used when READING a user back FROM the DB — the DB row already
 *    has an id, and we reconstruct the Java object to match exactly.
 */
public class User {

    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected Role role;

    // 1. No-arg constructor
    public User() {
    }

    // 2. No-id constructor (for creating a brand new user, before DB assigns id)
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // 3. Full constructor (for reconstructing a user FROM a DB row, id already exists)
    public User(int id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // --- Getters and setters ---
    // Encapsulation: fields are protected (visible to subclasses), but external
    // code must go through these methods. This lets us add validation later
    // (Phase 3) WITHOUT changing every place that touches these fields.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', role=" + role + "}";
    }
}