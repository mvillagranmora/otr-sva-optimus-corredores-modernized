package cl.bcs.contabilidad.infrastructure.persistence;

import cl.bcs.contabilidad.domain.model.clienterelacionado.Comprobante;
import cl.bcs.contabilidad.domain.model.clienterelacionado.CondicionConcepto;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ConceptoNormativoConfig;
import cl.bcs.contabilidad.domain.model.clienterelacionado.FuentesOperaciones;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionDolar;
import cl.bcs.contabilidad.domain.model.clienterelacionado.PactoTermino;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ValorCondicion;
import cl.bcs.contabilidad.domain.port.out.ClienteRelacionadoRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC adapter of {@link ClienteRelacionadoRepository}. Statements are the legacy files:
 * ConsultarOperacionesClienteRelacionado.java:29-35, ConsultarCondicionesConceptosNormativos.java:18 and the logo query
 * inlined at GenerarInformeClienteRelacionado.java:38 (externalized verbatim as
 * GenerarInformeClienteRelacionado_SelectLogoCorredora.sql). Columns are read by label; a column a statement does not
 * select stays null, as with legacy {@code SQL.query} (for example FolioOperacion of SelectPrestamos and FechaTermino
 * of SelectOperacionesTerminoCartera, which selects "FechFin").
 */
@Repository
public class ClienteRelacionadoJdbcRepository implements ClienteRelacionadoRepository {

    private static final String SQL_CONCEPTOS =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarCondicionesConceptosNormativos_SelectConceptosConfiguraciones.sql");
    private static final String SQL_COMPROBANTES =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectComprobantes.sql");
    private static final String SQL_PRESTAMOS =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectPrestamos.sql");
    private static final String SQL_PACTOS =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesPacto.sql");
    private static final String SQL_TERMINOS =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesTermino.sql");
    private static final String SQL_SPOT =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesSpot.sql");
    private static final String SQL_TERMINOS_CARTERA =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesTerminoCartera.sql");
    private static final String SQL_FORWARD =
            SqlResources.load("cl/bcs/contabilidad/sql/ConsultarOperacionesClienteRelacionado_SelectOperacionesForward.sql");
    private static final String SQL_LOGO =
            SqlResources.load("cl/bcs/contabilidad/sql/GenerarInformeClienteRelacionado_SelectLogoCorredora.sql");

    private static final RowMapper<Comprobante> COMPROBANTE = SqlRow.mapper(r -> new Comprobante(
            r.lng("FolioOperacion"), r.str("FechaOperacion"), r.str("Nombre"), r.str("Portafolio"),
            r.str("DocumentoIdentidad"), r.str("IdTipoInstrumento"), r.str("TipoDocumento"), r.str("CompraVenta"),
            r.str("IdTipoOperacion"), r.str("IdClasificacionTipoInstrumento"), r.str("IdMoneda"),
            r.dec("MontoOperado"), r.dec("MontoTotal"), r.dec("MontoCobro"), r.dec("TotalComision"),
            r.dec("TotalGasto"), r.dec("TotalImpto"), r.dec("TotalRetencion"), r.dec("TotalDerecho")));

    private static final RowMapper<OperacionDolar> DOLAR = SqlRow.mapper(r -> new OperacionDolar(
            r.lng("FolioOperacion"), r.str("FechaOperacion"), r.str("Nombre"), r.str("Portafolio"),
            r.str("DocumentoIdentidad"), r.str("IdTipoInstrumento"), r.str("TipoDocumento"), r.str("CompraVenta"),
            r.str("IdTipoOperacion"), r.str("IdClasificacionTipoInstrumento"), r.dec("MontoCompra"),
            r.dec("MontoVenta")));

    private static final RowMapper<PactoTermino> PACTO = SqlRow.mapper(r -> new PactoTermino(
            r.lng("FolioOperacion"), r.str("FechaOperacion"), r.str("Nombre"), r.str("Portafolio"),
            r.str("DocumentoIdentidad"), r.str("IdTipoInstrumento"), r.str("TipoDocumento"), r.str("CompraVenta"),
            r.str("IdTipoOperacion"), r.str("IdClasificacionTipoInstrumento"), r.str("FechaInicio"),
            r.str("FechaTermino"), r.dec("MontoFinal"), r.dec("MontoInicial"), r.dec("Perdida"), r.dec("Ganancia")));

