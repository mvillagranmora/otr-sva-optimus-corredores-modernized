package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.CrearContratoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/** JDBC adapter of {@link CrearContratoRepositorio}: the SQL text is the legacy one (resources {@code sql/}), the failures surface as {@code SQLException}. */
@Repository
public class CrearContratoJdbc implements CrearContratoRepositorio {

    private static final String SQL_SELECT_CONTRATO_SAAPV = SqlNombrado.recurso("CrearContratoSAAPV_SelectContratoSAAPV");
    private static final String SQL_SELECT_FOLIO_CONTRATO_SAAPV = SqlNombrado.recurso("CrearContratoSAAPV_SelectFolioContratoSAAPV");
    private static final String SQL_SELECT_INSTRUMENTO = SqlNombrado.recurso("ContratoSAAPV_ObtenerInstrumentoByNemotecnico");
    private static final String SQL_VALIDA_CONTRATO_VIG = SqlNombrado.recurso("CrearContratoSAAPV_ValidaContratoVigente");
    private static final String SQL_UPDATE_CONTRATO = SqlNombrado.recurso("CrearContratoSAAPV_CerrarContrato");
    private static final String SQL_SELECT_PRIMER_ID_PORTAFOLIO = SqlNombrado.recurso("CrearAporteSAAPV_SelectPrimerIdPortafolio");
    private static final String SQL_SELECT_ID_PORTAFOLIO = SqlNombrado.recurso("CrearAporteSAAPV_SelectIdPortafolio");
    private static final String SQL_SELECT_DATOS_APV = SqlNombrado.recurso("CrearAporteSAAPV_SelectDatosAPV");
    private static final String SQL_SELECT_RESIDENCIA = SqlNombrado.recurso("ConsultarDatosClienteResidencia");
    private static final String SQL_SELECT_REGIMEN = SqlNombrado.recurso("CrearContratoSAAPV_SelectRegimen");
    private static final String SQL_SELECT_FICHACLIENTE = SqlNombrado.recurso("ContratoSAAPV_SelectFichaClienteAPV");
    private static final String SQL_SELECT_ENTIDAD_RECAUDADORA = SqlNombrado.recurso("CrearContratoSAAPV_SelectEntidadRecaudadora");
    private static final String SQL_VALIDAR_PERFIL_COMERCIAL_APV = SqlNombrado.recurso("CrearContratoSAAPV_ValidarPerfilComercialAPV");

    private final SqlNombrado sql;

    public CrearContratoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** HashMap on purpose: a parameter may be null (the legacy bound NULL). */
    private static Map<String, Object> parametros(Object... clavesYValores) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < clavesYValores.length; i += 2) {
            m.put((String) clavesYValores[i], clavesYValores[i + 1]);
        }
        return m;
    }

    @Override
    public List<FilaFolioContrato> consultarFolioContrato(Long idCliente, Long idContratoSAAPV) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_FOLIO_CONTRATO_SAAPV,
                parametros("p_IdCliente", idCliente, "p_IdContratoSAAPV", idContratoSAAPV), FilaFolioContrato.class));
    }

    @Override
    public List<FilaContratoVigente> consultarContratosVigentesAprobados(Long idCliente) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_CONTRATO_SAAPV, parametros("p_IdCliente", idCliente), FilaContratoVigente.class));
    }

    @Override
    public List<FilaDatosApv> consultarDatosApv(String regimenTributario, String producto, Long idPlan) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_DATOS_APV,
                parametros("p_RegimenTriburario", regimenTributario, "p_Producto", producto, "p_IdPlan", idPlan), FilaDatosApv.class));
    }

    @Override
    public List<FilaIdPortafolio> consultarIdPortafolio(Long idCliente, Long idMatrizAgrupadorApv, Long idRegimenTributario) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_ID_PORTAFOLIO, parametros("p_IdCliente", idCliente,
                "p_IdMatrizAgrupadorAPV", idMatrizAgrupadorApv, "p_IdRegimenTributario", idRegimenTributario), FilaIdPortafolio.class));
    }

    @Override
    public List<FilaIdPortafolio> consultarPrimerIdPortafolio(Long idCliente) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_PRIMER_ID_PORTAFOLIO, parametros("p_IdCliente", idCliente), FilaIdPortafolio.class));
    }

    @Override
    public List<FilaEstadoContrato> consultarEstadoContrato(Long idContratoSAAPV) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_VALIDA_CONTRATO_VIG, parametros("IdContratoSAAPV", idContratoSAAPV), FilaEstadoContrato.class));
    }

    @Override
    public List<FilaResidencia> consultarResidencia(String documentoIdentidad) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_RESIDENCIA, parametros("DocumentoIdentidad", documentoIdentidad), FilaResidencia.class));
    }

    @Override
    public List<FilaCodigoRegimen> consultarCodigoRegimen(Long idRegimen) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_REGIMEN, parametros("IdRegimen", idRegimen), FilaCodigoRegimen.class));
    }

    @Override
    public List<FilaFichaCliente> consultarFichaCliente(Long idCliente, Long idPortafolio) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_FICHACLIENTE,
                parametros("p_IdCliente", idCliente, "p_IdPortafolio", idPortafolio), FilaFichaCliente.class));
    }

    @Override
    public List<FilaInstrumento> consultarInstrumentos(List<String> nemotecnicos) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_INSTRUMENTO, parametros("Nemotecnico", nemotecnicos), FilaInstrumento.class));
    }

    @Override
    public List<FilaEntidadRecaudadora> consultarEntidadRecaudadora(String documentoIdentidad) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT_ENTIDAD_RECAUDADORA,
                parametros("DocumentoIdentidad", documentoIdentidad), FilaEntidadRecaudadora.class));
    }

    @Override
    public List<FilaPerfilComercial> consultarPerfilComercial(String idPerfilComercialBase) {
        return Sneaky.sinChequear(() -> sql.consultar(SQL_VALIDAR_PERFIL_COMERCIAL_APV,
                parametros("p_IdPerfilComercialBase", idPerfilComercialBase), FilaPerfilComercial.class));
    }

    @Override
    public void cerrarContratos(List<ContratoACerrar> contratos) {
        Sneaky.sinChequear(() -> sql.lote(SQL_UPDATE_CONTRATO, contratos));
    }
}
