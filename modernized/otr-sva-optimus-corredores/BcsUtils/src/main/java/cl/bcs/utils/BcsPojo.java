package cl.bcs.utils;

/**
 * UTIL-21: base class of the platform POJOs whose {@code toString()} is their JSON (legacy {@code BcsPojo.java:5-7}).
 */
public abstract class BcsPojo {

    /** {@code Utils.printJson(this, false)}. */
    @Override
    public String toString() {
        return Utils.printJson(this, false);
    }
}
