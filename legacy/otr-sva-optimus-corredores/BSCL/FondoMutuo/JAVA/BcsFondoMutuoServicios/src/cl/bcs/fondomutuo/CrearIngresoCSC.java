package cl.bcs.fondomutuo;



import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.ObtenerFolioSuscripcion;
import cl.bcs.APV.ObtenerFolioSuscripcion.ObtenerFolioSuscripcion_Entrada;
import cl.bcs.APV.ObtenerFolioSuscripcion.ObtenerFolioSuscripcion_Salida;
import cl.bcs.fondomutuo.AnulacionContratoCSC.EntradaAnulacionContrato;
import cl.bcs.fondomutuo.AnulacionContratoCSC.InContrato;

import cl.bcs.fondomutuo.CrearAporteCSC.MensajesErrores;
import cl.bcs.fondomutuo.CrearAporteCSC.ParamsPortafolio_Update;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;


import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;

import cl.bcs.tesoreria.AnulacionMandatoPAC;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearIngresoCSC implements JavaCall{
	private static final Logger logger = LogManager.getLogger(CrearIngresoCSC.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();

	private final String SQL_INSERT_CONTRATO 			= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContrato.sql");
	private final String SQL_INSERT_CONTRATO_DETALLE 	= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertContratoDetalle.sql");	
	private final String SQL_SELECT_CONTRATO 			= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_SelectContrato.sql");	
	private final String SQL_SELECT_CONTRATOS 			= loadResourceToString("cl/bcs/fondomutuo/sql/CrearCotratoCSC_ConsultarContratoExistente.sql");
	private final String SQL_SELECT_CONTRATO_BIZ 		= loadResourceToString("cl/bcs/fondomutuo/sql/CrearContratoCSC_ConsultarContratosBIZ.sql");
	private final String SQL_SELECT_PORTAFOLIO 			= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_ConsultarPortafolio.sql");
	private final String SQL_SELECT_MANDATO_PAC 		= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_ConsultarMandatoPAC.sql");
	private final String SQL_INSERT_MANDATO_PAC 		= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoCSC_InsertarMandatoPAC.sql");
	private final String SQL_UPDATE_INDICADOR_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteCSC_InsertIndicadores.sql");

	private final String SQL_SELECT_SERIE_FONDO 		= loadResourceToString("cl/bcs/fondomutuo/sql/CrearIngresoContratoCSC_SelectFondoById.sql");	
	private final String SQL_CALL_CrearPortafolio 		= loadResourceToString("cl/bcs/APV/sql/Crear_Portafolio_APV_Call.sql");
	
	
	// --------------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------------
	public static class EntradaCrearIngresoCSC{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<InIngresoCSC> p_IngresoCSC;
		
		public EntradaCrearIngresoCSC() {
			this.p_IngresoCSC = new ArrayList<InIngresoCSC>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InIngresoCSC{
		public Long idfoliocsc;
		public String FolioCSC;
		public Long IdCliente = 0L;
		public Long IdPortafolio;
		public String Hora;
		public Date Fecha;
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;
		public BigDecimal MontoUSD;
		public String TipoPago;
		public Long IdBanco;
		public String DomicilioSucursal;
		public String CodigoSucursal;
		public String NombreFuncionario;
		public String DocumentoIdentificacionFuncionario;
		public String FirmaFuncionario;
		public Long DiaDescuento;
		public String AceptoCondiciones;
		public String Estado;
		public String Canal;
		public String FolioRelacionado;
		public String IdFolioEdit;
		public String IdEstado;
		public String CodigoPortafolio;
		public String AporteCSC = "N";
		public String MLT;
		
		public List<InIngresoCSCDetalle> p_IngresoCSCDetalle;   // Distribucion de la inversion
		
		public InIngresoCSC() {
			this.p_IngresoCSCDetalle = new ArrayList<InIngresoCSCDetalle>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InIngresoCSCDetalle{
		public String Serie;
		public BigDecimal Monto;
		public BigDecimal Porcentaje;
		public String Regimen;		

		public String toString() {
			return Utils.printJson(this, false);
		}		
	}

	
	// --------------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------------
	public static class SalidaCrearIngresoCSC{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoIngresoCSC> P_OUT_RESULTADO;
		public List <ResultadoOperacion> P_OUT_RESULTADO_OPERACION;

		public SalidaCrearIngresoCSC(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoIngresoCSC>();
			this.P_OUT_RESULTADO_OPERACION = new ArrayList<ResultadoOperacion>();
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ResultadoOperacion {
		public String Origen;
		public String Identificador;
		public List<MensajesErrores> Mensajes;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class ResultadoIngresoCSC{
		public Long IdFolioCSC;
		public String FolioCSC;
		public Long IdPortafolio;
	}
	
	
	// --------------------------------------------------------------
	// CLASES DE PROCESO
	// --------------------------------------------------------------
	public static class ContratoSuscripcionCuotas{
		public Long IdFolioCSC;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Hora;
		public Date Fecha;
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;
		public BigDecimal MontoUSD;
		public String TipoPago;
		public Long IdBanco;
		public String DomicilioSucursal;
		public String CodigoSucursal;
		public String NombreFuncionario;
		public String DocumentoIdentificacionFuncionario;
		public String FirmaFuncionario;
		public Long DiaDescuento;
		public String AceptoCondiciones;
		public String Estado;
		public String FolioCSC;
		public Long IdInterno;
		public String Canal;
		public String FolioRelacionado;
		public String IdFolioCSCEdit = "";
		public String IdCompuesto; 
		public String MLT;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MandatoPAC{
		public Long IdMandatoPAC;
		public Long IdCliente;
		public Long IdPortafolio;
		public String FolioCSC;
		public Long FolioPAC;
		public String CuentaCorriente;
		public String FolioSAAPV;
		public String Receptor;
		public Date Fecha;
		public Long NumeroMandato;
		public String Firma;
		public String IdentServicio;
		public String Estado;
		public String EstadoCargo;
		public Long IdEmpresa;
		public String Canal;
		public String EstadoRendicion;
		public String EstadoCargoBanco;
		public Long IdCarga;
		public String Compuesto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class INMandatoPAC{
		public Long IdCliente;
		public Long IdPortafolio;
		public String FolioCSC;
		public Long FolioPAC;
		public String CuentaCorriente;
		public String FolioSAAPV;
		public String Receptor;
		public Date Fecha;
		public Long NumeroMandato;
		public String Firma;
		public String IdentServicio;
		public String Estado;
		public String EstadoCargo;
		public Long IdEmpresa;
		public String Canal;
		public String EstadoRendicion;
		public String EstadoCargoBanco;
		public Long IdCarga;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratoSuscripcionCuotasDetalle{
		public Long IdCSCDetalle;
		public String Serie;
		public BigDecimal Monto;
		public BigDecimal Porcentaje;
		public String Regimen;
		public Long IdFolioCSC;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class CalculoMonto {
		public BigDecimal  Calculo (BigDecimal Monto, BigDecimal Porcentaje) {			
			BigDecimal MontoParcial = Monto.multiply(Porcentaje);
			BigDecimal MontoFinal = MontoParcial.divide(BigDecimal.valueOf(100));
			
			return MontoFinal;
	    }
	}
	
	public static class PortafolioProposito {
		public Long IdPortafolio;
		public String Proposito;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SerieFondo {
		public String IdSerieFondo;
		public String IdMoneda;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// --------------------------------------------------------------
	// CALL
	// --------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();	
		
		EntradaCrearIngresoCSC jin = gson.fromJson(in.toString(), EntradaCrearIngresoCSC.class);
		SalidaCrearIngresoCSC out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	
	// --------------------------------------------------------------
	// LOGICA
	// --------------------------------------------------------------
	public SalidaCrearIngresoCSC logica (Connection con, EntradaCrearIngresoCSC entrada) throws Exception{		
		logger.info("INICIO CrearIngresoCSC");
		SQL sql						=	new SQL();
		SalidaCrearIngresoCSC out 	= 	new SalidaCrearIngresoCSC();

		
		
		// Para creacion de contratps CSC
		List<ContratoSuscripcionCuotas> listContratoSuscripcionCuotas = new ArrayList<ContratoSuscripcionCuotas>();
		List<ContratoSuscripcionCuotasDetalle> listContratoSuscripcionCuotasDetalle = new ArrayList<ContratoSuscripcionCuotasDetalle>();
		List<INMandatoPAC> listMandatoPAC = new ArrayList<INMandatoPAC>();
		
		// Para anulacion de mandato PAC
		AnulacionMandatoPAC servicioAnularMandato = new AnulacionMandatoPAC();
		AnulacionMandatoPAC.EntradaAnulacionContrato entradaAnularMandato = new AnulacionMandatoPAC.EntradaAnulacionContrato();
		
		// Para anulacion de contrato CSC
		AnulacionContratoCSC servicioAnularContrato = new AnulacionContratoCSC();
		EntradaAnulacionContrato entradaAnulacionContrato = new EntradaAnulacionContrato();
		
		HashMap<String,Object> 	params  = new HashMap<String,Object>();
		ResultadoIngresoCSC resultado   = new ResultadoIngresoCSC();		
		ArrayList<String> correlativos  = new ArrayList<String>();
		ArrayList<Long>   IdsCliente    = new ArrayList<Long>();
		ArrayList<Long>   IdsPortafolio = new ArrayList<Long>();		
		List<String>        seriesFondo = new ArrayList<String>();
		
		try {
			
			String fechaSistema     = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
			Long   idPortafolio     = 0L;	
			
			// ---------------------------------------------------------
			// VALIDACIONES
			// ---------------------------------------------------------
			
			RevisarRequisitosBasicos basicos 				= 	new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos 	= 	basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				return out;
			}
			
			if(entrada.p_IngresoCSC.size() == 0){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			
			for (InIngresoCSC mov: entrada.p_IngresoCSC) {
				
				if(mov.Canal.equals("OPT")) {
					
					if(mov.IdFolioEdit == null || mov.IdFolioEdit.equals("")){
						out.P_OUT_ERRORESCONTADOR = 1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						return out;
					}
					
					if (correlativos.contains(mov.IdFolioEdit)) {
						out.P_OUT_ERRORESCONTADOR = 1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						return out;
					} else {
						correlativos.add(mov.IdFolioEdit);
						IdsCliente.add(mov.IdCliente);
						IdsPortafolio.add(mov.IdPortafolio);
					}
				}else {
					
					if( mov.Canal.equals("BIZ") && (mov.FolioCSC == null || mov.FolioCSC.equals(""))) {
						out.P_OUT_ERRORESCONTADOR = 1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						resultado.FolioCSC = mov.FolioCSC;
						resultado.IdFolioCSC = 0L;
						out.P_OUT_RESULTADO.add(resultado);
						return out;
					}
					if (correlativos.contains(mov.FolioCSC)) {
						out.P_OUT_ERRORESCONTADOR = 1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						resultado.FolioCSC = mov.FolioCSC;
						resultado.IdFolioCSC = 0L;
						out.P_OUT_RESULTADO.add(resultado);
						return out;
					} else {
						correlativos.add(mov.FolioCSC);
						IdsCliente.add(mov.IdCliente);
						IdsPortafolio.add(mov.IdPortafolio);
					}
				}
				
				
				if(mov.IdCliente == 0 && mov.IdPortafolio == null){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CLIENTE_O_PORTAFOLIO_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					resultado.FolioCSC = mov.FolioCSC;
					resultado.IdFolioCSC = 0L;
					out.P_OUT_RESULTADO.add(resultado);
					return out;
				}

				
				for (InIngresoCSCDetalle distribucion: mov.p_IngresoCSCDetalle) {
					if (!seriesFondo.contains(distribucion.Serie))
					{
						seriesFondo.add(distribucion.Serie);
					}
				}
				
	
			}
			
			// ---------------------------------------------------------------------
			// BUSCA INFORMACION PARA VALIDACIONES
			// ---------------------------------------------------------------------

			// Consulta datos de contratos CSC por Folio (para validar si ya existe el folio)
			params.clear();	
			params.put("FolioCSC", correlativos);
			List<ContratoSuscripcionCuotas> lstContratosExistentes = sql.query(con, SQL_SELECT_CONTRATO, params, ContratoSuscripcionCuotas.class);			
			HashMap<String,Object> hashContratosExistentes = Utils.List2Hash(lstContratosExistentes, "FolioCSC");

			
			params.clear();			
			params.put("IdsCliente", IdsCliente);
			params.put("IdsPortafolio", IdsPortafolio);
			List<ContratoSuscripcionCuotas> contratosIguales = sql.query(con, SQL_SELECT_CONTRATOS, params, ContratoSuscripcionCuotas.class);
			HashMap<String,Object> iguales = Utils.List2Hash(contratosIguales, "IdCompuesto");
			
			//Obtiene informacion de mandatos
			HashMap<String,Object> hMandatosExistentes =new HashMap<String, Object>();
			if(entrada.p_IngresoCSC.get(0).TipoPago.equalsIgnoreCase("PAC")) {
				List<MandatoPAC> lstMandatosExistentes = sql.query(con, SQL_SELECT_MANDATO_PAC, params, MandatoPAC.class);
				 hMandatosExistentes = Utils.List2Hash(lstMandatosExistentes, "Compuesto");	
			}
			
			
			//Obtiene informacion de contratos Biznet
			params.clear();			
			params.put("IdsCliente", IdsCliente);
			params.put("IdsPortafolio", IdsPortafolio);
			List<ContratoSuscripcionCuotas> lstContratosBiznet = sql.query(con, SQL_SELECT_CONTRATO_BIZ, params, ContratoSuscripcionCuotas.class);
			HashMap<String,Object> hashContratosBiznet = Utils.List2Hash(lstContratosBiznet, "IdCompuesto");
			
			
			

			// Consulta portafolios
			params.clear();	
			params.put("IdPortafolio", IdsPortafolio);
			List<PortafolioProposito> lstPortafolio = sql.query(con, SQL_SELECT_PORTAFOLIO, params, PortafolioProposito.class);
			HashMap<Long,Object> hashPortafolio = Utils.List2Hash(lstPortafolio, "IdPortafolio");
	
			
			
			// Consulta series fondos monedas		
			params.clear();	
			params.put("IdSerieFondo", seriesFondo);
			List<SerieFondo> lstSerieFondo = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondo.class);
			HashMap<Long,Object> hashSerieFondo = Utils.List2Hash(lstSerieFondo, "IdSerieFondo");
			
			params.clear();
			


			
			int i = 0;
			List<Long> secuenciaCSC = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::IdCSC",  new Long(entrada.p_IngresoCSC.size()));


			Long ErroresContador = 0L;
			for (InIngresoCSC ingresos : entrada.p_IngresoCSC ){
												
				idPortafolio = ingresos.IdPortafolio;
				
				
				// -----------------------------------------------------------
				// obtencion de folioSuscripcion 
				// -----------------------------------------------------------
				ObtenerFolioSuscripcion  obtenerFolioSuscripcion	= 	new ObtenerFolioSuscripcion();
				if(esGeneracionFolioAutomatico(ingresos.FolioCSC)) {
					ObtenerFolioSuscripcion_Entrada in = new ObtenerFolioSuscripcion_Entrada();
					ObtenerFolioSuscripcion_Salida salidaFolio = new ObtenerFolioSuscripcion_Salida();
					in.p_Canal = ingresos.FolioCSC;
					in.p_IdEmpresa = entrada.p_IdEmpresa;
					in.p_IdUsuario = entrada.p_IdUsuario;
					
					salidaFolio = obtenerFolioSuscripcion.logica(con, in);
					ingresos.FolioCSC = salidaFolio.p_Out_FolioSuscripcion;
				}
				
				if ("PAC".equalsIgnoreCase(ingresos.TipoPago) && (ingresos.MontoUSD.compareTo(ingresos.MontoUSD) != 0)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR,"MONEDA_NO_VALIDA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				// -----------------------------------------------------------
				// OTRAS VALIDACIONES
				// -----------------------------------------------------------

				//Valida que FolioCSC sea unico
				ContratoSuscripcionCuotas infoContrato = null;
				
				if(ingresos.Canal.equals("OPT")) {
					infoContrato = (ContratoSuscripcionCuotas) hashContratosExistentes.get(ingresos.IdFolioEdit);
				}else {
					infoContrato = (ContratoSuscripcionCuotas) hashContratosExistentes.get(ingresos.FolioCSC);
				}				
				
				if(infoContrato != null && noEsGeneracionFolioAutomatico(ingresos.FolioCSC)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR,"FOLIO_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					resultado.FolioCSC = ingresos.IdFolioEdit == null ? ingresos.FolioCSC : ingresos.IdFolioEdit;
					resultado.IdFolioCSC = 0L;
					out.P_OUT_RESULTADO.add(resultado);
					continue;
				}
				
				
				boolean esContratoBIZ = false;				
				ContratoSuscripcionCuotas c = null;
						
				if("BIZ".equals(ingresos.Canal) || noEsGeneracionFolioAutomatico(ingresos.FolioCSC)) 
				{	// Es un contrato Biznet
					c = (ContratoSuscripcionCuotas) hashContratosBiznet.get(ingresos.IdCliente+"_"+ingresos.IdPortafolio);
					
					esContratoBIZ = true;
				}else {
					if(iguales.size()>0 && !ingresos.Canal.equals("WEB")){
						c = (ContratoSuscripcionCuotas) iguales.get(ingresos.IdCliente+"_"+ingresos.IdPortafolio);
						if(c.IdCompuesto!=null && ingresos.TipoPago.equals("PAC")) {
							out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR,"CONTRATO_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
							continue;
						}
					}
				}
				
				// Valida que portafolio exista y sea proposito FFMM (110) // Si portafolio viene en cero se crea mas abajo				
				if (!(ingresos.AporteCSC.equalsIgnoreCase("S") && idPortafolio.compareTo(0L) == 0))
				{
					PortafolioProposito infoPortafolio = (PortafolioProposito) hashPortafolio.get(ingresos.IdPortafolio);
					
					if(infoPortafolio == null) {
						out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR, "PORTAFOLIO_NO_EXISTE" ,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						resultado.FolioCSC = ingresos.IdFolioEdit == null ? ingresos.FolioCSC : ingresos.IdFolioEdit;
						resultado.IdFolioCSC = 0L;
						out.P_OUT_RESULTADO.add(resultado);
						continue;
					}
					
					if(!_c.PROPOSITO_FFMM.equals(infoPortafolio.Proposito)) {
						out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR, "PROPOSITO_PORTAFOLIO_NO_ES_CORRECTO" ,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						resultado.FolioCSC = ingresos.IdFolioEdit == null ? ingresos.FolioCSC : ingresos.IdFolioEdit;
						resultado.IdFolioCSC = 0L;
						out.P_OUT_RESULTADO.add(resultado);
						continue;
					}	
				}
				

				
			

				// Valida que Serie Fondo exista				
				for (InIngresoCSCDetalle distribucion: ingresos.p_IngresoCSCDetalle) {
					SerieFondo infoSerieFondo = (SerieFondo) hashSerieFondo.get(distribucion.Serie);
					
					if (infoSerieFondo  == null) {						
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_SERIE_FONDO_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						resultado.FolioCSC = ingresos.IdFolioEdit == null ? ingresos.FolioCSC : ingresos.IdFolioEdit;
						resultado.IdFolioCSC = 0L;
						break;
					}					
				}
				

				
		
			
					
				
				// -----------------------------------------------------------
				// solo creacion de portafolio AporteCSC
				// -----------------------------------------------------------

				if(ingresos.AporteCSC.equalsIgnoreCase("S")) {	

					
					if (idPortafolio.compareTo(0L) == 0) {
						String propositoPortafolio = ConfiguracionSistema.getParametroConfiguracionString(con,	entrada.p_IdEmpresa, "FONDOMUTUO", "PropositoPortafolioAporteCSC");
						String perfilComercial     = ConfiguracionSistema.getParametroConfiguracionString(con,	entrada.p_IdEmpresa, "FONDOMUTUO", "PerfilComercialBaseAporteCSC");

						HashMap<String, Object> paramsPorta = new HashMap<String, Object>();
						paramsPorta.put("IdEmpresa", entrada.p_IdEmpresa);
						paramsPorta.put("IdUsuario", entrada.p_IdUsuario);
						paramsPorta.put("IdModulo", "FMM");
						paramsPorta.put("PropositoPortafolio", propositoPortafolio);
						paramsPorta.put("IdCliente", ingresos.IdCliente);
						paramsPorta.put("IdPerfilComercial", perfilComercial);
						paramsPorta.put("CodigoPortafolio", ingresos.CodigoPortafolio);
						paramsPorta.put("CodigoRegimen", "");
						paramsPorta.put("NombrePlan", "");
						paramsPorta.put("CodigoAgrupador", "");

						// Salida HashMap
						HashMap<String, Class> paramsPortaOut = new HashMap<String, Class>();
						paramsPortaOut.put("p_out_RefId", Long.class);
						paramsPortaOut.put("p_out_AdvertenciasContador", Long.class);
						paramsPortaOut.put("p_out_ErroresContador", Long.class);
						paramsPortaOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
						paramsPortaOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);

						HashMap<String, Object> resultSP = sql.call(con, SQL_CALL_CrearPortafolio, paramsPorta,paramsPortaOut);

						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
						List<ResultadoOperacion> listaResultadoOperacion = new ArrayList<ResultadoOperacion>();
						if (!((Long) resultSP.get("p_out_AdvertenciasContador")).equals(new Long(0))) {
							for (AdvertenciaDetalleCall advertencia : (List<AdvertenciaDetalleCall>) resultSP
									.get("p_out_AdvertenciasDetalle")) {
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ADVERTENCIA";
								mensajesErrores.Codigo = advertencia.Codigo;
								mensajesErrores.Mensaje = advertencia.Descripcion;

								objResultadoOperacion.Origen = "FFMM";
								objResultadoOperacion.Identificador = "0";
								listaMensajesErrores.add(mensajesErrores);
							}
							
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							listaResultadoOperacion.add(objResultadoOperacion);
							out.P_OUT_RESULTADO_OPERACION = listaResultadoOperacion;
						
						}

						if (!((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))) {
							for (ErrorDetalleCall error : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")) {
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = error.Codigo;
								mensajesErrores.Mensaje = error.Descripcion;

								objResultadoOperacion.Origen = "FFMM";
								objResultadoOperacion.Identificador = "0";

								listaMensajesErrores.add(mensajesErrores);
								objResultadoOperacion.Mensajes = listaMensajesErrores;
								listaResultadoOperacion.add(objResultadoOperacion);
								ErroresContador++;
							}
							out.P_OUT_ERRORESCONTADOR = ErroresContador;
							out.P_OUT_RESULTADO_OPERACION = listaResultadoOperacion;
							return out;
						} else {
							idPortafolio = (Long) resultSP.get("p_out_RefId");
							// Actualizar parametro OPERACUENTAINVERSION
							List<ParamsPortafolio_Update> update_portafolio = new ArrayList<ParamsPortafolio_Update>();
							ParamsPortafolio_Update param = new ParamsPortafolio_Update();
							param.p_IdPortafolio = idPortafolio;
							param.p_IdEmpresa = entrada.p_IdEmpresa;
							update_portafolio.add(param);

							sql.batch(con, SQL_UPDATE_INDICADOR_PORTAFOLIO, update_portafolio,
									ParamsPortafolio_Update.class);
						}
					}
					
					resultado.IdPortafolio = idPortafolio;
					ingresos.IdPortafolio = idPortafolio;
					out.P_OUT_RESULTADO.add(resultado);
				
				}


				


				
				
				// -----------------------------------------------------------------
				// PREPARA DATOS PARA CREAR MANDATO PAC (Solo si pago es PAC y cliente tiene un mandato anterior)
				// -----------------------------------------------------------------
				
				if (ingresos.TipoPago.equalsIgnoreCase("PAC") && hMandatosExistentes.get(ingresos.IdCliente + "_" + ingresos.IdPortafolio) != null && (ingresos.Canal.equalsIgnoreCase("WEB") || ingresos.Canal.equalsIgnoreCase("BIZ"))){
					
					MandatoPAC mandato = (MandatoPAC) hMandatosExistentes.get(ingresos.IdCliente + "_" + ingresos.IdPortafolio);
					//Para crear nuevo mandato a partir del existente
					INMandatoPAC ingresoPAC = new INMandatoPAC();					
					ingresoPAC.IdCliente 		= mandato.IdCliente;
					ingresoPAC.IdPortafolio 	= mandato.IdPortafolio;
					ingresoPAC.FolioCSC 		= ingresos.FolioCSC;
					ingresoPAC.FolioPAC 		= mandato.FolioPAC;
					ingresoPAC.CuentaCorriente 	= mandato.CuentaCorriente;
					ingresoPAC.FolioSAAPV 		= mandato.FolioSAAPV;
					ingresoPAC.Receptor 		= mandato.Receptor;
					ingresoPAC.Fecha 			= mandato.Fecha;
					ingresoPAC.NumeroMandato 	= mandato.NumeroMandato;
					ingresoPAC.Firma 			= mandato.Firma;
					ingresoPAC.IdentServicio 	= mandato.IdentServicio;
					ingresoPAC.Estado 			= mandato.Estado;
					ingresoPAC.EstadoCargo 		= mandato.EstadoCargo;
					ingresoPAC.IdEmpresa 		= mandato.IdEmpresa;
					ingresoPAC.Canal 			= mandato.Canal;
					ingresoPAC.EstadoRendicion 	= mandato.EstadoRendicion;
					ingresoPAC.EstadoCargoBanco = mandato.EstadoCargoBanco;
					ingresoPAC.IdCarga 			= mandato.IdCarga;
					listMandatoPAC.add(ingresoPAC);
					
					//Para anuacion de mandato existente
					AnulacionMandatoPAC.InContrato contrato = new AnulacionMandatoPAC.InContrato();
					contrato.IdContrato 		= mandato.IdMandatoPAC;
					contrato.Estado 			= "ANU";
					entradaAnularMandato.p_Contrato.add(contrato);									
				}
				
				
				
				// -----------------------------------------------------------------
				// PREPARA DATOS PARA CREAR CONTRATO CSC
				// -----------------------------------------------------------------
				
				ContratoSuscripcionCuotas contratoSuscripcionCuotas 	= 	new ContratoSuscripcionCuotas();
				
				contratoSuscripcionCuotas.IdFolioCSC = secuenciaCSC.get(i);
				
				if(ingresos.Canal.equals("OPT")) {
					contratoSuscripcionCuotas.FolioCSC = ingresos.IdFolioEdit;	
				}else {
					contratoSuscripcionCuotas.FolioCSC = ingresos.FolioCSC;
				}
				
				resultado.IdFolioCSC 	= contratoSuscripcionCuotas.IdFolioCSC;
				resultado.FolioCSC 		= contratoSuscripcionCuotas.FolioCSC;
				out.P_OUT_RESULTADO.add(resultado);
				
				contratoSuscripcionCuotas.IdCliente 		= ingresos.IdCliente;
				contratoSuscripcionCuotas.IdPortafolio		= ingresos.IdPortafolio;
				contratoSuscripcionCuotas.Hora 				= ingresos.Hora;
				contratoSuscripcionCuotas.Fecha 			= ingresos.Fecha;
				contratoSuscripcionCuotas.MontoPesos 		= ingresos.MontoPesos;
				contratoSuscripcionCuotas.MontoUF 			= ingresos.MontoUF;
				contratoSuscripcionCuotas.MontoUSD 			= ingresos.MontoUSD;
				contratoSuscripcionCuotas.TipoPago 			= ingresos.TipoPago;
				contratoSuscripcionCuotas.IdBanco 			= ingresos.IdBanco;
				contratoSuscripcionCuotas.NombreFuncionario = ingresos.NombreFuncionario;
				contratoSuscripcionCuotas.DomicilioSucursal = ingresos.DomicilioSucursal;
				contratoSuscripcionCuotas.CodigoSucursal 	= ingresos.CodigoSucursal;
				contratoSuscripcionCuotas.DocumentoIdentificacionFuncionario = ingresos.DocumentoIdentificacionFuncionario;
				contratoSuscripcionCuotas.FirmaFuncionario 	= ingresos.FirmaFuncionario;
				contratoSuscripcionCuotas.DiaDescuento 		= ingresos.DiaDescuento;
				contratoSuscripcionCuotas.AceptoCondiciones = ingresos.AceptoCondiciones;
				contratoSuscripcionCuotas.Canal 			= ingresos.Canal;
				contratoSuscripcionCuotas.MLT 				= ingresos.MLT;
				
				int suma = 0;
				for (InIngresoCSCDetalle ingresosdetalle : ingresos.p_IngresoCSCDetalle){
					suma = suma + ingresosdetalle.Porcentaje.intValue();
				}
			
				if (suma <= 100) {
					
					listContratoSuscripcionCuotas.add(contratoSuscripcionCuotas);
					
					for (InIngresoCSCDetalle ingresosdetalle : ingresos.p_IngresoCSCDetalle){		
						
						ContratoSuscripcionCuotasDetalle contratoSuscripcionCuotasDetalle = new ContratoSuscripcionCuotasDetalle();
						contratoSuscripcionCuotasDetalle.Serie = ingresosdetalle.Serie;
						
						contratoSuscripcionCuotasDetalle.Porcentaje = ingresosdetalle.Porcentaje;
						contratoSuscripcionCuotasDetalle.Regimen = ingresosdetalle.Regimen;
						contratoSuscripcionCuotasDetalle.IdFolioCSC = contratoSuscripcionCuotas.IdFolioCSC;
						
						if (contratoSuscripcionCuotasDetalle.Porcentaje.compareTo(BigDecimal.ZERO) == 0 ) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(Long.parseLong(i+""),MensajeSalida.MENSAJE_ERROR,"PORCENTAJE DEBE SER MAYOR A CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
							break;
						}
						
						if (ingresos.MontoPesos.intValue() != 0){
							CalculoMonto calculomonto = new CalculoMonto();
							BigDecimal monto_detalle = calculomonto.Calculo(contratoSuscripcionCuotas.MontoPesos, contratoSuscripcionCuotasDetalle.Porcentaje);
							contratoSuscripcionCuotasDetalle.Monto = monto_detalle;
							
						}
						
						if (ingresos.MontoUSD.intValue() != 0){
							CalculoMonto calculomonto = new CalculoMonto();
							BigDecimal monto_detalle = calculomonto.Calculo(contratoSuscripcionCuotas.MontoUSD, contratoSuscripcionCuotasDetalle.Porcentaje);
							contratoSuscripcionCuotasDetalle.Monto = monto_detalle;
							
						}
					
						if (ingresos.MontoUF.intValue() != 0){
							CalculoMonto calculomonto = new CalculoMonto();
							BigDecimal monto_detalle = calculomonto.Calculo(contratoSuscripcionCuotas.MontoUF, contratoSuscripcionCuotasDetalle.Porcentaje);
							contratoSuscripcionCuotasDetalle.Monto = monto_detalle;
						}
						
						if (contratoSuscripcionCuotasDetalle.Monto == null) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(Long.parseLong(i+""),MensajeSalida.MENSAJE_ERROR,"MONTO DEBE SER MAYOR A CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
							continue;
						}else {
							listContratoSuscripcionCuotasDetalle.add(contratoSuscripcionCuotasDetalle);
						}
						
	
					}
				
					
					//-------------------------------------------------------------
					// AUTORIZACION
					//-------------------------------------------------------------
										
					String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, _c.ID_CONCEPTO);
		
					if(requiereAutorizacion.equals("S")) {
						CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
						
						CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
						entradaEvtAutorizacion.P_IDEMPRESA = entrada.p_IdEmpresa;
						entradaEvtAutorizacion.P_IDMODULO = entrada.p_IdModulo;
						entradaEvtAutorizacion.P_IDUSUARIO = entrada.p_IdUsuario;
						entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
						entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";
						
						CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
						
						evento.IdTipoEvento = "NUECSC";
						evento.IdModuloSolicita = entrada.p_IdModulo;
						evento.IdUsuarioSolicita = entrada.p_IdUsuario;
						evento.Fecha = fechaSistema;
						evento.IdConcepto = "NUECSC";
						
						evento.FechaOperacionConcepto = fechaSistema;
						evento.IdCliente = (long) 1;
						
						evento.Glosa = "Ingreso Contrato CSC"+" "+"IdFolioCSC:"+contratoSuscripcionCuotas.IdFolioCSC;
						evento.NumeroOperacionConcepto = Long.toString(contratoSuscripcionCuotas.IdFolioCSC);
						
						entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
						
						CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(con, entradaEvtAutorizacion);
						
						for(CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salidaAut.P_OUT_ADVERTENCIASDETALLE){
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,advertencia.Descripcion,new HashMap<String,String>(),con,entrada.p_IdEmpresa));					
						}
						
						for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,error.Descripcion,new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						}
					}
					else 
					{
						contratoSuscripcionCuotas.Estado = _c.EstadoIngresado;					
					}	
					
					
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(Long.parseLong(i+""),MensajeSalida.MENSAJE_ERROR,"PORCENTAJE_MAYOR_100",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				i++;
			}

			// -----------------------------------------------------------------------
			// CREA CONTRATO CSC Y SU DETALLE
			// -----------------------------------------------------------------------
			
			try {
				if (listContratoSuscripcionCuotas.size() > 0) {
					sql.batch(con, SQL_INSERT_CONTRATO, listContratoSuscripcionCuotas, ContratoSuscripcionCuotas.class);
		
					long iterador = 0L;
					for (ContratoSuscripcionCuotas salida : listContratoSuscripcionCuotas){
						out.P_OUT_MENSAJES.add(new MensajeSalida(iterador,MensajeSalida.MENSAJE_OK,"CREACION_CONTRATO_OK",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
						iterador ++;
					}
				}
				
				if (listContratoSuscripcionCuotasDetalle.size() > 0) {
					sql.batch(con, SQL_INSERT_CONTRATO_DETALLE, listContratoSuscripcionCuotasDetalle, ContratoSuscripcionCuotasDetalle.class);
				}
			}catch(Exception er) {
				String codigoError = er.getMessage().substring(er.getMessage().indexOf("[")+1,er.getMessage().indexOf("]"));
				if("301".equalsIgnoreCase(codigoError)) {
					out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR,"FOLIO_YA_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					resultado.FolioCSC = "";
					resultado.IdFolioCSC = 0L;
					out.P_OUT_RESULTADO.add(resultado);
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida( Long.parseLong(i+""), MensajeSalida.MENSAJE_ERROR,"ERROR_CREAR_CONTRATO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					resultado.FolioCSC = "";
					resultado.IdFolioCSC = 0L;
					out.P_OUT_RESULTADO.add(resultado);
				}
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}

			
			// ---------------------------------------------------
			// ANULA CONTRATOS CSC ANTERIOR (Para contratos Biznet pagados con PAC)
			// ---------------------------------------------------
			if(entradaAnulacionContrato.p_Contrato.size()>0) 
			{
				entradaAnulacionContrato.p_IdEmpresa 	= entrada.p_IdEmpresa;
				entradaAnulacionContrato.p_IdModulo 	= entrada.p_IdModulo;
				entradaAnulacionContrato.p_IdUsuario 	= entrada.p_IdUsuario;
				servicioAnularContrato.logica(con, entradaAnulacionContrato);
			} 
			
			
			// ---------------------------------------------------------------------
			// CREA MANDATO PAC Y ANULA ANTERIOR
			// ---------------------------------------------------------------------

			if (listMandatoPAC.size() > 0){
				sql.batch(con, SQL_INSERT_MANDATO_PAC, listMandatoPAC, INMandatoPAC.class);
			}
			
			if (entradaAnularMandato.p_Contrato.size() > 0){
				entradaAnularMandato.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaAnularMandato.p_IdUsuario = entrada.p_IdUsuario;
				entradaAnularMandato.p_IdModulo = entrada.p_IdModulo;
				servicioAnularMandato.logica(con, entradaAnularMandato);
			}

			
			

			
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}catch (Exception error) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(error);
			throw error;
		}
		logger.info("FIN CrearIngresoCSC");
		return out;
	}
	
	private Boolean esGeneracionFolioAutomatico(String folioEntrada) {
		Boolean esGeneracionFolioAutomatico = folioEntrada != null && folioEntrada.length() == 1 && Character.isLetter(folioEntrada.charAt(0));
		return esGeneracionFolioAutomatico;
	}
	
	private Boolean noEsGeneracionFolioAutomatico(String folioEntrada) {
		return !esGeneracionFolioAutomatico(folioEntrada);
	}
	
	
	// --------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":150,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IngresoCSC\":[{\"IdFolioCSC\":0,\"IdCliente\":43080,\"IdFolioEdit\":898900,\"IdPortafolio\":38326,\"Hora\":\"12:00:00\",\"Fecha\":\"2019-10-16T03:00:00.000Z\",\"MontoPesos\":55000,\"MontoUF\":0,\"TipoPago\":\"VVI\",\"IdBanco\":0,\"DomicilioSucursal\":\"APOQUINDO\",\"CodigoSucursal\":\"01\",\"NombreFuncionario\":\"MACARENA\",\"DocumentoIdentificacionFuncionario\":\"16657034-4\",\"FirmaFuncionario\":true,\"DiaDescuento\":0,\"AceptoCondiciones\":true,\"Canal\":\"OPT\",\"IdEstado\":\"\",\"p_IngresoCSCDetalle\":[{\"Serie\":\"00038ACLP\",\"Monto\":0,\"Porcentaje\":0,\"Regimen\":\"18Q\"}]}]}";
			EntradaCrearIngresoCSC  Objin = gson.fromJson(json, EntradaCrearIngresoCSC .class);						
			CrearIngresoCSC  demo = new CrearIngresoCSC();
			try {
				SalidaCrearIngresoCSC  ObjOut = demo.logica(con, Objin);
				//con.commit();
				con.rollback();
				System.out.println(ObjOut);
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}			
		}
	}
}