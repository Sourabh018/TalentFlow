package model;

import java.time.LocalDateTime;

/**
 * Interview — links 1:1 to an Application, representing one scheduled interview event.
 *
 * WHY 1:1 WITH Application, NOT WITH Candidate DIRECTLY:
 * A Candidate could apply to MULTIPLE jobs, each with their own interview.
 * Linking Interview to Candidate directly would lose the context of WHICH
 * application this interview belongs to. Linking to Application (which already
 * ties candidate+job together) keeps that context intact with one foreign key.
 *
 * REAL-WORLD ANALOGY:
 * Like a single calendar booking tied to one specific case file, not to the
 * person in general — if the same person has 3 open case files, each needs
 * its own booking, not one shared one.
 */
public class Interview {

    private int id;
    private int applicationId;
    private LocalDateTime scheduledAt;
    private InterviewResult result;

    public Interview() {
    }

    // No-id constructor — used when newly scheduling an interview
    public Interview(int applicationId, LocalDateTime scheduledAt) {
        this.applicationId = applicationId;
        this.scheduledAt = scheduledAt;
        this.result = InterviewResult.PENDING; // every new interview starts PENDING, no exceptions
    }

    // Full constructor — used when reconstructing an existing Interview from the DB
    public Interview(int id, int applicationId, LocalDateTime scheduledAt, InterviewResult result) {
        this.id = id;
        this.applicationId = applicationId;
        this.scheduledAt = scheduledAt;
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public InterviewResult getResult() {
        return result;
    }

    public void setResult(InterviewResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Interview{id=" + id + ", applicationId=" + applicationId +
               ", scheduledAt=" + scheduledAt + ", result=" + result + "}";
    }
}