package cl.bcs.valo2.repositorios;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.archivos.ArchivoFeriados;
import cl.bcs.valo2.data.consultas.ConsultaBuilder;
import cl.bcs.valo2.fechas.Fecha;

public class RepositorioFeriados extends Repositorio {
    private final LoadingCache<String, Set<Fecha>> cache;
    private final ProveedorDatos<String, Set<Fecha>> proveedorFeriados;

    public RepositorioFeriados(
            final ValorizadorContexto contexto) throws SQLException {
        super(contexto);

        this.proveedorFeriados = newProveedorFeriados(contexto);
        this.cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(4, TimeUnit.HOURS)
                .build(new CacheLoader<String, Set<Fecha>>() {

                    @Override
                    public Set<Fecha> load(
                            final String calendario) {
                        return buscarEnRepositorio(calendario);
                    }
                });
    }

    public Set<Fecha> buscar(
            final String calendario) {
        return getContexto().getConfig().isCacheHabilitado() ? buscarEnCache(calendario)
                : buscarEnRepositorio(calendario);
    }

    public Set<Fecha> buscarEnCache(
            final String calendario) {
        try {
            return cache.get(calendario);
        } catch (final ExecutionException ex) {
            throw new RepositorioException("Error al buscar feriados en el cache: " + calendario,
                    ex);
        }
    }

    public Set<Fecha> buscarEnRepositorio(
            final String calendario) {
        final Set<Fecha> feriados = proveedorFeriados.buscar(calendario);
        if (feriados == null | feriados.isEmpty()) {
            throw new RegistroNoEncontradoException(
                    "No hay feriados registrados para: " + calendario);
        }

        return feriados;
    }

    @Override
    public void cleanUpCache() {
        cache.cleanUp();
    }

    public boolean isFeriado(
            final Fecha fecha,
            final String calendario) {
        if (fecha.esFinDeSemana()) {
            return true;
        }

        final Set<Fecha> feriados = buscar(calendario);
        return feriados.contains(fecha);
    }

    private ProveedorDatos<String, Set<Fecha>> newProveedorFeriados(
            final ValorizadorContexto contexto)
                    throws SQLException {
        if (contexto.getDataSource() == null) {
            return new ArchivoFeriados(contexto.getConfig().getRutaArchivoFeriados());
        }

        return ConsultaBuilder.newConsultaFeriados(contexto.getDataSource());
    }

}
