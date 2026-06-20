package model;

/**
 * CandidateStatus — represents where an Application currently sits in the pipeline.
 *
 * PIPELINE ORDER (one-directional, enforced in ApplicationService, not here):
 * APPLIED -> SHORTLISTED -> INTERVIEW -> SELECTED
 *                                     -> REJECTED
 * REJECTED can actually happen from ANY stage (a candidate can be rejected
 * right after applying, or after the interview) — that's a business rule,
 * which is why it's enforced in the service layer's canTransition() method,
 * not baked into this enum.
 *
 * REAL-WORLD ANALOGY:
 * Like a visa application status tracker — "Submitted" -> "Under Review" ->
 * "Interview Scheduled" -> "Approved"/"Rejected". You can't jump straight
 * from "Submitted" to "Approved" without passing through the steps in between.
 */
public enum CandidateStatus {
    APPLIED,
    SHORTLISTED,
    INTERVIEW,
    SELECTED,
    REJECTED
}