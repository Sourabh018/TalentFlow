package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Resume — holds a Candidate's skill-to-proficiency map.
 *
 * WHY THIS IS A SEPARATE ENTITY FROM Candidate:
 * Candidate.skills (Phase 1) was just a raw comma-separated String ("Java,SQL,React") —
 * fine for a quick display, but useless for SCORING, because it has no notion of
 * HOW GOOD the candidate is at each skill. Resume introduces a structured
 * Map<String,Integer> (skill name -> proficiency 1-10) which the ScoringService
 * can actually do math against. Separating it from Candidate also means a
 * candidate could (in a future phase) upload/revise multiple resume versions
 * without us needing to redesign the Candidate table.
 *
 * WHY HashMap<String,Integer> SPECIFICALLY:
 * - String key: skill names aren't a fixed enum (anyone can have any skill).
 * - Integer value: a 1-10 proficiency score is exactly what's needed for scoring math
 *   in ScoringService — not a String, not a boolean "has it / doesn't have it."
 *
 * REAL-WORLD ANALOGY:
 * Like a skills-rating sheet HR attaches to a CV — "Java: 8/10, SQL: 7/10" — not just
 * a plain list of skill names with no sense of depth.
 */
public class Resume {

    private int id;
    private int candidateId;
    private Map<String, Integer> skillScores;

    public Resume() {
        this.skillScores = new HashMap<>();
    }

    // No-id constructor — used when creating a NEW resume
    public Resume(int candidateId, Map<String, Integer> skillScores) {
        this.candidateId = candidateId;
        this.skillScores = skillScores;
    }

    // Full constructor — used when reading an existing resume back from the DB
    public Resume(int id, int candidateId, Map<String, Integer> skillScores) {
        this.id = id;
        this.candidateId = candidateId;
        this.skillScores = skillScores;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public Map<String, Integer> getSkillScores() {
        return skillScores;
    }

    public void setSkillScores(Map<String, Integer> skillScores) {
        this.skillScores = skillScores;
    }

    @Override
    public String toString() {
        return "Resume{id=" + id + ", candidateId=" + candidateId + ", skillScores=" + skillScores + "}";
    }
}