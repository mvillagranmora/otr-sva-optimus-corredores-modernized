package cl.bcs.apv.infrastructure.persistence.contratos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.ModificarContratosRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/**
 * JDBC adapter of {@link ModificarContratosRepositorio}: the SQL of the legacy {@code ModificarContratoSAAPV}, one resource file per statement
 * ({@code src/main/resources/sql}, the legacy text byte for byte). Failures are re-thrown as the {@link SQLException} the legacy threw.
 */
@Repository
public class ModificarContratosJdbc implements ModificarContratosRepositorio {

    private static final String CONSULTAR_CONTRATO = SqlNombrado.recurso("ContratoSAAPV_Consulta");
    private static final String MODIFICAR_CONTRATO = SqlNombrado.recurso("ContratoSAAPV_Update");
    private static final String CONSULTAR_TRASPASO = SqlNombrado.recurso("ModificarContratoSAAPV_ConsultarTraspaso");
    private static final String ELIMINAR_ORIGEN_TRASPASO = SqlNombrado.recurso("Eliminar_DetalleOrigenTraspaso");
    private static final String ELIMINAR_TRASPASO = SqlNombrado.recurso("Eliminar_Traspaso");
    private static final String ELIMINAR_DISTRIBUCION = SqlNombrado.recurso("EliminarDistribucionInversion");
    private static final String CREAR_TRASPASO = SqlNombrado.recurso("Traspaso_Insert");
    private static final String CREAR_DETALLE_ORIGEN = SqlNombrado.recurso("DetalleOrigenTraspaso_Insert");
    private static final String MODIFICAR_ID_TRASPASO_DETALLE = SqlNombrado.recurso("ModificarContratoSAAPV_ModificarIdTraspasoDetalleTraspaso");
    private static final String MODIFICAR_PORTAFOLIO_CONTRATO = SqlNombrado.recurso("ModificarContratoSAAPV_ModificarPortafolioContrato");
    private static final String MODIFICAR_ESTADO_TRASPASO = SqlNombrado.recurso("Traspaso_Update");
    private static final String CREAR_DISTRIBUCION = SqlNombrado.recurso("AgregarDistribucionInversion");
    private static final String ELIMINAR_PORTAFOLIOS = SqlNombrado.recurso("Eliminar_PortafolioContratoSAAPV");
    private static final String CREAR_PORTAFOLIO_CONTRATO = SqlNombrado.recurso("Crear_PortafolioContratoSAAPV");
    private static final String CONSULTAR_TRASPASO_EGRESOS = SqlNombrado.recurso("ConsultarTraspasosEgresos");
    private static final String ELIMINAR_DETALLE_EGRESO = SqlNombrado.recurso("Eliminar_DetalleOrigenTraspasoEgreso");
    private static final String MODIFICAR_TRASPASO_EGRESOS = SqlNombrado.recurso("ModificarTraspasosEgresos");

    private final NamedParameterJdbcTemplate jdbc;
    private final SqlNombrado sql;

