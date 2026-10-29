package cl.bcs.normativa.circulares;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class NotificarCIRCULAR1985 implements JavaCall {
	static final Logger logger = LogManager.getLogger(NotificarCIRCULAR1985.class);

	//private static final Logger logger = LogManager.getLogger(NotificarNCG365FFMM.class);
	
	private final String SQL_SELECT_ORDENES_EJECUTADAS		= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarCIRCULAR1985_SelectOrdenesEjecutadas.sql");
	private final String SQL_SELECT_EVENTOS		= loadResourceToString("cl/bcs/normativa/circulares/sql/NotificarCIRCULAR1985_SelectEventosAGenerar.sql");


	public static class NotificarCIRCULAR1985_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_FechaSistema = "0001-01-01";  //Fecha de certificacion de las operaciones a incluir en el correo. Si viene 0001-01-01  toma fecha de sistema
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden {
		public Long IdOrden;
		
	}
	
	public static class ObjSeries {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPersona;
		public String  DocumentoIdentidad;
		public String IdSerieFondo;
		public String NombreFondo;
		public Long    DiasComision;
		public BigDecimal PorcentajeComision;
		public BigDecimal PorcentajeRemuneracion;
		public BigDecimal PorcentajeGastoOpera;
		public BigDecimal PorcentajeCostoAdm;
		
		
		private String Tabla;
		
		public String getTabla() {
			return Tabla;
		}

		public void setTabla(String tabla) {
			Tabla = tabla;
		}
		
		private String Row;
		
		
		public void setRow(String row) {
			Row = row;
		}
		public String getRow() {
			return Row;
		}

		
	}
	
	
	public static class ObjSecuencias {
		public Long 	IdCliente;
		public Long 	IdPortafolio;
		public Long 	IdPersona;
		public Long 	IdDireccionElectronicaPrincipal;
		public String  	DocumentoIdentidad;
		public String  	NombreDestinatario;
		public String  	RazonCorredora;
		public String  	RutCorredora;
		public String  	Agrupador;
		
		private String Tabla;
		
		public String getTabla() {
			return Tabla;
		}

		public void setTabla(String tabla) {
			Tabla = tabla;
		}
		
	}
	
	public static class ObjEventos{
		public Long IdPersona;
		public Long IdDireccionElectronica;
		public Long Secuencia;
		public Long IdPortafolio;
		public String RazonSocial;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String FechaEjecucion;
		public String CodigoPortafolio;
		public String DireccionFisica;
		public String NombreAgente;
		public String TelefonoAgente;
		
		
		
		private String Tabla;
		
		public String getTabla() {
			return Tabla;
		}

		public void setTabla(String tabla) {
			Tabla = tabla;
		}
	}
	public static class ObjEjecucionOrdenes{
		public Long	IdEjecucion;
		public Long		IdDocumento;
		public String		NombreCompleto;
		public String		DireccionFisica;
		public String		RutCliente;
		public Long			IdPersona;
		public String		Telefono;
		public Long			IdDireccionElectronica;
		public String		DireccionElectronica;
		public Long		FolioGeneracion;
		public Long		IdOrden;
		public String	Folio;
		public Long		IdCliente;
		public Long		IdPortafolio;
		public String	CodigoPortafolio;
		public String	IdClasificacionTipoInstrumento;
		public String	IdTipoInstrumento;
		public String	IdTipoOperacion;
		public Long	IdInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal	Monto;
		public BigDecimal	MontoValorizado;
		public BigDecimal	Precio;
		public String	IdMoneda;
		public String	IdCondicionDeLiquidacion;
		public String	FechaRecepcion;
		public String	FechaAceptacion;
		public String	TipoEjecucion;
		public String	FechaEjecucion;
		public String	FechaHoraEjecucion;
		public String	IdBolsa;
		public String	EsOD;
		public String	Modalidad;
		public BigDecimal	PrecioEjecucion;
		public BigDecimal	MontoTransado;
		public BigDecimal	CantidadTransada;
		public BigDecimal	ComisionTransada;
		public BigDecimal	MejorPrecioOtraBolsa;
		public BigDecimal	CantidadMejorPrecioOtraBolsa;
		public String	PrecioMinHoraBolsaMcdo;
		public String	PrecioMaxHoraBolsaMcdo;
		public BigDecimal	MejorPrecioMercado;
		public BigDecimal	PrecioMinimoMercado;
		public String	HoraPrecioMinimoMercado;
		public String	IdBolsaPrecioMinimoMercado;
		public BigDecimal	PrecioMaximoMercado;
		public String	HoraPrecioMaximoMercado;
		public String	IdBolsaPrecioMaximoMercado;
		public String	PrecioMinHoraBolsaCorr;
		public String	PrecioMaxHoraBolsaCorr;
		public BigDecimal	MayorPrecioOperacionCorredor;
		public String	HoraPrecioMayorCorredor;
		public BigDecimal	MenorPrecioOperacionCorredor;
		public String	HoraPrecioMenorCorredor;
		public Long	IdEmpleadoDigitador;
		public Long	FolioComprobanteFacturacion;
		public Timestamp	FechaGeneracion;
		public String	FolioExterno;
		public String	IdEstadoNotificacion;
		public String	IdEstadoCarta;
		public Timestamp	FechaIngreso;
		public Timestamp	FechaCreacion;
		public Long	IdUsuarioCreador;
		public String	NombreAgente;
		public String	AccionOperacion;
		public String	TelefonoAgente;
		public String	Nemotecnico;
		public String	RelacionCorredor;
		public String	RazonCorredora;
		public String	DireccionCorredora;
		public String	RutCorredora;
		public String	TelefonoCorredora;
		public String 	PrecioMinHoraBolsaBEC;
		public String 	PrecioMaxHoraBolsaBEC;
		public String		FolioOrden;
		private String Row;
		
		
		public void setRow(String row) {
			Row = row;
		}
		public String getRow() {
			return Row;
		}
	}
	
	public static class ObjInfo {
		
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPersona;
		public String TipoSolicitud;
		public String IdSerieFondo;
		public String IdTipoMovimientoFondoMutuo;
		public Date FechaCertificacion;
		public Date FechaLiquidacion;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal CostoUF;
		public String IdMoneda;
		public Long IdEmpresa;
		public String EstadoSolicitud;
		public Long IdInstrumento;
		public String NombreSerieFondo;
		public String CodigoSerie;
		public String IdFondo;
		public String IdEmisor;
		public String Nemotecnico;
		public String NombreFondo;
		public String DescripcionTipoSolicitud;
		public String DescripcionTipoMovimiento;
		public Date FechaCertificacionRegistro;
		public String IdRegimenTributario;
		public String DescripcionRegimenTributario;
		public String NombreDestinatario;
		public String DocumentoIdentidad;
		
		public String RutEmisor;
		public String FechaCertificacionRegistroStr;
		public String HoraCertificacionRegistro;
		public String NombreMoneda;
		
		public String DiaCertificacionRegistro;
		public String MesCertificacionRegistro;
		public String AgnoCertificacionRegistro;
		
		
		
		
		private String Row;
		
		
		public void setRow(String row) {
			Row = row;
		}
		public String getRow() {
			return Row;
		}

		
	
		
	}	
	
	public static class ObjDirs {
		public Long 	  IdPortafolio;
		public Integer 	IdDireccion;
		public String 	  TipoDestinatario;
		@Override
		public String toString() {
			return "ObjDirs [IdPortafolio=" + IdPortafolio + ", IdDireccion=" + IdDireccion + ", TipoDestinatario="
					+ TipoDestinatario + "]";
		}	
	}
	
	public static class NotificarCIRCULAR1985_Salida {
		 public Long P_OUT_ERRORESCONTADOR;
	}

	public void logicaAsync(Connection con,NotificarCIRCULAR1985_Entrada in) throws Exception {
		Gson gson = getGsonBuilder();	
		JsonParser parser 	= new JsonParser();
        JsonObject dataObj	= parser.parse(gson.toJson(in)).getAsJsonObject();		
        String CallBatchURL=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchURL");
        String CallBatchToken=ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchToken");
		Utils.CallBatchAsync(CallBatchURL, CallBatchToken, "PLUGIN_NOR.jar@cl.bcs.normativa.circulares.NotificarNCG365FFMM", dataObj);
	}
	
	
	// ---------------------------------------------------
	// LOGICA
	// ---------------------------------------------------
	
	public NotificarCIRCULAR1985_Salida logica(Connection con, NotificarCIRCULAR1985_Entrada in) throws Exception {
		SQL sql = new SQL();
		
		logger.info("INICIO SERVICIO");
		
		NotificarCIRCULAR1985_Salida out = new NotificarCIRCULAR1985_Salida();
		
        int SegundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
        Fecha.setLocalOffSet(SegundosZonaHoraria);        
        String 	tFechaHoraSistema 	= FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
		Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);  
		
        out.P_OUT_ERRORESCONTADOR=0L;
		
		AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
        notifIn.P_IDEMPRESA 		= in.p_IdEmpresa;
        notifIn.P_IDUSUARIO 		= in.p_IdUsuario;
        notifIn.P_IDMODULO 			= in.p_IdModulo;
        notifIn.P_EVENTOS 			= new ArrayList<>();
        notifIn.P_VALORES 			= new ArrayList<>();
        notifIn.P_ARCHIVOSADJUNTOS 	= new ArrayList<>();
        notifIn.P_IDDIRECCIONES 	= new ArrayList<>(); 
   

        HashMap<String, Object> params = new HashMap<>();
        params.put("p_FechaSistema", in.p_FechaSistema);

        List<ObjEjecucionOrdenes> listaOrdenesEjecutadas = sql.query(con, SQL_SELECT_ORDENES_EJECUTADAS, params, ObjEjecucionOrdenes.class);
        List<ObjEventos> listaEventos = sql.query(con, SQL_SELECT_EVENTOS, params, ObjEventos.class);
        logger.info("Ordenes: " + listaOrdenesEjecutadas.size() + " Eventos: " + listaEventos.size());

        if (listaEventos.isEmpty()) {
            logger.info("Sin eventos para notificar");
            return out;
        }
     	
     	

     	
        for (ObjEventos ord:listaEventos) {        	
        	//logger.info("Armando Evento > IdPortafolio:" + ord.IdPortafolio + " IdDireccionElectronicaPrincipal:" + ord.IdDireccionElectronicaPrincipal);
        	
            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
            evt.CodigoTipoEvento 		= "NOTIFICAR_NORMATIVA_CIR1985";
            evt.IdSecuencia 			= ord.Secuencia;
            evt.FechaCreacionSistema 	= Fecha.toStringDate(vFechaHoraSistema);            
            evt.IdPersonaRemitente 		= (long) 0;
            evt.IdPersonaDestinatario 	= ord.IdPersona;            
            evt.IdPortafolio 			= ord.IdPortafolio;
            notifIn.P_EVENTOS.add(evt);
         }
        
        // Agregar direcciones al evento
        for (ObjEventos dir:listaEventos) { 
        	if (dir.IdDireccionElectronica==null || dir.IdDireccionElectronica==0) continue;
         	
            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
            ndir.IdSecuencia 		= dir.Secuencia;
            ndir.TipoDestinatario 	= "TO";
            ndir.IdDireccion 		= new Long(dir.IdDireccionElectronica);
            notifIn.P_IDDIRECCIONES.add(ndir);
       
        }
        
        final String headerTabla1 = "<br>";
        
        final String footerTablas = "";

        // Detalles TABLA 1
        Map<String, String> tabla1 = new HashMap<String, String>();
        for (ObjEjecucionOrdenes info:listaOrdenesEjecutadas) { 
        	
        	String condicionLiquidacion	=	"";
        	if(info.IdCondicionDeLiquidacion.equalsIgnoreCase("T0")) {
        		condicionLiquidacion	=	"Pago Hoy";
        	}else if(info.IdCondicionDeLiquidacion.equalsIgnoreCase("T1")) {
        		condicionLiquidacion	=	"Pago Ma&ntilde;ana";
        	}else if(info.IdCondicionDeLiquidacion.equalsIgnoreCase("T2")) {
        		condicionLiquidacion	=	"Contado Normal";
        	}else {
        		condicionLiquidacion	=	info.IdCondicionDeLiquidacion;
        	}

         	
        	String monto = MonedaFormato.formatear(con, info.Monto, MonedaFormato.MONTO, info.IdMoneda, info.IdTipoInstrumento, info.IdTipoOperacion).valorConFormato;
        	String cantidad = MonedaFormato.formatear(con, info.Cantidad, MonedaFormato.CANTIDAD, info.IdMoneda).valorConFormato;
        	String precio = MonedaFormato.formatear(con, info.Precio, MonedaFormato.PRECIO, info.IdMoneda).valorConFormato;
        	String MejorPrecioOtraBolsa = info.MejorPrecioOtraBolsa.compareTo(BigDecimal.ZERO) == 0 ? "-/--" : MonedaFormato.formatear(con, info.MejorPrecioOtraBolsa, MonedaFormato.PRECIO, info.IdMoneda).valorConFormato + "/BEC";
			String PrecioEjecucion = MonedaFormato.formatear(con, info.PrecioEjecucion, MonedaFormato.PRECIO, info.IdMoneda).valorConFormato;
    		String MontoTransado = MonedaFormato.formatear(con, info.MontoTransado, MonedaFormato.MONTO, info.IdMoneda).valorConFormato;
    		String CantidadTransada = MonedaFormato.formatear(con, info.CantidadTransada, MonedaFormato.CANTIDAD, info.IdMoneda).valorConFormato;
        	String comision = MonedaFormato.formatear(con, info.ComisionTransada, MonedaFormato.MONTO, info.IdMoneda).valorConFormato;
            String key = info.IdPersona + "_" + info.IdPortafolio;
            String PrecioMinHoraBolsaMcdo = "0".equals(info.PrecioMinHoraBolsaMcdo) ? "0 / 00:00 / BCS" : info.PrecioMinHoraBolsaMcdo;
            String PrecioMaxHoraBolsaMcdo = "0".equals(info.PrecioMaxHoraBolsaMcdo) ? "0 / 00:00 / BCS" : info.PrecioMaxHoraBolsaMcdo;
            
            info.setRow("<div>\r\n"
            		+ "<table  width=\"750\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;border-spacing:0;\">\r\n"
            		+ "            <tbody>\r\n"
            		+ "					<tr>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:11px;color:#000000;line-height:26px;text-align:left;font-weight:bold;\">Detalle Orden</td>\r\n"
            		+ "					</tr>\r\n"
            		+ "					<tr>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Detalle</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Cantidad</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Precio</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Monto</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Fecha y hora recepci&oacute;n</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Fecha y hora aceptaci&oacute;n</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Condici&oacute;n Liquidaci&oacute;n</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Folio Orden</td>\r\n"
            		+ "					</tr>\r\n"
            		+ "					<tr>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.AccionOperacion + "  "+ info.Nemotecnico +"</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + cantidad + "</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + precio + "</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" +monto + "</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.FechaRecepcion + "</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.FechaAceptacion + "</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + condicionLiquidacion + "</td>\r\n"
            		+ "	    				<td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.FolioOrden + "</td>\r\n"
            		+ "					</tr>\r\n"
            		+ "				</tbody>\r\n"
            		+ "</table>\r\n"
            		+ "<br>\r\n"   		
            		+ "        <table  width=\"750\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;border-spacing:0;\">\r\n"
            		+ "            <tbody>\r\n"
            		+ "                <tr>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:11px;color:#000000;line-height:26px;text-align:left;font-weight:bold;\">Ejecuciones</td>\r\n"
            		+ "                </tr>\r\n"
            		+ "                <tr>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Folio Trans</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Cantidad</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Precio</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Monto</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Fecha y Hora Ejecuci&oacute;n</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Bolsa</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Modalidad</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Tipo Contraparte</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Comisi&oacute;n</td>\r\n"
            		+ "                </tr>\r\n"
            		+ "                <tr>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.Folio + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + CantidadTransada + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + PrecioEjecucion + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + MontoTransado + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.FechaHoraEjecucion + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.IdBolsa + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.Modalidad + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + info.RelacionCorredor + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + comision + "</td>\r\n"
            		+ "                </tr>\r\n"
            		+ "            </tbody>\r\n"
            		+ "</table>\r\n"
            		+ "<table width=\"750\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;border-spacing:0;\">\r\n"
            		+ "    <tbody>\r\n"
            		+ "       <tr>\r\n"
            		+ "            <td height=\"30\" colspan=\"17\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:9px;color:#000000;line-height:16px;font-weight:bold;text-align:left;\">Esta corredora da preferencia a la ejecuci&oacute;n de &oacute;rdenes a precio de mercado. De entre las &oacute;rdenes a precio l&iacute;mite, se privilegia a las con mejor precio y, a igualdad de condiciones, se da preferencia a las &oacute;rdenes recibidas con anterioridad</td>\r\n"
            		+ "       </tr>\r\n"
            		+ "        <tr>\r\n"
            		+ "            <td height=\"15\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:9px;color:#000000;line-height:16px;font-weight:bold;text-align:left;\"></td>\r\n"        
            		+ "        </tr>\r\n"
            		+ "        <tr>\r\n"
            		+ "            <td height=\"15\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:9px;color:#000000;line-height:16px;font-weight:bold;text-align:left;\">(1) Modalidad: OS: Operaci&oacute;n con corredor contraparte; OD: Corredor opera como comprador y vendedor, para cuenta propia o de un tercero</td>\r\n"
            		+ "        </tr>\r\n"
            		+ "        <tr>\r\n"
            		+ "            <td height=\"15\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:9px;color:#000000;line-height:16px;font-weight:bold;text-align:left;\">(2) Contraparte: SREL: Sin relaci&oacute;n Corredor; PROP: Cartera propia corredor; REL/I: Relacionado interno; REL/E: Relacionado Externo de la corredora</td>\r\n"
            		+ "        </tr>\r\n"
            		+ "</table>\r\n"
            		+ "<br>\r\n"
            		+ "<table  width=\"750\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;border-spacing:0;\">\r\n"
            		+ "            <tbody>\r\n"
            		+ "                <tr\">\r\n"
            		+ "                    <td height=\"30\" colspan=\"17\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:11px;color:#000000;line-height:26px;font-weight:bold;text-align:center;\">Detalle Mercado</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:11px;color:#000000;line-height:26px;font-weight:bold;text-align:center;\">Detalle Otras Bolsas</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"17\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:11px;color:#000000;line-height:26px;font-weight:bold;text-align:center;\">Detalle Corredor</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"17\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:11px;color:#000000;line-height:26px;font-weight:bold;text-align:center;\">Detalle BEC</td>\r\n"
            		+ "\r\n"
            		+ "                </tr>\r\n"
            		+ "                <tr>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Precio minimo/hora/bolsa</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Precio maximo/hora/bolsa</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Mejor Precio Vigente (OB)</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Menor Precio/hora</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Mejor Precio/hora</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Precio minimo/hora/bolsa</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Times New Roman, Times, serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">Precio maximo/hora/bolsa</td>\r\n"
            		+ "                </tr>\r\n"
            		+ "                <tr>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + PrecioMinHoraBolsaMcdo + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + PrecioMaxHoraBolsaMcdo + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + MejorPrecioOtraBolsa + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + info.PrecioMinHoraBolsaCorr + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + info.PrecioMaxHoraBolsaCorr + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + info.PrecioMinHoraBolsaBEC + "</td>\r\n"
            		+ "                    <td height=\"30\" colspan=\"7\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:10px;color:#000000;line-height:26px;text-align:center;border:solid 1px #000000;\">" + "$" + info.PrecioMaxHoraBolsaBEC + "</td>\r\n"
            		+ "                </tr>\r\n"
            		+ "            </tbody>\r\n"
            		+ "        </table>\r\n"
            		+ "<table width=\"750\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;border-spacing:0;\">\r\n"
            		+ "    <tbody>\r\n"
            		+ "        <tr>\r\n"
            		+ "            <td height=\"30\" colspan=\"17\" bgcolor=\"#fff\" style=\"font-family:Arial, Helvetica, sans-serif;font-size:8px;color:#000000;line-height:16px;font-weight:bold;text-align:left;\">*El M&aacute;ximo y M&iacute;nimo Precio/Hora bolsa corresponde a operaciones ejecutadas bajo condici&oacute;n de Liquidaci&oacute;n Contado Normal, lo cual puede diferir de la condici&oacute;n de la orden espec&iacute;fica. </td>\r\n"
            		+ "        </tr>\r\n"
            		+ "    </tbody>\r\n"
            		+ "</table>\r\n"        
            		+ "<br><br>\r\n"
            		+ "<table width=\"750\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;border-spacing:0;border-bottom: 1px solid #000000;\"></table>\r\n"
            		+ "<br>\r\n"
            		+ "</div>\r\n");
            
            // agrupar por evento a generar
            // agrupamiento
            if (tabla1.get(key) != null) {
                tabla1.put(key, tabla1.get(key) + info.getRow());
            } else {
                tabla1.put(key, info.getRow());
            }
             
        };	
        	
        
        
        	
        
        for (String mapKey : tabla1.keySet()) {
                tabla1.put(mapKey,tabla1.get(mapKey));
        };  
        
        // Agrupar tabla1
        HashMap<String, ObjEventos> listaInfoAgrupado = new HashMap<String, ObjEventos>();
        String logoCorredorRutaPublica = "";
		ConsultarImagen consultarImagen = new ConsultarImagen();
		ConsultarImagenEntrada consultarImagenIn = new ConsultarImagenEntrada();
		consultarImagenIn.p_IdEmpresa = in.p_IdEmpresa;
		consultarImagenIn.p_imagenNombres.add("LOGO_CORREDOR");
		ConsultarImagenSalida consultarImagenOut = consultarImagen.logica(con, consultarImagenIn);
		if (consultarImagenOut.OUT_IMAGENES.size() > 0 ) {
			logoCorredorRutaPublica = consultarImagenOut.OUT_IMAGENES.get(0).RutaPublica;
		}

        for (ObjEventos iterable : listaEventos) {
            if (listaInfoAgrupado.get(iterable.IdPersona + "_" + iterable.IdPortafolio) == null) {
            	
                String tablaOrdenes = (tabla1.get(iterable.IdPersona + "_" + iterable.IdPortafolio) == null) ? headerTabla1 + footerTablas : tabla1.get(iterable.IdPersona + "_" + iterable.IdPortafolio );
                iterable.setTabla(tablaOrdenes);
                
                notifIn.addValorBlob(iterable.Secuencia, "TABLA1", iterable.getTabla());
                
                notifIn.addValor(iterable.Secuencia, "NombreCompleto", iterable.NombreCompleto)
           						.addValor(iterable.Secuencia, "DireccionFisica", iterable.DireccionFisica)
           						.addValor(iterable.Secuencia, "Portafolio", iterable.CodigoPortafolio)
           						.addValor(iterable.Secuencia, "FechaEjecucion", iterable.FechaEjecucion)
           						.addValor(iterable.Secuencia, "RutCliente", iterable.DocumentoIdentidad)
           						.addValor(iterable.Secuencia, "NombreAgente", iterable.NombreAgente)
           						.addValor(iterable.Secuencia, "TelefonoAgente", iterable.TelefonoAgente)
           						.addValor(iterable.Secuencia, "RazonSocialCorredora", iterable.RazonSocial)
           						.addValor(iterable.Secuencia, "LogoCorredor", logoCorredorRutaPublica);
        	   	
        	   	//localiza regitro
        	   	listaInfoAgrupado.put(iterable.IdPersona + "_" + iterable.IdPortafolio, iterable);
        	}
        }
        
        
        
        if (notifIn.P_EVENTOS.size() > 0) {
        	
        	AgregarEvento notif = new AgregarEvento();
            AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);

        }
   
        return out;
	}
	
	
	// ---------------------------------------------------
	// CALL
	// ---------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		NotificarCIRCULAR1985_Entrada in=gson.fromJson(jin.toString() ,NotificarCIRCULAR1985_Entrada.class);
		NotificarCIRCULAR1985_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	
	
	// ---------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ---------------------------------------------------
	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			NotificarCIRCULAR1985 sp = new NotificarCIRCULAR1985();
			NotificarCIRCULAR1985_Entrada in = new NotificarCIRCULAR1985_Entrada();
			in.p_IdEmpresa 			= 1L;
			in.p_IdModulo 			= "NOR";
			in.p_IdUsuario 			= Long.parseLong("1");
			in.p_FechaSistema = "2026-06-02"; // "2020-08-03";  // Si va en 0001-01-01  toma la fecha de sistema
			
				
			sp.logica(con, in);
			con.commit();
			//con.rollback();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}