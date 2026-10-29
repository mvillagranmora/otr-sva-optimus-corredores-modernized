package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Recaudacion
import cl.bcs.APV.CambiarEstadoRecaudacion;
import cl.bcs.APV.GrabarRecaudacionTraspaso.CambiarEstadoTraspaso;
import cl.bcs.tesoreria.CrearComprobante;
//Tesoreria
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearTraspasoTesoreria implements JavaCall   {
 
	/*
	 * El log
	 */	
	private final Log log = new Log("CrearTraspasoTesoreria");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	private final String SQL_CtaBco = "SELECT \"DestinoCtaBco\" FROM \"BSCL.APV.BD::APV.OrigenDestinoTransicion\" WHERE \"Operacion\" = 'DEV' AND \"IdEmpresa\" = :IdEmpresa";
	private final String SQL_CONTRATOSAAPV = "SELECT \"IdContratoSAAPV\" FROM \"BSCL.APV.BD::TRA.Traspaso\" WHERE \"IdTraspaso\" = :IdTraspaso";
	public String SQL_MODIFICA_TRASPASO = loadResourceToString("cl/bcs/APV/sql/Traspaso_Update.sql");
	public String SQL_CAMBIAR_ESTADO_TRASPASO = loadResourceToString("cl/bcs/APV/sql/CambiarEstadoTraspasoAPV.sql");
	public String SQL_SELECT_CONFIGURACION_RETENCION = loadResourceToString("cl/bcs/APV/sql/ConsultarRetencion.sql");
	
	public String SQL_PORTAFOLIO_PERSONA = "SELECT POR.\"IdPortafolio\", CLI.\"IdCliente\""
											+ " FROM \"BSCL.Portafolio.BD::POR.Portafolio\" AS POR"
											+ " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" AS CLI"
											+ "		ON CLI.\"IdCliente\" = POR.\"IdCliente\""
											+ " INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" AS SOC"
											+ "		ON SOC.\"IdCliente\" = CLI.\"IdCliente\""
											+ "	   AND SOC.\"IdPersona\" = CLI.\"IdPersona\""
											+ " WHERE SOC.\"IdPersona\" = :IdPersona AND \"IdMatrizAgrupadorAPV\" > 0 AND POR.\"IdEstado\" <> 'INA' " ;
	private final String SQL_CONSULTARPORTAFOLIOEMPRESA = "SELECT Empresa.\"IdPersona\",\"IdPortafolioDefecto\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" Empresa"
														+ " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" Cliente"
														+ " ON Cliente.\"IdSocioDeNegocio\" = Empresa.\"IdSocioDeNegocio\""
														+ " WHERE Empresa.\"IdEmpresa\" = :IdEmpresa";

	private final String SQL_CONSULTA_CUENTA_INVERSION = loadResourceToString("cl/bcs/APV/sql/ConsultarCuentaInversionClientePortafolio_APV.sql");
	private final String SQL_CONSULTA_ORDENES_RV_FFMM = loadResourceToString("cl/bcs/APV/sql/ConsultaOrdenesTraspaso.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/

	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public List<TraspasoTesoreria>	p_Parametros;

		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<TraspasoTesoreria>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;

		public List<CrearIngresoEgreso.IngresoEgreso_Entrada>		p_IngresoEgreso;
		public List<CrearIngresoEgreso.FormaDePago_Entrada> 		p_FormaDePago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			p_IngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
			p_FormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
		}		
	};

	public static class TraspasoTesoreria{
		public Long IdTraspaso;
		public Long IdPersona;
		public Long IdPersonaEntidad;
		public Long IdPersonaCliente;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Fecha;
		public Long IdBanco;
		public Long IdCuentaCorriente;
		public String IdMedioPago;
		public BigDecimal MontoTotal;
	}
	
	public static class DestinoCtaBco{
		public Long DestinoCtaBco;
	}
	
	public static class ContratoSAAPV{
		public Long IdContratoSAAPV;
	}
	
	public static class Portafolio{
		public Long IdPortafolio;
		public Long IdCliente;
	}
	
	public static class DatosEmpresa{
		public Long IdPersona;
		public Long IdPortafolioDefecto;
	}
	public static class DatosCuentaInversion{
		public Long IdEjecutivo;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
	}
	
	public static class OrdenTraspaso {
		public Long IdTraspaso;
		public Long IdOrdenTraspaso;
		public String RazonRetiro;
		public String Regimen;
		public Long IdOrden;
		public Long IdSolicitudAporteRescate;
		public BigDecimal MontoOrden;
		public BigDecimal MontoOrdenFFMM; 
		public Long IdPortafolio;
		public Long IdMatrizAgrupadorAPV;
		public Long IdRegimenTributarioMatriz;
	}
	
	public static class InformacionDetalle{
		public String Codigo;
		public String Descripcion;
	}
	
	public static class AdvertenciasDetalle{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErroresDetalle{
		public String Codigo;
		public String Descripcion;
	}
	
	public static class CuentaInversion{
		public String CodigoCuentaInversion; 
		public Long IdCliente;
		public String NombreCliente;
		public String  DocumentoIdentidad;
		public Long  IdPortafolio;
		public String  NombrePortafolio;
		public String  NombrePlan;
		public Long IdPlanTipoInversion;
		public Long  IdProductoAgrupador;
		public String  Producto;
		public Long  IdRegimenTributario;
		public String  Regimen;
		public String  NombreAgrupador;
		public String  FechaSaldo;
		public String  IdMoneda;
		public String  CodigoMoneda;
		public String  NombreMoneda;
		public BigDecimal  SaldoDisponible;
		public BigDecimal  SaldoEnGarantia;
		public BigDecimal  SaldoAPlazo;
		public BigDecimal  SaldoRetiro;
		public String  IdEstado;
	}

	public static class Retencion{
		public String CodigoRetiro;
		public Long IdPersonaRetencion;
		public Long IdRegimenTributario;
		public BigDecimal PorcentajeRetencion;
		public BigDecimal MontoTopeRetencion;
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		MensajeSalida msg = new MensajeSalida();
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		Long time = System.currentTimeMillis();
		java.sql.Date FechaSistema = new java.sql.Date(time);
		//MensajeSalida               msg             = new MensajeSalida();
		String ClientesBuscados = "";
		Long contador = 1L;
		//Arreglos
		ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
		ArrayList<CrearIngresoEgreso.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
		List<CrearComprobante.FormaDePago_Entrada> paramsFormaPagoComprobante_Entrada = new ArrayList<CrearComprobante.FormaDePago_Entrada>();
		List<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado>();
		List<CambiarEstadoTraspaso> lstTraspaso = new ArrayList<CambiarEstadoTraspaso>();
		
		Long IdCtaBco = 0L;
		HashMap<String,Object> ParamsSelectBanco= new HashMap<String,Object>();
		ParamsSelectBanco.put("IdEmpresa", in.p_IdEmpresa);
		List<DestinoCtaBco> _CtaBco = sql.query(con, SQL_CtaBco, ParamsSelectBanco, DestinoCtaBco.class);
		IdCtaBco = _CtaBco.get(0).DestinoCtaBco;
		
		Long IdContratoSAAPV = 0L;
		HashMap<String,Object> ParamsSelectContrato = new HashMap<String,Object>();
		Long contadorIds = 1L;
		Long contadorCARABO = 1L;
		
		List<CrearCargoAbono.ObjCargoAbonoAGenerar> paramsCargoAbono = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
		
		HashMap<String,Object> ParamsSelectPortafolioEmpresa = new HashMap<String,Object>();
		ParamsSelectPortafolioEmpresa.put("IdEmpresa", in.p_IdEmpresa);
		List<DatosEmpresa> _DatosEmpresa = sql.query(con, SQL_CONSULTARPORTAFOLIOEMPRESA, ParamsSelectPortafolioEmpresa, DatosEmpresa.class);

		HashMap<String,Object> ParamsSelectConfRetencion = new HashMap<String,Object>();
		List<Retencion> _ConfiguracionRetencion = sql.query(con, SQL_SELECT_CONFIGURACION_RETENCION, ParamsSelectConfRetencion, Retencion.class);
		
		String FechaIngreso = "";
		Long IdPortafolioCorredora = _DatosEmpresa.get(0).IdPortafolioDefecto;
		Long IdPersonaCorredora = _DatosEmpresa.get(0).IdPersona;
		
		for(TraspasoTesoreria fila : in.p_Parametros){
			FechaIngreso = fila.Fecha;
			HashMap<String,Object> ParamsSelectPortafolio = new HashMap<String,Object>();
			ParamsSelectPortafolio.put("IdPersona", fila.IdPersonaCliente);
			List<Portafolio> _Portafolios = sql.query(con, SQL_PORTAFOLIO_PERSONA, ParamsSelectPortafolio, Portafolio.class);
			
			if (_Portafolios.size() <= 0)
			{
				msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "Warning";
				msg.codigo = "CLIENTE_SIN_PORTAFOLIO";
				msg.descripcion = "Cliente no cuenta con Portafolio APV.";
				salida.P_OUT_MENSAJES.add(msg);
				
				return salida;
			}

			List<DatosCuentaInversion> lstCtaInversion = new ArrayList<DatosCuentaInversion>();
			DatosCuentaInversion _DatosCuentaInversion = new DatosCuentaInversion();
			_DatosCuentaInversion.IdEjecutivo = null;
			_DatosCuentaInversion.IdCliente = _Portafolios.get(0).IdCliente;
			_DatosCuentaInversion.IdPortafolio = null;
			_DatosCuentaInversion.IdMoneda = "CLP";
			lstCtaInversion.add(_DatosCuentaInversion);
			
			SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #p_CuentaInversionAConsultar"
					     + "(\"IdEjecutivo\"	 BIGINT" 
						 + ",\"IdCliente\" BIGINT"
						 + ",\"IdMoneda\" NVARCHAR(255)"
						 + ",\"IdPortafolio\" BIGINT"
						 + ")");
			
			if (lstCtaInversion.size() > 0)
			{
				//log.write("SOLICITA LIQUIDAR POR COMPROBANTE");
				sql.batch(con,
						"INSERT INTO #p_CuentaInversionAConsultar"
						+ "(\"IdEjecutivo\""
						+ ",\"IdCliente\""
						+ ",\"IdMoneda\""
						+ ",\"IdPortafolio\") "
						+ "VALUES(:IdEjecutivo"
						+ ", :IdCliente"
						+ ", :IdMoneda"
						+ ", :IdPortafolio)",
						lstCtaInversion, 
						DatosCuentaInversion.class);
			}
			/* Inicio - Obtengo registros desde cuenta de inversion*/
			HashMap<String, Object> paramsParametroCtaInv;
			paramsParametroCtaInv = new HashMap<>(); 		
	  		paramsParametroCtaInv.put("p_IdEmpresa", in.p_IdEmpresa);								
	  		paramsParametroCtaInv.put("p_IdUsuario", in.p_IdUsuario);        		
	  		paramsParametroCtaInv.put("p_IdModulo",	 in.p_IdModulo);			
	  		paramsParametroCtaInv.put("p_UsaReglasNegocio", "N");		
	  		paramsParametroCtaInv.put("p_IncluyeOtrosSaldos","N");							
	  		paramsParametroCtaInv.put("p_TipoAgrupador","PO");			
	  		paramsParametroCtaInv.put("p_CuentaInversionAConsultar", lstCtaInversion);
	        paramsParametroCtaInv.put("p_FechaDesde",FechaIngreso);
	        paramsParametroCtaInv.put("p_FechaHasta",FechaIngreso);
			
	        //Salida HashMap
			HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
			paramsOut.put("p_out_SaldoPorFechaMovimiento",CuentaInversion.class);
			paramsOut.put("p_out_InformacionContador", Long.class);
			paramsOut.put("p_out_InformacionDetalle", InformacionDetalle.class);
			paramsOut.put("p_out_AdvertenciasContador", Long.class);
			paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciasDetalle.class);
			paramsOut.put("p_out_ErroresContador", Long.class);
			paramsOut.put("p_out_ErroresDetalle", ErroresDetalle.class);
			
			HashMap<String,Object> resultSP = sql.call(con, SQL_CONSULTA_CUENTA_INVERSION, paramsParametroCtaInv, paramsOut);
			List<CuentaInversion> cuentaInversion = new ArrayList<CuentaInversion>();
			cuentaInversion = (List<CuentaInversion>)resultSP.get("p_out_SaldoPorFechaMovimiento");
			/* Fin - Obtengo registros desde cuenta de inversion*/
			
			SQL.exec(con, "DROP TABLE #p_CuentaInversionAConsultar");
			
			/* Inicio - Obtengo ordenes ffmm certificadas o ordenes rv facturadas para la creacion de comprobantes*/
			List<OrdenTraspaso> consultaOrdenTraspaso = new ArrayList<OrdenTraspaso>();
			HashMap<String,Object> ParamsSelectOrdenTraspaso = new HashMap<String,Object>();
			ParamsSelectOrdenTraspaso.put("IdTraspaso", fila.IdTraspaso);
			consultaOrdenTraspaso = sql.query(con, SQL_CONSULTA_ORDENES_RV_FFMM, ParamsSelectOrdenTraspaso, OrdenTraspaso.class);
			/* Fin - Obtengo ordenes ffmm certificadas o ordenes rv facturadas para la creacion de comprobantes*/
			
			for(CuentaInversion ciRow : cuentaInversion) {
				for(OrdenTraspaso otRow : consultaOrdenTraspaso) {
					
					if(otRow.RazonRetiro.equals("RES") && otRow.Regimen.contains("DC")) {
						//msg = new MensajeSalida();
						//msg.id = 1L;
						//msg.tipo = "OK";
						//msg.codigo = "TRASPASO_RESCATE_DC";
						//msg.descripcion = "Orden Traspaso " + (otRow.IdOrden == null ? otRow.IdSolicitudAporteRescate : otRow.IdOrden) + " de tipo Rescate Regimen DC.");
						//salida.P_OUT_MENSAJES.add(msg);
						continue;
					} 
					
					if(otRow.MontoOrden == null && otRow.MontoOrdenFFMM == null) continue;
					if(fila.IdTraspaso.compareTo(otRow.IdTraspaso) != 0) continue;
						
					BigDecimal MontoOrden = otRow.MontoOrden == null || otRow.MontoOrden.compareTo(BigDecimal.ZERO)==0 ? otRow.MontoOrdenFFMM : otRow.MontoOrden;
					BigDecimal MontoRetencion = BigDecimal.ZERO;
					Long IdPersonaRetencion = 0L;
					Long IdDetalleTraspaso = 0L;

					if(otRow.IdPortafolio.equals(ciRow.IdPortafolio) && ciRow.SaldoDisponible.compareTo(MontoOrden) >= 0) {
					//if(otRow.IdPortafolio.equals(ciRow.IdPortafolio)) {
						/*Puedo Generar Comprobante*/
						
						System.out.println("IdPortafolio : " +otRow.IdPortafolio.toString());
						System.out.println("Orden de " + (otRow.IdOrden == null ? "FFMM" : "RV"));
						System.out.println("IdOrden : " + (otRow.IdOrden == null ? otRow.IdSolicitudAporteRescate : otRow.IdOrden));
						System.out.println("Monto Disponible : " + ciRow.SaldoDisponible.toString());
						System.out.println("Monto Orden : " + MontoOrden.toString());
						
						//for(Portafolio portafolio : _Portafolios) {	
							CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvAbono = new CrearCargoAbono.ObjCargoAbonoAGenerar();
							CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvCargo = new CrearCargoAbono.ObjCargoAbonoAGenerar();
							//obtejos de tesoreria y ordenes
							CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgreso = new CrearIngresoEgreso.IngresoEgreso_Entrada(); //IngresoEgreso
							CrearIngresoEgreso.FormaDePago_Entrada FormaPagoEgreso = new CrearIngresoEgreso.FormaDePago_Entrada();
							CrearComprobante.FormaDePago_Entrada FormaPagoComprobante = new CrearComprobante.FormaDePago_Entrada();

							/*CONSULTA RETENCION
							**/
							for(Retencion retRow : _ConfiguracionRetencion){
								if (otRow.RazonRetiro.equals(retRow.CodigoRetiro)
								&& retRow.PorcentajeRetencion.compareTo(BigDecimal.ZERO)!=0
								){
									System.out.println(retRow.IdRegimenTributario == null);
									System.out.println(retRow.IdRegimenTributario == otRow.IdRegimenTributarioMatriz);
									System.out.println(retRow.IdRegimenTributario);
									System.out.println(otRow.IdMatrizAgrupadorAPV);
									if(retRow.IdRegimenTributario == null || 
									   retRow.IdRegimenTributario == otRow.IdRegimenTributarioMatriz){
											
										MontoRetencion = MontoOrden.multiply(retRow.PorcentajeRetencion);
										//MontoRetencion = MontoOrden.multiply(BigDecimal.valueOf(0.15));
										MontoOrden = MontoOrden.subtract(MontoRetencion);
										IdPersonaRetencion = retRow.IdPersonaRetencion;
										IdDetalleTraspaso = otRow.IdTraspaso;
									}
								}
							}

							//Movimiento de cargo al cliente y abono a la entidad
							CtaInvCargo.NumeroCargoAbono = contadorCARABO;
							CtaInvCargo.IdPortafolio = otRow.IdPortafolio;
							CtaInvCargo.IdMoneda = "CLP";
							CtaInvCargo.IdTipoMovimiento = "MOVCAR";
							CtaInvCargo.IdConcepto = "CARAPV";
							CtaInvCargo.Descripcion = "APV";
							CtaInvCargo.IdCondicionDeLiquidacion = "T0";
							CtaInvCargo.FechaCondicionDeLiquidacion = FechaIngreso;
							CtaInvCargo.Monto = MontoOrden;
							CtaInvCargo.IdEjecutivo = 0L;
							CtaInvCargo.IdSucursal = 0L;
							CtaInvCargo.IdTipoProducto = "";
							CtaInvCargo.IdTipoOperacion = "";
							CtaInvCargo.IdOperacion = fila.IdTraspaso;
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
							
							contadorCARABO = contadorCARABO + 1;
							
							//se crea cargo o abono para mover la cuenta inversion
							CtaInvAbono.NumeroCargoAbono = contadorCARABO;
							CtaInvAbono.IdPortafolio = IdPortafolioCorredora;
							CtaInvAbono.IdMoneda = "CLP";
							CtaInvAbono.IdTipoMovimiento = "MOVABN";
							CtaInvAbono.IdConcepto = "ABOAPV";
							CtaInvAbono.Descripcion = "APV";
							CtaInvAbono.IdCondicionDeLiquidacion = "T0";
							CtaInvAbono.FechaCondicionDeLiquidacion = FechaIngreso;
							CtaInvAbono.Monto = MontoOrden;//fila.MontoTotal;
							CtaInvAbono.IdEjecutivo = 0L;
							CtaInvAbono.IdSucursal = 0L;
							CtaInvAbono.IdTipoProducto = "";
							CtaInvAbono.IdTipoOperacion = "";
							CtaInvAbono.IdOperacion = fila.IdTraspaso;
							CtaInvAbono.FechaOperacion = "0001-01-01";
							CtaInvAbono.FechaLiquidacionOperacion = "0001-01-01";
							CtaInvAbono.FechaVencimientoOperacion = null;
							CtaInvAbono.IdMonedaOperacion = "";
							CtaInvAbono.MontoOperacion = null;
							CtaInvAbono.TipoCambioOperacion = BigDecimal.valueOf(0L);
							CtaInvAbono.IdContraparte = null;
							CtaInvAbono.IdTipoComprobante = null;
							CtaInvAbono.IdComprobante = null;
							CtaInvAbono.FolioComprobante = null;
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
							
							contadorCARABO = contadorCARABO + 1;

							//Se crea movimiento de tesoreria con los datos de recaudacion
							IngresoEgreso = new CrearIngresoEgreso.IngresoEgreso_Entrada();
							IngresoEgreso.NumeroReferencia = contadorIds;
							IngresoEgreso.AgruparEnComprobante = "N";
							IngresoEgreso.Bilateral = "N";
							IngresoEgreso.FechaComprobante = "0001-01-01";
							IngresoEgreso.FechaLiquidacion = FechaIngreso; 
							IngresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
							IngresoEgreso.FechaOperacion = fila.Fecha + " 00:00:00";
							IngresoEgreso.FechaParidad = "0001-01-01";
							IngresoEgreso.FolioCartera = 0L;
							IngresoEgreso.GeneraComprobante = "N";
							IngresoEgreso.Glosa = "";
							IngresoEgreso.IdBolsa = "";
							IngresoEgreso.IdCaja = 0L;
							IngresoEgreso.IdClasificacionTipoInstrumento = "";
							IngresoEgreso.IdComprobante = 0L;
							IngresoEgreso.IdConcepto = "TREAPV";
							IngresoEgreso.IdCondicionLiquidacion = "";
							IngresoEgreso.IdEmpleadoDigitador = 0L;
							IngresoEgreso.IdInstrumento = 0L;
							IngresoEgreso.IdMoneda = "CLP";
							IngresoEgreso.IdMonedaOperacion = "";
							IngresoEgreso.IdOperacion = fila.IdTraspaso; 
							IngresoEgreso.IdPersona = fila.IdPersona;
							IngresoEgreso.IdPersonaTitularPago = fila.IdPersona;
							IngresoEgreso.IdPortafolio = 0L;
							IngresoEgreso.IdPortafolioTitularPago = 0L;
							IngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
							IngresoEgreso.IdTipoComprobante = "APV";
							IngresoEgreso.IdTipoInstrumento = "";
							IngresoEgreso.IdTipoMovimiento = "MOVEGR";
							IngresoEgreso.IdTipoOperacion = "EGRTES";
							IngresoEgreso.IdTipoPersona = "PER";
							IngresoEgreso.IdTipoPersonaTitularPago = "PER";
							IngresoEgreso.IdTipoProducto = "";
							IngresoEgreso.IdTipoReajuste = "";
							IngresoEgreso.Monto = MontoOrden;
							IngresoEgreso.MontoOperacion = MontoOrden;
							IngresoEgreso.MueveCuentaInversion = "S";
							IngresoEgreso.SecuenciaAgrupacion = "";
							IngresoEgreso.SecuenciaRelacionada = null;
							IngresoEgreso.TitularPago = "S";
							IngresoEgreso.ValorParidad = BigDecimal.valueOf(0);	
							IngresoEgreso.EsTraspaso = "S";
							paramsIngresoEgreso.add(IngresoEgreso);
						

							//Se crea forma de pago correspondiente al movimiento de tesoreria con los datos de recaudacion
							FormaPagoEgreso = new CrearIngresoEgreso.FormaDePago_Entrada();
							FormaPagoEgreso.NumeroReferencia = contadorIds;
							FormaPagoEgreso.NumeroReferenciaFormaDePago = contadorIds;
							FormaPagoEgreso.FechaDocumentoPagoCorredora = "0001-01-01";
							FormaPagoEgreso.FechaDocumentoPagoSocio = FechaIngreso + " 00:00:00";
							FormaPagoEgreso.IdBancoSocio = fila.IdBanco; 
							FormaPagoEgreso.IdChequera = 0L;
							FormaPagoEgreso.IdCuentaBancariaCorredora = IdCtaBco; //122L
							FormaPagoEgreso.IdCuentaBancariaSocio = 0L;
							FormaPagoEgreso.IdMedioDePago = fila.IdMedioPago; 
							FormaPagoEgreso.IdModalidadDePago = "MODDEP";
							FormaPagoEgreso.IdMoneda = "CLP";
							FormaPagoEgreso.IdTarjetaBancariaCorredora = 0L;
							FormaPagoEgreso.IdTarjetaBancariaSocio = 0L;
							FormaPagoEgreso.IdTipoCuentaBancariaSocio = "";
							FormaPagoEgreso.IdTipoTarjetaBancariaSocio = "";
							FormaPagoEgreso.Monto = MontoOrden;
							FormaPagoEgreso.NombreCuentaBancariaSocio = "";
							FormaPagoEgreso.NombreTipoCuentaBancariaSocio = "";
							FormaPagoEgreso.NombreTipoTarjetaBancariaSocio = "";
							FormaPagoEgreso.NumeroCuentaBancariaSocio = "";
							FormaPagoEgreso.NumeroDocumentoCorredora = 0L;
							FormaPagoEgreso.NumeroDocumentoSocio = 0L;
							FormaPagoEgreso.NumeroSerieSocio = "";
							FormaPagoEgreso.NumeroTarjetaBancariaSocio = "";
							paramsFormaDePago.add(FormaPagoEgreso);
						
							//Se crea forma de pago correspondiente al comprobante de tesoreria con los datos de recaudacion
							FormaPagoComprobante = new CrearComprobante.FormaDePago_Entrada();
							FormaPagoComprobante.NumeroReferencia = 1L;//contadorIds;
							FormaPagoComprobante.IdMedioDePago = fila.IdMedioPago;
							FormaPagoComprobante.IdModalidadDePago = "MODDEP";
							FormaPagoComprobante.IdMoneda = "CLP";
							FormaPagoComprobante.Monto = MontoOrden;
							FormaPagoComprobante.IdCuentaBancariaCorredora = IdCtaBco; //=129L;
							FormaPagoComprobante.IdTarjetaBancariaCorredora =0L;
							FormaPagoComprobante.IdChequera =0L;
							FormaPagoComprobante.NumeroDocumentoCorredora =0L;
							FormaPagoComprobante.FechaDocumentoPagoCorredora ="0001-01-01";
							FormaPagoComprobante.IdCuentaBancariaSocio =0L;
							FormaPagoComprobante.IdBancoSocio =0L;
							FormaPagoComprobante.NombreCuentaBancariaSocio ="";
							FormaPagoComprobante.NumeroCuentaBancariaSocio ="";
							FormaPagoComprobante.IdTipoCuentaBancariaSocio ="";
							FormaPagoComprobante.NombreTipoCuentaBancariaSocio ="";
							FormaPagoComprobante.IdTarjetaBancariaSocio =0L;
							FormaPagoComprobante.IdTipoTarjetaBancariaSocio ="";
							FormaPagoComprobante.NombreTipoTarjetaBancariaSocio ="";
							FormaPagoComprobante.NumeroTarjetaBancariaSocio ="";
							FormaPagoComprobante.NumeroSerieSocio ="";
							FormaPagoComprobante.NumeroDocumentoSocio = 0L;
							FormaPagoComprobante.FechaDocumentoPagoSocio = FechaIngreso;			
							paramsFormaPagoComprobante_Entrada.add(FormaPagoComprobante);
							
							contadorIds = contadorIds + 1;

							//En caso de retencion, los movimientos respectivos
							if(MontoRetencion.compareTo(BigDecimal.ZERO)>0){
								//CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgresoRetencion = (CrearIngresoEgreso.IngresoEgreso_Entrada) IngresoEgreso.clone();

								CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvAbonoRetencion = (CrearCargoAbono.ObjCargoAbonoAGenerar) CtaInvAbono.clone();
								CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvCargoRetencion = (CrearCargoAbono.ObjCargoAbonoAGenerar) CtaInvCargo.clone();
								
								CtaInvCargoRetencion.NumeroCargoAbono = contadorCARABO; 
								//CtaInvCargoRetencion.IdTipoMovimiento = "MOVCAR";
								CtaInvCargoRetencion.IdConcepto = "EGRRET";
								CtaInvCargoRetencion.Monto = MontoRetencion;
								CtaInvCargoRetencion.IdOperacion = otRow.IdOrdenTraspaso;
								
								contadorCARABO = contadorCARABO + 1;
								
								CtaInvAbonoRetencion.NumeroCargoAbono = contadorCARABO; 
								//CtaInvCargoRetencion.IdTipoMovimiento = "MOVCAR";
								//CtaInvCargoRetencion.IdConcepto = "EGRRET";
								CtaInvAbonoRetencion.Monto = MontoRetencion;
								CtaInvAbonoRetencion.IdConcepto = "EGRRET";
								CtaInvAbonoRetencion.IdOperacion = otRow.IdOrdenTraspaso;
								
								contadorCARABO = contadorCARABO + 1;
								
								paramsCargoAbono.add(CtaInvAbonoRetencion);
								paramsCargoAbono.add(CtaInvCargoRetencion);
								//contadorIds = contadorIds + 1;
							}
							/*
							//En caso de retencion, los movimientos respectivos
							if(MontoRetencion.compareTo(BigDecimal.ZERO)>0){
								//CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgreso2 = new IngresoEgreso();
								CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgresoRetencion = (CrearIngresoEgreso.IngresoEgreso_Entrada) IngresoEgreso.clone();
								IngresoEgresoRetencion.NumeroReferencia = contadorIds; 
								IngresoEgresoRetencion.IdConcepto = "EGRRET";
								IngresoEgresoRetencion.IdPersona = IdPersonaRetencion;
								IngresoEgresoRetencion.IdPersonaTitularPago = IdPersonaRetencion;
								IngresoEgresoRetencion.Monto = MontoRetencion;
								IngresoEgresoRetencion.MontoOperacion = MontoRetencion;
								paramsIngresoEgreso.add(IngresoEgresoRetencion);
								
								CrearIngresoEgreso.FormaDePago_Entrada FormaPagoEgresoRetencion = (CrearIngresoEgreso.FormaDePago_Entrada) FormaPagoEgreso.clone() ;

								FormaPagoEgresoRetencion.NumeroReferencia = contadorIds;
								FormaPagoEgresoRetencion.NumeroReferenciaFormaDePago = contadorIds;
								FormaPagoEgresoRetencion.Monto = MontoRetencion;
								FormaPagoEgresoRetencion.IdBancoSocio = 0L; 
								paramsFormaDePago.add(FormaPagoEgresoRetencion);
								
								CrearComprobante.FormaDePago_Entrada FormaPagoComprobanteRetencion = (CrearComprobante.FormaDePago_Entrada) FormaPagoComprobante.clone() ;

								FormaPagoComprobanteRetencion.NumeroReferencia = 2L;//contadorIds;
								FormaPagoComprobanteRetencion.IdCuentaBancariaCorredora = IdCtaBco; //=129L;
								FormaPagoComprobanteRetencion.Monto = MontoRetencion;
								paramsFormaPagoComprobante_Entrada.add(FormaPagoComprobanteRetencion);
								
								contadorIds = contadorIds + 1;
							}*/
							
						//}
					}
				}
			}

			ParamsSelectContrato.put("IdTraspaso", fila.IdTraspaso);
			List<ContratoSAAPV> _IdContratoSAAPV = sql.query(con, SQL_CONTRATOSAAPV, ParamsSelectContrato, ContratoSAAPV.class);
			IdContratoSAAPV = _IdContratoSAAPV.get(0).IdContratoSAAPV;
			 /*Se llena Lista para cambiar Estado Traspaso Egreso*/
			CambiarEstadoTraspaso Traspaso = new CambiarEstadoTraspaso();
			Traspaso.IdContratoSAAPV = IdContratoSAAPV;
			Traspaso.IdEmpresa = in.p_IdEmpresa;
			Traspaso.IdTraspaso = fila.IdTraspaso;
			Traspaso.IdEstado = "COM";
			lstTraspaso.add(Traspaso);
		}
		//Se genera el Cargo y Abono
		CrearCargoAbono.CrearCargoAbonoEntrada paramsCuentaInversionCargoAbono_Entrada = new CrearCargoAbono.CrearCargoAbonoEntrada();

		paramsCuentaInversionCargoAbono_Entrada.p_IdEmpresa = in.p_IdEmpresa;
		paramsCuentaInversionCargoAbono_Entrada.p_IdUsuario = in.p_IdUsuario;
		paramsCuentaInversionCargoAbono_Entrada.p_IdModulo = in.p_IdModulo;
		paramsCuentaInversionCargoAbono_Entrada.p_simularOperacion = "N";
		paramsCuentaInversionCargoAbono_Entrada.p_UsaReglasNegocio = "S";
		paramsCuentaInversionCargoAbono_Entrada.p_FechaSistema = FechaIngreso;	
		paramsCuentaInversionCargoAbono_Entrada.p_CargoAbonoAGenerar = paramsCargoAbono;
		paramsCuentaInversionCargoAbono_Entrada.p_EsIngresoManual = "N";
		
		CrearCargoAbono ProcesoCrearCargoAbono = new CrearCargoAbono();
		CrearCargoAbono.CrearCargoAbonoSalida OutCuentaInversion = ProcesoCrearCargoAbono.logica(con, paramsCuentaInversionCargoAbono_Entrada); 

		System.out.println(paramsCargoAbono.toString());
		if(OutCuentaInversion.P_OUT_ERRORESCONTADOR == 0)
		{
			System.out.println(OutCuentaInversion.toString());
			for(MensajeSalida mensaje :OutCuentaInversion.P_OUT_MENSAJES){
				msg = new MensajeSalida();
				msg.id = mensaje.id;
				msg.tipo = mensaje.tipo;
				msg.codigo = mensaje.codigo;
				msg.descripcion = mensaje.descripcion;
				salida.P_OUT_MENSAJES.add(mensaje);
			};					
		}
		
		//Se Genera movimientos a Tesoreria
	    CrearIngresoEgreso_Entrada paramsIngresoEgreso_Entrada = new CrearIngresoEgreso_Entrada();
	    
	    paramsIngresoEgreso_Entrada.p_IdEmpresa = in.p_IdEmpresa;
	    paramsIngresoEgreso_Entrada.p_IdUsuario = in.p_IdUsuario;
	    paramsIngresoEgreso_Entrada.p_IdModulo = in.p_IdModulo;
	    paramsIngresoEgreso_Entrada.p_SimularOperacion = "N";
	    paramsIngresoEgreso_Entrada.p_UsaReglasNegocio = "S";
	    paramsIngresoEgreso_Entrada.p_EsIngresoAutomatico = "N";
	    paramsIngresoEgreso_Entrada.p_FormaDePago = paramsFormaDePago;
	    paramsIngresoEgreso_Entrada.p_IngresoEgresoGenerar = paramsIngresoEgreso;
	    
	    CrearIngresoEgreso procesoIngresoEgreso = new CrearIngresoEgreso();
		List<CrearComprobante.IngresoEgresoGenerar_Entrada> paramsComprobanteIngresoEgreso_Entrada = new ArrayList<CrearComprobante.IngresoEgresoGenerar_Entrada>();
	    CrearIngresoEgreso_Salida OutTesoreria = procesoIngresoEgreso.logica(con, paramsIngresoEgreso_Entrada); 
	    

		if (MensajeSalida.getNumeroErrores(OutTesoreria.P_OUT_MENSAJES) > 0) 
		{
			salida.P_OUT_MENSAJES.addAll(OutTesoreria.P_OUT_MENSAJES);
			return salida;
		}
		else{
			contadorIds = 1L;
			salida.P_OUT_MENSAJES.addAll(OutTesoreria.P_OUT_MENSAJES);
			
			for(CrearIngresoEgreso.IngresoEgresoGenerado_Salida outIngresoEgreso :OutTesoreria.P_OUT_INGRESOEGRESOGENERADO){

				CrearComprobante.IngresoEgresoGenerar_Entrada ComprobanteIngresoEgreso_Entrada = new CrearComprobante.IngresoEgresoGenerar_Entrada();
				ComprobanteIngresoEgreso_Entrada.NumeroReferencia = 1L;//contadorIds;//outIngresoEgreso.NumeroReferencia;
				ComprobanteIngresoEgreso_Entrada.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
				ComprobanteIngresoEgreso_Entrada.TipoComprobante = "PAREGR";
				
				for(CrearIngresoEgreso.IngresoEgreso_Entrada filaIngEgr : paramsIngresoEgreso){
					if(filaIngEgr.NumeroReferencia.toString().equals(outIngresoEgreso.NumeroReferencia.toString())
							&& filaIngEgr.IdConcepto.equals("EGRRET")){
						ComprobanteIngresoEgreso_Entrada.NumeroReferencia = 2L;
						
					}
				}
				paramsComprobanteIngresoEgreso_Entrada.add(ComprobanteIngresoEgreso_Entrada);
				contadorIds = contadorIds + 1;
			};
		}
		
		
	    //COMPROBANTE
		CrearComprobante.CrearComprobante_Entrada paramsComprobante_Entrada = new CrearComprobante.CrearComprobante_Entrada();
		List<CrearComprobante.ComprobanteGenerar_Entrada> paramsComprobanteGenerar_Entrada = new ArrayList<CrearComprobante.ComprobanteGenerar_Entrada>();
		paramsComprobante_Entrada.p_IdEmpresa = in.p_IdEmpresa;
		paramsComprobante_Entrada.p_IdUsuario = in.p_IdUsuario;
		paramsComprobante_Entrada.p_IdModulo = in.p_IdModulo;
		paramsComprobante_Entrada.p_UsaReglasNegocio = "S";
		paramsComprobante_Entrada.p_FechaMovimiento = FechaIngreso;
		paramsComprobante_Entrada.p_IdCaja = 1L;
		paramsComprobante_Entrada.p_GenerarComprobanteDesdeMovimientos = "S";
		paramsComprobante_Entrada.p_ComprobanteGenerar = paramsComprobanteGenerar_Entrada;
		paramsComprobante_Entrada.p_IngresoEgresoGenerar = paramsComprobanteIngresoEgreso_Entrada;
		paramsComprobante_Entrada.p_FormaDePago = paramsFormaPagoComprobante_Entrada;
		
		CrearComprobante ProcesoCrearComprobante = new CrearComprobante();
	    CrearComprobante.CrearComprobante_Salida OutComprobante = ProcesoCrearComprobante.logica(con, paramsComprobante_Entrada); 
	    
	    List<LiquidarComprobante.ComprobanteLiquidar_Entrada> paramsComprobanteLiquidar_Entrada = new ArrayList<LiquidarComprobante.ComprobanteLiquidar_Entrada>();

	    if (MensajeSalida.getNumeroErrores(OutComprobante.P_OUT_MENSAJES) > 0) 
		{
			for (MensajeSalida filaMsg : OutComprobante.P_OUT_MENSAJES) {
				msg = new MensajeSalida();
				msg.id = (long) contadorIds;
				msg.tipo = filaMsg.tipo;
				msg.codigo = filaMsg.codigo;
				msg.descripcion = filaMsg.descripcion;
				salida.P_OUT_MENSAJES.add(msg);	
			}
			return salida;
		}else{
			for (MensajeSalida filaMsg : OutComprobante.P_OUT_MENSAJES) {
				msg = new MensajeSalida();
				msg.id = (long) contadorIds;
				msg.tipo = filaMsg.tipo;
				msg.codigo = filaMsg.codigo;
				msg.descripcion = filaMsg.descripcion;
				salida.P_OUT_MENSAJES.add(msg);	
			}
			for(int i = 0; i < OutComprobante.P_OUT_RESULTADOPROCESO.size(); i ++){
				if(OutComprobante.P_OUT_RESULTADOPROCESO.get(i).Campo.equals("IdComprobante")){
					LiquidarComprobante.ComprobanteLiquidar_Entrada ComprobanteLiquidar_Entrada = new LiquidarComprobante.ComprobanteLiquidar_Entrada();
					ComprobanteLiquidar_Entrada.IdComprobante = OutComprobante.P_OUT_RESULTADOPROCESO.get(i).Id;
					ComprobanteLiquidar_Entrada.IdTipoMovimiento = "MOVEGR";
					ComprobanteLiquidar_Entrada.AfectaCuentaInversion = "COR";
					paramsComprobanteLiquidar_Entrada.add(ComprobanteLiquidar_Entrada);
				}
			}
				
		}					
	  	//CAMBIO ESTADOS
  		/*
	    int[] result = sql.batch(con,SQL_CAMBIAR_ESTADO_TRASPASO,lstTraspaso, CambiarEstadoTraspaso.class);
		if(result != null && result.length > 0 && result[0] == 1){
		    msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = "OK";
			msg.codigo = "CAMBIAR_ESTADO_TRASPASO";
			msg.descripcion = "Cambio de Estado de Traspaso exitoso.";
			salida.P_OUT_MENSAJES.add(msg);
		} */   

		return salida;
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getUATConnection()) {
			try {
				CrearTraspasoTesoreria demo = new CrearTraspasoTesoreria();	

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdTraspaso\":64,\"IdPersona\":25807,\"IdPersonaCliente\":26391,\"IdPersonaEntidad\":25807,\"IdPortafolio\":0,\"Fecha\":\"2018-07-27\",\"IdBanco\":12,\"IdCuentaCorriente\":\"566667\",\"IdMedioPago\":\"BDE\",\"MontoTotal\":540000},{\"IdTraspaso\":65,\"IdPersona\":25807,\"IdPersonaCliente\":26391,\"IdPersonaEntidad\":25807,\"IdPortafolio\":0,\"Fecha\":\"2018-07-27\",\"IdBanco\":12,\"IdCuentaCorriente\":\"566667\",\"IdMedioPago\":\"BDE\",\"MontoTotal\":270000}]}";

				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":23,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdTraspaso\":122,\"IdPersona\":1765331,\"IdPersonaCliente\":1774553,\"IdPersonaEntidad\":1765331,\"IdPortafolio\":0,\"Fecha\":\"2018-06-05\",\"IdBanco\":8,\"IdCuentaCorriente\":\"111122223\",\"IdMedioPago\":\"TRA\",\"MontoTotal\":337000000}]}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":431,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdTraspaso\":349,\"IdPersona\":183610,\"IdPersonaCliente\":219650,\"IdPersonaEntidad\":183610,\"IdPortafolio\":0,\"Fecha\":\"2019-01-04\",\"IdBanco\":580,\"IdCuentaCorriente\":\"20406080\",\"IdMedioPago\":\"BDE\",\"MontoTotal\":1000}]}";
				
				
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.commit();
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