package cl.bcs.utils;

/**
 * UTIL-41: plugin metadata contract (legacy {@code JavaCallMetadata.java:3-17}).
 *
 * <p>Legacy defect kept as pinned: {@link #metadata} is an interface constant, so ONE {@code String[4]} is shared by
 * every implementation and every caller of {@link #getMetadata()}.
 */
@FunctionalInterface
public interface JavaCallMetadata {

    String[] metadata = new String[4];

    void setMetadata(Object in, Object out);

    default String[] getMetadata() {
        return metadata;
    }
}
