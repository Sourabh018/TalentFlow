package service;

import java.sql.SQLException;
import java.util.List;
import dao.CandidateDAO;
import model.Candidate;
import util.ValidationUtil;

/**
 * CandidateService — sits between main/console and CandidateDAO.
 *
 * WHY THIS LAYER EXISTS (and why this logic can't just live in CandidateDAO):
 * CandidateDAO's ONLY job is talking to SQL correctly. If we put "is this email
 * valid" checks inside CandidateDAO, the DAO now has two responsibilities
 * (SQL correctness AND business rules) — meaning a change to a validation rule
 * risks breaking SQL code, and vice versa. Single Responsibility Principle.
 *
 * WHY NOT IN main/App.java EITHER:
 * If validation lived in App.java, every new entry point (a future GUI, a future
 * REST API, a different console menu) would need to re-implement the SAME rules.
 * Putting it in the service layer means ANY caller gets the same guaranteed rules.
 *
 * REAL-WORLD ANALOGY:
 * Like a form-checking clerk at a government office who reviews your application
 * for completeness and correctness BEFORE it's handed to the records department
 * (DAO) to be filed. The records department doesn't re-check your handwriting —
 * it trusts the clerk already did that.
 */
public class CandidateService {

    private CandidateDAO candidateDAO = new CandidateDAO();

    public int registerCandidate(Candidate c) throws InvalidDataException, SQLException {
        validate(c);
        return candidateDAO.insertCandidate(c);
    }

    public Candidate getCandidateById(int id) throws SQLException {
        return candidateDAO.getCandidateById(id);
    }

    public List<Candidate> getAllCandidates() throws SQLException {
        return candidateDAO.getAllCandidates();
    }

    public void updateCandidate(Candidate c) throws InvalidDataException, SQLException {
        validate(c);
        candidateDAO.updateCandidate(c);
    }

    public void deleteCandidate(int id) throws SQLException {
        candidateDAO.deleteCandidate(id);
    }

    /**
     * All candidate validation rules live HERE, in one place.
     * WHY THROW INSTEAD OF RETURN boolean:
     * A boolean return ("false = invalid") forces the caller to remember to
     * check it every time, and gives no detail on WHICH rule failed.
     * Throwing forces the caller to handle failure explicitly, and the exception
     * message tells them exactly what was wrong — better for both console output
     * and a future UI showing the user a real error.
     */
    private void validate(Candidate c) throws InvalidDataException {
        if (!ValidationUtil.isNonEmpty(c.getName())) {
            throw new InvalidDataException("Candidate name cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(c.getEmail())) {
            throw new InvalidDataException("Invalid email format: " + c.getEmail());
        }
        if (!ValidationUtil.isNonEmpty(c.getPassword())) {
            throw new InvalidDataException("Password cannot be empty.");
        }
        if (!ValidationUtil.isNonNegative(c.getExperienceYears())) {
            throw new InvalidDataException("Experience years cannot be negative.");
        }
    }
}