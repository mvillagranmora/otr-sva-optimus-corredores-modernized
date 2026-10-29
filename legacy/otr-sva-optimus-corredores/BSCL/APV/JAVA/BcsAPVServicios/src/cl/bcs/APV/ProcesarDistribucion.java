package cl.bcs.APV;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import java.util.stream.Collectors;

import cl.bcs.APV.DevolucionPagoRezagos.IdPortafolioDefecto;
import cl.bcs.APV.TraspasoCuentasBancarias.OrigenRecaudacion;
import cl.bcs.APV.ValidaContratoSAAPV.ContratosSAAPV;
import cl.bcs.APV.ValidaContratoSAAPV.DetalleValidacionContratoSAAPV;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CalcularMontoYCobrosOrden;
import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.Instrumento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ProcesarDistribucion implements JavaCall   {

	static final Logger logger = LogManager.getLogger(ProcesarDistribucion.class);
	
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_SELECT_Recaudacion = loadResourceToString("cl/bcs/APV/sql/ConsultarDetalleRecaudacionADistribuir.sql");
	public String SQL_SELECT_DistribucionContrato = loadResourceToString("cl/bcs/APV/sql/ConsultaDistribucionContrato.sql");
	public String SQL_SELECT_DistribucionContrato_ProcesarDistribucionSinFondoRestringido = loadResourceToString("cl/bcs/APV/sql/ConsultaDistribucionContratoSinFondoRestringido.sql");
	public String SQL_SELECT_DistribucionDefecto = loadResourceToString("cl/bcs/APV/sql/ConsultaDistribucionDefecto.sql");
	public String SQL_CREAR_Recaudacion = loadResourceToString("cl/bcs/APV/sql/Deposito_Insert.sql");
	public String SQL_CAMBIA_ESTADO_Recaudacion = loadResourceToString("cl/bcs/APV/sql/Deposito_Update.sql");
	public String SQL_CAMBIA_ESTADO_Empleador = loadResourceToString("cl/bcs/APV/sql/DetalleEmpleador_CambiaEstado.sql");
	public String SQL_CAMBIA_ESTADO_Cliente = loadResourceToString("cl/bcs/APV/sql/DetalleCliente_CambiaEstado.sql");
	public String SQL_INSERT_EstadoTransicion = loadResourceToString("cl/bcs/APV/sql/EstadoTransicion_Insert.sql");
	public String SQL_UPDATE_ContratoDistribucion= loadResourceToString("cl/bcs/APV/sql/ContratoDistribucion_Update.sql");
	public String SQL_CAMBIA_ESTADO_ContratoSAAPV = loadResourceToString("cl/bcs/APV/sql/CambiaEstadoContratoSAAPVRecaudacion.sql");
	public String SQL_SELECT_CONTRATO_SAAPV = loadResourceToString("cl/bcs/APV/sql/ProcesarDistribucion_SelectContratoSAAPV.sql");
	public String SQL_SELECT_SOLICITUDES_FFMM_ASOCIADAS = loadResourceToString("cl/bcs/APV/sql/ProcesarDistribucion_SelectSolicitudesFFMMAsociadasARecaudacionCliente.sql");
	public String SQL_SELECT_BLOQUEO_DISTRIBUCION = loadResourceToString("cl/bcs/APV/sql/ProcesarDistribucion_SelectBloqueoDistribucion.sql");
	


	private final String SQL_PortafolioEmpresa = "SELECT \"CLI\".\"IdPortafolioDefecto\"" + 
			"FROM \"BSCL.Plataforma.BD::PLA.Empresa\" AS \"EMP\"" + 
			"INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" AS \"SOC\" ON (\"SOC\".\"IdSocioDeNegocio\" = \"EMP\".\"IdSocioDeNegocio\")" + 
			"INNER JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" AS \"CLI\" ON ( \"CLI\".\"IdPersona\" = \"SOC\".\"IdPersona\" )" + 
			"WHERE \"EMP\".\"IdEmpresa\" = :IdEmpresa";
			
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class ParametrosRecaudacion{
		public Long IdRecaudacion;
		public Long IdContratoSAAPV;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Recaudacion{
		public Long FolioRecaudacion;
		public Long IdRecaudacionCliente;
		public Long IdRecaudacionEmpleador;			
		public Long IdPersonaCliente;				
		public Long IdCliente;			
		public String RutEmpleador;		
		public Date FechaDeposito;				
		public BigDecimal RecTotal;				
		public Long IdBanco;			
		public Long IdContrato;	
		public Long IdPortafolio;	
		public String IdRegimen;	
		public String IdTipoDeposito;				
		public String NombreTipoDeposito;		
		public String CuentaCorriente;			
		public String CodigoEstado;		
		public String Observacion;	
		public String Pago;
		public List<DistribucionContrato> Distribucion;
		public String RutCliente;
		
		public Recaudacion(){
			this.Distribucion = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	
	public static class DistribucionContrato{
		public Long IdDistribucionInversion;
		public Long IdContratoSAAPV;
		public String IdTipoInstrumento;
		public Long IdInstrumento;		
		public String Nemotecnico;				
		public BigDecimal MontoPesos;			
		public BigDecimal Porcentaje;	
		public String IdEstado;	
		public String IdMoneda;		
		public String IdMonedaOrigen; 
		public DistribucionContrato(){
			IdDistribucionInversion = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}


	public static class SeriesFFMM{
		public Long IdInterno;
		public Date Fecha;
		public String IdSerieFondo;
		public String CalculoPorMontoCuota;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String IdMonedaFondo;
		public String IdMonedaInversion;
		public String TipoSolicitud;
		public String TipoFactorAjuste;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	public static class ResultadoSeriesFFMM{
		public Long IdInterno;
		public String TipoSolicitud;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal MontoDestino;
		public BigDecimal ValorParidad;
		public BigDecimal ValorFactorAjuste;
		public BigDecimal CostoEnFactorAjuste;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public String Resultado;
		public String CodigoMensaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class ResultadoFFMM{
		public Long IdInterno;
		public Long IdSolicitudAporteRescate;
		public String TipoSolicitud;
		public String IdSerieFondo;
		public String NombreSerieFondo;
		public String CodigoSerie;
		public String IdTipoMovimientoFondoMutuo;
		public String IdSerieFondoDestino;
		public String IdEmisorDestino; 
		public String IndicadorMLT;
		public String Overnight;
		public String CalculoPorMontoCuota;
		public String EsPorMontoLiquido;
		public java.sql.Date FechaAceptacion;//DATE;
		public java.sql.Date FechaOriginalInversion;//DATE;
		public java.sql.Timestamp FechaIngresoRegistro;//TIMESTAMP;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal CostoUF;
		public BigDecimal CostoMonedaLocal;
		public BigDecimal ValorUFFechaInversion;
		public BigDecimal ParidadFechaInversion;
		public String IdMoneda;
		public BigDecimal MontoComision;
		public BigDecimal MontoPago;
		public BigDecimal MontoComisionLocal;
		public BigDecimal MontoPagoLocal;
		public String IdFondo;
		public String Nemotecnico;
		public String NombreFondo;
		public String NombreCorto;
		public String IdEmisorOrigen;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreCliente;
		public java.sql.Date FechaLiquidacion;    //Date
		public java.sql.Date FechaCertificacion;	//Date
		public BigDecimal MontoTransferencia;
		public BigDecimal NumeroCuotaTransferencia;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AdvertenciasDetalleFFMM{
		public Long IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErroresDetalleFFMM{
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CalculoMontoCuotaFFMM{
		public Long IdInterno;
		public String Fecha;
		public String IdSerieFondo;
		public String CalculoPorMontoCuota;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public String IdMonedaFondo;
		public String IdMonedaInversion;
		public String TipoSolicitud;
		public String TipoFactorAjuste;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ResultadoMontoCuotaFFMM{
		public Long IdInterno; 
		public String Fecha;
		public String IdSerieFondo;
		public BigDecimal Monto;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public BigDecimal MontoDestino;
		public BigDecimal ValorParidad;
		public BigDecimal ValorFactorAjuste;
		public BigDecimal CostoEnFactorAjuste;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public String Resultado;
		public String CodigoMensaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Recaudacion_CambiaEstado{
		public Long IdRecaudacion;
		public String FolioCartola;
		public Long LineaCartola;
		public Long Sucursal;
		public Date FechaDeposito;
		public String CuentaCorriente;
		public Long MontoDeposito;
		public Long ConceptoRecaudacion;
		public Long Origen;
		public Long Observacion;
		public Long NumeroDocumento;
		public Long IdTipoDeposito;
		public Long IdBanco;
		public Long IdRegimenTributario;
		public Long IdCarga;
		public String IdEstado;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
				
	}
	
	public static class DetalleCliente_CambiaEstado{
		public Long IdRecaudacionCliente;
		public String IdEstado;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleEmpleador_CambiaEstado{
		public Long IdRecaudacionEmpleador;
		public String IdEstado;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CambiaEstadoContratoSAAPVRecaudacion{
		public Long IdContratoSAAPV;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ParamsInsertEstadoTransicion{
		public String Tipo;
		public Long IdRegistro;
		public java.sql.Timestamp FechaTransicion;
		public String EstadoAnterior;
		public String EstadoNuevo;
		public String Observacion;
		public Long Usuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class UpdateContratoDistribucion{
		public Long IdRecaudacionCliente;
		public Long IdContratoSAAPV;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PortafolioReaperturaRecaudacion{
		public Long IdPortafolio;
	}

	public static class ResultadoInstrumento{
		public Long IdInstrumento; 				
		public String IdBolsa; 					
		public String FechaConsulta;				
		public Date Fecha;						
		public BigDecimal VolumenTransado; 		
		public BigDecimal MontoTransado; 		
		public BigDecimal PrecioBase;			
		public BigDecimal PrecioCierre;			
		public BigDecimal PuntaCompra;			
		public BigDecimal PuntaVenta;			
		public BigDecimal Variacion;			
		public String IdMoneda;					
		public BigDecimal PrecioApertura;		
		public BigDecimal PrecioMayor;			
		public BigDecimal PrecioMenor;			
		public BigDecimal PrecioMedio;
		public BigDecimal PrecioMercado;		
		public BigDecimal TasaMercado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		public 	String					p_Opcion;
		public List<ParametrosRecaudacion>		p_Parametros;
		public String 					p_Simula = "N";
		public String                   p_Canal = "";
		public String					p_SoloAporteCuentaInversion;
		public String					p_SoloAporteSAAPV;
		public String 					p_PermiteOperarProspectos;
		public List<Long>				p_IdsDetalleTraspasoReapertura;
		public String					p_DistribucionOrigen;
		public String 					p_Origen;
		public ParametrosEntradaServicio(){
			p_SoloAporteCuentaInversion = "N";
			p_SoloAporteSAAPV = "N";
			p_Parametros = new ArrayList<ParametrosRecaudacion>();
			p_PermiteOperarProspectos = "N";
			p_IdsDetalleTraspasoReapertura = new ArrayList<Long>();
			p_DistribucionOrigen = "N";
			p_Origen= "OPT";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			
	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<SalidaSolicitudFFMM> P_OUT_SOLICITUDES_FFMM; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES 		  = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_SOLICITUDES_FFMM 	= new ArrayList<SalidaSolicitudFFMM>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
		public  String		FolioContratoSAAPV;
		public  String		Canal;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaSolicitudFFMM{
		public Long IdSolicitudAporteRescate;
		public Long FolioContrato;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudesFFMMAsociadas extends BcsPojo {
		public Long IdRecaudacion;
		public Long IdRecaudacionCliente;
		public Long Cantidad;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Metodos Basicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		
		Long init = System.currentTimeMillis();
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		
		
		logger.info("INICIO:: ProcesarDistribucion");
		
		
		Long contadorFFMM = 1L; 
		Long contadorOrden = 1L;
		Long contadorF=0L;
		Long contadorFFMMSAR=0L;

		Long time = System.currentTimeMillis();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String AbreRegistrosTesoreria = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "APV.AbreRegistrosTesoreria");
		
		Long IdPortafolioCorredora = 0L;
		java.sql.Timestamp FechaHora = new java.sql.Timestamp(time);
		HashMap<String,Object> ParamsRecaudacion = new HashMap<String,Object>();
		BigDecimal sumaMontosDistribuido = BigDecimal.valueOf(0);
		BigDecimal sumaMontosRezagado = BigDecimal.valueOf(0);
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", 1L);
		
		ArrayList<Long> idsRecaudacion = new ArrayList<Long>();
		if (in.p_Parametros.get(0).IdRecaudacion == 0){
			ParamsRecaudacion.put("IdRecaudacion",0);
			ParamsRecaudacion.put("IdsRecaudacion",0);
		}
		else {
			for(ParametrosRecaudacion fila : in.p_Parametros){
				idsRecaudacion.add(fila.IdRecaudacion);	
			}
			ParamsRecaudacion.put("IdRecaudacion",1);
			ParamsRecaudacion.put("IdsRecaudacion", idsRecaudacion);
		}
		
		if ("DIS".equalsIgnoreCase(in.p_Opcion)) {
			bloqueoDistribucion(con, ParamsRecaudacion);
		}
		
		List<Recaudacion> _Recaudacion = sql.query(con, SQL_SELECT_Recaudacion, ParamsRecaudacion, Recaudacion.class);
		
		List<Long> listaIdRecaudacionCliente = new ArrayList<>();
		List<SolicitudesFFMMAsociadas> listaSolicitudesAsociadas = new ArrayList<>();
		
		if(!_Recaudacion.isEmpty() && "DIS".equalsIgnoreCase(in.p_Opcion)) {
			listaIdRecaudacionCliente = _Recaudacion.stream().map(recaudacion ->{
				Long idRecaudacionCliente = recaudacion.IdRecaudacionCliente != null ? recaudacion.IdRecaudacionCliente : 0L;
				return idRecaudacionCliente;
			}).distinct().collect(Collectors.toList());
			
			if(!listaIdRecaudacionCliente.isEmpty()) {
				listaSolicitudesAsociadas = buscarSolicitudesFFMMAsociadas(con, listaIdRecaudacionCliente);
			}
			
			if(!listaSolicitudesAsociadas.isEmpty()) {
				validarSolicitudesFFMMAsociadas(con, in.p_IdEmpresa, _Recaudacion, listaSolicitudesAsociadas, salida);
			}
		}
			
		//Arrays y listas de entrada para las demas clases
		List<CrearCargoAbono.ObjCargoAbonoAGenerar> paramsCargoAbono = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
		List<CrearOrdenes.ObjOrdenesEntrada> ObjOrden = new ArrayList<CrearOrdenes.ObjOrdenesEntrada>();
		List<CalcularMontoYCobrosOrden.Orden_Entrada> ObjCalculoMontoOrden = new ArrayList<CalcularMontoYCobrosOrden.Orden_Entrada>();
		List<OrigenRecaudacion> recadacionATraspasar = new ArrayList<OrigenRecaudacion>();
			
		//Listas validacion SAAPV
		ValidaContratoSAAPV ValSAAPV = new ValidaContratoSAAPV();
		ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada paramsValSAAPV_Entrada = new ValidaContratoSAAPV.ValidaContratoSAAPV_Entrada();
		List<ValidaContratoSAAPV.RecaudacionCliente> paramsValSAAPVRecaudacionCliente = new ArrayList<ValidaContratoSAAPV.RecaudacionCliente>();
		ValidaContratoSAAPV.RecaudacionCliente ValSAAPVRecaudacionCliente;
		HashMap<String,String> valDic = new HashMap<String,String>();
		
		if(_Recaudacion.size() == 0){
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRARON_REGISTROS_PARA_DISTRIBUIR",valDic,con,in.p_IdEmpresa));
			return salida;
		}
		
		
		//-----------------------------------------------------------------
		// Ejecuta Validaciones SAAPV
		//-----------------------------------------------------------------
		for(Recaudacion fila : _Recaudacion){
			ValSAAPVRecaudacionCliente = new ValidaContratoSAAPV.RecaudacionCliente();
			ValSAAPVRecaudacionCliente.IdRecaudacion = fila.FolioRecaudacion;
			ValSAAPVRecaudacionCliente.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
			ValSAAPVRecaudacionCliente.IdRecaudacionCliente = fila.IdRecaudacionCliente;
			ValSAAPVRecaudacionCliente.IdEstadoRecaudacionCliente = fila.CodigoEstado;
			ValSAAPVRecaudacionCliente.Monto = fila.RecTotal;
			paramsValSAAPVRecaudacionCliente.add(ValSAAPVRecaudacionCliente);
		}

		List<ContratosSAAPV> contrato = new ArrayList<ContratosSAAPV>();
		
		paramsValSAAPV_Entrada.p_IdEmpresa = in.p_IdEmpresa;
		paramsValSAAPV_Entrada.p_IdUsuario = in.p_IdUsuario;
		paramsValSAAPV_Entrada.p_IdModulo = in.p_IdModulo;
		paramsValSAAPV_Entrada.p_Opcion = in.p_Opcion.equals("RECTRA") ? "RECTRA" : "V";
		paramsValSAAPV_Entrada.p_ContratosSAAPV = contrato;
		paramsValSAAPV_Entrada.p_RecaudacionCliente = paramsValSAAPVRecaudacionCliente;
		paramsValSAAPV_Entrada.p_IdsDetalleTraspasoReapertura = in.p_IdsDetalleTraspasoReapertura;

		logger.info("Inicio ValidaContratoSAAPV");
	    ValidaContratoSAAPV.ValidaContratoSAAPV_Salida OutValSAAPV = ValSAAPV.logica(con, paramsValSAAPV_Entrada); 
	    logger.info("Fin ValidaContratoSAAPV");
	    
		for(DetalleValidacionContratoSAAPV fila :OutValSAAPV.P_IDSRECAUDACION){
			if (fila.ValidacionOK.compareTo("NOK") ==0 && !fila.IdEstadoRecaudacionCliente.equals("REZ") && fila.Monto.compareTo(BigDecimal.ZERO) > 0){

				sumaMontosRezagado = sumaMontosRezagado.add(fila.Monto);
				OrigenRecaudacion oriRec = new OrigenRecaudacion();
				oriRec.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				recadacionATraspasar.add(oriRec);

				salida.P_OUT_MENSAJES.add(new MensajeSalida(fila.IdRecaudacionCliente, MensajeSalida.MENSAJE_ADVERTENCIA, "CAMBIO_DE_ESTADO_A_REZAGO","Se cambia estado a rezago"));
			
			}
		}

		if(sumaMontosRezagado.compareTo(BigDecimal.ZERO) > 0){
		    TraspasoCuentasBancarias procesoTrasCtaBca = new TraspasoCuentasBancarias();
			TraspasoCuentasBancarias.ParametrosEntradaServicio paramsTrasCtaBca_Entrada = new TraspasoCuentasBancarias.ParametrosEntradaServicio();
			paramsTrasCtaBca_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsTrasCtaBca_Entrada.p_IdUsuario = in.p_IdUsuario;
		    paramsTrasCtaBca_Entrada.p_IdModulo = in.p_IdModulo;
		    paramsTrasCtaBca_Entrada.p_Opcion = "REZ";
		    paramsTrasCtaBca_Entrada.p_Fecha = FechaSistema.toString();
		    paramsTrasCtaBca_Entrada.p_Monto = sumaMontosRezagado;		
		    paramsTrasCtaBca_Entrada.p_OrigenRecaudacion = recadacionATraspasar;
		    
		    paramsTrasCtaBca_Entrada.p_Simula=in.p_Simula;

		    logger.info("Inicio TraspasoCuentasBancarias Rezago");
			TraspasoCuentasBancarias.ParametrosSalidaServicio OutTrasCtaBca_Entrada = procesoTrasCtaBca.logica(con, paramsTrasCtaBca_Entrada);
			logger.info("Fin TraspasoCuentasBancarias Rezago");
			
			salida.P_OUT_ERRORESCONTADOR += OutTrasCtaBca_Entrada.P_OUT_ERRORESCONTADOR;
			
			for(MensajeSalida filaMensaje : OutTrasCtaBca_Entrada.P_OUT_MENSAJES){

			
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, filaMensaje.tipo, filaMensaje.codigo,filaMensaje.descripcion));
				
				
			}
		}


		List<ParamsInsertEstadoTransicion> lstEstadoTransicion1 = new ArrayList<ParamsInsertEstadoTransicion>();
		List<DetalleCliente_CambiaEstado> lstEstadoCliente1    = new ArrayList<DetalleCliente_CambiaEstado>();	
		ParamsInsertEstadoTransicion objEstadoTransicion1;
		DetalleCliente_CambiaEstado objEstadoCliente1;
		
		
		
		for(DetalleValidacionContratoSAAPV fila :OutValSAAPV.P_IDSRECAUDACION){
			if (fila.ValidacionOK.compareTo("NOK") ==0){

				//Estado Transicion				
				objEstadoTransicion1 = new ParamsInsertEstadoTransicion();
				objEstadoTransicion1.Tipo ="Recaudacion";
				objEstadoTransicion1.IdRegistro = fila.IdRecaudacionCliente;
				objEstadoTransicion1.FechaTransicion = FechaHora;
				objEstadoTransicion1.EstadoAnterior = fila.IdEstadoRecaudacionCliente;
				objEstadoTransicion1.EstadoNuevo = "REZ";
				objEstadoTransicion1.Observacion = fila.DetalleValidacion;
				objEstadoTransicion1.Usuario = in.p_IdUsuario;
				lstEstadoTransicion1.add(objEstadoTransicion1);
				
				//Mesajes de salida de validaciones no cumplidas 
				salida.P_OUT_MENSAJES.add(new MensajeSalida(fila.IdRecaudacionCliente, MensajeSalida.MENSAJE_ADVERTENCIA, "DETALLE_VALIDACION_SAAPV",fila.DetalleValidacion));
				
				//Estado detalle cliente				
				objEstadoCliente1 = new DetalleCliente_CambiaEstado();				
				objEstadoCliente1.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				objEstadoCliente1.IdEstado = "REZ";
				objEstadoCliente1.IdEmpresa = in.p_IdEmpresa;
				lstEstadoCliente1.add(objEstadoCliente1);

				for(int i = _Recaudacion.size()-1; i >= 0; i --){
					if(_Recaudacion.get(i).IdRecaudacionCliente == fila.IdRecaudacionCliente){
						_Recaudacion.remove(i);
					}
				}
			}
			else {
				
				for(int i = _Recaudacion.size()-1; i >= 0; i --){
					if (_Recaudacion.get(i).IdRecaudacionCliente == fila.IdRecaudacionCliente){
						_Recaudacion.get(i).IdPortafolio = fila.IdPortafolio;
						_Recaudacion.get(i).IdContrato = fila.IdContrato;
						_Recaudacion.get(i).Pago = fila.Pago;
						_Recaudacion.get(i).IdRegimen = fila.IdRegimen;
					}
				}
			}
		} // for(DetalleValidacionContratoSAAPV fila :OutValSAAPV.P_IDSRECAUDACION)
		
		
	
		if (lstEstadoTransicion1.size() > 0)
		{
			sql.batch(con,SQL_INSERT_EstadoTransicion,lstEstadoTransicion1,ParamsInsertEstadoTransicion.class);
		}
		
		if (lstEstadoCliente1.size() > 0)
		{		
			sql.batch(con,SQL_CAMBIA_ESTADO_Cliente,lstEstadoCliente1,DetalleCliente_CambiaEstado.class);
		}
		
		logger.info("Cantidad de Registros a Distribuir : "+_Recaudacion.size());

		HashMap<String,Object> ParamsSelectPortafolio = new HashMap<String,Object>();
		ParamsSelectPortafolio.put("IdEmpresa", in.p_IdEmpresa);
		List<IdPortafolioDefecto> _IdPortafolioDefecto = sql.query(con, SQL_PortafolioEmpresa, ParamsSelectPortafolio, IdPortafolioDefecto.class);

		CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
		EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
		

		
		Long contadorCtaInv = 1L;
		IdPortafolioCorredora = _IdPortafolioDefecto.get(0).IdPortafolioDefecto;
		
		CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvAbono;			 //Abono
		CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvCargo ;			 //Cargo
		

		List<Instrumento> lstInstrumentoPrecioBolsa = new ArrayList<Instrumento>();
		Instrumento objInstrumentoPrecioBolsa;
		
		for(Recaudacion fila : _Recaudacion){
			HashMap<String,Object> ParamsConsultaDistribucionContrato = new HashMap<String,Object>();
			Long idContrato = fila.IdContrato == 0?  in.p_Parametros.get(0).IdContratoSAAPV:fila.IdContrato;
			ParamsConsultaDistribucionContrato.put("IdContrato", idContrato);
			List<ContratoConsultado> _ContratoSAAPV = sql.query(con, SQL_SELECT_CONTRATO_SAAPV, ParamsConsultaDistribucionContrato, ContratoConsultado.class);
			ContratoConsultado contratoSAAPV = _ContratoSAAPV.stream().findFirst().orElse(new ContratoConsultado());
			
			// si viene de "DistribucionOrigen" S traerá fondos restringidos para aporte
			ParamsConsultaDistribucionContrato.put("DistribucionOrigen", in.p_DistribucionOrigen);
			
			List<DistribucionContrato> _DistribucionContrato  = sql.query(con, SQL_SELECT_DistribucionContrato_ProcesarDistribucionSinFondoRestringido, ParamsConsultaDistribucionContrato, DistribucionContrato.class);
		
			if(_DistribucionContrato.size() == 0 && "N".equals(in.p_SoloAporteCuentaInversion )){
				HashMap<String,Object> ParamsConsultaDistribucionDefecto = new HashMap<String,Object>();
				_DistribucionContrato = sql.query(con, SQL_SELECT_DistribucionDefecto, ParamsConsultaDistribucionDefecto, DistribucionContrato.class);
			}
			
			fila.Distribucion = _DistribucionContrato;
			
			CtaInvAbono = new CrearCargoAbono.ObjCargoAbonoAGenerar();			
			CtaInvCargo = new CrearCargoAbono.ObjCargoAbonoAGenerar();			
			
			//Sumatoria de montos para posteriores usos
			sumaMontosDistribuido = sumaMontosDistribuido.add(fila.RecTotal);
		
			//se crea cargo o abono para mover la cuenta inversion
			if( "N".equals(in.p_SoloAporteSAAPV) ) {
				
				CtaInvAbono.NumeroCargoAbono = contadorCtaInv;
				CtaInvAbono.IdPortafolio =fila.IdPortafolio == 0? in.p_Parametros.get(0).IdPortafolio:fila.IdPortafolio;
				CtaInvAbono.IdMoneda = "CLP";
				CtaInvAbono.IdTipoMovimiento = "MOVABN";
				CtaInvAbono.IdConcepto = fila.Distribucion.size() > 0 ? "ABOAPV" : "ACIAPV";
				CtaInvAbono.Descripcion = "APV";
				CtaInvAbono.IdCondicionDeLiquidacion = "T0";
				CtaInvAbono.FechaCondicionDeLiquidacion = FechaSistema.toString();
				CtaInvAbono.Monto = fila.RecTotal;
				CtaInvAbono.IdEjecutivo = 0L;
				CtaInvAbono.IdSucursal = 0L;
				CtaInvAbono.IdTipoProducto = "";
				CtaInvAbono.IdTipoOperacion = "";
				CtaInvAbono.IdOperacion = fila.IdRecaudacionCliente;
				CtaInvAbono.FechaOperacion = "0001-01-01";
				CtaInvAbono.FechaLiquidacionOperacion = "0001-01-01";
				CtaInvAbono.FechaVencimientoOperacion = null;
				CtaInvAbono.IdMonedaOperacion = "";
				CtaInvAbono.MontoOperacion = null;
				CtaInvAbono.TipoCambioOperacion = BigDecimal.valueOf(0L);
				CtaInvAbono.IdContraparte = null;
				CtaInvAbono.IdTipoComprobante = "APV";
				CtaInvAbono.IdComprobante = contratoSAAPV.IdContratoSAAPV;
				CtaInvAbono.FolioComprobante = contratoSAAPV.FolioContratoSAAPV;
				CtaInvAbono.IdComprobanteDetalle = null;
				CtaInvAbono.IdComprobanteFormaDePago = null;
				CtaInvAbono.IdTipoMovimientoAsociado = null;
				CtaInvAbono.IdMovimientoAsociado = null;
				CtaInvAbono.FechaComprobante = null;
				CtaInvAbono.IdPersonaPagoCobro = null;
				CtaInvAbono.IdInstrumento = null;
				CtaInvAbono.IdTipoInstrumento = null;
				CtaInvAbono.Nemo = null;
				CtaInvAbono.EsIndicadorBilateral = "N";
				CtaInvAbono.EsCargoAbonoPorTraspaso = "N";
				CtaInvAbono.IdPortafolioTraspaso = null;
				CtaInvAbono.GeneroContraparteEnTesoreria = "N";
				CtaInvAbono.CertificadoAutomatico = "S";
				paramsCargoAbono.add(CtaInvAbono);
				contadorCtaInv++;
				
				CtaInvCargo.NumeroCargoAbono = contadorCtaInv;
				CtaInvCargo.IdPortafolio = IdPortafolioCorredora;
				CtaInvCargo.IdMoneda = "CLP";
				CtaInvCargo.IdTipoMovimiento = "MOVCAR";
				CtaInvCargo.IdConcepto = "CARAPV";
				CtaInvCargo.Descripcion = "APV";
				CtaInvCargo.IdCondicionDeLiquidacion = "T0";
				CtaInvCargo.FechaCondicionDeLiquidacion = FechaSistema.toString();
				CtaInvCargo.Monto = fila.RecTotal;
				CtaInvCargo.IdEjecutivo = 0L;
				CtaInvCargo.IdSucursal = 0L;
				CtaInvCargo.IdTipoProducto = "";
				CtaInvCargo.IdTipoOperacion = "";
				CtaInvCargo.IdOperacion = fila.IdRecaudacionCliente;
				CtaInvCargo.FechaOperacion = "0001-01-01";
				CtaInvCargo.FechaLiquidacionOperacion = "0001-01-01";
				CtaInvCargo.FechaVencimientoOperacion = null;
				CtaInvCargo.IdMonedaOperacion = "";
				CtaInvCargo.MontoOperacion = null;
				CtaInvCargo.TipoCambioOperacion = BigDecimal.valueOf(0L);
				CtaInvCargo.IdContraparte = null;
				CtaInvCargo.IdTipoComprobante = null;
				CtaInvCargo.IdComprobante = null;
				CtaInvCargo.FolioComprobante = null;
				CtaInvCargo.IdComprobanteDetalle = null;
				CtaInvCargo.IdComprobanteFormaDePago = null;
				CtaInvCargo.IdTipoMovimientoAsociado = null;
				CtaInvCargo.IdMovimientoAsociado = null;
				CtaInvCargo.FechaComprobante = null;
				CtaInvCargo.IdPersonaPagoCobro = null;
				CtaInvCargo.IdInstrumento = null;
				CtaInvCargo.IdTipoInstrumento = null;
				CtaInvCargo.Nemo = null;
				CtaInvCargo.EsIndicadorBilateral = "N";
				CtaInvCargo.EsCargoAbonoPorTraspaso = "N";
				CtaInvCargo.IdPortafolioTraspaso = null;
				CtaInvCargo.GeneroContraparteEnTesoreria = "N";
				CtaInvCargo.CertificadoAutomatico = "S";
				paramsCargoAbono.add(CtaInvCargo);
				contadorCtaInv++;
				
			}
			
			
			HashMap<Long,BigDecimal> hashMontoMonedaPago = new HashMap<Long,BigDecimal>();
			
			//contadorFFMM = 1L; 

			BigDecimal montoTotal = fila.RecTotal;
			BigDecimal suma = BigDecimal.ZERO;
			for(DistribucionContrato filaDis : _DistribucionContrato){
				//distribuir en hash
				BigDecimal montoDistribucion = montoTotal.multiply(filaDis.Porcentaje).divide(new BigDecimal("100"),20,RoundingMode.HALF_UP).setScale(0,RoundingMode.DOWN);
				montoDistribucion  = MonedaFormato.formatear(con, montoDistribucion, MonedaFormato.MONTO, filaDis.IdMoneda).valor;
				hashMontoMonedaPago.put(contadorFFMM,montoDistribucion);
				suma = suma.add(montoDistribucion);
				contadorFFMM++; 
			}
			
			if(_DistribucionContrato.size() > 0) {
				//si nos falto repartir unos pesos
				if (montoTotal.compareTo(suma) == 1) {
					BigDecimal diferencia = montoTotal.subtract(suma);
					BigDecimal ultimo = hashMontoMonedaPago.get(contadorFFMM-1);
					hashMontoMonedaPago.put(contadorFFMM-1,ultimo.add(diferencia));	
				}
				
				if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size()==0){
					contadorFFMM = 1L;
				}else {
					contadorFFMM = contadorFFMMSAR;
				}
			}
			
			for(DistribucionContrato filaDistribucionContrato : _DistribucionContrato){				
				
			    if (filaDistribucionContrato.IdTipoInstrumento.equals("FFM")){
					CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = new CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate();
					s.IdInterno = contadorFFMM;
					s.IdCliente = fila.IdCliente;
					s.IdPortafolio = fila.IdPortafolio == 0? in.p_Parametros.get(0).IdPortafolio:fila.IdPortafolio;
					s.TipoSolicitud = _c.TipoSolicitudAporte;
					s.IdSerieFondo = filaDistribucionContrato.Nemotecnico;
					s.CalculoPorMontoCuota = _c.CalculoPorMonto;
					s.EsPorMontoLiquido = _c.NO;
					s.Monto = hashMontoMonedaPago.get(contadorFFMM);
					s.NumeroCuotas = BigDecimal.valueOf(0);
					s.IdMoneda = filaDistribucionContrato.IdMoneda;
					s.IdMonedaPago = idMonedaSistema;
					s.IdMedioDePago = _c.CuentaInversion;
					s.IdPreferenciaPagoPortafolio = 0L;
					s.Visado = _c.NO;
					s.SolicitudBatch = _c.NO;
					s.OrigenOperacion = _c.OrigenManual;
					s.FechaOperacion = FechaSistema.toString();
					s.IdRegimenTributario = fila.IdRegimen == null ? fila.IdRegimen : fila.IdRegimen.toString();
					s.Glosa = fila.IdRecaudacionCliente.toString();
					s.IndicadorMLT = _c.NO;
					s.IdSerieFondoDestino = "";
					s.IdEmisorDestino = "";
					s.IdAutorizadoOrigen = 0L;
					s.Porcentaje = BigDecimal.ZERO;
					s.FolioContrato = secuencias.get(0);
					s.IngresoAutomatico = ("OPT".equalsIgnoreCase(in.p_Canal) && "WEB".equalsIgnoreCase(in.p_Origen)) ? _c.NO : esAporteSAAPVOrigenWebService(in.p_Canal) ? _c.NO : "UNI".equalsIgnoreCase(fila.Pago) ? _c.NO : _c.SI;
					s.TipoContratoOperacion = _c.SAAPV;
					s.IdOrigenRecaCliente = fila.IdRecaudacionCliente;
					
					boolean esDistribucionAPVCorredores = esAporteSAAPVOrigenWebService(in.p_Canal) == false && "DIS".equals(in.p_Opcion) && _c.APV.equals(in.p_IdModulo);
					if (esDistribucionAPVCorredores) {
						s.IgnorarMontoMinimo = _c.SI;
					}
					
					if(("APV").equalsIgnoreCase(in.p_IdModulo) && ("RECTRA").equalsIgnoreCase(in.p_Opcion) )
						s.EsTraspaso = _c.SI;
					if(_ContratoSAAPV.size() > 0) {
						s.Canal = _ContratoSAAPV.get(0).Canal;
						s.FolioContratoOperacion = _ContratoSAAPV.get(0).FolioContratoSAAPV;
					}else {
						s.Canal = "";
						s.FolioContratoOperacion = "";
					}
					
					if(_c.Dolar.equals(s.IdMoneda)){

						List<InFormaPagoSpot> p_FormaPagoSpot = new ArrayList<>();
						p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
						contadorF = contadorF+1;
						
						InFormaPagoSpot FormaPagoSpot = new InFormaPagoSpot();
						FormaPagoSpot.IdInternoPagoOperacion = contadorF;
						FormaPagoSpot.IdComprobantePagoOperacion= 0L;
						FormaPagoSpot.IdTipoMovimiento = "MOVEGR";
						FormaPagoSpot.IdFormaDePago    = "VENSPT_CIN_USD_0";
						FormaPagoSpot.IdOperacion      = 	contadorFFMM;
						FormaPagoSpot.RecibePagaPortafolio = "P";
						FormaPagoSpot.IdMoneda         =  s.IdMoneda;
						FormaPagoSpot.IdMonedaLiquidacion  =  s.IdMoneda;
						FormaPagoSpot.MontoFormaDePago = BigDecimal.ZERO;  // Depues(cuando se llame a CalcularMontoNumeroCuota) se repasa con el monto correcto
						FormaPagoSpot.AfectarTesoreria =  "N";
						p_FormaPagoSpot.add(FormaPagoSpot);
						
						contadorF = contadorF+1;
						
						InFormaPagoSpot FormaPagoSpot2 = new InFormaPagoSpot();
						FormaPagoSpot2.IdInternoPagoOperacion = contadorF;
						FormaPagoSpot2.IdComprobantePagoOperacion= 0L;
						FormaPagoSpot2.IdTipoMovimiento = "MOVING";
						FormaPagoSpot2.IdFormaDePago    = "COMSPT_CIN_CLP_0";
						FormaPagoSpot2.IdOperacion      = 	contadorFFMM;
						FormaPagoSpot2.RecibePagaPortafolio = "R";
						FormaPagoSpot2.IdMoneda         = "CLP";
						FormaPagoSpot2.IdMonedaLiquidacion =  "CLP";
						FormaPagoSpot2.MontoFormaDePago = s.Monto;
						FormaPagoSpot2.AfectarTesoreria =  "N";
						p_FormaPagoSpot.add(FormaPagoSpot2);
						
						s.ForzarSpot=_c.SI;
					
						
						entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
						entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot.addAll(p_FormaPagoSpot);

					}else {
						entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
						

					}
					
					contadorFFMM = contadorFFMM+1; 
					contadorFFMMSAR = contadorFFMM;
				}
			    else
			    {

					CrearOrdenes.ObjOrdenesEntrada Orden = new CrearOrdenes.ObjOrdenesEntrada();
					
									
					objInstrumentoPrecioBolsa = new Instrumento();					
					objInstrumentoPrecioBolsa.IdEmpresa = in.p_IdEmpresa;
					objInstrumentoPrecioBolsa.IdInstrumento = filaDistribucionContrato.IdInstrumento;
					objInstrumentoPrecioBolsa.IdBolsa = "BCS";
					objInstrumentoPrecioBolsa.Fecha = FechaSistema;
					objInstrumentoPrecioBolsa.IdCondicionDeLiquidacion = "T0";
					
					if (!lstInstrumentoPrecioBolsa.contains(objInstrumentoPrecioBolsa))
					{
						lstInstrumentoPrecioBolsa.add(objInstrumentoPrecioBolsa);
					}
					
					
				

					CalcularMontoYCobrosOrden.Orden_Entrada CalculoMontoOrdenes = new CalcularMontoYCobrosOrden.Orden_Entrada();
					CalculoMontoOrdenes.IdInterno = contadorOrden;
					CalculoMontoOrdenes.CantidadOrden=BigDecimal.ZERO;
					CalculoMontoOrdenes.EsFungible="N";
					CalculoMontoOrdenes.IdBolsa="BCS";
					CalculoMontoOrdenes.IdCorredorCorresponsal=null;
					CalculoMontoOrdenes.IdInstrumento=filaDistribucionContrato.IdInstrumento;
					CalculoMontoOrdenes.IdInstrumentoSubyacente=0L;
					CalculoMontoOrdenes.IdMonedaLiquidacionOrden=filaDistribucionContrato.IdMoneda;
					CalculoMontoOrdenes.IdPortafolio=fila.IdPortafolio;
					CalculoMontoOrdenes.IdTipoProducto="INAC";
					CalculoMontoOrdenes.IdTipoOperacion="COMACN";
					CalculoMontoOrdenes.IdTipoCanalIngreso="AGE";
					CalculoMontoOrdenes.FechaOperacion=FechaSistema;
					CalculoMontoOrdenes.MontoOrden=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaSimultanea=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaRFIF=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaVentaCorta=BigDecimal.ZERO;
					CalculoMontoOrdenes.TipoCambio=BigDecimal.ZERO;
					CalculoMontoOrdenes.ValorRescateOrden=BigDecimal.ZERO;
					CalculoMontoOrdenes.PlazoSimultanea=0L;
					CalculoMontoOrdenes.PlazoVentaCorta=0L;
					CalculoMontoOrdenes.PlazoIF=0L;
					CalculoMontoOrdenes.EsAPlazo="N";
					CalculoMontoOrdenes.MontoTotal=(fila.RecTotal.multiply(filaDistribucionContrato.Porcentaje).divide(BigDecimal.valueOf(100)));
					CalculoMontoOrdenes.EsPorMonto="S";
					CalculoMontoOrdenes.TasaReporte=BigDecimal.ZERO;
					CalculoMontoOrdenes.TasaReportado=BigDecimal.ZERO;
					CalculoMontoOrdenes.PlazoReporte=0L;
					CalculoMontoOrdenes.BaseTasa=0L;
					ObjCalculoMontoOrden.add(CalculoMontoOrdenes);
					
					Orden.IdInterno = contadorOrden;
					Orden.FechaRecepcion = "0001-01-01 00:00:00";
					Orden.FechaVigencia = "2018-05-09";
					Orden.FechaEmision = "0001-01-01";
					Orden.FechaOperacion = "0001-01-01";
					Orden.IdCondicionDeLiquidacion = "T0"; 
					Orden.IdTipoOperacion = "COMACN";
					Orden.IdTipoVigencia = "SH";
					Orden.Glosa = fila.IdRecaudacionCliente.toString();
					Orden.Cantidad = BigDecimal.valueOf(0L);
					Orden.MontoNeto = (fila.RecTotal.multiply(filaDistribucionContrato.Porcentaje).divide(BigDecimal.valueOf(100)));
					Orden.ValorRescate = BigDecimal.valueOf(0L);
					Orden.IdBoveda = "DCV";
					Orden.IdFormaCustodia = "NOM";
					Orden.IdPorcentajeVisibilidad = 1L;
					Orden.PrecioLimite = BigDecimal.valueOf(10000L);
					Orden.EsFacturaInmediata = "N";
					Orden.IdTipoPrecio = "M";
					Orden.IdPortafolio = fila.IdPortafolio;
					Orden.IdTipoCanalIngreso = "AGE";
					Orden.IdTipoReajuste = "";
					Orden.IdEmisor = filaDistribucionContrato.Nemotecnico;
					Orden.IdBolsa = "BCS";
					Orden.EspecificaBolsa = "S";
					Orden.IdMonedaLiquidacionOrden = "CLP";
					Orden.TipoCambio = null;
					Orden.IdInstrumento = filaDistribucionContrato.IdInstrumento;
					Orden.IdInstrumentoSubyacente = 0L;
					Orden.IdCondicionDeLiquidacionPlazo = "";
					Orden.TasaVentaCorta = BigDecimal.valueOf(0);
					Orden.PlazoVentaCorta = 0L;
					Orden.EsVentaCorta = "N";
					Orden.TasaSimultanea = BigDecimal.valueOf(0);
					Orden.PlazoSimultanea = 0L;
					Orden.PlazoIF = 0L;
					Orden.TasaRFIF = BigDecimal.valueOf(0);
					Orden.EsOD = "N";
					Orden.EsFungible = "N";
					Orden.IdTipoLibroOrden = "NORMAL";
					Orden.IdLibroOrdenes = null;
					Orden.IdCuentaCustodioPortafolio = null;
					Orden.EsPorMonto = "S";
					Orden.IdCorredorCorresponsal = null;
					Orden.IdPersonaAutorizadoDarOrdenes = null;
					Orden.IdTipoCanalDistribucion = "MAN";
					Orden.EsOrdenEspecial = "N";
					Orden.PagoCobroCuentaInversion = "S";
					Orden.IdPreferenciaPagoPortafolio = fila.IdPortafolio;
					Orden.BaseTasa = 0L;
					Orden.TipoInteres = "";
					Orden.IdMecanismoCaptacion = "OM";
				
					ObjOrden.add(Orden);

					contadorOrden++;
					
				}
			}
		}


		//Se repasa precio de orden
		if (lstInstrumentoPrecioBolsa.size() > 0)
		{
		
			ConsultarInstrumentoPrecioBolsa consultarInstrumentoPrecioBolsa = new ConsultarInstrumentoPrecioBolsa();
			
			ConsultarInstrumentoPrecioBolsa_Entrada entradaConsultarInstrumentoPrecioBolsa = new ConsultarInstrumentoPrecioBolsa_Entrada();
			entradaConsultarInstrumentoPrecioBolsa.p_IdEmpresa 		= in.p_IdEmpresa;
			entradaConsultarInstrumentoPrecioBolsa.p_IdUsuario 		= in.p_IdUsuario;
			entradaConsultarInstrumentoPrecioBolsa.p_Instrumentos 	= lstInstrumentoPrecioBolsa;
			
			logger.info("Inicio consultarInstrumentoPrecioBolsa");
			ConsultarInstrumentoPrecioBolsa_Salida resultadoPrecioInstrumento = consultarInstrumentoPrecioBolsa.logica(con, entradaConsultarInstrumentoPrecioBolsa );
			logger.info("Fin consultarInstrumentoPrecioBolsa");
			
			if (resultadoPrecioInstrumento.P_OUT_INSTRUMENTO.size() > 0)
			{
				HashMap<Long,Object> hashResultadoPrecioInstrumento = Utils.List2Hash(resultadoPrecioInstrumento.P_OUT_INSTRUMENTO, "IdInstrumento");   
				
				for (CalcularMontoYCobrosOrden.Orden_Entrada valOrden : ObjCalculoMontoOrden)
				{
					DatosInstrumentosPrecioBolsa_Salida infoPrecioInstrumento = (DatosInstrumentosPrecioBolsa_Salida) hashResultadoPrecioInstrumento.get(valOrden.IdInstrumento);
					valOrden.PrecioOrden = infoPrecioInstrumento.PrecioCierre;
				}
				
			}

		}
	

		
		
		
		
		
		//---------------------------------------------------------
		// CUENTA INVERSION
		//---------------------------------------------------------	  
		
		if (paramsCargoAbono.size() > 0)
		{
			CrearCargoAbono.CrearCargoAbonoEntrada paramsCuentaInversionCargoAbono_Entrada = new CrearCargoAbono.CrearCargoAbonoEntrada();

			paramsCuentaInversionCargoAbono_Entrada.p_IdEmpresa 			= in.p_IdEmpresa;
			paramsCuentaInversionCargoAbono_Entrada.p_IdUsuario 			= in.p_IdUsuario;
			paramsCuentaInversionCargoAbono_Entrada.p_IdModulo 				= in.p_IdModulo;
			paramsCuentaInversionCargoAbono_Entrada.p_simularOperacion 		= "N";
			paramsCuentaInversionCargoAbono_Entrada.p_UsaReglasNegocio 		= "S";
			paramsCuentaInversionCargoAbono_Entrada.p_FechaSistema 			= FechaSistema.toString();
			paramsCuentaInversionCargoAbono_Entrada.p_CargoAbonoAGenerar 	= paramsCargoAbono;
			paramsCuentaInversionCargoAbono_Entrada.p_EsIngresoManual 		= "N";
			
			CrearCargoAbono ProcesoCrearCargoAbono = new CrearCargoAbono();
			
			logger.info("Inicio Cuenta Inversion");
			CrearCargoAbono.CrearCargoAbonoSalida OutCuentaInversion = ProcesoCrearCargoAbono.logica(con, paramsCuentaInversionCargoAbono_Entrada);
			List<MensajeSalida> mensajesErroresCuentaInversion = OutCuentaInversion.P_OUT_MENSAJES.parallelStream().filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).collect(Collectors.toList());
			salida.P_OUT_MENSAJES.addAll(mensajesErroresCuentaInversion);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			logger.info("Fin Cuenta Inversion");
		}
		

		

		//---------------------------------------------------------
		// ORDENES
		//---------------------------------------------------------	
		if (ObjOrden.size()>0){

			CalcularMontoYCobrosOrden procesoCalcularMonto = new CalcularMontoYCobrosOrden();
			CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada paramsCalcularMonto_Entrada = new CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada();
			paramsCalcularMonto_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsCalcularMonto_Entrada.p_IdUsuario = in.p_IdUsuario;
			paramsCalcularMonto_Entrada.p_IdModulo = in.p_IdModulo;    
			paramsCalcularMonto_Entrada.p_Ordenes = ObjCalculoMontoOrden;

			CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida OutCalcularmonto = procesoCalcularMonto.logica(con, paramsCalcularMonto_Entrada); 

			for(int i = OutCalcularmonto.p_out_Ordenes.size()-1; i >= 0; i --){
				for(int j = ObjOrden.size()-1; j >= 0; j --){
					if(OutCalcularmonto.p_out_Ordenes.get(i).IdInterno.toString().equals(ObjOrden.get(j).IdInterno.toString())){
						ObjOrden.get(j).Cantidad = OutCalcularmonto.p_out_Ordenes.get(i).CantidadOperacion;
					}
					
				}
			}
				
			CrearOrdenes.CrearOrdenesEntrada paramsOrdenesEntrada = new CrearOrdenes.CrearOrdenesEntrada();
		    
			paramsOrdenesEntrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsOrdenesEntrada.p_IdUsuario = in.p_IdUsuario;
			paramsOrdenesEntrada.p_IdModulo = "APV";
			paramsOrdenesEntrada.p_simularOperacion = "N";
			paramsOrdenesEntrada.p_UsaReglasNegocio = "S";
			paramsOrdenesEntrada.p_Ordenes = ObjOrden;
		    
			CrearOrdenes procesoOrdenes =new CrearOrdenes();

			logger.info("Inicio Crear Ordenes");
			CrearOrdenes.CrearOrdenesSalida OutOrdenes = procesoOrdenes.logica(con, paramsOrdenesEntrada);
			logger.info("Fin  Crear Ordenes");
			
			for(MensajeSalida filaMensajeRV : OutOrdenes.P_OUT_MENSAJES ) {
				
				for(int i = _Recaudacion.size()-1; i >= 0; i --){
					
					for(int j = ObjOrden.size()-1; j >= 0; j --){
						
						if(_Recaudacion.get(i).IdRecaudacionCliente.toString().equals(ObjOrden.get(j).Glosa) &&	ObjOrden.get(j).IdInterno.toString().equals(filaMensajeRV.id.toString())){
							
							if (_Recaudacion.get(i).Observacion == null) _Recaudacion.get(i).Observacion = "RV: " + ObjOrden.get(j).NemotecnicoSVS + ": " +filaMensajeRV.descripcion;
							else _Recaudacion.get(i).Observacion = _Recaudacion.get(i).Observacion + "\n" +  ObjOrden.get(j).NemotecnicoSVS + ": " + filaMensajeRV.descripcion;
														
						
							salida.P_OUT_MENSAJES.add(new MensajeSalida(_Recaudacion.get(i).IdRecaudacionCliente,filaMensajeRV.tipo, filaMensajeRV.codigo,filaMensajeRV.descripcion));
							
							
						}
					}
				}
			}
		}

		
		
		
		
		//---------------------------------------------------------
		// FONDOS MUTUOS
		//---------------------------------------------------------		
			
		if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size()>0){

			try {

				List<CalcularMontoNumeroCuota.Serie> lstMontoNumeroCuotaSerie = new ArrayList<CalcularMontoNumeroCuota.Serie>();
				CalcularMontoNumeroCuota servicioCalcularMontoNumeroCuota = new CalcularMontoNumeroCuota();
				EntradaCalcularMontoNumeroCuota entradaCalcularMontoNumeroCuota = new EntradaCalcularMontoNumeroCuota();
				CalcularMontoNumeroCuota.Serie objSerie; 
				
				for(InSolicitudesAporteRescate sol : entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate)
				{
					objSerie = new CalcularMontoNumeroCuota.Serie(); 
					objSerie.Fecha = FechaSistema.toString();
					
					/////serie.IdInterno = contadorF ;
					objSerie.IdInterno 		= sol.IdInterno;					
					objSerie.IdSerieFondo = sol.IdSerieFondo;
					objSerie.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
					objSerie.Monto = sol.Monto;
					objSerie.NumeroCuotas = sol.NumeroCuotas;
					objSerie.IdMonedaFondo = sol.IdMoneda;
					
					/////////serie.IdMonedaInversion = filaAporteCalculoMonto.IdMoneda;
					objSerie.IdMonedaInversion = (_c.Dolar.equals(sol.IdMoneda)) ?  "CLP" : sol.IdMoneda;
					objSerie.TipoSolicitud = sol.TipoSolicitud;
					objSerie.TipoFactorAjuste = _c.UF;
					objSerie.IdEmpresa = in.p_IdEmpresa;
					lstMontoNumeroCuotaSerie.add(objSerie);


				}
				
				if (!lstMontoNumeroCuotaSerie.isEmpty())
				{
					entradaCalcularMontoNumeroCuota.p_IdEmpresa = in.p_IdEmpresa;
					entradaCalcularMontoNumeroCuota.p_Series    = lstMontoNumeroCuotaSerie;
					
					logger.info("Inicio Calcular Monto Numero Cuota");
					SalidaCalcularMontoNumeroCuota salidaCalculo = servicioCalcularMontoNumeroCuota.logica(con, entradaCalcularMontoNumeroCuota);	
					logger.info("Fin Calcular Monto Numero Cuota");
					List<CalcularMontoNumeroCuota.Resultado> resultadoSeriesFFMM = salidaCalculo.P_OUT_RESULTADO;
					HashMap<Long,Object> hashResultadoSeriesFFMM = Utils.List2Hash(resultadoSeriesFFMM, "IdInterno");   
					
					
					for(InSolicitudesAporteRescate sol : entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate)
					{
						CalcularMontoNumeroCuota.Resultado infoResultadoSeriesFFMM = (CalcularMontoNumeroCuota.Resultado) hashResultadoSeriesFFMM.get(sol.IdInterno);
						sol.NumeroCuotas = infoResultadoSeriesFFMM.NumeroCuotas;
	
						if (_c.Dolar.equals(sol.IdMoneda))
						{
							sol.Monto        = infoResultadoSeriesFFMM.Monto == null ? BigDecimal.ZERO: infoResultadoSeriesFFMM.Monto;
							sol.NumeroCuotas = infoResultadoSeriesFFMM.NumeroCuotas;	
						}
						
					}
					

					//Se repasa monto de forma de pago spot
					for (InFormaPagoSpot forPag : entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot)
					{
						CalcularMontoNumeroCuota.Resultado infoResultadoSeriesFFMM = (CalcularMontoNumeroCuota.Resultado) hashResultadoSeriesFFMM.get(forPag.IdOperacion);	
						
						if (forPag.IdTipoMovimiento.equalsIgnoreCase("MOVEGR"))
						{
							forPag.MontoFormaDePago = infoResultadoSeriesFFMM.Monto == null ? BigDecimal.ZERO: infoResultadoSeriesFFMM.Monto;
						}
					}
					
				}

				
				
				
				
	
				
				entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = in.p_IdUsuario;
				entradaCrearSolicitudAporteRescateNormal.p_IdModulo = in.p_IdModulo;
				

				entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = _c.SI;
				entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = ("".equalsIgnoreCase(in.p_Canal) ? _c.CanalAgente : in.p_Canal);
				entradaCrearSolicitudAporteRescateNormal.p_NumeroDividendo = 0L;
				
				
				entradaCrearSolicitudAporteRescateNormal.p_IdOrigenProceso =  esAporteSAAPVOrigenWebService(in.p_Canal) ? ""  : "REC"; // Se indica que el origen de la operacion es Recaudacion (REC)
				entradaCrearSolicitudAporteRescateNormal.p_PermiteOperarProspectos = in.p_PermiteOperarProspectos;
			
				//Se ejecula la creacion de Solicitud Aporte Rescate Normal de las redaudaciones validas
				
				if ( entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0) {
					logger.info("Inicio Crear Solicitud Aporte Rescate Normal (Registros validos)");
					entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = in.p_Simula.trim().equalsIgnoreCase("S") ? _c.SI : _c.NO;
					SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
					logger.info("Fin Crear Solicitud Aporte Rescate Normal (Registros validos)");
					salida.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR;
					for(MensajeSalida mje : salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
							salida.P_OUT_MENSAJES.add(mje);
						}
					}
					
					if (salida.P_OUT_ERRORESCONTADOR == 0) {
						for(ResultadoSolicitud resultado : salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO) {
							SalidaSolicitudFFMM solicitud = new SalidaSolicitudFFMM();
							solicitud.FolioContrato = resultado.FolioContrato;
							solicitud.IdSolicitudAporteRescate = resultado.IdSolicitudAporteRescate;
							salida.P_OUT_SOLICITUDES_FFMM.add(solicitud);
						}
					}
				}
				
					

			}
			catch (Exception er1) //para validar errores de llamada ffmm
			{
				er1.printStackTrace();
				for(Recaudacion fila : _Recaudacion){

					if (fila.Observacion == null) fila.Observacion = "No se ingresa aporte FFMM: Error al crear Aporte FFMM";
					else fila.Observacion = fila.Observacion + "\n" +  "No se ingresa aporte FFMM: Error al crear Aporte FFMM";
				}
			}
		}
		

		
		recadacionATraspasar.clear();
		for(Recaudacion fila : _Recaudacion){
			OrigenRecaudacion oriRec = new OrigenRecaudacion();
			oriRec.IdRecaudacionCliente = fila.IdRecaudacionCliente;
			recadacionATraspasar.add(oriRec);
			
		}	
		
		if(recadacionATraspasar.size()>0){

			TraspasoCuentasBancarias.ParametrosEntradaServicio paramsTrasCtaBca_Entrada = new TraspasoCuentasBancarias.ParametrosEntradaServicio();
		    
			paramsTrasCtaBca_Entrada.p_IdEmpresa 	= in.p_IdEmpresa;
			paramsTrasCtaBca_Entrada.p_IdUsuario 	= in.p_IdUsuario;
		    paramsTrasCtaBca_Entrada.p_IdModulo 	= in.p_IdModulo;
		    paramsTrasCtaBca_Entrada.p_Opcion 		= "DIS";
		    paramsTrasCtaBca_Entrada.p_Fecha 		= FechaSistema.toString();
		    paramsTrasCtaBca_Entrada.p_Monto 		= sumaMontosDistribuido;	
		    paramsTrasCtaBca_Entrada.p_OrigenRecaudacion = recadacionATraspasar;	
		    paramsTrasCtaBca_Entrada.p_Simula 		= in.p_Simula;
		    paramsTrasCtaBca_Entrada.p_EsTraspaso	= esProvenienteDeUnaRecaudacionPorTraspaso(in);
		    
		    TraspasoCuentasBancarias procesoTrasCtaBca = new TraspasoCuentasBancarias();
		    logger.info("Inicio Traspaso Cuenta Bancarias");
			TraspasoCuentasBancarias.ParametrosSalidaServicio OutTrasCtaBca = procesoTrasCtaBca.logica(con, paramsTrasCtaBca_Entrada);
			logger.info("Fin Traspaso Cuenta Bancarias");
			salida.P_OUT_ERRORESCONTADOR += OutTrasCtaBca.P_OUT_ERRORESCONTADOR;
			
			for(MensajeSalida filaMensaje : OutTrasCtaBca.P_OUT_MENSAJES){
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,filaMensaje.tipo, filaMensaje.codigo,filaMensaje.descripcion));
			}
			
			if (salida.P_OUT_ERRORESCONTADOR == 0) {
				
				//----------------------------------------------------------
				// ACTUALIZACION DE ESTADOS FINALES
				//-----------------------------------------------------------
				
			
				List<ParamsInsertEstadoTransicion> lstEstadoTransicion2 = new ArrayList<ParamsInsertEstadoTransicion>();
				List<UpdateContratoDistribucion> lstContratoDistribucion = new ArrayList<UpdateContratoDistribucion>();
				List<Recaudacion_CambiaEstado> lstEstadoRecaudacion = new ArrayList<Recaudacion_CambiaEstado>();
				List<DetalleEmpleador_CambiaEstado> lstEstadoEmpleador = new ArrayList<DetalleEmpleador_CambiaEstado>();
				List<DetalleCliente_CambiaEstado> lstEstadoCliente = new ArrayList<DetalleCliente_CambiaEstado>();
				List<CambiaEstadoContratoSAAPVRecaudacion> lstEstadoContratoSAAPV = new ArrayList<CambiaEstadoContratoSAAPVRecaudacion>();	
				
				ParamsInsertEstadoTransicion objEstadoTransicion2;			
				UpdateContratoDistribucion objContratoDistribucion;			
				Recaudacion_CambiaEstado objEstadoRecaudacion;
				DetalleEmpleador_CambiaEstado objEstadoEmpleador;
				DetalleCliente_CambiaEstado objEstadoCliente;
				CambiaEstadoContratoSAAPVRecaudacion objEstadoContratoSAAPV;	
				
				
				for(Recaudacion fila : _Recaudacion){
					if(fila.Distribucion.size() > 0) {
						//Estado Transicion
						objEstadoTransicion2 = new ParamsInsertEstadoTransicion();				
						objEstadoTransicion2.Tipo ="Recaudacion";
						objEstadoTransicion2.IdRegistro = fila.IdRecaudacionCliente;
						objEstadoTransicion2.FechaTransicion = FechaHora;
						objEstadoTransicion2.EstadoAnterior = fila.CodigoEstado;
						if (fila.Observacion == null)
							objEstadoTransicion2.Observacion = "Distribucion Procesada";
						else 	
							objEstadoTransicion2.Observacion = fila.Observacion;
					
						objEstadoTransicion2.EstadoNuevo = "DIS";
						objEstadoTransicion2.Usuario = in.p_IdUsuario;
						lstEstadoTransicion2.add(objEstadoTransicion2);
		
		
						//Actualiza contrato distribucion
						objContratoDistribucion = new UpdateContratoDistribucion();
						objContratoDistribucion.IdRecaudacionCliente = fila.IdRecaudacionCliente;
						objContratoDistribucion.IdContratoSAAPV = fila.IdContrato;
						lstContratoDistribucion.add(objContratoDistribucion);
						
						
						//Estado Recaudacion
						objEstadoRecaudacion = new Recaudacion_CambiaEstado();				
						objEstadoRecaudacion.IdRecaudacion = fila.FolioRecaudacion;
						objEstadoRecaudacion.IdEstado = "DIS"; 
						objEstadoRecaudacion.IdEmpresa = in.p_IdEmpresa; //fila.IdEmpresa;
						lstEstadoRecaudacion.add(objEstadoRecaudacion);
						
		
						//Estado detalle empleador
						objEstadoEmpleador = new DetalleEmpleador_CambiaEstado();				
						objEstadoEmpleador.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
						objEstadoEmpleador.IdEstado = "DIS";
						objEstadoEmpleador.IdEmpresa = in.p_IdEmpresa; //fila.IdEmpresa;
						lstEstadoEmpleador.add(objEstadoEmpleador);
					
						
						//Estado detalle cliente
						objEstadoCliente = new DetalleCliente_CambiaEstado();			
						objEstadoCliente.IdRecaudacionCliente = fila.IdRecaudacionCliente;
						objEstadoCliente.IdEstado = "DIS";
						objEstadoCliente.IdEmpresa = in.p_IdEmpresa; //fila.IdEmpresa;
						lstEstadoCliente.add(objEstadoCliente);
						
		
						
						//Estado ContratoSAAPV
						if(fila.Pago != null) {	
							if(fila.Pago.equals("UNI")) {	
								objEstadoContratoSAAPV = new CambiaEstadoContratoSAAPVRecaudacion();			
								objEstadoContratoSAAPV.IdEstado = "COM";
								objEstadoContratoSAAPV.IdContratoSAAPV = fila.IdContrato;
								lstEstadoContratoSAAPV.add(objEstadoContratoSAAPV);						
							}
						}
					}			
					
						
						
				}
				
				if (lstEstadoTransicion2.size()>0)
				{
					logger.info("Inicio Actualiza Estado Transicion");
					sql.batch(con,SQL_INSERT_EstadoTransicion,lstEstadoTransicion2,ParamsInsertEstadoTransicion.class);
					
				}
				
				if (lstContratoDistribucion.size()>0)
				{
					logger.info("Inicio Actualiza Contrato Distribucion");
					sql.batch(con,SQL_UPDATE_ContratoDistribucion,lstContratoDistribucion,UpdateContratoDistribucion.class);				
				}
				
				if (lstEstadoRecaudacion.size()>0)
				{
					logger.info("Inicio Actualiza Estado Recaudacion");
					sql.batch(con,SQL_CAMBIA_ESTADO_Recaudacion,lstEstadoRecaudacion,Recaudacion_CambiaEstado.class);
					//Log
					agregarLog(con, in, FechaSistema, lstEstadoRecaudacion);
				}
				
				if (lstEstadoEmpleador.size()>0)
				{
					logger.info("Inicio Actualiza Estado Empleador");
					sql.batch(con,SQL_CAMBIA_ESTADO_Empleador,lstEstadoEmpleador,DetalleEmpleador_CambiaEstado.class);
				}
				if (lstEstadoCliente.size()>0)
				{
					logger.info("Inicio Actualiza Estado Cliente");
					sql.batch(con,SQL_CAMBIA_ESTADO_Cliente,lstEstadoCliente,DetalleCliente_CambiaEstado.class);
				}
				if (lstEstadoContratoSAAPV.size()>0)
				{
					logger.info("Inicio Actualiza Estado ContratoSAAPV");
					sql.batch(con, SQL_CAMBIA_ESTADO_ContratoSAAPV, lstEstadoContratoSAAPV,CambiaEstadoContratoSAAPVRecaudacion.class);
				}
				
				//Mesajes de Salida ok
				for(Recaudacion fila : _Recaudacion)
				{
					salida.P_OUT_MENSAJES.add(new MensajeSalida(fila.IdRecaudacionCliente,MensajeSalida.MENSAJE_OK,"PROCESO_DISTRIBUCION_COMPLETADO",valDic,con,in.p_IdEmpresa));
					
				}
			}else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(salida.P_OUT_ERRORESCONTADOR++,MensajeSalida.MENSAJE_ERROR,"PROCESO_DISTRIBUCION_CON_ERRORES",valDic,con,in.p_IdEmpresa));
			}
	

		
			
		}

		logger.info("Termino del servicio ProcesarDistribucion, tiempo:" + (System.currentTimeMillis() - init));
		
		return salida;
	}
	
	private Boolean esProvenienteDeUnaRecaudacionPorTraspaso(ParametrosEntradaServicio parametrosEntradaServicio) {
		logger.info("INICIO::ProcesarDistribucion.esProvenienteDeUnaRecaudacionPorTraspaso");
		Boolean esProvenienteDeUnaRecaudacionPorTraspaso = esOpcionRecaudacionTraspaso(parametrosEntradaServicio.p_Opcion) && noEsAporteSAAPVOrigenWebService(parametrosEntradaServicio.p_Canal);
		logger.info("FIN::ProcesarDistribucion.esProvenienteDeUnaRecaudacionPorTraspaso");
		return esProvenienteDeUnaRecaudacionPorTraspaso;
	}
	
	private Boolean esOpcionRecaudacionTraspaso(String opcion) {
		logger.info("INICIO::ProcesarDistribucion.esOpcionRecaudacionTraspaso");
		Boolean esOpcionRecaudacionTraspaso = "RECTRA".equals(opcion);
		logger.info("FIN::ProcesarDistribucion.esOpcionRecaudacionTraspaso");
		return esOpcionRecaudacionTraspaso;
	}
	
	private Boolean noEsAporteSAAPVOrigenWebService(String canal) {
		logger.info("INICIO::ProcesarDistribucion.noEsAporteSAAPVOrigenWebService");
		Boolean esAporteSAAPVOrigenWebService = !_c.CanalWEB.equals(canal) && !_c.CanalOperador.equals(canal) && !_c.CanalEjecutivo.equals(canal);
		logger.info("FIN::ProcesarDistribucion.noEsAporteSAAPVOrigenWebService");
		return esAporteSAAPVOrigenWebService;
	}
	
	private Boolean esAporteSAAPVOrigenWebService(String canal) {
		logger.info("INICIO::ProcesarDistribucion.esAporteSAAPVOrigenWebService");
		Boolean esAporteSAAPVOrigenWebService = _c.CanalWEB.equals(canal) || _c.CanalOperador.equals(canal) || _c.CanalEjecutivo.equals(canal);
		logger.info("FIN::ProcesarDistribucion.esAporteSAAPVOrigenWebService");
		return esAporteSAAPVOrigenWebService;
	}
	
	private void agregarLog(Connection con, ParametrosEntradaServicio in, String fechaSistema, List<Recaudacion_CambiaEstado> lstEstadoRecaudacion) throws Exception {
		logger.trace("INICIO::ProcesarDistribucion.agregarLog");
		long inicio = System.currentTimeMillis();
		
		InAgregarLog entradaAgregarLog = new InAgregarLog();
		
		if(!lstEstadoRecaudacion.isEmpty()) {
						
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
			log.IdCabeceraLog = 1L;
			log.IdTrabajo = idTrabajo;
			log.IdTipoLog = "INFORMATIVO";
			log.IdModulo = in.p_IdModulo;
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "DISTRIBUCION APV";
			log.NombreProceso = "Procesar Distribucion";
			entradaAgregarLog.p_Log.add(log);
			List<Long> listaIdRecaudacion = new ArrayList<>();
			
			lstEstadoRecaudacion.stream()
				.filter(recaudacion -> recaudacion.IdRecaudacion != null && recaudacion.IdRecaudacion.compareTo(0L)> 0)
				.sorted(Comparator.comparingLong(recaudacion -> recaudacion.IdRecaudacion))
				.forEach(recaudacion ->{
					if(!listaIdRecaudacion.contains(recaudacion.IdRecaudacion)) {
						String subRegistro = String.format("Folio Recaudación %s", recaudacion.IdRecaudacion.toString());
						entradaAgregarLog.agregarDetalleLogBasico(1L, idTrabajo, "IdEstado", "", "DIS", subRegistro);
						listaIdRecaudacion.add(recaudacion.IdRecaudacion);
					}
				});
			
			AgregarLog agregarLogService = new AgregarLog();
			if(!entradaAgregarLog.p_LogDetalle.isEmpty()) {
				agregarLogService.logica(con, entradaAgregarLog);
			}
		}
		
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarDistribucion.agregarLog", fin - inicio);
	}

	private List<SolicitudesFFMMAsociadas> buscarSolicitudesFFMMAsociadas(Connection con, List<Long> listaIdRecaudacionCliente) throws SQLException{
		logger.debug("INICIO::ProcesarDistribucion.buscarSolicitudesFFMMAsociadas");
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> paramsSolicitudesAsociadas = new HashMap<String,Object>();
		paramsSolicitudesAsociadas.put("IdRecaudacionCliente", listaIdRecaudacionCliente);
		
		List<SolicitudesFFMMAsociadas> listaSolicitudesAsociadas = sql.query(con, SQL_SELECT_SOLICITUDES_FFMM_ASOCIADAS, paramsSolicitudesAsociadas, SolicitudesFFMMAsociadas.class);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarDistribucion.buscarSolicitudesFFMMAsociadas, tiempo:" + (fin - inicio));
		return listaSolicitudesAsociadas;
	}
	
	private void validarSolicitudesFFMMAsociadas (Connection con, Long idEmpresa, List<Recaudacion> listaRecaudacion, List<SolicitudesFFMMAsociadas> listaSolicitudesAsociadas, ParametrosSalidaServicio salida) throws Exception {
		logger.debug("INICIO::ProcesarDistribucion.validarSolicitudesFFMMAsociadas");
		Long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashIdRecaudacionCliente = Utils.List2Hash(listaSolicitudesAsociadas,"IdRecaudacionCliente");
		if(!listaRecaudacion.isEmpty()) {
			for(int i = listaRecaudacion.size()-1; i >= 0; i --){
				Long idRecaudacionCliente = listaRecaudacion.get(i).IdRecaudacionCliente != null ? listaRecaudacion.get(i).IdRecaudacionCliente: 0L;
				SolicitudesFFMMAsociadas solicitudAsociada = (SolicitudesFFMMAsociadas) hashIdRecaudacionCliente.get(idRecaudacionCliente);
				if(solicitudAsociada != null && solicitudAsociada.Cantidad.compareTo(0L) > 0) {
					HashMap<String, String> parametrosMensaje = new HashMap<String, String>();
					String folioRecaudacion = listaRecaudacion.get(i).FolioRecaudacion != null ? listaRecaudacion.get(i).FolioRecaudacion.toString() : "";
					String rutCliente = listaRecaudacion.get(i).RutCliente != null ? listaRecaudacion.get(i).RutCliente : "";
					parametrosMensaje.put("IdRecaudacion", folioRecaudacion);
					parametrosMensaje.put("DocumentoIdentidadCiente", rutCliente);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(idRecaudacionCliente, MensajeSalida.MENSAJE_ADVERTENCIA, "RECAUDACION_CON_SOLICITUD_FFMM_ASOCIADA", parametrosMensaje, con, idEmpresa));
					listaRecaudacion.remove(i);
				}
			};
		}
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarDistribucion.validarSolicitudesFFMMAsociadas, tiempo:" + (fin - inicio));	
	}
	
	private void bloqueoDistribucion (Connection con, HashMap<String,Object> paramsDistribucion) throws SQLException {
		logger.debug("INICIO::ProcesarDistribucion.bloqueoDistribucion");
		SQL sql = new SQL();
		sql.query(con, SQL_SELECT_BLOQUEO_DISTRIBUCION, paramsDistribucion, Recaudacion.class);
		logger.debug("FIN::ProcesarDistribucion.bloqueoDistribucion");
		return;
	}
	
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"DIS\",\"p_Parametros\":[{\"IdRecaudacion\":\"217116\"},{\"IdRecaudacion\":\"217115\"},{\"IdRecaudacion\":\"217115\"}],\"p_DistribucionOrigen\":\"S\"}";
				
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				ProcesarDistribucion demo = new ProcesarDistribucion();		 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				//con.commit();
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}