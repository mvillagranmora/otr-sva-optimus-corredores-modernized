package cl.bcs.valo2.repositorios;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.archivos.ArchivoParidades;
import cl.bcs.valo2.data.consultas.ConsultaBuilder;
import cl.bcs.valo2.entidades.Indicador;
import cl.bcs.valo2.entidades.Indicador.Clave;
import cl.bcs.valo2.fechas.Fecha;

public final class RepositorioParidades extends Repositorio {
    private final LoadingCache<Clave, Indicador> cache0;
    private final LoadingCache<Clave, Indicador> cache1;
    private final ProveedorDatos<Clave, Indicador> proveedorParidades;

    public RepositorioParidades(
            final ValorizadorContexto contexto) throws SQLException {
        super(contexto);

        this.proveedorParidades = newProveedorParidades(contexto);
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
                "No se logro determinar paridad: " + nombre + "[" + fecha + "]");
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
            throw new RepositorioException("Error al buscar paridad: " + clave, ex);
        }
    }

    public Indicador buscarEnRepositorio(
            final Clave clave) {
        final Indicador indicador = proveedorParidades.buscar(clave);
        if (indicador == null) {
            throw new RegistroNoEncontradoException("No hay paridad registrada para: " + clave);
        }

        return indicador;
    }

    @Override
    public void cleanUpCache() {
        cache0.cleanUp();
        cache1.cleanUp();
    }

    private ProveedorDatos<Clave, Indicador> newProveedorParidades(
            final ValorizadorContexto contexto)
                    throws SQLException {
        if (contexto.getDataSource() == null) {
            return new ArchivoParidades(contexto.getConfig().getRutaArchivoParidades());
        }

        return ConsultaBuilder.newConsultaParidades(contexto.getDataSource());
    }

}
