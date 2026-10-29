package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.doc.pojo.FirmaDocumento;
import cl.bcs.socionegocio.doc.pojo.SolicitudFirmaDocumento;
import cl.bcs.socionegocio.doc.pojo.TipoDocumentoIndicador;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenDocumentoAnexo;
import cl.bcs.socionegocio.soc.pojo.ResumenPersona;
import cl.bcs.utils.BcsPojo;
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
import java.util.Objects;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.socionegocio.doc.SolicitarFirmaDocumento;
import cl.bcs.socionegocio.doc.SolicitarFirmaDocumento.EntradaSolicitarFirmaDocumento;
import cl.bcs.socionegocio.doc.SolicitarFirmaDocumento.SalidaSolicitarFirmaDocumento;
public class FirmarDocumento implements JavaCall {
    static final Logger logger = LogManager.getLogger(FirmarDocumento.class);

    private final String SQL_UPDATE_FIRMA_REPRESENTANTE_DOCUMENTO_ANEXO = loadResourceToString("cl/bcs/socionegocio/doc/sql/FirmarDocumento_UpdateFirmaRepresentanteDocumentoAnexo.sql"); 
    private final String SQL_SELECT_CANALES_DOCUMENTO = loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarFirmaCanalesDocumento.sql");

    public static class EntradaFirmarDocumento extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;