    public ModificarContratosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sql = new SqlNombrado(jdbc);
    }

    // legacy parameter holders (the names are the :parameters of the SQL files)
    private record IdContrato(Long IdContratoSAAPV) {
    }

    public record IdTraspasoFila(Long IdTraspaso) {
    }

    public record IdDetalleFila(Long IdDetalleOrigenTraspaso) {
    }

    private record CambioIdTraspaso(Long IdTraspaso, Long IdTraspasoNuevo) {
    }

    private record CambioIdContrato(Long IdContratoSAAPV, Long IdContratoSAAPVNuevo) {
    }

    private record CambioEstadoTraspaso(Long IdTraspaso, String IdEstado) {
    }

    @Override
    public List<ContratoEstado> consultarContrato(Long idContratoSAAPV) {
        return Sneaky.sinChequear(() -> {
            List<ContratoEstadoFila> filas = sql.consultar(CONSULTAR_CONTRATO, param("IdContratoSAAPV", idContratoSAAPV), ContratoEstadoFila.class);
            List<ContratoEstado> out = new ArrayList<>();
            for (ContratoEstadoFila f : filas) {
                out.add(new ContratoEstado(f.IdContratoSAAPV(), f.IdEstado()));
            }
            return out;
        });
    }

    public record ContratoEstadoFila(Long IdContratoSAAPV, String IdEstado) {
    }

    @Override
    public void modificarContrato(ContratoModificado contrato) {
        Sneaky.sinChequear(() -> LoteContratoModificacion.ejecutar(jdbc, MODIFICAR_CONTRATO, List.of(contrato)));
    }

    @Override
    public List<Long> consultarIdsTraspaso(Long idContratoSAAPV) {
        return Sneaky.sinChequear(() -> {
            List<Long> out = new ArrayList<>();
            for (IdTraspasoFila f : sql.consultar(CONSULTAR_TRASPASO, param("IdContratoSAAPV", idContratoSAAPV), IdTraspasoFila.class)) {
                out.add(f.IdTraspaso());
            }
            return out;
        });
    }

    @Override
    public void eliminarDetalleOrigenTraspaso(Long idContratoSAAPV) {
        Sneaky.sinChequear(() -> LoteContratoModificacion.ejecutar(jdbc, ELIMINAR_ORIGEN_TRASPASO, List.of(new IdContrato(idContratoSAAPV))));
    }

    @Override
    public void eliminarTraspaso(Long idContratoSAAPV) {
        Sneaky.sinChequear(() -> LoteContratoModificacion.ejecutar(jdbc, ELIMINAR_TRASPASO, List.of(new IdContrato(idContratoSAAPV))));
    }

    @Override
    public void eliminarDistribucion(Long idContratoSAAPV) {
        Sneaky.sinChequear(() -> LoteContratoModificacion.ejecutar(jdbc, ELIMINAR_DISTRIBUCION, List.of(new IdContrato(idContratoSAAPV))));
    }

    @Override
    public boolean crearTraspaso(TraspasoNuevo traspaso) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(LoteContratoModificacion.ejecutar(jdbc, CREAR_TRASPASO, List.of(traspaso))));
    }

    @Override
    public boolean crearDetallesOrigenTraspaso(List<DetalleOrigenNuevo> detalles) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(LoteContratoModificacion.ejecutar(jdbc, CREAR_DETALLE_ORIGEN, detalles)));
    }

    @Override
    public boolean modificarIdTraspasoDetalle(Long idTraspaso, Long idTraspasoNuevo) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(
                LoteContratoModificacion.ejecutar(jdbc, MODIFICAR_ID_TRASPASO_DETALLE, List.of(new CambioIdTraspaso(idTraspaso, idTraspasoNuevo)))));
    }

    @Override
    public boolean modificarContratoDePortafolios(Long idContratoSAAPV, Long idContratoSAAPVNuevo) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(
                LoteContratoModificacion.ejecutar(jdbc, MODIFICAR_PORTAFOLIO_CONTRATO, List.of(new CambioIdContrato(idContratoSAAPV, idContratoSAAPVNuevo)))));
    }

    @Override
    public boolean modificarEstadoTraspaso(Long idTraspaso, String idEstado) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(
                LoteContratoModificacion.ejecutar(jdbc, MODIFICAR_ESTADO_TRASPASO, List.of(new CambioEstadoTraspaso(idTraspaso, idEstado)))));
    }

    @Override
    public boolean crearDistribuciones(List<DistribucionNueva> distribuciones) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(LoteContratoModificacion.ejecutar(jdbc, CREAR_DISTRIBUCION, distribuciones)));
    }

    @Override
    public void eliminarPortafoliosDelContrato(Long idContratoSAAPV) {
        Sneaky.sinChequear(() -> LoteContratoModificacion.ejecutar(jdbc, ELIMINAR_PORTAFOLIOS, List.of(new IdContrato(idContratoSAAPV))));
    }

    @Override
    public boolean crearPortafolioContrato(PortafolioContratoNuevo portafolio) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(LoteContratoModificacion.ejecutar(jdbc, CREAR_PORTAFOLIO_CONTRATO, List.of(portafolio))));
    }

    @Override
    public List<Long> consultarDetallesTraspasoEgreso(Long idTraspaso) {
        return Sneaky.sinChequear(() -> {
            List<Long> out = new ArrayList<>();
            for (IdDetalleFila f : sql.consultar(CONSULTAR_TRASPASO_EGRESOS, param("IdTraspaso", idTraspaso), IdDetalleFila.class)) {
                out.add(f.IdDetalleOrigenTraspaso());
            }
            return out;
        });
    }

    @Override
    public void eliminarDetallesOrigenEgreso(List<Long> idsDetalleOrigenTraspaso) {
        Sneaky.sinChequear(() -> {
            List<IdDetalleFila> filas = new ArrayList<>();
            for (Long id : idsDetalleOrigenTraspaso) {
                filas.add(new IdDetalleFila(id));
            }
            LoteContratoModificacion.ejecutar(jdbc, ELIMINAR_DETALLE_EGRESO, filas);
        });
    }

    @Override
    public boolean modificarTraspasoEgreso(TraspasoEgresoModificado traspaso) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.primeroEsUno(LoteContratoModificacion.ejecutar(jdbc, MODIFICAR_TRASPASO_EGRESOS, List.of(traspaso))));
    }

    /** {@code Map.of} rejects null values: the legacy bound a null id as SQL NULL. */
    private static Map<String, Object> param(String nombre, Object valor) {
        Map<String, Object> m = new HashMap<>();
        m.put(nombre, valor);
        return m;
    }
}
