package service;

import java.util.Map;

/**
 * ScoringService — computes a numeric match score between a Candidate's
 * skill map and a Job's required skills.
 *
 * WHY THIS LOGIC ISN'T INSIDE ResumeDAO OR Resume ITSELF:
 * Scoring is a business RULE (how do we define "good match"), not a data
 * storage concern. The DAO shouldn't know what scoring even means — it just
 * stores/retrieves the map. Putting scoring logic in the model class (Resume)
 * would also be wrong: a model class represents DATA, not BEHAVIOR/RULES —
 * mixing the two makes the model harder to reason about and reuse.
 *
 * REAL-WORLD ANALOGY:
 * Like a loan officer's credit-scoring formula — the bank's record-keeping
 * system (DAO) just stores your financial history; the SCORING FORMULA
 * (this service) is a separate, explicit piece of business logic applied
 * ON TOP of that stored data.
 */
public class ScoringService {

    /**
     * Computes a match score out of 100.
     *
     * ALGORITHM (explainable in one breath to an interviewer):
     * 1. Split the job's comma-separated required skills into a list.
     * 2. For each required skill, check if the candidate has it in their skill map.
     * 3. If yes, add the candidate's proficiency score (1-10) for that skill.
     * 4. If no, add 0 — candidate gets no credit for a skill they don't have.
     * 5. Normalize: divide by (number of required skills * 10) and multiply by 100,
     *    so the result is always comparable on a 0-100 scale regardless of
     *    how many skills a job requires.
     *
     * WHY NORMALIZE INSTEAD OF JUST SUMMING RAW SCORES:
     * A job requiring 2 skills and a job requiring 5 skills would otherwise produce
     * scores on different scales, making cross-job comparison meaningless.
     * Normalizing to 0-100 means "85" always means the same thing regardless
     * of how many skills were required.
     */
    public int computeMatchScore(Map<String, Integer> candidateSkills, String requiredSkillsCsv) {
        if (requiredSkillsCsv == null || requiredSkillsCsv.trim().isEmpty()) {
            return 0;
        }

        String[] requiredSkills = requiredSkillsCsv.split(",");
        int totalEarned = 0;
        int maxPossible = requiredSkills.length * 10; // each skill worth max 10 points

        for (String skill : requiredSkills) {
            String trimmedSkill = skill.trim();

            // candidateSkills keys might differ in case ("java" vs "Java") —
            // we normalize both sides to be forgiving about that.
            int proficiency = getProficiencyCaseInsensitive(candidateSkills, trimmedSkill);
            totalEarned += proficiency;
        }

        if (maxPossible == 0) {
            return 0;
        }

        // (totalEarned / maxPossible) * 100, using double math to avoid integer division
        // truncating to 0 (e.g. 7/100 in int math = 0, which would be wrong).
        return (int) Math.round((totalEarned * 100.0) / maxPossible);
    }

    private int getProficiencyCaseInsensitive(Map<String, Integer> candidateSkills, String skillName) {
        for (Map.Entry<String, Integer> entry : candidateSkills.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(skillName)) {
                return entry.getValue();
            }
        }
        return 0; // candidate doesn't have this skill at all
    }
}