    private record FilaCondicion(Long idConcepto, String concepto, String idTipoDatoEvento, String valorVarchar,
                                 String operadorFuncion, Long idCondicion) {}

    private final NamedParameterJdbcTemplate jdbc;

    public ClienteRelacionadoJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Legacy ConsultarCondicionesConceptosNormativos.logica (:79-113): one concept per IdConceptoNormativo; conditions
     * grouped by IdTipoDatoEvento case-insensitively, keeping the first spelling; LEFT JOIN rows without a condition
     * add nothing.
     */
    @Override
    public List<ConceptoNormativoConfig> consultarConceptosNormativos() {
        List<FilaCondicion> filas = jdbc.query(SQL_CONCEPTOS, new MapSqlParameterSource(), SqlRow.mapper(r ->
                new FilaCondicion(r.lng("IdConceptoNormativo"), r.str("ConceptoNormativo"), r.str("IdTipoDatoEvento"),
                        r.str("ValorVarchar"), r.str("OperadorFuncion"), r.lng("IdCondicionConceptoNormativo"))));

        Map<Long, String> nombres = new HashMap<>();
        Map<Long, List<Grupo>> grupos = new HashMap<>();
        for (FilaCondicion f : filas) {
            nombres.putIfAbsent(f.idConcepto(), f.concepto());
            List<Grupo> delConcepto = grupos.computeIfAbsent(f.idConcepto(), k -> new ArrayList<>());
            if (f.idCondicion() == null) continue;
            Grupo grupo = delConcepto.stream().filter(g -> g.id.equalsIgnoreCase(f.idTipoDatoEvento())).findFirst()
                    .orElseGet(() -> {
                        Grupo nuevo = new Grupo(f.idTipoDatoEvento());
                        delConcepto.add(nuevo);
                        return nuevo;
                    });
            grupo.valores.add(new ValorCondicion(f.valorVarchar(), f.operadorFuncion()));
        }
        List<ConceptoNormativoConfig> conceptos = new ArrayList<>();
        grupos.forEach((id, gs) -> conceptos.add(new ConceptoNormativoConfig(id, nombres.get(id),
                gs.stream().map(g -> new CondicionConcepto(g.id, g.valores)).toList())));
        return conceptos;
    }

    private static final class Grupo {
        final String id;
        final List<ValorCondicion> valores = new ArrayList<>();

        Grupo(String id) {
            this.id = id;
        }
    }

    /** COCR:151-153 and 175-181: the date strings are bound unchanged. */
    @Override
    public FuentesOperaciones consultarOperaciones(String fechaInicio, String fechaFin) {
        MapSqlParameterSource p = new MapSqlParameterSource("FechaInicio", fechaInicio).addValue("FechaFin", fechaFin);
        return new FuentesOperaciones(
                jdbc.query(SQL_COMPROBANTES, p, COMPROBANTE),
                jdbc.query(SQL_PRESTAMOS, p, COMPROBANTE),
                jdbc.query(SQL_PACTOS, p, PACTO),
                jdbc.query(SQL_TERMINOS, p, PACTO),
                jdbc.query(SQL_SPOT, p, DOLAR),
                jdbc.query(SQL_TERMINOS_CARTERA, p, PACTO),
                jdbc.query(SQL_FORWARD, p, DOLAR));
    }

    /** GICR:214: first row or an empty image. */
    @Override
    public byte[] consultarLogoCorredora(Long idEmpresa) {
        List<byte[]> filas = jdbc.query(SQL_LOGO, new MapSqlParameterSource("IdEmpresa", idEmpresa),
                SqlRow.mapper(r -> bytes(r.obj("Contenido"))));
        return filas.isEmpty() || filas.getFirst() == null ? new byte[0] : filas.getFirst();
    }

    private static byte[] bytes(Object valor) {
        if (valor == null) return null;
        if (valor instanceof byte[] b) return b;
        if (valor instanceof Blob blob) {
            try {
                return blob.getBytes(1, (int) blob.length());
            } catch (SQLException e) {
                throw new IllegalStateException("No se pudo leer el logo de la corredora", e);
            }
        }
        throw new IllegalStateException("Tipo de columna inesperado para el logo: " + valor.getClass().getName());
    }
}
