package service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import dao.ApplicationDAO;
import dao.InterviewDAO;
import model.Application;
import model.CandidateStatus;
import model.Interview;
import model.InterviewResult;

/**
 * InterviewService — builds a FIFO queue of applications ready for interview,
 * schedules them one by one, and records results.
 *
 * WHY Queue<Application> AND NOT JUST A SORTED List<Application>:
 * A List would ALSO work for "process in order" — but Queue communicates INTENT
 * directly: this collection is meant to be consumed front-to-back, one at a time,
 * via poll(), not randomly accessed by index. Using the right collection type
 * for the right access pattern is itself something worth explaining — a List
 * lets you do list.get(5) which makes no conceptual sense for "next person up."
 *
 * WHY WE REBUILD THE QUEUE FRESH EACH TIME INSTEAD OF KEEPING IT AS A FIELD:
 * If InterviewService held the queue as instance state, it would go stale the
 * moment a NEW application reaches INTERVIEW status elsewhere in the app —
 * we'd need complex synchronization to keep it updated. Rebuilding from the DB
 * (ordered by applied_at) every time guarantees it always reflects current truth.
 *
 * REAL-WORLD ANALOGY:
 * Like a take-a-number system at a clinic — the queue isn't a physical object
 * staff carry around; it's just "whoever has been waiting longest goes next,"
 * recomputed from arrival timestamps each time the front desk calls the next patient.
 */
public class InterviewService {

    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private InterviewDAO interviewDAO = new InterviewDAO();

    /**
     * Builds a FIFO queue of every application currently sitting at INTERVIEW status
     * for a given job, ordered by who applied earliest.
     */
    public Queue<Application> buildInterviewQueue(int jobId) throws SQLException {
        List<Application> interviewStageApps = applicationDAO.getApplicationsByJob(jobId).stream()
            .filter(app -> app.getStatus() == CandidateStatus.INTERVIEW)
            .sorted((a, b) -> a.getAppliedAt().compareTo(b.getAppliedAt())) // earliest applied first
            .collect(Collectors.toList());

        // LinkedList implements Queue — offer() adds to the back, poll() removes from the front.
        return new LinkedList<>(interviewStageApps);
    }

    /**
     * Pulls the next application off the queue and schedules an interview for it
     * at the given time. Returns the created Interview's id, or -1 if queue was empty.
     */
    public int scheduleNext(Queue<Application> queue, LocalDateTime scheduledAt) throws SQLException {
        Application next = queue.poll(); // removes AND returns the front of the queue; null if empty
        if (next == null) {
            return -1;
        }

        Interview interview = new Interview(next.getId(), scheduledAt);
        return interviewDAO.insertInterview(interview);
    }

    /**
     * Records the outcome of an interview, and — if PASSED — also advances the
     * underlying Application to SELECTED. If FAILED, advances it to REJECTED.
     * WHY THIS SERVICE TOUCHES ApplicationService'S DOMAIN:
     * An interview result has a direct, mandatory consequence on the pipeline —
     * recording PASSED but leaving the application stuck at INTERVIEW forever
     * would be a real bug, not just an incomplete feature. So this method
     * deliberately closes that loop in one atomic-feeling action.
     */
    public void recordResult(int interviewId, int applicationId, InterviewResult result, ApplicationService applicationService)
            throws SQLException, InvalidDataException {

        interviewDAO.updateResult(interviewId, result);

        if (result == InterviewResult.PASSED) {
            applicationService.advanceStatus(applicationId, CandidateStatus.SELECTED);
        } else if (result == InterviewResult.FAILED) {
            applicationService.advanceStatus(applicationId, CandidateStatus.REJECTED);
        }
        // PENDING result requires no pipeline change — interview just hasn't happened yet.
    }
}