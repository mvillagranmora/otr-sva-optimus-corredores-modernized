package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.ModificarPortafoliosTraspasoRepositorio;

/** JDBC adapter of {@link ModificarPortafoliosTraspasoRepositorio}; the SQL files are the legacy ones, byte for byte (sql/tra). */
@Repository
public class ModificarPortafoliosTraspasoJdbc implements ModificarPortafoliosTraspasoRepositorio {

    private static final String SQL_DETALLES_A_ELIMINAR = SqlTraspasos.recurso("ConsultarPortafolioDetalleTraspaso");
    private static final String SQL_ELIMINAR_DETALLE = SqlTraspasos.recurso("DetalleTraspaso_Delete");
    private static final String SQL_ELIMINAR_PORTAFOLIO_CONTRATO = SqlTraspasos.recurso("PortafolioContratoSAAPV_Eliminar");
    private static final String SQL_CANTIDAD_PORTAFOLIO_CONTRATO = SqlTraspasos.recurso("ConsultarIdPortafolioContratoSAAPV");
    private static final String SQL_ESTADO_PORTAFOLIO = SqlTraspasos.recurso("InhabilitarPortafolioTraspaso");
    private static final String SQL_CLIENTE_CONTRATO = SqlTraspasos.recurso("ModificarPortafolioTraspaso_SelectClienteContrato");
    private static final String SQL_DETALLE_TRASPASO = SqlTraspasos.recurso("ConsultarDetalleTraspasoIngreso");
    private static final String SQL_INACTIVAR_DETALLE = SqlTraspasos.recurso("ModificarPortafolioTraspaso_UpdateInactivarDetalleTraspRepetido");
    private static final String SQL_MATRIZ = SqlTraspasos.recurso("ModificarPortafolioTraspaso_SelectMatrizAgrupadorApv");
    private static final String SQL_PORTAFOLIOS_MATRIZ = SqlTraspasos.recurso("ConsultarMatrizAgrupadorAPVPortafolio");
    private static final String SQL_RELACIONAR = SqlTraspasos.recurso("Crear_PortafolioContratoSAAPV");
    private static final String SQL_ACTUALIZAR_DETALLE = SqlTraspasos.recurso("ModificarDetalleTraspaso");
    private static final String SQL_MODIFICAR_MONTO = SqlTraspasos.recurso("ModificarMontoContratoSAAPV");
    private static final String SQL_INSERTAR_DETALLE = SqlTraspasos.recurso("DetalleTraspaso_Insert");
    private static final String SQL_PORTAFOLIO_CREADO = SqlTraspasos.recurso("ModificarPortafolioTraspaso_SelectPortafoliosCreados");
    private static final String SQL_PORTAFOLIOS_CLIENTE = SqlTraspasos.recurso("ModificarPortafolioTraspaso_SelectPortafoliosClientes");
    private static final String SQL_PLAN_INVERSION = SqlTraspasos.recurso("ModificarPortafolioTraspaso_SelectPlanInversion");

    private final SqlTraspasos sql;

    public ModificarPortafoliosTraspasoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    /** Parameters of the legacy {@code HashMap}: nulls are bound as NULL. */
    private static Map<String, Object> p(Object... pares) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            m.put((String) pares[i], pares[i + 1]);
        }
        return m;
    }

    /** Batch element (public fields = SQL parameter names). */
    public static final class IdsDetalle {
        public Long IdDetalleTraspaso;
        public String IdEstado;
    }

    public static final class IdDetalle {
        public Long IdDetalleTraspaso;
    }

    public static final class PortafolioContrato {
        public Long IdContratoSAAPV;
        public Long IdPortafolio;
    }

    public static final class EstadoPortafolio {
        public Long IdPortafolio;
        public String IdEstado;
        public String IdEstadoAutorizacion;
    }

    public static final class MontoContrato {
        public BigDecimal MontoPesos;
        public BigDecimal MontoUF;
        public Long IdPortafolioNuevo;
        public Long IdPortafolio;
        public Long IdContratoSAAPV;
    }

    /** Row of {@code SelectMatrizAgrupadorApv.sql}. */
    public record Matriz(Long IdMatrizAgrupadorAPV) {
    }

    @Override
    public List<DetalleAEliminar> detallesAEliminar(Long idTraspaso, Long idContratoSAAPV) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DETALLES_A_ELIMINAR, p("IdTraspaso", idTraspaso, "IdContratoSAAPV", idContratoSAAPV), DetalleAEliminar.class));
    }

    @Override
    public int[] eliminarDetalleTraspaso(Long idDetalleTraspaso) {
        IdDetalle fila = new IdDetalle();
        fila.IdDetalleTraspaso = idDetalleTraspaso;
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_ELIMINAR_DETALLE, List.of(fila)));
    }

    @Override
    public int[] eliminarPortafolioContrato(Long idPortafolio, Long idContratoSAAPV) {
        PortafolioContrato fila = new PortafolioContrato();
        fila.IdPortafolio = idPortafolio;
        fila.IdContratoSAAPV = idContratoSAAPV;
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_ELIMINAR_PORTAFOLIO_CONTRATO, List.of(fila)));
    }

    @Override
    public Long contarOtrosContratosDelPortafolio(Long idPortafolio, Long idContratoSAAPV) {
        return Sneaky.sinChequear(() -> sql.contar(SQL_CANTIDAD_PORTAFOLIO_CONTRATO, p("IdPortafolio", idPortafolio, "IdContratoSAAPV", idContratoSAAPV)));
    }

    @Override
    public int[] cambiarEstadoPortafolio(Long idPortafolio, String idEstado, String idEstadoAutorizacion) {
        EstadoPortafolio fila = new EstadoPortafolio();
        fila.IdPortafolio = idPortafolio;
        fila.IdEstado = idEstado;
        fila.IdEstadoAutorizacion = idEstadoAutorizacion;
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_ESTADO_PORTAFOLIO, List.of(fila)));
    }

    @Override
    public List<ClienteContrato> clienteDelContrato(Long idContratoSAAPV) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CLIENTE_CONTRATO, p("IdContratoSAAPV", idContratoSAAPV), ClienteContrato.class));
    }

    @Override
    public List<DetalleVigente> detallesDelTraspaso(Long idTraspaso, Long idPortafolio, Long idDetalleTraspaso) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DETALLE_TRASPASO,
                p("IdTraspaso", idTraspaso, "IdPortafolio", idPortafolio, "IdDetalleTraspaso", idDetalleTraspaso), DetalleVigente.class));
    }

    @Override
    public int[] cambiarEstadoDetalle(Long idDetalleTraspaso, String idEstado) {
        IdsDetalle fila = new IdsDetalle();
        fila.IdDetalleTraspaso = idDetalleTraspaso;
        fila.IdEstado = idEstado;
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_INACTIVAR_DETALLE, List.of(fila)));
    }

    @Override
    public List<Long> matricesAgrupador(Long idPlanTipoInversion, Long idProductoAgrupador, Long idRegimenTributario) {
        List<Matriz> filas = Sneaky.sinChequear(() -> sql.consultar(SQL_MATRIZ,
                p("p_IdPlanTipoInversion", idPlanTipoInversion, "p_IdProductoAgrupador", idProductoAgrupador, "p_IdRegimenTributario", idRegimenTributario), Matriz.class));
        // an ArrayList: get(0) on it throws IndexOutOfBoundsException as the legacy list did (List.toList() throws the array subclass)
        return new java.util.ArrayList<>(filas.stream().map(Matriz::IdMatrizAgrupadorAPV).collect(java.util.stream.Collectors.toList()));
    }

    @Override
    public List<PortafolioDeMatriz> portafoliosDeMatriz(Long idMatrizAgrupadorAPV, Long idCliente) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_PORTAFOLIOS_MATRIZ, p("IdMatrizAgrupadorAPV", idMatrizAgrupadorAPV, "IdCliente", idCliente), PortafolioDeMatriz.class));
    }

    @Override
    public int[] relacionarPortafolioContrato(RelacionPortafolioContrato fila) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_RELACIONAR, List.of(fila)));
    }

    @Override
    public int[] actualizarDetalleTraspaso(DetalleModificado fila) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_ACTUALIZAR_DETALLE, List.of(fila)));
    }

    @Override
    public int[] modificarMontoContrato(Long idContratoSAAPV, Long idPortafolio, Long idPortafolioNuevo, BigDecimal montoPesos, BigDecimal montoUF) {
        MontoContrato fila = new MontoContrato();
        fila.IdContratoSAAPV = idContratoSAAPV;
        fila.IdPortafolio = idPortafolio;
        fila.IdPortafolioNuevo = idPortafolioNuevo;
        fila.MontoPesos = montoPesos;
        fila.MontoUF = montoUF;
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_MODIFICAR_MONTO, List.of(fila)));
    }

    @Override
    public int[] insertarDetalleTraspaso(DetalleNuevo fila) {
        return Sneaky.sinChequear(() -> sql.loteConConteos(SQL_INSERTAR_DETALLE, List.of(fila)));
    }

    @Override
    public List<PortafolioCreado> portafolioCreado(Long idPortafolio) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_PORTAFOLIO_CREADO, p("IdPortafolio", idPortafolio), PortafolioCreado.class));
    }

    @Override
    public List<PortafolioCliente> portafoliosDelCliente(Long idCliente) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_PORTAFOLIOS_CLIENTE, p("IdCliente", idCliente), PortafolioCliente.class));
    }

    @Override
    public List<PlanPortafolio> planesDelCliente(Long idCliente) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_PLAN_INVERSION, p("IdCliente", idCliente), PlanPortafolio.class));
    }
}
