package model;

/**
 * InterviewResult — outcome of a scheduled interview.
 *
 * WHY A SEPARATE ENUM FROM CandidateStatus:
 * CandidateStatus tracks the APPLICATION's pipeline position (APPLIED, INTERVIEW, etc).
 * InterviewResult tracks the OUTCOME of one specific interview event (did they pass?).
 * These answer different questions — conflating them would mean, for example,
 * "PENDING" interview result somehow also has to double as an application status,
 * which breaks the clean separation Phase 4 already established.
 *
 * REAL-WORLD ANALOGY:
 * Like a job interview's outcome slip vs. the candidate's overall file status —
 * the slip says "Passed technical round," the file status says "Currently in
 * Interview stage." Related, but not the same record.
 */
public enum InterviewResult {
    PENDING,
    PASSED,
    FAILED
}