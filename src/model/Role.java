package model;

/**
 * Role — defines what kind of user this is.
 *
 * WHY ENUM, NOT STRING:
 * If we used a String "candidate"/"recruiter", nothing stops someone from
 * typing "Candidate", "CANDIDATE", "canddiate" (typo) — the compiler won't catch it.
 * An enum is a closed, fixed set of valid values, checked at COMPILE time, not runtime.
 *
 * REAL-WORLD ANALOGY:
 * Think of this like a badge color at a conference — Speaker, Attendee, Staff.
 * You can't invent a new badge color on the spot; the set is fixed by the organizers.
 */
public enum Role {
    CANDIDATE,
    RECRUITER
}