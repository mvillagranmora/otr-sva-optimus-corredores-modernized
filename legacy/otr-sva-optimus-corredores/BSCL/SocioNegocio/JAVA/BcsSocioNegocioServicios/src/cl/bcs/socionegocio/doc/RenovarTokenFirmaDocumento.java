package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenCorredora;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.socionegocio.soc.pojo.ResumenPersona;
import cl.bcs.utils.AgregarNotificacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RenovarTokenFirmaDocumento implements JavaCall {
    static final Logger logger = LogManager.getLogger(RenovarTokenFirmaDocumento.class);

    private final String SQL_SELECT_TOKEN_FIRMA_ANEXO_CLIENTE = loadResourceToString("cl/bcs/socionegocio/doc/sql/RenovarTokenFirmaDocumento_SelectTokenFirmaAnexo.sql");
    private final String SQL_UPDATE_TOKEN_FIRMA_ANEXO_FIRMANTE = loadResourceToString("cl/bcs/socionegocio/doc/sql/RenovarTokenFirmaDocumento_UpdateTokenFirmaAnexoFirmante.sql");

    private final int SEGUNDOS_EN_MINUTO = 60;

    public static class EntradaRenovarTokenFirmaDocumento extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public Long p_IdAnexo;
        public Long p_IdPersonaSolicitante;
        public String p_DocumentoIdentidadSolicitante;
        public String p_IdTipoCanalIngreso;
    }

    public static class SalidaRenovarTokenFirmaDocumento extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<AgregarNotificacion.IdentificadorEvento> P_OUT_IDEVENTO;

        public SalidaRenovarTokenFirmaDocumento() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_IDEVENTO = new ArrayList<>();
        }

        public void agregarValidacionesSalida(List<Validacion> validaciones) {
            for (Validacion validacion : validaciones) {
                agregarValidacionSalida(validacion);
            }
        }

        public void agregarValidacionSalida(Validacion validacion) {
            if (validacion.EsError) {
                this.P_OUT_ERRORESCONTADOR += 1L;
                this.P_OUT_MENSAJES.add(validacion.MensajeError);
            }
        }
    }

    public static class TokenFirmaAnexo extends BcsPojo {
        public Long IdTokenFirmaAnexo;
        public Long IdSolicitudFirmaAnexo;
        public java.sql.Timestamp FechaGeneracion;
        public java.sql.Timestamp FechaExpiracion;
        public String Token;

        public String FechaGeneracionLegible;
        public String FechaExpiracionLegible;
    }

    public static class Notificacion extends BcsPojo {
        public Long IdUsuario;
        public String RazonCorredora;
        public String TelefonoCorredora;
    }

    public static class Usuario extends BcsPojo {
        public Long IdUsuario;
        public Long IdPersona;
    }

    public static class ResultadoNotificarUsuario extends BcsPojo {
        public List<Validacion> Validaciones;
        public List<AgregarNotificacion.IdentificadorEvento> IdentificadoresEventos;

        public ResultadoNotificarUsuario() {
            Validaciones = new ArrayList<>();
            IdentificadoresEventos = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaRenovarTokenFirmaDocumento jin = gson.fromJson(in.toString(), EntradaRenovarTokenFirmaDocumento.class);
        SalidaRenovarTokenFirmaDocumento out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaRenovarTokenFirmaDocumento logica(Connection conn, EntradaRenovarTokenFirmaDocumento entrada) throws Exception {
        logger.info("INICIO::RenovarTokenFirmaDocumento");
        long inicio = System.currentTimeMillis();

        SalidaRenovarTokenFirmaDocumento salida = new SalidaRenovarTokenFirmaDocumento();

        ResumenPersona resumenPersonaSolicitante = obtenerResumenPersona(conn, entrada);

        ResumenDocumentoAnexo resumenAnexo = SocioNegocioUtils.obtenerResumenDocumentoAnexoPorIdAnexo(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdAnexo))).get(entrada.p_IdAnexo);
        if (resumenAnexo == null) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_CLIENTE_ANEXO", new HashMap<>(), conn, entrada.p_IdEmpresa);
            salida.agregarValidacionSalida(validacion);

            return salida;
        }

        TokenFirmaAnexo tokenFirmaAnexo = obtenerTokenFirmaAnexo(conn, entrada, resumenPersonaSolicitante);
        if (tokenFirmaAnexo.IdTokenFirmaAnexo == null) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_SOLICITUD_FIRMA_PARA_USUARIO", new HashMap<>(), conn, entrada.p_IdEmpresa);
            salida.agregarValidacionSalida(validacion);

            return salida;
        }

        actualizarToken(conn, entrada, tokenFirmaAnexo);

        ResumenCorredora resumenCorredora = SocioNegocioUtils.obtenerResumenCorredora(conn, entrada.p_IdEmpresa);

        ResultadoNotificarUsuario resultadoNotificarUsuario = notificarUsuario(conn, entrada, resumenAnexo, tokenFirmaAnexo, resumenCorredora, resumenPersonaSolicitante);
        salida.agregarValidacionesSalida(resultadoNotificarUsuario.Validaciones);
        salida.P_OUT_IDEVENTO.addAll(resultadoNotificarUsuario.IdentificadoresEventos);

        long fin = System.currentTimeMillis();
        logger.info("FIN::RenovarTokenFirmaDocumento", fin - inicio);

        return salida;
    }

    private ResumenPersona obtenerResumenPersona(Connection conn, EntradaRenovarTokenFirmaDocumento entrada) throws SQLException {
        logger.debug("INICIO::RenovarTokenFirmaDocumento.obtenerResumenPersona");
        long inicio = System.currentTimeMillis();

        ResumenPersona resumenPersona;
        if (entrada.p_IdPersonaSolicitante == null) {
            resumenPersona = SocioNegocioUtils.obtenerResumenPersonaPorDocumentoIdentidad(conn, new ArrayList<>(Collections.singletonList(entrada.p_DocumentoIdentidadSolicitante))).getOrDefault(entrada.p_DocumentoIdentidadSolicitante, new ResumenPersona());
        } else {
            resumenPersona = SocioNegocioUtils.obtenerResumenPersonaPorIdPersona(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdPersonaSolicitante))).getOrDefault(entrada.p_IdPersonaSolicitante, new ResumenPersona());
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::RenovarTokenFirmaDocumento.obtenerResumenPersona", fin - inicio);

        return resumenPersona;
    }

    private TokenFirmaAnexo obtenerTokenFirmaAnexo(Connection conn, EntradaRenovarTokenFirmaDocumento entrada, ResumenPersona resumenPersona) throws SQLException {
        logger.debug("INICIO::RenovarTokenFirmaDocumento.obtenerTokenFirmaAnexo");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(4);
        params.put("IdPersonaFirmante", resumenPersona.IdPersona);
        params.put("IdAnexo", entrada.p_IdAnexo);

        SQL sql = new SQL();
        TokenFirmaAnexo tokenFirmaAnexo = sql.query(conn, SQL_SELECT_TOKEN_FIRMA_ANEXO_CLIENTE, params, TokenFirmaAnexo.class).stream().findFirst().orElse(new TokenFirmaAnexo());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::RenovarTokenFirmaDocumento.obtenerTokenFirmaAnexo", fin - inicio);

        return tokenFirmaAnexo;
    }

    private void actualizarToken(Connection conn, EntradaRenovarTokenFirmaDocumento entrada, TokenFirmaAnexo tokenFirmaAnexo) throws Exception {
        logger.debug("INICIO::RenovarTokenFirmaDocumento.actualizarToken");
        long inicio = System.currentTimeMillis();

        final int largoToken = Integer.valueOf(Utils.getConfiguracion(conn, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.LargoTokenFirmaDocumento"), 10);
        final int minutosDuracionToken = Integer.valueOf(Utils.getConfiguracion(conn, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.MinutosDuracionTokenFirmaDocumento"), 10);
        final int segundosDuracionToken = minutosDuracionToken * SEGUNDOS_EN_MINUTO;

        final java.sql.Timestamp fechaGeneracionToken = FechaDB.ObtenerFechaHoraTs(conn, entrada.p_IdEmpresa);
        final java.sql.Timestamp fechaExpiracionToken = Fecha.addSeconds(fechaGeneracionToken, segundosDuracionToken);

        final String fechaGeneracionTokenLegible = Fecha.toFormat(fechaGeneracionToken, "hh:mm a dd MMMM yyyy");
        final String fechaExpiracionTokenLegible = Fecha.toFormat(fechaExpiracionToken, "hh:mm a dd MMMM yyyy");

        tokenFirmaAnexo.Token = Utils.generateAlphanumericToken(largoToken);
        tokenFirmaAnexo.FechaGeneracion = fechaGeneracionToken;
        tokenFirmaAnexo.FechaExpiracion = fechaExpiracionToken;
        tokenFirmaAnexo.FechaGeneracionLegible = fechaGeneracionTokenLegible;
        tokenFirmaAnexo.FechaExpiracionLegible = fechaExpiracionTokenLegible;

        SQL sql = new SQL();
        sql.batch(conn, SQL_UPDATE_TOKEN_FIRMA_ANEXO_FIRMANTE, Collections.singletonList(tokenFirmaAnexo), TokenFirmaAnexo.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::RenovarTokenFirmaDocumento.actualizarToken", fin - inicio);
    }

    private ResultadoNotificarUsuario notificarUsuario(Connection conn, EntradaRenovarTokenFirmaDocumento entrada, ResumenDocumentoAnexo resumenDocumentoAnexo, TokenFirmaAnexo tokenFirmaAnexo, ResumenCorredora resumenCorredora, ResumenPersona resumenPersona) throws Exception {
        logger.debug("INICIO::RenovarTokenFirmaDocumento.notificarUsuario");
        long inicio = System.currentTimeMillis();

        ResultadoNotificarUsuario resultadoNotificarUsuario = new ResultadoNotificarUsuario();

        final long idEvento = 0L;
        List<Long> idsDireccionesElectronicasUsuario = SocioNegocioUtils
                .obtenerDireccionesElectronicasPorIdPersona(conn, new ArrayList<>(Collections.singletonList(resumenPersona.IdPersona)))
                .computeIfAbsent(resumenDocumentoAnexo.IdPersona, idPersona -> new ArrayList<>());
        if (idsDireccionesElectronicasUsuario.isEmpty()) {
            HashMap<String, String> params = new HashMap<>(2);
            params.put("IdPersona", String.valueOf(resumenDocumentoAnexo.IdPersona));

            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(resumenDocumentoAnexo.IdPersona, MensajeSalida.MENSAJE_ERROR, "UI_PERSONA_SIN_CORREOS_ELECTRONICOS_ACTIVOS", params, conn, entrada.p_IdEmpresa);

            resultadoNotificarUsuario.Validaciones.add(validacion);
        }

        if (resultadoNotificarUsuario.Validaciones.isEmpty()) {
            String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa);

            AgregarNotificacion.EntradaAgregarNotificacion entradaAgregarNotificacion = new AgregarNotificacion.EntradaAgregarNotificacion();
            entradaAgregarNotificacion.P_IDEMPRESA = entrada.p_IdEmpresa;
            entradaAgregarNotificacion.P_IDUSUARIO = entrada.p_IdUsuario;
            entradaAgregarNotificacion.P_IDMODULO = "SOC";
            entradaAgregarNotificacion.P_EVENTOS = new ArrayList<>();
            entradaAgregarNotificacion.P_VALORES = new ArrayList<>();
            entradaAgregarNotificacion.P_ARCHIVOSADJUNTOS = new ArrayList<>();
            entradaAgregarNotificacion.P_IDDIRECCIONES = new ArrayList<>();

            AgregarNotificacion.Evento eventoNotificacion = new AgregarNotificacion.Evento();
            eventoNotificacion.CodigoTipoEvento = "NOTIF_TOKEN_FIRMA_CONTRATO";
            eventoNotificacion.IdSecuencia = idEvento;
            eventoNotificacion.FechaCreacionSistema = fechaSistema;
            eventoNotificacion.IdPersonaRemitente = 0L;
            eventoNotificacion.IdPersonaDestinatario = resumenPersona.IdPersona;
            eventoNotificacion.IdPortafolio = 0L;
            entradaAgregarNotificacion.P_EVENTOS.add(eventoNotificacion);

            for (Long idDireccionEletronica : idsDireccionesElectronicasUsuario) {
                AgregarNotificacion.DireccionEvento direccionEventoNotificacion = new AgregarNotificacion.DireccionEvento();
                direccionEventoNotificacion.IdSecuencia = idEvento;
                direccionEventoNotificacion.TipoDestinatario = "TO";
                direccionEventoNotificacion.IdDireccion = idDireccionEletronica;
                entradaAgregarNotificacion.P_IDDIRECCIONES.add(direccionEventoNotificacion);
            }

            entradaAgregarNotificacion
                    .addValor(idEvento, "CLIENTE", resumenDocumentoAnexo.NombreCompletoCliente)
                    .addValor(idEvento, "AGENTE", resumenDocumentoAnexo.NombreCompletoAgente)
                    .addValor(idEvento, "DOCUMENTOIDENTIDAD", resumenDocumentoAnexo.DocumentoIdentidadCliente)
                    .addValor(idEvento, "TOKEN", tokenFirmaAnexo.Token)
                    .addValor(idEvento, "FECHAEXPIRACION", tokenFirmaAnexo.FechaExpiracionLegible)
                    .addValor(idEvento, "CORREDORA", resumenCorredora.RazonCorredora)
                    .addValor(idEvento, "ATENCIONCLIENTE", resumenCorredora.TelefonoCorredora)
                    .addValor(idEvento, "NOMBRECONTRATO", resumenDocumentoAnexo.NombreDocumento)
                    .addValor(idEvento, "ZONAHORARIA", "UTC")
                    .addValor(idEvento, "FIRMANTE", resumenPersona.NombreCompleto);

            AgregarNotificacion agregarNotificacion = new AgregarNotificacion();
            List<AgregarNotificacion.IdentificadorEvento> idsEventos = agregarNotificacion.logica(conn, entradaAgregarNotificacion).P_OUT_IDEVENTO;
            resultadoNotificarUsuario.IdentificadoresEventos.addAll(idsEventos);
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::RenovarTokenFirmaDocumento.notificarUsuario {}", fin - inicio);

        return resultadoNotificarUsuario;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        EntradaRenovarTokenFirmaDocumento entradaRenovarTokenFirmaDocumento = new EntradaRenovarTokenFirmaDocumento();
        entradaRenovarTokenFirmaDocumento.p_IdEmpresa = 1L;
        entradaRenovarTokenFirmaDocumento.p_IdAnexo = 44L;
        entradaRenovarTokenFirmaDocumento.p_IdUsuario = 106139L;
        entradaRenovarTokenFirmaDocumento.p_IdPersonaSolicitante = 459500L;
        entradaRenovarTokenFirmaDocumento.p_IdTipoCanalIngreso = "CLI";

        try (Connection conn = PlataformaUtils.getUAT2Connection()) {
            try {
                RenovarTokenFirmaDocumento renovarTokenFirmaDocumento = new RenovarTokenFirmaDocumento();
                SalidaRenovarTokenFirmaDocumento salidaRenovarTokenFirmaDocumento = renovarTokenFirmaDocumento.logica(conn, entradaRenovarTokenFirmaDocumento);
                System.out.println(salidaRenovarTokenFirmaDocumento);

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
