package cl.bcs.valo2.repositorios;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import cl.bcs.valo2.ValorizadorContexto;
import cl.bcs.valo2.data.ProveedorDatos;
import cl.bcs.valo2.data.archivos.ArchivoBonos;
import cl.bcs.valo2.data.consultas.ConsultaBuilder;
import cl.bcs.valo2.entidades.Bono;

public final class RepositorioBonos extends Repositorio {
    private final LoadingCache<String, Bono> cache;
    private final ProveedorDatos<String, Bono> proveedorBonos;

    public RepositorioBonos(
            final ValorizadorContexto contexto) throws SQLException {
        super(contexto);

        this.proveedorBonos = newProveedorBonos(contexto);
        this.cache = CacheBuilder.newBuilder().maximumSize(1000)
                .build(new CacheLoader<String, Bono>() {

                    @Override
                    public Bono load(
                            final String nemo) {
                        return buscarEnRepositorio(nemo);
                    }
                });
    }

    public Bono buscar(
            final String nemo) {
        return getContexto().getConfig().isCacheHabilitado() ? buscarEnCache(nemo)
                : buscarEnRepositorio(nemo);
    }

    public Bono buscarEnCache(
            final String nemo) {
        try {
            return cache.get(nemo);
        } catch (final ExecutionException ex) {
            throw new RepositorioException("Error al buscar bono: " + nemo, ex);
        }
    }

    public Bono buscarEnRepositorio(
            final String nemo) {
        final Bono bono = proveedorBonos.buscar(nemo);
        if (bono == null) {
            throw new RegistroNoEncontradoException("Bono no existe: " + nemo);
        }

        return bono;
    }

    @Override
    public void cleanUpCache() {
        cache.cleanUp();
    }

    private ProveedorDatos<String, Bono> newProveedorBonos(
            final ValorizadorContexto contexto)
                    throws SQLException {
        if (getContexto().getDataSource() == null) {
            return new ArchivoBonos(contexto.getConfig().getRutaArchivoBonos());
        }

        return ConsultaBuilder.newConsultaBonos(contexto.getDataSource());
    }

}
