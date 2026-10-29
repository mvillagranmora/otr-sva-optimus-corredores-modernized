package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.api.dto.validarcontrato.ModificaContratoSAAPV;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.ValidarContratoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/** JDBC adapter of {@link ValidarContratoRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class ValidarContratoJdbc implements ValidarContratoRepositorio {

    private static final String SQL_MODIFICA_CONTRATOS = SqlNombrado.recurso("ModificaIdsContratoSAAPV");
    private static final String SQL_VALIDA_RECAUDACION = SqlNombrado.recurso("ValidacionSAAPV_validaRecaudacion");
    private static final String SQL_DETALLES_TRASPASO = SqlNombrado.recurso("ValidaContrato_SelectDetallesTraspaso");
    private static final String SQL_PERSONA = SqlNombrado.recurso("ValidaContratoSAAPV_SelectDocumentoIdentidad");
    private static final String SQL_CLIENTE = SqlNombrado.recurso("ValidaContratoSAAPV_SelectCliente");
    private static final String SQL_PORTAFOLIO = SqlNombrado.recurso("ConsultarPortafolioMatrizAgrupadorDisponible");
    private static final String SQL_FOLIO = SqlNombrado.recurso("ValidaContratoSAAPV_SelectFolio");

    private final SqlNombrado sql;

    public ValidarContratoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** Batch parameters: the legacy read them from the public fields of {@code ModificaContratoSAAPV} (legacy names). */
    public static class FilaModifica {
        public Long IdPersona;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdEmpleador;
        public String FolioContratoSAAPV;
    }

    @Override
    public List<ExistePersona> personasPorDocumento(String documentoIdentidad, String tipoPersona) {
        Map<String, Object> p = new HashMap<>();
        p.put("DocumentoIdentidad", documentoIdentidad);
        p.put("TipoPersona", tipoPersona);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_PERSONA, p, ExistePersona.class));
    }

    @Override
    public List<ExisteCliente> clientesPorPersona(Long idPersona) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CLIENTE, p, ExisteCliente.class));
    }

    @Override
    public List<ExistePortafolio> portafoliosApv(Long idPersona, String codigoRegimen, String nombrePlan, String codigoAgrupador) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        p.put("CodigoRegimen", codigoRegimen);
        p.put("NombrePlan", nombrePlan);
        p.put("CodigoAgrupador", codigoAgrupador);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_PORTAFOLIO, p, ExistePortafolio.class));
    }

    @Override
    public List<ExisteFolioContrato> foliosExistentes(String folioContratoSAAPV) {
        Map<String, Object> p = new HashMap<>();
        p.put("FolioContratoSAAPV", folioContratoSAAPV);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_FOLIO, p, ExisteFolioContrato.class));
    }

    @Override
    public void modificarIdsContratos(List<ModificaContratoSAAPV> filas) {
        List<FilaModifica> lote = new ArrayList<>();
        for (ModificaContratoSAAPV f : filas) {
            FilaModifica m = new FilaModifica();
            m.IdPersona = f.idPersona();
            m.IdCliente = f.idCliente();
            m.IdPortafolio = f.idPortafolio();
            m.IdEmpleador = f.idEmpleador();
            m.FolioContratoSAAPV = f.folioContratoSAAPV();
            lote.add(m);
        }
        Sneaky.sinChequear(() -> sql.lote(SQL_MODIFICA_CONTRATOS, lote));
    }

    @Override
    public List<RecaudacionValidada> recaudacionValidada(List<Long> idsRecaudacionCliente, String periodoSistema) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdsRecaudacionCliente", idsRecaudacionCliente);
        p.put("PeriodoSistema", periodoSistema);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_VALIDA_RECAUDACION, p, RecaudacionValidada.class));
    }

    @Override
    public List<TraspasoReapertura> detalleTraspaso(Long idDetalleTraspaso) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdDetalleTraspaso", idDetalleTraspaso);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DETALLES_TRASPASO, p, TraspasoReapertura.class));
    }
}
