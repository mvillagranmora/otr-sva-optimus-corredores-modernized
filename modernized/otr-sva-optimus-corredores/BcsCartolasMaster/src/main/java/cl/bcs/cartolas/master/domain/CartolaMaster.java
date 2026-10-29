package cl.bcs.cartolas.master.domain;

import java.util.Objects;

/**
 * Valor inmutable que representa una cartola generada en bytes con su nombre de archivo.
 *
 * <p>Equivalente modernizado de {@code CartolaMaster} legacy (mismo contrato).</p>
 */
public final class CartolaMaster {

    private final String nombreArchivo;
    private final byte[] bytes;

    public CartolaMaster(final String nombreArchivo, final byte[] bytes) {
        this.nombreArchivo = Objects.requireNonNull(nombreArchivo, "nombreArchivo es requerido");
        this.bytes = Objects.requireNonNull(bytes, "bytes es requerido");
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getSizeBytes() {
        return bytes.length;
    }
}
