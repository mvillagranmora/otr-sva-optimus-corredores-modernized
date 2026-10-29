package cl.bcs.apv.infrastructure.persistence.contratos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.domain.port.out.contratos.ContratoNuevo;
import cl.bcs.apv.domain.port.out.contratos.GrabarContratosRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/**
 * JDBC adapter of {@link GrabarContratosRepositorio}. The SQL text is the legacy text, byte for byte, under {@code src/main/resources/sql}. Queries and
 * DDL go through {@link SqlNombrado}; the batches are run here because the legacy inspects their update counts ({@code int[]}): the parameters are read
 * from the record components by name and a {@link BigDecimal} is written with {@code setScale(16, HALF_DOWN)} (legacy {@code SQL.batch}); an empty list
 * executes nothing and answers an empty array.
 */
@Repository
public class GrabarContratosJdbc implements GrabarContratosRepositorio {

    private static final Pattern COMENTARIOS = Pattern.compile("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)");

    private final NamedParameterJdbcTemplate jdbc;
    private final SqlNombrado sql;

    public GrabarContratosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<EstadoCliente> estadosCliente(List<String> documentosIdentidad) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("documentoIdentidad", documentosIdentidad);
        return sql.consultar(SqlNombrado.recurso("GrabarContratoSAAPV_SelectEstadoCliente"), p, EstadoCliente.class);
    }

    @Override
    public List<ConsultaDireccion> direccionesFisicas(Long idPersona) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        return sql.consultar(SqlNombrado.recurso("ConsultarDireccionFisica"), p, ConsultaDireccion.class);
    }

    @Override
    public int[] copiarDireccionesConPropositoApv(List<ConsultaDireccion> direcciones) throws SQLException {
        return lote("CreaDireccionPropositoAPV", direcciones);
    }

    @Override
    public int[] cambiarCargoPersona(List<CambiarCargoPersona> cargos) throws SQLException {
        return lote("CrearPersonaActividaCargo_Update", cargos);
    }

    @Override
    public List<TipoIndicador> tiposIndicador(String idTipoEntidad, String grupoIndicador) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTipoEntidad", idTipoEntidad);
        p.put("GrupoIndicador", grupoIndicador);
        return sql.consultar(SqlNombrado.recurso("ConsultaTipoIndicadorPersona"), p, TipoIndicador.class);
    }

    @Override
    public void crearTablaRegistroAtributo() throws SQLException {
        sql.ejecutar(SqlNombrado.recurso("GrabarContratoSAAPV_CrearRegistroAtributo"));
    }

    @Override
    public int[] insertarRegistroAtributo(List<IndicadorEncargadoRRHH> indicadores) throws SQLException {
        return lote("GrabarContratoSAAPV_InsertRegistroAtributo", indicadores);
    }

    @Override
    public void eliminarTablaRegistroAtributo() throws SQLException {
        sql.ejecutar(SqlNombrado.recurso("GrabarContratoSAAPV_DropRegistroAtributo"));
    }

    @Override
    public List<ConsultaRelacionPersona> relacionesPersona(Long idPersona, Long idEmpleador) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        p.put("IdEmpleador", idEmpleador);
        return sql.consultar(SqlNombrado.recurso("ConsultarRelacionPersona"), p, ConsultaRelacionPersona.class);
    }

    @Override
    public int[] insertarContrato(ContratoNuevo contrato) throws SQLException {
        return lote("ContratoSAAPV_Insert", List.of(contrato));
    }

    @Override
    public List<ContratoConsultado> consultarContrato(Long idContratoSAAPV) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdContratoSAAPV", idContratoSAAPV);
        return sql.consultar(SqlNombrado.recurso("ContratoSAAPV_Consulta"), p, ContratoConsultado.class);
    }

    @Override
    public int[] eliminarContrato(List<ContratoConsultado> contratos) throws SQLException {
        return lote("ContratoSAAPV_Delete", contratos);
    }

    @Override
    public int[] eliminarDistribucion(List<ContratoConsultado> contratos) throws SQLException {
        return lote("EliminarDistribucionInversion", contratos);
    }

    @Override
    public void crearTablaPortafoliosIngresados() throws SQLException {
        sql.ejecutar(SqlNombrado.recurso("GrabarContratoSAAPV_CrearPortafoliosIngresados"));
    }

    @Override
    public int[] insertarPortafoliosIngresados(List<PortafolioACerrar> portafolios) throws SQLException {
        return lote("GrabarContratoSAAPV_InsertPortafoliosIngresados", portafolios);
    }

    @Override
    public void eliminarTablaPortafoliosIngresados() throws SQLException {
        sql.ejecutar(SqlNombrado.recurso("GrabarContratoSAAPV_DropPortafoliosIngresados"));
    }

    @Override
    public List<ContratoSAAPVID> contratosConLasMismasCondiciones(Long idMedioRecaudacionFormaPago, String formaDePago) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdMedioRecaudacionFormaPago", idMedioRecaudacionFormaPago);
        p.put("FormaDePago", formaDePago);
        return sql.consultar(SqlNombrado.recurso("ContratoSAAPV_Consulta_Condiciones_Duplicadas"), p, ContratoSAAPVID.class);
    }

    @Override
    public List<MedioRecaudacion> mediosRecaudacionPac() throws SQLException {
        return sql.consultar(SqlNombrado.recurso("ContratoSAAPV_Consulta_MedioRecaudacionPAC"), MedioRecaudacion.class);
    }

    @Override
    public List<PortafoliosSAAPV> portafoliosDeContrato(Long idContratoSAAPV) throws SQLException {
        Map<String, Object> p = new HashMap<>();
        p.put("IdContratoSAAPV", idContratoSAAPV);
        return sql.consultar(SqlNombrado.recurso("ContratoSAAPV_Consulta_PortafoliosPorIdContratoSAAPV"), p, PortafoliosSAAPV.class);
    }

    @Override
    public int[] cerrarContratos(List<ContratoSAAPVID> contratos) throws SQLException {
        return lote("ContratoSAAPV_Condiciones_Duplicadas_Cerrar", contratos);
    }

    @Override
    public int[] cerrarDistribuciones(List<ContratoSAAPVID> contratos) throws SQLException {
        return lote("ContratoSAAPV_Distribucion_Condiciones_Duplicadas_Cerrar", contratos);
    }

    @Override
    public int[] insertarDistribuciones(List<DistribucionNueva> distribuciones) throws SQLException {
        return lote("AgregarDistribucionInversion", distribuciones);
    }

    @Override
    public int[] insertarPortafoliosContrato(List<PortafolioContratoNuevo> portafolios) throws SQLException {
        return lote("Crear_PortafolioContratoSAAPV", portafolios);
    }

    @Override
    public int[] insertarTraspaso(List<TraspasoNuevo> traspasos) throws SQLException {
        return lote("Traspaso_Insert", traspasos);
    }

    @Override
    public int[] modificarEstadoTraspaso(List<TraspasoEstado> traspasos) throws SQLException {
        return lote("Traspaso_Update", traspasos);
    }

    @Override
    public int[] insertarDetallesOrigen(List<DetalleOrigenNuevo> detalles) throws SQLException {
        return lote("DetalleOrigenTraspaso_Insert", detalles);
    }

    // ------------------------------------------------------------------------------------------------ batch

    private int[] lote(String nombreSql, List<? extends Record> filas) throws SQLException {
        if (filas.isEmpty()) {
            return new int[0];
        }
        String texto = COMENTARIOS.matcher(SqlNombrado.recurso(nombreSql)).replaceAll("");
        SqlParameterSource[] fuentes = filas.stream().map(ComponentesDeRegistro::new).toArray(SqlParameterSource[]::new);
        try {
            return jdbc.batchUpdate(texto, fuentes);
        } catch (DataAccessException e) {
            Throwable causa = e.getMostSpecificCause();
            throw causa instanceof SQLException s ? s : new SQLException(e.getMessage(), e);
        }
    }

    /** Parameters read from the components of a record by name; BigDecimal at scale 16 (legacy SQL.batch). */
    private static final class ComponentesDeRegistro extends AbstractSqlParameterSource {
        private final Record fila;

        ComponentesDeRegistro(Record fila) {
            this.fila = fila;
        }

        @Override
        public boolean hasValue(String nombre) {
            return Arrays.stream(fila.getClass().getRecordComponents()).anyMatch(c -> c.getName().equals(nombre));
        }

        @Override
        public Object getValue(String nombre) {
            try {
                for (var c : fila.getClass().getRecordComponents()) {
                    if (c.getName().equals(nombre)) {
                        Object v = c.getAccessor().invoke(fila);
                        return v instanceof BigDecimal bd ? bd.setScale(16, RoundingMode.HALF_DOWN) : v;
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new InvalidDataAccessApiUsageException("Object field unknown: " + nombre, e);
            }
            throw new InvalidDataAccessApiUsageException("Object field unknown: " + nombre);
        }
    }
}
