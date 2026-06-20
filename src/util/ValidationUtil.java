package util;

/**
 * ValidationUtil — static helper methods for common validation checks.
 *
 * WHY THESE LIVE HERE, NOT INSIDE CandidateService/JobService DIRECTLY:
 * Email validation logic doesn't belong to "candidate rules" or "job rules" —
 * it's a generic, reusable check. If CandidateService AND a future
 * RecruiterService both need to validate an email, they both call THIS,
 * instead of each writing their own regex (and possibly writing it differently/wrong).
 */
public class ValidationUtil {

    // Simple, practical email regex — not RFC-5322-perfect, but catches real mistakes
    // like missing "@", missing domain, etc. Good enough for an ATS signup form.
    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$";

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches(EMAIL_REGEX);
    }

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isPositive(double value) {
        return value > 0;
    }

    public static boolean isNonNegative(int value) {
        return value >= 0;
    }
}