package cl.bcs.socionegocio.doc;


import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.doc.pojo.FirmaDocumento;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenCorredora;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.socionegocio.soc.pojo.ResumenPersona;
import cl.bcs.socionegocio.soc.pojo.ResumenUsuario;
import cl.bcs.utils.AgregarNotificacion;
import cl.bcs.utils.AgregarNotificacion.IdentificadorEvento;
import cl.bcs.utils.AgregarNotificacion.SalidaAgregarNotificacion;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SolicitarFirmaDocumento implements JavaCall {
    static final Logger logger = LogManager.getLogger(SolicitarFirmaDocumento.class);

    private static final String SEQUENCE_FIRMA_ANEXO = "BSCL.Plataforma.BD.Secuencias::FirmaAnexo";
    private static final String SEQUENCE_TOKEN_FIRMA_ANEXO = "BSCL.Plataforma.BD.Secuencias::TokenFirmaAnexo";
    private static final String SEQUENCE_SOLICITUD_FIRMA_ANEXO = "BSCL.Plataforma.BD.Secuencias::SolicitudFirmaAnexo";

    private final String SQL_INSERT_FIRMA_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/SolicitarFirmaDocumento_InsertFirmaAnexo.sql");
    private final String SQL_INSERT_TOKEN_FIRMA_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/SolicitarFirmaDocumento_InsertTokenFirmaAnexo.sql");
    private final String SQL_INSERT_SOLICITUD_FIRMA_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/SolicitarFirmaDocumento_InsertSolicitudFirmaAnexo.sql");

    private final String SQL_SELECT_EXISTE_SOLICITUD_FIRMA_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/SolicitarFirmaDocumento_SelectExisteSolicitudFirmaAnexo.sql"); 

    private final String SQL_SELECT_CANALES_FIRMA_DOCUMENTO = loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarFirmaCanalesDocumento.sql");
    
    


    private final int SEGUNDOS_EN_MINUTO = 60; 

    public static class EntradaSolicitarFirmaDocumento extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public Long p_IdAnexo;
        public Long p_IdPersonaSolicitante;
        public String p_DocumentoIdentidadSolicitante;
        public String p_IdTipoCanalIngreso;
    }
 
    public static class SalidaSolicitarFirmaDocumento extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<IdentificadorEvento> P_OUT_IDEVENTO;

        public SalidaSolicitarFirmaDocumento() {
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

    public static class FirmaAnexo extends BcsPojo {
        public Long IdFirmaAnexo;
        public Long IdAnexo;
        public Long IdSolicitudFirmaAnexo;
        public String IdEstado;
        public String IdTipoCanalIngreso;
        public Long IdUsuario;
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

    public static class SolicitudFirmaAnexo extends BcsPojo {
        public Long IdSolicitudFirmaAnexo;
        public Long IdPersonaSolicitante;
        public Long IdPersonaFirmante;
        public String IdTipoCanalIngreso;
        public String NombreCompletoFirmante;
        public TokenFirmaAnexo tokenFirmaAnexo;
    }

    public static class EntidadesFirma extends BcsPojo {
        public List<FirmaAnexo> firmasAnexo;
        public List<SolicitudFirmaAnexo> solicitudesFirmasAnexo;
        public List<TokenFirmaAnexo> tokensFirmasAnexo;

        public EntidadesFirma() {
            this.firmasAnexo = new ArrayList<>();
            this.solicitudesFirmasAnexo = new ArrayList<>();
            this.tokensFirmasAnexo = new ArrayList<>();
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaSolicitarFirmaDocumento jin = gson.fromJson(in.toString(), EntradaSolicitarFirmaDocumento.class);
        SalidaSolicitarFirmaDocumento out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    public SalidaSolicitarFirmaDocumento logica(Connection conn, EntradaSolicitarFirmaDocumento entrada){
        logger.info("INICIO::SolicitarFirmaDocumento");
        long inicio = System.currentTimeMillis();

        SalidaSolicitarFirmaDocumento salida = new SalidaSolicitarFirmaDocumento();
        String validarTokenFirmaDocumento = "S";     
 
        try {
        	SQL sql = new SQL();
        	HashMap<String,Object> paramsIN = new HashMap<>();
        	paramsIN.put("AnexoDocumento", entrada.p_IdAnexo ); 
        	paramsIN.put("Canal", entrada.p_IdTipoCanalIngreso ); 	
        	if ( sql.countQuery(conn, SQL_SELECT_CANALES_FIRMA_DOCUMENTO, paramsIN)==0 ) {		// FirmaDocumentosBE -  Se verifica que antes de solicitar la firma se comprueba que el documento este configurado para el canal de solicitud de firma
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CANAL_NO_CONFIGURADO", new HashMap<>(), conn, entrada.p_IdEmpresa);
                salida.agregarValidacionSalida(validacion);
                return salida;
        	} 
        }catch (Exception e) { }   
        
        try
        {
        	validarTokenFirmaDocumento = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "DOC.ValidarTokenFirmaDocumento");
        }
        catch(Exception e)
        {
        	validarTokenFirmaDocumento = "S";  //Valor por defecto
        }
        
 
        
        try {
        ResumenPersona resumenPersonaSolicitante = obtenerResumenPersona(conn, entrada);

        ResumenDocumentoAnexo resumenAnexo = SocioNegocioUtils.obtenerResumenDocumentoAnexoPorIdAnexo(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdAnexo))).get(entrada.p_IdAnexo);
        if (resumenAnexo == null) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_CLIENTE_ANEXO", new HashMap<>(), conn, entrada.p_IdEmpresa);
            salida.agregarValidacionSalida(validacion);

            return salida;
        }

        boolean existeSolicitudFirmaDocumento = existeSolicitudFirmaDocumento(conn, entrada);
        if (existeSolicitudFirmaDocumento) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(entrada.p_IdAnexo, MensajeSalida.MENSAJE_ERROR, "UI_EXISTE_SOLICITUD_FIRMA_ANEXO", new HashMap<>(), conn, entrada.p_IdEmpresa);
            salida.agregarValidacionSalida(validacion);

            return salida;
        }

        List<ResumenPersona> firmantesCliente = SocioNegocioUtils.obtenerFirmantesPersona(conn, resumenAnexo.IdPersona);

        EntidadesFirma entidadesFirma = obtenerEntidadesFirma(conn, entrada, resumenPersonaSolicitante, firmantesCliente);

        boolean socioNoEsFirmante = !socioEsFirmante(entidadesFirma.solicitudesFirmasAnexo, resumenPersonaSolicitante);
        if (socioNoEsFirmante) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_SOLICITANTE_FIRMA_NO_ES_FIRMANTE_CLIENTE", new HashMap<>(), conn, entrada.p_IdEmpresa);
            salida.agregarValidacionSalida(validacion);

            return salida;
        }

        

        insertarSolicitudesFirma(conn, entidadesFirma.solicitudesFirmasAnexo);
        insertarTokenFirmas(conn, entidadesFirma.tokensFirmasAnexo);
        insertarAnexoFirmas(conn, entidadesFirma.firmasAnexo);

        //La opcion de NO validar el token debe darse SOLO via WS (Esta opcion permite solicitar la firma y firmar el documento de una. No se envia el mail con el token y este no se valida)
        if (!(validarTokenFirmaDocumento.equalsIgnoreCase("N") &&  ((entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("WBS")||entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("APP")||entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("OWP") )) )  )
        {
        	ResumenCorredora resumenCorredora = SocioNegocioUtils.obtenerResumenCorredora(conn, entrada.p_IdEmpresa);
        	notificarFirmantes(conn, entrada, entidadesFirma.solicitudesFirmasAnexo, resumenAnexo, resumenCorredora);
        }
        
        }catch(Exception e) {
        	
        }
        
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::SolicitarFirmaDocumento", fin - inicio);

        return salida;
    }

    private ResumenPersona obtenerResumenPersona(Connection conn, EntradaSolicitarFirmaDocumento entrada) throws SQLException {
        logger.debug("INICIO::SolicitarFirmaDocumento.obtenerResumenPersona");
        long inicio = System.currentTimeMillis();

        ResumenPersona resumenPersona;
        if (entrada.p_IdPersonaSolicitante == null) {
            resumenPersona = SocioNegocioUtils.obtenerResumenPersonaPorDocumentoIdentidad(conn, new ArrayList<>(Collections.singletonList(entrada.p_DocumentoIdentidadSolicitante))).getOrDefault(entrada.p_DocumentoIdentidadSolicitante, new ResumenPersona());
        } else {
            resumenPersona = SocioNegocioUtils.obtenerResumenPersonaPorIdPersona(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdPersonaSolicitante))).getOrDefault(entrada.p_IdPersonaSolicitante, new ResumenPersona());
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::SolicitarFirmaDocumento.obtenerResumenPersona", fin - inicio);

        return resumenPersona;
    }

    public boolean existeSolicitudFirmaDocumento(Connection conn, EntradaSolicitarFirmaDocumento entrada) throws SQLException {
        logger.debug("INICIO::SolicitarFirmaDocumento.existeSolicitudFirmaDocumento");
        long inicio = System.currentTimeMillis();

        HashMap<String, Object> params = new HashMap<>(2);
        params.put("IdAnexo", entrada.p_IdAnexo);

        SQL sql = new SQL();
        boolean tieneSolicitudFirma = sql.countQuery(conn, SQL_SELECT_EXISTE_SOLICITUD_FIRMA_ANEXO, params) > 0;

        long fin = System.currentTimeMillis();
        logger.debug("FIN::SolicitarFirmaDocumento.existeSolicitudFirmaDocumento", fin - inicio);

        return tieneSolicitudFirma;
    }

    private SalidaAgregarNotificacion notificarFirmantes(Connection conn, EntradaSolicitarFirmaDocumento entrada, List<SolicitudFirmaAnexo> firmantes, ResumenDocumentoAnexo resumenAnexo, ResumenCorredora resumenCorredora) throws Exception {
        logger.debug("INICIO::SolicitarFirmaDocumento.notificarFirmantes");
        long inicio = System.currentTimeMillis();

        List<Long> personasIds = firmantes.stream().map(firmante -> firmante.IdPersonaFirmante).collect(Collectors.toList());
        Map<Long, List<Long>> idDireccionesPorIdPersona = SocioNegocioUtils.obtenerDireccionesElectronicasPorIdPersona(conn, personasIds);

        AgregarNotificacion.EntradaAgregarNotificacion entradaAgregarNotificacion = new AgregarNotificacion.EntradaAgregarNotificacion();
        entradaAgregarNotificacion.P_IDEMPRESA = entrada.p_IdEmpresa;
        entradaAgregarNotificacion.P_IDUSUARIO = entrada.p_IdUsuario;
        entradaAgregarNotificacion.P_IDMODULO = "SOC";
        entradaAgregarNotificacion.P_EVENTOS = new ArrayList<>();
        entradaAgregarNotificacion.P_VALORES = new ArrayList<>();
        entradaAgregarNotificacion.P_ARCHIVOSADJUNTOS = new ArrayList<>();
        entradaAgregarNotificacion.P_IDDIRECCIONES = new ArrayList<>();

        for (SolicitudFirmaAnexo firmante : firmantes) {
            long idEntidadFirmante = firmante.IdPersonaFirmante;
            long idEvento = firmante.IdSolicitudFirmaAnexo;
            List<Long> idsDireccionesElectronicas = idDireccionesPorIdPersona.get(idEntidadFirmante);
            if (idsDireccionesElectronicas == null || idsDireccionesElectronicas.isEmpty()) {
                logger.warn("El firmante con ID persona: " + idEntidadFirmante + " NO tiene correos electronicos activos, se omite envio de correo.");
                continue;
            }

            String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa);

            AgregarNotificacion.Evento eventoNotificacion = new AgregarNotificacion.Evento();
            eventoNotificacion.CodigoTipoEvento = "NOTIF_TOKEN_FIRMA_CONTRATO";
            eventoNotificacion.IdSecuencia = idEvento;
            eventoNotificacion.FechaCreacionSistema = fechaSistema;
            eventoNotificacion.IdPersonaRemitente = 0L;
            eventoNotificacion.IdPersonaDestinatario = idEntidadFirmante;
            eventoNotificacion.IdPortafolio = 0L;
            entradaAgregarNotificacion.P_EVENTOS.add(eventoNotificacion);

            for (Long idDireccionEletronica : idsDireccionesElectronicas) {
                AgregarNotificacion.DireccionEvento direccionEventoNotificacion = new AgregarNotificacion.DireccionEvento();
                direccionEventoNotificacion.IdSecuencia = idEvento;
                direccionEventoNotificacion.TipoDestinatario = "TO";
                direccionEventoNotificacion.IdDireccion = idDireccionEletronica;
                entradaAgregarNotificacion.P_IDDIRECCIONES.add(direccionEventoNotificacion);
            }

            entradaAgregarNotificacion
                    .addValor(idEvento, "CLIENTE", resumenAnexo.NombreCompletoCliente)
                    .addValor(idEvento, "AGENTE", resumenAnexo.NombreCompletoAgente)
                    .addValor(idEvento, "DOCUMENTOIDENTIDAD", resumenAnexo.DocumentoIdentidadCliente)
                    .addValor(idEvento, "TOKEN", firmante.tokenFirmaAnexo.Token)
                    .addValor(idEvento, "FECHAEXPIRACION", firmante.tokenFirmaAnexo.FechaExpiracionLegible)
                    .addValor(idEvento, "CORREDORA", resumenCorredora.RazonCorredora)
                    .addValor(idEvento, "ATENCIONCLIENTE", resumenCorredora.TelefonoCorredora)
                    .addValor(idEvento, "NOMBRECONTRATO", resumenAnexo.NombreDocumento)
                    .addValor(idEvento, "ZONAHORARIA", "UTC")
                    .addValor(idEvento, "FIRMANTE", firmante.NombreCompletoFirmante);
        }

        AgregarNotificacion agregarNotificacion = new AgregarNotificacion();
        SalidaAgregarNotificacion salidaAgregarNotificacion = agregarNotificacion.logica(conn, entradaAgregarNotificacion);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::SolicitarFirmaDocumento.notificarFirmantes", fin - inicio);

        return salidaAgregarNotificacion;
    }

    private EntidadesFirma obtenerEntidadesFirma(Connection conn, EntradaSolicitarFirmaDocumento entrada, ResumenPersona resumenPersonaSolicitante, List<ResumenPersona> firmantesCliente) throws Exception {
        logger.debug("INICIO::SolicitarFirmaDocumento.obtenerEntidadesFirma");
        long inicio = System.currentTimeMillis();

        EntidadesFirma entidadesFirma = new EntidadesFirma();

        for (ResumenPersona firmanteCliente : firmantesCliente) {
            SolicitudFirmaAnexo representanteFirmante = new SolicitudFirmaAnexo();
            representanteFirmante.IdPersonaFirmante = firmanteCliente.IdPersona;
            representanteFirmante.NombreCompletoFirmante = firmanteCliente.NombreCompleto;
            entidadesFirma.solicitudesFirmasAnexo.add(representanteFirmante);
        }

        final int largoToken = Integer.valueOf(Utils.getConfiguracion(conn, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.LargoTokenFirmaDocumento"), 10);
        final int minutosDuracionToken = Integer.valueOf(Utils.getConfiguracion(conn, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.MinutosDuracionTokenFirmaDocumento"), 10);
        final int segundosDuracionToken = minutosDuracionToken * SEGUNDOS_EN_MINUTO;

        final java.sql.Timestamp fechaGeneracionToken = FechaDB.ObtenerFechaHoraTs(conn, entrada.p_IdEmpresa);
        final java.sql.Timestamp fechaExpiracionToken = Fecha.addSeconds(fechaGeneracionToken, segundosDuracionToken);

        final String fechaGeneracionTokenLegible = Fecha.toFormat(fechaGeneracionToken, "hh:mm a dd MMMM yyyy");
        final String fechaExpiracionTokenLegible = Fecha.toFormat(fechaExpiracionToken, "hh:mm a dd MMMM yyyy");

        List<Long> idsSolicitudes = Utils.getSecuencia(conn, SEQUENCE_SOLICITUD_FIRMA_ANEXO, (long) entidadesFirma.solicitudesFirmasAnexo.size());
        List<Long> idsTokenFirmas = Utils.getSecuencia(conn, SEQUENCE_TOKEN_FIRMA_ANEXO, (long) entidadesFirma.solicitudesFirmasAnexo.size());
        List<Long> idsFirmasAnexo = Utils.getSecuencia(conn, SEQUENCE_FIRMA_ANEXO, (long) entidadesFirma.solicitudesFirmasAnexo.size());

        for (int i = 0; i < entidadesFirma.solicitudesFirmasAnexo.size(); i++) {
            SolicitudFirmaAnexo solicitudFirmaAnexo = entidadesFirma.solicitudesFirmasAnexo.get(i);
            solicitudFirmaAnexo.IdSolicitudFirmaAnexo = idsSolicitudes.get(i);
            solicitudFirmaAnexo.IdPersonaSolicitante = resumenPersonaSolicitante.IdPersona;
            solicitudFirmaAnexo.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;

            TokenFirmaAnexo tokenFirmaAnexo = new TokenFirmaAnexo();
            tokenFirmaAnexo.IdTokenFirmaAnexo = idsTokenFirmas.get(i);
            tokenFirmaAnexo.IdSolicitudFirmaAnexo = solicitudFirmaAnexo.IdSolicitudFirmaAnexo;
            tokenFirmaAnexo.FechaGeneracion = fechaGeneracionToken;
            tokenFirmaAnexo.FechaExpiracion = fechaExpiracionToken;
            tokenFirmaAnexo.Token = Utils.generateAlphanumericToken(largoToken);
            tokenFirmaAnexo.FechaGeneracionLegible = fechaGeneracionTokenLegible;
            tokenFirmaAnexo.FechaExpiracionLegible = fechaExpiracionTokenLegible;

            solicitudFirmaAnexo.tokenFirmaAnexo = tokenFirmaAnexo;

            FirmaAnexo firmaAnexo = new FirmaAnexo();
            firmaAnexo.IdFirmaAnexo = idsFirmasAnexo.get(i);
            firmaAnexo.IdAnexo = entrada.p_IdAnexo;
            firmaAnexo.IdSolicitudFirmaAnexo = solicitudFirmaAnexo.IdSolicitudFirmaAnexo;
            firmaAnexo.IdEstado = "PEN";
            firmaAnexo.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
            firmaAnexo.IdUsuario = entrada.p_IdUsuario;
            

            entidadesFirma.firmasAnexo.add(firmaAnexo);
            entidadesFirma.tokensFirmasAnexo.add(tokenFirmaAnexo);
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::SolicitarFirmaDocumento.obtenerEntidadesFirma", fin - inicio);

        return entidadesFirma;
    }

    private void insertarSolicitudesFirma(Connection conn, List<SolicitudFirmaAnexo> firmantes) throws Exception {
        logger.debug("INICIO::SolicitarFirmaDocumento.insertarSolicitudesFirma");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_SOLICITUD_FIRMA_ANEXO, firmantes, SolicitudFirmaAnexo.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::SolicitarFirmaDocumento.insertarSolicitudesFirma", fin - inicio);
    }

    private void insertarTokenFirmas(Connection conn, List<TokenFirmaAnexo> tokenFirmas) throws SQLException {
        logger.debug("INICIO::SolicitarFirmaDocumento.insertarTokenFirmas");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_TOKEN_FIRMA_ANEXO, tokenFirmas, TokenFirmaAnexo.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::SolicitarFirmaDocumento.insertarTokenFirmas", fin - inicio);
    }

    private void insertarAnexoFirmas(Connection conn, List<FirmaAnexo> anexoFirmas) throws SQLException {
        logger.debug("INICIO::SolicitarFirmaDocumento.insertarAnexoFirmas");
        long inicio = System.currentTimeMillis();

        SQL sql = new SQL();
        sql.batch(conn, SQL_INSERT_FIRMA_ANEXO, anexoFirmas, FirmaAnexo.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::SolicitarFirmaDocumento.insertarAnexoFirmas", fin - inicio);
    }

    private boolean socioEsFirmante(List<SolicitudFirmaAnexo> firmantes, ResumenPersona resumenPersonaSolicitante) {
        logger.trace("INICIO::SolicitarFirmaDocumento.socioEsFirmante");
        long inicio = System.currentTimeMillis();

        boolean esFirmante = false;
        for (SolicitudFirmaAnexo firmante : firmantes) {
            esFirmante = Objects.equals(firmante.IdPersonaFirmante, resumenPersonaSolicitante.IdPersona);
            if (esFirmante) {
                break;
            }
        }

        long fin = System.currentTimeMillis();
        logger.trace("FIN::SolicitarFirmaDocumento.socioEsFirmante", fin - inicio);

        return esFirmante;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        EntradaSolicitarFirmaDocumento entradaSolicitarFirmaDocumento = new EntradaSolicitarFirmaDocumento();
        entradaSolicitarFirmaDocumento.p_IdEmpresa = 1L;
        entradaSolicitarFirmaDocumento.p_IdAnexo = 3453L;
        entradaSolicitarFirmaDocumento.p_IdUsuario = 296L;
        entradaSolicitarFirmaDocumento.p_IdPersonaSolicitante = 296L;
//        entradaSolicitarFirmaDocumento.p_DocumentoIdentidadSolicitante = "89090100-K";
        entradaSolicitarFirmaDocumento.p_IdTipoCanalIngreso = "asd";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            try {
                SolicitarFirmaDocumento solicitarFirmaDocumento = new SolicitarFirmaDocumento();
                SalidaSolicitarFirmaDocumento salidaSolicitarFirmaDocumento = solicitarFirmaDocumento.logica(conn, entradaSolicitarFirmaDocumento);
                System.out.println(salidaSolicitarFirmaDocumento);

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
