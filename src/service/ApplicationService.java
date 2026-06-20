package service;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import dao.ApplicationDAO;
import model.Application;
import model.CandidateStatus;

/**
 * ApplicationService — owns the pipeline's state machine rules.
 *
 * WHY THE TRANSITION MAP LIVES HERE, NOT IN THE ENUM OR THE DAO:
 * The DAO must remain "dumb" — it executes whatever SQL it's told to, no
 * business judgment. The enum is just a list of valid states. The DECISION
 * of "is APPLIED -> SELECTED allowed" is a business rule, and business rules
 * belong in the service layer, where they're isolated and unit-testable
 * without touching SQL or a database at all.
 *
 * REAL-WORLD ANALOGY:
 * Like an airport boarding pass scanner — it doesn't just check if your gate
 * exists (the enum equivalent), it checks if you're allowed to board THIS
 * flight from where you currently are in the process (security cleared?
 * already boarded once?). That gate logic is a rule layer above "which gates exist."
 */
public class ApplicationService {

    private ApplicationDAO applicationDAO = new ApplicationDAO();

    // EnumMap: a Map implementation specifically optimized for enum keys —
    // faster and more memory-efficient than a regular HashMap<CandidateStatus, ...>
    // because it stores values in an array ordered by the enum's declaration order.
    private static final Map<CandidateStatus, Set<CandidateStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(CandidateStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CandidateStatus.APPLIED, EnumSet.of(CandidateStatus.SHORTLISTED, CandidateStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(CandidateStatus.SHORTLISTED, EnumSet.of(CandidateStatus.INTERVIEW, CandidateStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(CandidateStatus.INTERVIEW, EnumSet.of(CandidateStatus.SELECTED, CandidateStatus.REJECTED));
        // SELECTED and REJECTED are terminal states — no outgoing transitions.
        // Not adding them to the map means ALLOWED_TRANSITIONS.get(SELECTED) returns null,
        // which canTransition() below treats as "nothing allowed from here."
    }

    public int apply(int candidateId, int jobId) throws SQLException {
        Application app = new Application(candidateId, jobId);
        return applicationDAO.insertApplication(app);
    }

    public List<Application> getApplicationsByJob(int jobId) throws SQLException {
        return applicationDAO.getApplicationsByJob(jobId);
    }

    public List<Application> getApplicationsByCandidate(int candidateId) throws SQLException {
        return applicationDAO.getApplicationsByCandidate(candidateId);
    }

    /**
     * Advances an application's status — but ONLY if the transition is legal.
     * Throws InvalidDataException (reusing Phase 3's exception — a status jump
     * IS a form of invalid data) if the move isn't allowed.
     */
    public void advanceStatus(int applicationId, CandidateStatus newStatus) throws SQLException, InvalidDataException {
        Application app = applicationDAO.getApplicationById(applicationId);
        if (app == null) {
            throw new InvalidDataException("No application found with id: " + applicationId);
        }

        CandidateStatus current = app.getStatus();

        if (!canTransition(current, newStatus)) {
            throw new InvalidDataException(
                "Illegal status transition: " + current + " -> " + newStatus
            );
        }

        applicationDAO.updateStatus(applicationId, newStatus);
    }

    /** Pure logic, no DB/SQL involved — easy to explain and easy to unit test in isolation. */
    private boolean canTransition(CandidateStatus from, CandidateStatus to) {
        Set<CandidateStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}