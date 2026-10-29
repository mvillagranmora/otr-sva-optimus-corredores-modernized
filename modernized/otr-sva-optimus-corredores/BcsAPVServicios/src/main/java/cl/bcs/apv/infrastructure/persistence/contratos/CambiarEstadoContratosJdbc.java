package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoContratosRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/** JDBC adapter of {@link CambiarEstadoContratosRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class CambiarEstadoContratosJdbc implements CambiarEstadoContratosRepositorio {

    private static final String SQL_ESTADOS = SqlNombrado.recurso("CambiarEstadoContratosSAAPV");
    private static final String SQL_GLOSA = SqlNombrado.recurso("CambiarGlosaContratosSAAPV");
    private static final String SQL_DATOS_PERSONA = SqlNombrado.recurso("ConsultarDatosPersona");
    private static final String SQL_CAMBIAR_ESTADO_TRASPASO = SqlNombrado.recurso("CambiarEstadoTraspasoAPV");
    private static final String SQL_CONTRATO_TRASPASO = SqlNombrado.recurso("ConsultarExisteContratoTraspaso");
    private static final String SQL_CONTRATO_TRASPASO_CERRAR = SqlNombrado.recurso("CambiarEstadoContratosSAAPV_SelectContratoTraspasoCerrar");
    private static final String SQL_ESTADO_GLOSA = SqlNombrado.recurso("ContratoSAAPV_ConsultarEstadoGlosa");
    private static final String SQL_UPDATE_INGRESO_EGRESO = SqlNombrado.recurso("ContratoSAAPV_Modificar_Ingreso_Egreso");
    private static final String SQL_SELECT_INGRESO_EGRESO = SqlNombrado.recurso("ContratoSAAPV_Consulta_Ingreso_Egreso");
    private static final String SQL_MANDATOS = SqlNombrado.recurso("CambiarEstadoContratosSAAPV_SelectContratos");

    private final SqlNombrado sql;

    public CambiarEstadoContratosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** Batch parameters of the contract updates: the legacy {@code ContratosSAAPV} public fields (legacy names). */
    public static class FilaContrato {
        public String IdFolioContratoSAAPV;
        public String IdEstado;
        public String IdEstadoEnvioNotificacion;
        public String IdEstadoSolicitud;
        public Long IdGlosa;
        public String DescripcionGlosa;
    }

    /** Batch parameters of the traspaso state change: the legacy {@code CambiarEstadoTraspaso}. */
    public static class FilaTraspaso {
        public Long IdContratoSAAPV;
        public Long IdEmpresa;
        public Long IdTraspaso;
        public String IdEstado;
        public String CierreSolicitud;
    }

    private static List<FilaContrato> filas(List<ContratosSAAPV> contratos) {
        List<FilaContrato> lote = new ArrayList<>();
        for (ContratosSAAPV c : contratos) {
            FilaContrato f = new FilaContrato();
            f.IdFolioContratoSAAPV = c.idFolioContratoSAAPV();
            f.IdEstado = c.idEstado();
            f.IdEstadoEnvioNotificacion = c.idEstadoEnvioNotificacion();
            f.IdEstadoSolicitud = c.idEstadoSolicitud();
            f.IdGlosa = c.idGlosa();
            f.DescripcionGlosa = c.descripcionGlosa();
            lote.add(f);
        }
        return lote;
    }

    @Override
    public List<DatosPersona> datosPersona(Long idPersona) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPersona", idPersona);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DATOS_PERSONA, p, DatosPersona.class));
    }

    private static Map<String, Object> foliosYPersonas(List<String> folios, List<Long> personas) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdFolioContratoSAAPV", folios);
        p.put("IdPersona", personas);
        return p;
    }

    @Override
    public List<ContratoEstado> contratosConTraspaso(List<String> folios, List<Long> personas) {
        Map<String, Object> p = foliosYPersonas(folios, personas);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CONTRATO_TRASPASO, p, ContratoEstado.class));
    }

    @Override
    public List<ContratoEstado> contratosConTraspasoYMandato(List<String> folios, List<Long> personas) {
        Map<String, Object> p = foliosYPersonas(folios, personas);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CONTRATO_TRASPASO_CERRAR, p, ContratoEstado.class));
    }

    @Override
    public List<EstadoGlosa> estadoGlosa(String folio) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdFolioContratoSAAPV", folio);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_ESTADO_GLOSA, p, EstadoGlosa.class));
    }

    @Override
    public List<ContratoEstado> traspasoDelContrato(String folio) {
        Map<String, Object> p = new HashMap<>();
        p.put("FolioContratoSAAPV", folio);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_INGRESO_EGRESO, p, ContratoEstado.class));
    }

    @Override
    public List<MandatoPACRelacionado> mandatosRelacionados(List<String> folios) {
        Map<String, Object> p = new HashMap<>();
        p.put("FolioContratoSAAPV", folios);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_MANDATOS, p, MandatoPACRelacionado.class));
    }

    @Override
    public void actualizarEstados(List<ContratosSAAPV> contratos) {
        List<FilaContrato> lote = filas(contratos);
        Sneaky.sinChequear(() -> sql.lote(SQL_ESTADOS, lote));
    }

    @Override
    public void actualizarGlosas(List<ContratosSAAPV> contratos) {
        List<FilaContrato> lote = filas(contratos);
        Sneaky.sinChequear(() -> sql.lote(SQL_GLOSA, lote));
    }

    @Override
    public void cambiarEstadoTraspasos(List<TraspasoACambiar> traspasos) {
        List<FilaTraspaso> lote = new ArrayList<>();
        for (TraspasoACambiar t : traspasos) {
            FilaTraspaso f = new FilaTraspaso();
            f.IdContratoSAAPV = t.idContratoSAAPV();
            f.IdEmpresa = t.idEmpresa();
            f.IdTraspaso = t.idTraspaso();
            f.IdEstado = t.idEstado();
            f.CierreSolicitud = t.cierreSolicitud();
            lote.add(f);
        }
        Sneaky.sinChequear(() -> sql.lote(SQL_CAMBIAR_ESTADO_TRASPASO, lote));
    }

    @Override
    public void actualizarEstadoTraspaso(List<ContratoEstado> filas) {
        Sneaky.sinChequear(() -> sql.lote(SQL_UPDATE_INGRESO_EGRESO, filas));
    }
}
