package cl.bcs.tesoreria.javacall;

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
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV;
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV.ContratosSAAPV;
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CambiarEstadoContratosSAAPV.ContratosSAAPV_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.javacall.kit.colaborador.RevocacionContratoCSC;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.RevocacionMandatoPAC} (TES-B-148..159): revokes PAC mandates (state RVC) and propagates the revocation to the related CSC
 * contracts and SAAPV contracts through their services. The request is decoded with a plain Gson, as the legacy {@code call} did.
 */
@Service
public class RevocacionMandatoPACService {

    private static final Logger log = LoggerFactory.getLogger(RevocacionMandatoPACService.class);
    private static final Gson GSON = new Gson();

    private static final String SQL_MANDATO_UPDATE = SqlNombrado.recurso("Mandato_Revocacion_UpdateMandatoPAC");
    private static final String SQL_SELECT_CONTRATO = SqlNombrado.recurso("MandatoPAC_Revocacion_SelectContratos");
    private static final String SQL_SELECT_CONTRATO_SAAPV = SqlNombrado.recurso("MandatoPAC_Revocacion_SelectContratosSAAPV");

    // ------------------------------------------------------------------------------------------------ contract

    public static class InMandato {
        public Long IdMandato;
        public String Estado = "";
    }

    public static class UpdMandato {
        public Long IdMandato;
        public String Estado;
    }

    public static class ContratoCSCRelacionado {
        public Long IdMandatoPAC;
        public Long IdFolioCSC;
        public String FolioCSC;
        public String Estado = "";
    }

    public static class EntradaRevocacionMandato {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<InMandato> p_Mandato = new ArrayList<>();
        public String p_DesdeCSC = "N";
        public String p_DesdeSAAPV = "N";
    }

    public static class SalidaRevocacionMandato {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        /** Legacy {@code List<ResultadoSolicitud>}: never filled by this service. */
        public List<Object> P_OUT_RESULTADO = new ArrayList<>();
    }

    // ------------------------------------------------------------------------------------------------ service

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public RevocacionMandatoPACService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** JavaCall entry point: legacy {@code call(conn, json)}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaRevocacionMandato jin = GSON.fromJson(entrada.toString(), EntradaRevocacionMandato.class);
        SalidaRevocacionMandato out = revocar(jin);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    /** Legacy {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaRevocacionMandato revocar(EntradaRevocacionMandato entrada) throws Exception {
        log.debug("INICIO SalidaRevocacionMandato");
        SalidaRevocacionMandato out = new SalidaRevocacionMandato();

        RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
        if (MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            return out;
        }

        if (entrada.p_Mandato.isEmpty()) {
            return error(out, entrada, "NO_SE_INGRESARON_MODIFICACIONES_DE_TRASPASO");
        }

        List<Long> correlativos = new ArrayList<>();
        for (InMandato mov : entrada.p_Mandato) {
            if (mov.IdMandato.longValue() == 0L) {
                return error(out, entrada, "ID_INTERNO_NO_CERO");
            }
            if (correlativos.contains(mov.IdMandato)) {
                return error(out, entrada, "CORRELATIIVO_INCORRECTO"); // sic: the legacy code (and its dictionary key) is misspelled
            }
            correlativos.add(mov.IdMandato);
        }

        List<Long> idMandatos = entrada.p_Mandato.stream().map(m -> m.IdMandato).toList();

        if (!entrada.p_DesdeCSC.equals("S")) {
            RevocacionContratoCSC.EntradaRevocacionContrato revocaCsc = new RevocacionContratoCSC.EntradaRevocacionContrato();
            revocaCsc.p_IdEmpresa = entrada.p_IdEmpresa;
            revocaCsc.p_IdUsuario = entrada.p_IdUsuario;
            revocaCsc.p_IdModulo = "FMM";
            revocaCsc.p_DesdePac = "S";

            List<ContratoCSCRelacionado> relacionados = sql.consultar(SQL_SELECT_CONTRATO, Map.of("IdMandatoPAC", idMandatos), ContratoCSCRelacionado.class);
            for (InMandato mov : entrada.p_Mandato) {
                ContratoCSCRelacionado contrato = relacionados.stream().filter(m -> Objects.equals(m.IdMandatoPAC, mov.IdMandato)).findFirst().orElse(null);
                if (contrato.IdFolioCSC != null) { // a mandate without a row here is a NullPointerException, as in the legacy
                    RevocacionContratoCSC.InContrato revocar = new RevocacionContratoCSC.InContrato();
                    revocar.IdContrato = contrato.IdFolioCSC;
                    revocar.Estado = contrato.Estado;
                    revocaCsc.p_Contrato.add(revocar);
                }
            }

            if (!revocaCsc.p_Contrato.isEmpty()) {
                RevocacionContratoCSC.SalidaRevocacionContrato salida = RevocacionContratoCSC.invocar(colaboradores, revocaCsc);
                if (salida.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
                    out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
                    out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
                    return out;
                }
                out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
            }
        }

        List<UpdMandato> actualizar = new ArrayList<>();
        for (Long procesar : correlativos) {
            UpdMandato reg = new UpdMandato();
            reg.IdMandato = procesar;
            actualizar.add(reg);
        }
        if (!actualizar.isEmpty()) {
            sql.lote(SQL_MANDATO_UPDATE, actualizar);
            for (UpdMandato salida : actualizar) {
                Map<String, String> valores = new HashMap<>();
                valores.put("IdContrato", salida.IdMandato.toString());
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_OK, "REVOCACION_MANDATO_PAC_EXITOSA", valores));
            }
        }

        if (!entrada.p_DesdeSAAPV.equals("S")) {
            ContratosSAAPV_Entrada revocaSaapv = new ContratosSAAPV_Entrada();
            revocaSaapv.p_IdEmpresa = entrada.p_IdEmpresa;
            revocaSaapv.p_IdUsuario = entrada.p_IdUsuario;
            revocaSaapv.p_IdModulo = entrada.p_IdModulo;
            revocaSaapv.p_Opcion = "REV";
            revocaSaapv.p_DesdePac = "S";

            List<ContratosSAAPV> relacionados = sql.consultar(SQL_SELECT_CONTRATO_SAAPV, Map.of("IdMandatoPAC", idMandatos), ContratosSAAPV.class);
            for (ContratosSAAPV mov : relacionados) {
                if (mov.IdFolioContratoSAAPV != null) {
                    ContratosSAAPV revocar = new ContratosSAAPV();
                    revocar.IdFolioContratoSAAPV = mov.IdFolioContratoSAAPV;
                    revocar.IdEstado = "REV";
                    revocar.IdPersona = mov.IdPersona;
                    revocar.IdCliente = mov.IdCliente;
                    revocar.IdEstadoSolicitud = "REV";
                    revocaSaapv.p_ContratosSAAPV.add(revocar);
                }
            }

            if (!revocaSaapv.p_ContratosSAAPV.isEmpty()) {
                ContratosSAAPV_Salida salida = CambiarEstadoContratosSAAPV.invocar(colaboradores, revocaSaapv);
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

    private SalidaRevocacionMandato error(SalidaRevocacionMandato out, EntradaRevocacionMandato entrada, String codigo) {
        out.P_OUT_ERRORESCONTADOR = 1L;
        out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>()));
        return out;
    }
}
