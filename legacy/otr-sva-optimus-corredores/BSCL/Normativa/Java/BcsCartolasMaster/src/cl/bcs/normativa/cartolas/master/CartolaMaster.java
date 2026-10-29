package cl.bcs.normativa.cartolas.master;

public class CartolaMaster {

    private final byte[] bytes;
    private final String nombreArchivo;

    public CartolaMaster(
            final String nombreArchivo,
            final byte[] bytes) {
        this.nombreArchivo = nombreArchivo;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

}
