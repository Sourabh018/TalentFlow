package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import model.Resume;
import util.DBConnection;

/**
 * ResumeDAO — handles SQL for the resumes table, including hand-rolled
 * JSON serialize/deserialize for the skill_scores column.
 *
 * WHY HAND-ROLL JSON INSTEAD OF A LIBRARY (like Jackson/Gson):
 * Our data shape is always exactly Map<String,Integer> — flat, no nesting,
 * no arrays. A full JSON library is built for arbitrary nested object graphs;
 * pulling one in here would be a dependency to explain in an interview for
 * a problem this small. Writing it by hand also means there's NOTHING
 * happening that you can't explain line-by-line.
 */
public class ResumeDAO {

    public int insertResume(Resume resume) throws SQLException {
        String sql = "INSERT INTO resumes (candidate_id, skill_scores) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, resume.getCandidateId());
            ps.setString(2, toJson(resume.getSkillScores()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public Resume getResumeByCandidateId(int candidateId) throws SQLException {
        String sql = "SELECT * FROM resumes WHERE candidate_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, candidateId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Resume mapRow(ResultSet rs) throws SQLException {
        return new Resume(
            rs.getInt("id"),
            rs.getInt("candidate_id"),
            fromJson(rs.getString("skill_scores"))
        );
    }

    /**
     * Converts {"Java"=8, "SQL"=7} into the string: {"Java":8,"SQL":7}
     * WHY THIS EXACT FORMAT: it's valid JSON syntax MySQL's JSON column type
     * will accept and validate, even though we're not using a JSON library to build it.
     */
    private String toJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Parses a JSON string like {"Java":8,"SQL":7} back into a HashMap.
     * WHY MANUAL STRING PARSING IS SAFE HERE: we control BOTH the writer (toJson above)
     * AND the reader (this method) — the format is guaranteed flat, no nested objects,
     * no escaped quotes inside skill names. A general-purpose JSON parser has to handle
     * cases we will never actually produce.
     */
    private Map<String, Integer> fromJson(String json) {
        Map<String, Integer> map = new HashMap<>();

        // Strip the outer { and }
        String trimmed = json.trim();
        trimmed = trimmed.substring(1, trimmed.length() - 1); // remove { }

        if (trimmed.isEmpty()) {
            return map; // empty skill map, e.g. "{}"
        }

        // Split on commas BETWEEN entries — safe here because no skill name or
        // value in our controlled format will ever contain a literal comma.
        String[] pairs = trimmed.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim().replaceAll("^\"|\"$", ""); // strip surrounding quotes
            int value = Integer.parseInt(keyValue[1].trim());
            map.put(key, value);
        }

        return map;
    }
}