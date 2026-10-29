package cl.bcs.tesoreria.javacall;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.AnulacionContratoCSC;
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV;
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV.ContratosSAAPV;
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV.ContratosSAAPV_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.AnulacionMandatoPAC} (TES-B-135..147): annuls PAC mandates (state ANU) and propagates the annulment to the related CSC
 * contracts and SAAPV contracts through their services. Called by {@link IngresarMandatoPACService} to annul the mandates a new one replaces.
 * The request is decoded with a plain Gson, as the legacy {@code call} did.
 */
@Service
public class AnulacionMandatoPACService {

    private static final Logger log = LoggerFactory.getLogger(AnulacionMandatoPACService.class);
    private static final Gson GSON = new Gson();

    private static final String SQL_MANDATO_UPDATE = SqlNombrado.recurso("MandatoPAC_Anulacion_UpdateContratoPAC");
    private static final String SQL_SELECT_CONTRATO = SqlNombrado.recurso("MandatoPAC_Anulacion_SelectContratos");
    private static final String SQL_SELECT_CONTRATO_SAAPV = SqlNombrado.recurso("MandatoPAC_Anulacion_SelectContratosSAAPV");

    // ------------------------------------------------------------------------------------------------ contract

    public static class InContrato {
        public Long IdContrato;
        public String Estado = "";
    }

    public static class UpdContrato {
        public Long IdContrato;
        public String Estado;
    }

    public static class ContratoCSCRelacionado {
        public Long IdMandatoPAC;
        public Long IdFolioCSC;
        public String Estado = "";
    }

    public static class EntradaAnulacionContrato {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<InContrato> p_Contrato = new ArrayList<>();
        public String p_DesdeCSC = "N";
        public String p_DesdeSAAPV = "N";
    }

    public static class SalidaAnulacionContrato {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        /** Legacy {@code List<ResultadoSolicitud>}: never filled by this service. */
        public List<Object> P_OUT_RESULTADO = new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------------ service

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public AnulacionMandatoPACService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** JavaCall entry point: legacy {@code call(conn, json)}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaAnulacionContrato jin = GSON.fromJson(entrada.toString(), EntradaAnulacionContrato.class);
        SalidaAnulacionContrato out = anular(jin);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaAnulacionContrato anular(EntradaAnulacionContrato entrada) throws Exception {
        log.debug("INICIO SalidaAnulacionContrato");
        SalidaAnulacionContrato out = new SalidaAnulacionContrato();

        RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        if (entrada.p_Contrato.isEmpty()) {
            return error(out, entrada, "NO_SE_INGRESARON_MODIFICACIONES_DE_TRASPASO");
        }

        List<Long> correlativos = new ArrayList<>();
        for (InContrato mov : entrada.p_Contrato) {
            if (mov.IdContrato.longValue() == 0L) {
                return error(out, entrada, "ID_INTERNO_NO_CERO");
            }
            if (correlativos.contains(mov.IdContrato)) {
                return error(out, entrada, "CORRELATIIVO_INCORRECTO"); // sic: the legacy code (and its dictionary key) is misspelled
            }
            correlativos.add(mov.IdContrato);
        }

        List<Long> idMandatos = entrada.p_Contrato.stream().map(m -> m.IdContrato).toList();

        if (!entrada.p_DesdeCSC.equals("S")) {
            AnulacionContratoCSC.EntradaAnulacionContrato anulaCsc = new AnulacionContratoCSC.EntradaAnulacionContrato();
            anulaCsc.p_IdEmpresa = entrada.p_IdEmpresa;
            anulaCsc.p_IdUsuario = entrada.p_IdUsuario;
            anulaCsc.p_IdModulo = "FMM";
            anulaCsc.p_DesdePac = "S";

            List<ContratoCSCRelacionado> relacionados = sql.consultar(SQL_SELECT_CONTRATO, Map.of("IdMandatoPAC", idMandatos), ContratoCSCRelacionado.class);
            for (InContrato mov : entrada.p_Contrato) {
                ContratoCSCRelacionado contrato = relacionados.stream().filter(m -> Objects.equals(m.IdMandatoPAC, mov.IdContrato)).findFirst().orElse(null);
                if (contrato.IdFolioCSC != null) { // a mandate without a row here is a NullPointerException, as in the legacy
                    AnulacionContratoCSC.InContrato anular = new AnulacionContratoCSC.InContrato();
                    anular.IdContrato = contrato.IdFolioCSC;
                    anular.Estado = contrato.Estado;
                    anulaCsc.p_Contrato.add(anular);
                }
            }

            if (!anulaCsc.p_Contrato.isEmpty()) {
                AnulacionContratoCSC.SalidaAnulacionContrato salida = AnulacionContratoCSC.invocar(colaboradores, anulaCsc);
                if (salida.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
                    out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
                    out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
                    return out;
                }
                out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
            }
        }

        List<UpdContrato> actualizar = new ArrayList<>();
        for (Long procesar : correlativos) {
            UpdContrato reg = new UpdContrato();
            reg.IdContrato = procesar;
            actualizar.add(reg);
        }
        if (!actualizar.isEmpty()) {
            sql.lote(SQL_MANDATO_UPDATE, actualizar);
            for (UpdContrato salida : actualizar) {
                Map<String, String> valores = new HashMap<>();
                valores.put("IdContrato", salida.IdContrato.toString());
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_OK, "ANULACION_MANDATO_PAC_EXITOSA", valores));
            }
        }

        if (!entrada.p_DesdeSAAPV.equals("S")) {
            ContratosSAAPV_Entrada anulaSaapv = new ContratosSAAPV_Entrada();
            anulaSaapv.p_IdEmpresa = entrada.p_IdEmpresa;
            anulaSaapv.p_IdUsuario = entrada.p_IdUsuario;
            anulaSaapv.p_IdModulo = entrada.p_IdModulo;
            anulaSaapv.p_Opcion = "ANU";
            anulaSaapv.p_DesdePac = "S";

            List<ContratosSAAPV> relacionados = sql.consultar(SQL_SELECT_CONTRATO_SAAPV, Map.of("IdMandatoPAC", idMandatos), ContratosSAAPV.class);
            for (ContratosSAAPV mov : relacionados) {
                ContratosSAAPV anular = new ContratosSAAPV();
                anular.IdFolioContratoSAAPV = mov.IdFolioContratoSAAPV;
                anular.IdEstado = "ANU";
                anular.IdPersona = mov.IdPersona;
                anular.IdCliente = mov.IdCliente;
                anular.IdEstadoSolicitud = "ANU";
                anulaSaapv.p_ContratosSAAPV.add(anular);
            }

            if (!anulaSaapv.p_ContratosSAAPV.isEmpty()) {
                ContratosSAAPV_Salida salida = CambiarEstadoContratosSAAPV.invocar(colaboradores, anulaSaapv);
                if (salida.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
                    out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
                    out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
                    return out;
                }
                out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
            }
        }
        return out;
    }

    private SalidaAnulacionContrato error(SalidaAnulacionContrato out, EntradaAnulacionContrato entrada, String codigo) {
        out.P_OUT_ERRORESCONTADOR = 1L;
        out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>()));
        return out;
    }
}
