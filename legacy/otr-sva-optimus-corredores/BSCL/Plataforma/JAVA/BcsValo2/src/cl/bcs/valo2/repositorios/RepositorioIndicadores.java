package cl.bcs.valo2.repositorios;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.archivos.ArchivoIndicadores;
import cl.bcs.valo2.data.consultas.ConsultaBuilder;
import cl.bcs.valo2.entidades.Indicador;
import cl.bcs.valo2.entidades.Indicador.Clave;
import cl.bcs.valo2.fechas.Fecha;

public final class RepositorioIndicadores extends Repositorio {
    private final LoadingCache<Clave, Indicador> cache0;
    private final LoadingCache<Clave, Indicador> cache1;
    private final ProveedorDatos<Clave, Indicador> proveedorIndicadores;

    public RepositorioIndicadores(
            final ValorizadorContexto contexto) throws SQLException {
        super(contexto);

        this.proveedorIndicadores = newProveedorIndicadores(contexto);
        this.cache0 = CacheBuilder.newBuilder().maximumSize(10000)
                .build(new CacheLoader<Clave, Indicador>() {

                    @Override
                    public Indicador load(
                            final Clave clave) {
                        return buscarEnRepositorio(clave);
                    }
                });
        this.cache1 = CacheBuilder.newBuilder().maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES).build(new CacheLoader<Clave, Indicador>() {

                    @Override
                    public Indicador load(
                            final Clave clave) {
                        return buscarEnRepositorio(clave);
                    }
                });
    }

    public Indicador buscar(
            final Clave clave) {
        return getContexto().getConfig().isCacheHabilitado() ? buscarEnCache(clave)
                : buscarEnRepositorio(clave);
    }

    public Indicador buscar(
            final String nombre,
            final Fecha fecha) {
        return buscar(nombre, fecha, 0);
    }

    public Indicador buscar(
            final String nombre,
            final Fecha fecha,
            final int dias) {
        Fecha fec = fecha;
        for (int i = 0; i <= dias; i++) {
            try {
                final Clave clave = new Clave(nombre, fec);
                return buscar(clave);
            } catch (final RegistroNoEncontradoException ex) {
                fec = fec.sumarDias(-1);
            }
        }

        throw new RepositorioException(
                "No se logro determinar indicador: " + nombre + "[" + fecha + "]");
    }

    public Indicador buscarEnCache(
            final Clave clave) {
        try {
            if (clave.getFecha().compareTo(Fecha.hoy()) < 0) {
                return cache0.get(clave);
            } else {
                return cache1.get(clave);
            }
        } catch (final ExecutionException ex) {
            throw new RepositorioException("Error al buscar indicador: " + clave, ex);
        }
    }

    public Indicador buscarEnRepositorio(
            final Clave clave) {
        final Indicador indicador = proveedorIndicadores.buscar(clave);
        if (indicador == null) {
            throw new RegistroNoEncontradoException("No hay indicador registrado para: " + clave);
        }

        return indicador;
    }

    @Override
    public void cleanUpCache() {
        cache0.cleanUp();
        cache1.cleanUp();
    }

    private ProveedorDatos<Clave, Indicador> newProveedorIndicadores(
            final ValorizadorContexto contexto)
                    throws SQLException {
        if (contexto.getDataSource() == null) {
            return new ArchivoIndicadores(contexto.getConfig().getRutaArchivoIndicadores());
        }

        return ConsultaBuilder.newConsultaIndicadores(contexto.getDataSource());
    }

}