        public Long p_IdAnexo;
        public Long p_IdPersonaFirmante;
        public String p_DocumentoIdentidadFirmante;
        public String p_Token = "";   // valor opcional por parametro
    }

    public static class SalidaFirmarDocumento extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;

        public SalidaFirmarDocumento() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<>();
        }

        public void agregarValidacionesSalida(List<Validacion> validaciones) {
            for (Validacion validacion : validaciones) {
                agregarValidacionSalida(validacion);
            }
        }

        public void agregarValidacionSalida(Validacion validacion) {
            if (validacion.EsError) {
                this.P_OUT_ERRORESCONTADOR += 1L;
            }

            this.P_OUT_MENSAJES.add(validacion.MensajeError);
        }
    }

    public static class Anexo extends BcsPojo {
        public Long IdAnexo;
        public String IdEstado;
        public java.sql.Timestamp FechaActivacion;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaFirmarDocumento jin = gson.fromJson(in.toString(), EntradaFirmarDocumento.class);
        SalidaFirmarDocumento out = logica(conn, jin);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(out)).getAsJsonObject();
    }

    
    
    
    // ----------------------------------------------------------------------
    // LOGICA
    // ----------------------------------------------------------------------
    public SalidaFirmarDocumento logica(Connection conn, EntradaFirmarDocumento entrada) throws Exception {
        logger.info("INICIO::FirmarDocumento");
        long inicio = System.currentTimeMillis();

        SalidaFirmarDocumento salida = new SalidaFirmarDocumento();
        try {
        	SQL sql = new SQL();
        	HashMap<String,Object> paramsIN = new HashMap<>();
        	paramsIN.put("AnexoDocumento", entrada.p_IdAnexo ); 
        	paramsIN.put("Canal", entrada.p_IdTipoCanalIngreso ); 	
        	if ( sql.countQuery(conn, SQL_SELECT_CANALES_DOCUMENTO, paramsIN)==0 ) {
        		System.out.println("1");
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CANAL_NO_PERMITE_INGRESADO", new HashMap<>(), conn, entrada.p_IdEmpresa);
                salida.agregarValidacionSalida(validacion);
                return salida;
        	}
        }catch (Exception e) {
        	System.out.println(e);
        }     
        String validarTokenFirmaDocumento = "S";
        
        try
        {
        	validarTokenFirmaDocumento = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "SOCIODENEGOCIO", "DOC.ValidarTokenFirmaDocumento");
        }
        catch(Exception e)
        {
        	validarTokenFirmaDocumento = "S";  //Valor por defecto
        }
        
     

        //La opcion de NO validar el token debe darse SOLO via WS (Esta opcion permite solicitar la firma y firmar el documento de una. No se envia el mail con el token y este no se valida)
        if (validarTokenFirmaDocumento.equalsIgnoreCase("N") &&  (entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("WBS")||entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("APP")||entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("OWP") ) )
        {
        	
        	SolicitarFirmaDocumento servicioSolicitarFirmaDocumento = new  SolicitarFirmaDocumento();        	
        	EntradaSolicitarFirmaDocumento entradaSolicitarFirmaDocumento = new EntradaSolicitarFirmaDocumento();
        	SalidaSolicitarFirmaDocumento salidaSolicitarFirmaDocumento = new SalidaSolicitarFirmaDocumento();
        	
        	entradaSolicitarFirmaDocumento.p_IdEmpresa 	= entrada.p_IdEmpresa;
        	entradaSolicitarFirmaDocumento.p_IdUsuario 	= entrada.p_IdUsuario;
        	entradaSolicitarFirmaDocumento.p_IdAnexo 	= entrada.p_IdAnexo;
        	entradaSolicitarFirmaDocumento.p_IdPersonaSolicitante = null;
        	entradaSolicitarFirmaDocumento.p_DocumentoIdentidadSolicitante = entrada.p_DocumentoIdentidadFirmante;
        	entradaSolicitarFirmaDocumento.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
            
        	boolean existeSolicitudFirmaDocumento = servicioSolicitarFirmaDocumento.existeSolicitudFirmaDocumento(conn, entradaSolicitarFirmaDocumento);
      
        	if (!existeSolicitudFirmaDocumento)
        	{	// Si no existe solicitud, se crea (si existe mas de un firmante la solicitud se crea con la primera persona que firma)
	        	salidaSolicitarFirmaDocumento = servicioSolicitarFirmaDocumento.logica(conn, entradaSolicitarFirmaDocumento);	        	
	        	if (salidaSolicitarFirmaDocumento.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0)
	        	{
	        		salida.P_OUT_MENSAJES.addAll(salidaSolicitarFirmaDocumento.P_OUT_MENSAJES);
	        		return salida;
	        	} 
        	}
        	
        }
        
        ResumenPersona resumenPersonaFirmante = obtenerResumenPersona(conn, entrada);
        ResumenDocumentoAnexo resumenAnexo = SocioNegocioUtils
                .obtenerResumenDocumentoAnexoPorIdAnexo(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdAnexo)))
                .get(entrada.p_IdAnexo);
        List<TipoDocumentoIndicador> documentoTiposIndicadores = DocumentoUtils
                .obtenerTiposIndicadoresPorIdTipoDocumento(conn, entrada.p_IdEmpresa, new ArrayList<>(Collections.singletonList(resumenAnexo.IdTipoDocumento)))
                .getOrDefault(resumenAnexo.IdTipoDocumento, Collections.emptyList());

        if ("FULL".equals(resumenAnexo.TipoEnrolamiento)) {
            SocioNegocioUtils.ResultadoValidacionRequisitosEnrolamientoFull resultadoValidacionRequisitosEnrolamientoFull = SocioNegocioUtils.validarEnrolamientoFull(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_UsaReglasNegocio, entrada.p_IdTipoCanalIngreso, Collections.singletonList(resumenAnexo.IdCliente));
            if (resultadoValidacionRequisitosEnrolamientoFull.validaciones.size() > 0) {
                salida.agregarValidacionesSalida(resultadoValidacionRequisitosEnrolamientoFull.validaciones);
                salida.P_OUT_REQUISITOS_ENR_FULL.addAll(resultadoValidacionRequisitosEnrolamientoFull.requisitos);

                return salida;
            }
        }

        SolicitudFirmaDocumento solicitudFirmaDocumento = DocumentoUtils
                .obtenerSolicitudFirmaPendientesPorIdPersonaFirmante(conn, entrada.p_IdAnexo, new ArrayList<>(Collections.singletonList(resumenPersonaFirmante.IdPersona)))
                .get(resumenPersonaFirmante.IdPersona);

        salida.agregarValidacionesSalida(validarAnexo(conn, entrada.p_IdEmpresa, resumenAnexo, documentoTiposIndicadores));
        
        //La opcion de NO validar el token debe darse SOLO via WS (Esta opcion permite solicitar la firma y firmar el documento de una. No se envia el mail con el token y este no se valida)
        if ( !(validarTokenFirmaDocumento.equalsIgnoreCase("N") && (entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("WBS")||entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("APP")||entrada.p_IdTipoCanalIngreso.equalsIgnoreCase("OWP") ) ) )
        {     	
       	 	salida.agregarValidacionesSalida(validarTokenFirma(conn, entrada, solicitudFirmaDocumento));
        }
       
        
        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            return salida;
        }

        activarFirma(conn, entrada, solicitudFirmaDocumento);

        ActivarDocumentos.SalidaActivarDocumentos salidaActivarDocumentos = DocumentoUtils.activarDocumentos(conn, entrada.p_IdEmpresa, Collections.singletonList(resumenAnexo));
        salida.P_OUT_ERRORESCONTADOR += salidaActivarDocumentos.P_OUT_ERRORESCONTADOR;
        salida.P_OUT_MENSAJES.addAll(salidaActivarDocumentos.P_OUT_MENSAJES);
        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            return salida;
        }
        else {
        	if(salida.P_OUT_ERRORESCONTADOR == 0 && salida.P_OUT_MENSAJES.isEmpty()) {
    			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_OK, "UI_DOCUMENTO_FIRMADO", new HashMap<>(), conn, entrada.p_IdEmpresa));
    		}
        }

        DocumentoUtils.agregarLogFirmaDocumento(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, Collections.singletonList(resumenPersonaFirmante.IdPersona), resumenAnexo);

        long fin = System.currentTimeMillis();
        logger.info("FIN::FirmarDocumento", fin - inicio);

        return salida;
    }

    private ResumenPersona obtenerResumenPersona(Connection conn, EntradaFirmarDocumento entrada) throws SQLException {
        logger.debug("INICIO::FirmarDocumento.obtenerResumenPersona");
        long inicio = System.currentTimeMillis();

        ResumenPersona resumenPersona;
        if (entrada.p_IdPersonaFirmante == null) {
            resumenPersona = SocioNegocioUtils.obtenerResumenPersonaPorDocumentoIdentidad(conn, new ArrayList<>(Collections.singletonList(entrada.p_DocumentoIdentidadFirmante))).getOrDefault(entrada.p_DocumentoIdentidadFirmante, new ResumenPersona());
        } else {
            resumenPersona = SocioNegocioUtils.obtenerResumenPersonaPorIdPersona(conn, new ArrayList<>(Collections.singletonList(entrada.p_IdPersonaFirmante))).getOrDefault(entrada.p_IdPersonaFirmante, new ResumenPersona());
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::FirmarDocumento.obtenerResumenPersona", fin - inicio);

        return resumenPersona;
    }

    private List<Validacion> validarTokenFirma(Connection conn, EntradaFirmarDocumento entrada, SolicitudFirmaDocumento solicitudFirmaDocumento) throws Exception {
        logger.debug("INICIO::FirmarDocumento.validarTokenFirma");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();
        java.sql.Timestamp fechaActual = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);

        if (solicitudFirmaDocumento == null) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_FIRMA_PENDIENTE", new HashMap<>(), conn, entrada.p_IdEmpresa);
            validaciones.add(validacion);
        }

        if (Objects.nonNull(solicitudFirmaDocumento)) {
            boolean tokenExpirado = solicitudFirmaDocumento.FechaExpiracionToken.before(fechaActual);
            boolean tokenIncorrecto = !Objects.equals(solicitudFirmaDocumento.Token, entrada.p_Token);

            if (tokenExpirado || tokenIncorrecto) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_TOKEN_INVALIDO", new HashMap<>(), conn, entrada.p_IdEmpresa);
                validaciones.add(validacion);
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::FirmarDocumento.validarTokenFirma", fin - inicio);

        return validaciones;
    }

    private List<Validacion> validarAnexo(Connection conn, Long idEmpresa, ResumenDocumentoAnexo resumenAnexo, List<TipoDocumentoIndicador> tiposIndicadoresDocumento) throws Exception {
        logger.debug("INICIO::FirmarDocumento.validarAnexo");
        long inicio = System.currentTimeMillis();

        List<Validacion> validaciones = new ArrayList<>();

        if (resumenAnexo == null) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_CLIENTE_ANEXO", new HashMap<>(), conn, idEmpresa);
            validaciones.add(validacion);
        }

        HashMap<String, String> parametrosMensaje = new HashMap<>(2);
        for (TipoDocumentoIndicador tipoIndicadorDocumento : tiposIndicadoresDocumento) {
            if (tipoIndicadorDocumento.IdTipoIndicador == null) {
                parametrosMensaje.put("IdTipoDocumentoIndicador", String.valueOf(tipoIndicadorDocumento.IdTipoDocumentoIndicador));

                Validacion validacion = new Validacion();
                validacion.EsError = true;
                validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_TIPO_INDICADOR", parametrosMensaje, conn, idEmpresa);
                validaciones.add(validacion);

                parametrosMensaje.clear();
            }
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::FirmarDocumento.validarAnexo", fin - inicio);

        return validaciones;
    }

    private void activarFirma(Connection conn, EntradaFirmarDocumento entrada, SolicitudFirmaDocumento solicitudFirma) throws Exception {
        logger.debug("INICIO::FirmarDocumento.activarFirma");
        long inicio = System.currentTimeMillis();

        FirmaDocumento firmaDocumento = new FirmaDocumento();
        firmaDocumento.FechaFirma = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
        firmaDocumento.IdEstado = "ACT";
        firmaDocumento.IdFirmaAnexo = solicitudFirma.IdFirmaAnexo;
        firmaDocumento.IdAnexo = solicitudFirma.IdAnexo;
        firmaDocumento.IdSolicitudFirmaAnexo = solicitudFirma.IdSolicitudFirmaAnexo;
        firmaDocumento.IdPersonaAgente = null;
        firmaDocumento.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
        firmaDocumento.EsAutorizado = "S";

        SQL sql = new SQL();
        sql.batch(conn, SQL_UPDATE_FIRMA_REPRESENTANTE_DOCUMENTO_ANEXO, new ArrayList<>(Collections.singletonList(firmaDocumento)), FirmaDocumento.class);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::FirmarDocumento.activarFirma", fin - inicio);
    }

    
    
    // ----------------------------------------------------------------------
    // PARA PRUEBAS LOCALES
    // ----------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        EntradaFirmarDocumento entradaFirmarDocumento = new EntradaFirmarDocumento();
        entradaFirmarDocumento.p_IdEmpresa = 1L;
        entradaFirmarDocumento.p_IdUsuario = 0L;
        entradaFirmarDocumento.p_IdModulo = "SOC";
        entradaFirmarDocumento.p_UsaReglasNegocio = "S";
        entradaFirmarDocumento.p_IdTipoCanalIngreso = "OWP";

        entradaFirmarDocumento.p_IdAnexo = 4073L;
        entradaFirmarDocumento.p_Token = "";  //"nnF?Jv5D";
        entradaFirmarDocumento.p_DocumentoIdentidadFirmante = "5746654-5";

        //String jsonString = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_IdAnexo\":410,\"p_IdPersonaFirmantes\":[213957]}";

        try (Connection conn = PlataformaUtils.getMASTER01Connection()) {
            try {
                //Gson gson = Utils.getGsonBuilder();
                //EntradaFirmarDocumento json = gson.fromJson(jsonString, EntradaFirmarDocumento.class);
                FirmarDocumento solicitarFirmaDocumento = new FirmarDocumento();
                SalidaFirmarDocumento salidaFirmarDocumento = solicitarFirmaDocumento.logica(conn, entradaFirmarDocumento);
                System.out.println(salidaFirmarDocumento);

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                conn.commit();
               // conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
