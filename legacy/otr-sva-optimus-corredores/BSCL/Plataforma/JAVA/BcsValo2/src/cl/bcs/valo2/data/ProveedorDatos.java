package cl.bcs.valo2.data;

public interface ProveedorDatos<K, T> {

    T buscar(
            final K key)
                    throws ProveedorDatosException;

}
