package service;

/**
 * InvalidDataException — thrown when data fails business validation
 * BEFORE it ever reaches the DAO layer.
 *
 * WHY A CUSTOM EXCEPTION INSTEAD OF JUST RuntimeException OR SQLException:
 * SQLException means "the database rejected something." That's a DIFFERENT
 * failure than "the business rules rejected something before we even touched the DB."
 * Mixing the two makes error handling in main/console confusing — you can't tell
 * if a bad-salary error came from MySQL or from our own validation.
 *
 * WHY CHECKED (extends Exception, not RuntimeException):
 * Checked forces every caller to explicitly handle or declare it — for
 * something as important as "rejected invalid candidate data," we want that
 * visible at compile time, not silently ignorable.
 *
 * REAL-WORLD ANALOGY:
 * Like a bank teller rejecting a withdrawal slip with a negative amount
 * BEFORE it ever reaches the vault/ledger system. The rejection happens
 * at the counter, not deep inside the bank's backend.
 */
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}