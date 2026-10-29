package cl.bcs.valo2.repositorios;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.archivos.ArchivoCupones;
import cl.bcs.valo2.data.consultas.ConsultaBuilder;
import cl.bcs.valo2.entidades.Cupon;

public final class RepositorioCupones extends Repositorio {
    private final LoadingCache<String, List<Cupon>> cache;
    private final ProveedorDatos<String, List<Cupon>> proveedorCupones;

    public RepositorioCupones(
            final ValorizadorContexto contexto) throws SQLException {
        super(contexto);

        this.proveedorCupones = newProveedorCupones(contexto);
        this.cache = CacheBuilder.newBuilder().maximumSize(1000)
                .build(new CacheLoader<String, List<Cupon>>() {

                    @Override
                    public List<Cupon> load(
                            final String nemo) {
                        return buscarEnRepositorio(nemo);
                    }
                });

    }

    public List<Cupon> buscar(
            final String nemo) {
        return getContexto().getConfig().isCacheHabilitado() ? buscarEnCache(nemo)
                : buscarEnRepositorio(nemo);
    }

    public List<Cupon> buscarEnCache(
            final String nemo) {
        try {
            return cache.get(nemo);
        } catch (final ExecutionException ex) {
            throw new RepositorioException("Error al buscar cupones: " + nemo, ex);
        }
    }

    public List<Cupon> buscarEnRepositorio(
            final String nemo) {
        final List<Cupon> cupones = proveedorCupones.buscar(nemo);
        if (cupones == null || cupones.isEmpty()) {
            throw new RegistroNoEncontradoException("No hay cupones registrados para: " + nemo);
        }

        return cupones;
    }

    @Override
    public void cleanUpCache() {
        cache.cleanUp();
    }

    private ProveedorDatos<String, List<Cupon>> newProveedorCupones(
            final ValorizadorContexto contexto)
                    throws SQLException {
        if (getContexto().getDataSource() == null) {
            return new ArchivoCupones(contexto.getConfig().getRutaArchivoCupones());
        }

        return ConsultaBuilder.newConsultaCupones(contexto.getDataSource());
    }

}
