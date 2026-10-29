package cl.bcs.filters.port.out;

/** Port — SSO CSRF state persistence (RULE-046). */
public interface SsoStateRepository {

    /** Persists a generated UUID state token. */
    void storeState(String state);

    /**
     * Validates that the state was previously stored (not a replay / forgery)
     * and deletes it atomically (one-time use).
     *
     * @return true if state was found and consumed; false otherwise
     */
    boolean validateAndConsumeState(String state);
}
