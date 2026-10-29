package cl.bcs.APV;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.Date;
//import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Tesoreria
import cl.bcs.APV.ConsultarCustodiaPortafolioAPV;
import cl.bcs.APV.ConsultarCustodiaPortafolioAPV.SalidaConsultarCustodiaPortafolioAPV;
import cl.bcs.APV.ConsultarCustodiaPortafolioAPV.Custodia;
import cl.bcs.APV.CrearRecaudacionDeposito.Recaudacion;
import cl.bcs.APV.DevolucionPagoRezagos.IdPortafolioDefecto;
import cl.bcs.APV.TraspasoCuentasBancarias.OrigenDestinoTransicion;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.ordenes.CalcularMontoYCobrosOrden;
import cl.bcs.ordenes.CrearOrdenes;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.Instrumento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.CrearCliente.AdvertenciaDetalleCall;
import cl.bcs.socionegocio.cli.CrearCliente.ErrorDetalleCall;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class EjecutarBonificacion implements JavaCall   {

	/*
	 * El log
	 */	
	private final Log log = new Log("EjecutarBonificacion");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_INSERT_ANOTACION = loadResourceToString("cl/bcs/APV/sql/Anotacion_Insert.sql");
	public String SQL_SELECT_OrigenDestinoTransicion = loadResourceToString("cl/bcs/APV/sql/ConsultaOrigenDestinoTransicion.sql");
	public String SQL_SELECT_ConsultarBonificacion = loadResourceToString("cl/bcs/APV/sql/ConsultarBonificacionAEjecutar.sql");
	public String SQL_CALL_CrearPortafolio = loadResourceToString("cl/bcs/APV/sql/Crear_Portafolio_APV_Call.sql");
	public String SQL_UPDATE_CAMBIO_ESTADO = loadResourceToString("cl/bcs/APV/sql/Bonificacion_cambioEstado.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	public String SQL_UPDATE_Portafolio = "UPDATE \"BSCL.Portafolio.BD::POR.Portafolio\" SET \"CodigoPortafolio\" = 'BF' WHERE \"IdPortafolio\" =:IdPortafolio";
	private final String SQL_PortafolioEmpresa = "SELECT \"IdPortafolioDefecto\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" Empresa"
			+ " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" Cliente"
			+ " ON Cliente.\"IdSocioDeNegocio\" = Empresa.\"IdSocioDeNegocio\""
			+ " WHERE Empresa.\"IdEmpresa\" = :IdEmpresa";


	private final String SQL_Moneda_Fondo = "SELECT \"IdMoneda\" FROM \"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo\" WHERE \"IdSerieFondo\" = :IdSerieFondo";
	//private final String SQL_Moneda_Fondo = "SELECT \"IdMoneda\" FROM \"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo\" WHERE \"IdSerieFondo\" = :IdSerieFondo";

	/********************
	 * Clases IN/OUT	*
	 ********************/

	public static class ParametrosBonificacion{

		public Long IdBonificacion;
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
		
	}

	public static class IdMonedaFondo{

		public String IdMoneda;
	}

	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long									p_IdEmpresa;
		public	Long									p_IdUsuario;
		public 	String									p_IdModulo;
		public  List<ParametrosBonificacion>			p_Parametros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}		
	};

	public static class Bonificacion{

		public Long 		IdInterno;
		public Long 		IdBonificacion;
		public Long 		AnoAhorro;
		public Long 		AnoProceso;
		public BigDecimal 	MontoPagadoNeto;
		public BigDecimal 	MontoPagadoReajustado;
		public BigDecimal 	MontoCalculado;
		public BigDecimal 	MontoBonificacionAfiliadoNeto;
		public BigDecimal 	MontoDiferencia;
		//public String 		SeDevuelveDiferencia;
		public Long 		IdPersona;
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public Long 		IdMatrizAgrupadorAPV;
		public Long 		IdRegimenTributario;
		public Long 		IdPortafolioBonificacion;

		public String		IdPropositoPortafolio;
		public String		NombrePlan;
		public String		CodigoAgrupador;
		public String 		IdPerfilComercialBaseOrigen;

		public String 		TipoAhorro;
		public BigDecimal	MontoAfiliadoNeto;
		public BigDecimal	MontoReajustadoPagadoAfiliado;

		public Long 		IdTraspaso;
		//public String 		IdEstado;

		public Bonificacion(){
			IdInterno = 0L;
			IdTraspaso = 0L;
		}		
	}

	public static class BonificacionUpdate{

		public String		IdEstado;
		public Long 		IdBonificacion;
		public Long			IdEmpresa;
	}
	
	
	public static class UpdatePortafolio{
		public Long 		IdPortafolio;
		
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
	 * Metodos Basicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();

		//===========================================================//
		// DECLARACION VARIABLES                                     //
		//===========================================================//
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		Long contador = 1L;
		Long IdCtaBco = 0L;
		Long IdBco = 0L;
		Long IdPortafolioCorredora = 0L;
		BigDecimal sumaMontosDiferencia = BigDecimal.valueOf(0);

		Long contadorFFMM = 0L;
		Long contadorOrden = 1L;
		CrearRecaudacionDeposito CrearRecaudacionDeposito = new CrearRecaudacionDeposito();
		CrearRecaudacionCliente CrearRecaudacionCliente = new CrearRecaudacionCliente();
		List<CrearRecaudacionDeposito.Recaudacion> ListParamsRecaudacion = new ArrayList<CrearRecaudacionDeposito.Recaudacion>();	
		List<CrearRecaudacionCliente.Cliente> ListParamsCliente = new ArrayList<CrearRecaudacionCliente.Cliente>();
		//List<ApoResFFMM> ObjAporteRescateFFMM = new ArrayList<ApoResFFMM>();
		EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
		List<SeriesFFMM> ObjSeriesFFMM = new ArrayList<SeriesFFMM>();
		List<CrearOrdenes.ObjOrdenesEntrada> ObjOrden = new ArrayList<CrearOrdenes.ObjOrdenesEntrada>();
		List<CalcularMontoYCobrosOrden.Orden_Entrada> ObjCalculoMontoOrden = new ArrayList<CalcularMontoYCobrosOrden.Orden_Entrada>();
		List<BonificacionUpdate> ListBon_update = new ArrayList<BonificacionUpdate>();
		
		//===========================================================//
		// OBTENCION DE VARIABLES INICIALES                         //
		//===========================================================//
		HashMap<String,Object> ParamsSELECTOrigenDestinoTransicion = new HashMap<String,Object>();
		ParamsSELECTOrigenDestinoTransicion.put("IdEmpresa", in.p_IdEmpresa);
		ParamsSELECTOrigenDestinoTransicion.put("Operacion", "BON");
		//Se obtiene id banco BON para destinar el movimiento de banco
		List<OrigenDestinoTransicion> _Transicion = sql.query(con, SQL_SELECT_OrigenDestinoTransicion, ParamsSELECTOrigenDestinoTransicion, OrigenDestinoTransicion.class);
		for (OrigenDestinoTransicion fila : _Transicion){
			if (fila.OrigenDestino.equals("DESTINO")) {
				IdCtaBco = fila.IdCuentaBanco;
				IdBco = fila.IdBanco;
			}
		}
		//Se obtiene id banco BON para destinar el movimiento de banco
		ArrayList<Long> IdsBonificacion = new ArrayList<Long>();
		HashMap<String,Object> parametrosConsultaBonificacion = new HashMap<String,Object>();
		for(ParametrosBonificacion fila : in.p_Parametros){
			IdsBonificacion.add(fila.IdBonificacion);
		}
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		parametrosConsultaBonificacion.put("IdsBonificacion", IdsBonificacion);
		List<Bonificacion> ListaBonificacion = sql.query(con, SQL_SELECT_ConsultarBonificacion, parametrosConsultaBonificacion, Bonificacion.class);

		HashMap<String,Object> ParamsSelectPortafolio = new HashMap<String,Object>();
		ParamsSelectPortafolio.put("IdEmpresa", in.p_IdEmpresa);
		List<IdPortafolioDefecto> _IdPortafolioDefecto = sql.query(con, SQL_PortafolioEmpresa, ParamsSelectPortafolio, IdPortafolioDefecto.class);

		Long contadorCtaInv = 1L;
		IdPortafolioCorredora = _IdPortafolioDefecto.get(0).IdPortafolioDefecto;
		//===========================================================//
		// CICLO DE PROCESO                                          //
		//===========================================================//
		Long contadorId = 0L;
		for(Bonificacion fila :ListaBonificacion){

			List<CrearOrdenes.ObjOrdenesEntrada> ListOrden = new ArrayList<CrearOrdenes.ObjOrdenesEntrada>();
			contadorId = contadorId+1;
			fila.IdInterno = contadorId;
			//***************************//
			//   CUSTODIA	             //
			//***************************//
			
			ConsultarCustodiaPortafolioAPV ConsultarCustodiaPortafolioAPV = new ConsultarCustodiaPortafolioAPV();
			
			ConsultarCustodiaPortafolioAPV.EntradaConsultarCustodiaPortafolioAPV paramsCUSTIn1 = new ConsultarCustodiaPortafolioAPV.EntradaConsultarCustodiaPortafolioAPV();
			
			
			paramsCUSTIn1.p_IdEmpresa= in.p_IdEmpresa;
			paramsCUSTIn1.p_IdUsuario=  in.p_IdUsuario;
			paramsCUSTIn1.p_IdModulo= in.p_IdModulo;

			//paramsCUSTIn.put("p_IdCliente"						, 123570L);//ListaBonificacion.get(idCli).IdCliente);
			paramsCUSTIn1.p_IdCliente= fila.IdCliente;
			paramsCUSTIn1.p_IdPortafolio=  fila.IdPortafolio;
			paramsCUSTIn1.p_IdClasificacionTipoInstrumento= "";
			paramsCUSTIn1.p_IdTipoInstrumento= "";
			paramsCUSTIn1.p_IdInstrumento= 0L;
			paramsCUSTIn1.p_IdBoveda= "";
			paramsCUSTIn1.p_AgrupaPorBoveda= "N";
			paramsCUSTIn1.p_AgrupaPorRelacion= "N";
			paramsCUSTIn1.p_FechaConsulta= FechaSistema;
			paramsCUSTIn1.p_TipoFechaConsulta="LIQ";
			paramsCUSTIn1.p_IdRegimen= 0L;

			ConsultarCustodiaPortafolioAPV.SalidaConsultarCustodiaPortafolioAPV OutValSAAPV = ConsultarCustodiaPortafolioAPV.logica(con, paramsCUSTIn1); 
						
			//RESULTADOCUSTODIA
			SalidaConsultarCustodiaPortafolioAPV rpt = OutValSAAPV;
			
			List<Custodia> resultadoCustodia = new ArrayList<ConsultarCustodiaPortafolioAPV.Custodia>();
			resultadoCustodia = rpt.P_OUT_CUSTODIA;
			
			
			for(Custodia filaresultadoCustodia : resultadoCustodia){
				log.write("RESULTADO CUSTODIA : "+filaresultadoCustodia.Nemotecnico);
				log.write("RESULTADO CUSTODIA : "+filaresultadoCustodia.MontoPPPCustodia);
			}

			BigDecimal	TotalAPagar =BigDecimal.ZERO;
			BigDecimal	totalFFM = (BigDecimal) rpt.P_OUT_TOTALFFMM;
			for(Custodia filaresultadoCustodia : resultadoCustodia){
				if(filaresultadoCustodia.MontoPPPCustodia.compareTo(BigDecimal.ZERO)!=0){
					TotalAPagar = TotalAPagar.add(filaresultadoCustodia.MontoPPPCustodia);	
				}
			}

				//***************************//
				//   PORTAFOLIO BF           //
				//***************************//
				//Valida si ya tiene uno, sino lo crea
				if(fila.IdPortafolioBonificacion == 0){
					
					HashMap<String,Object> paramsPorta = new HashMap<String,Object>();
					paramsPorta.put("IdEmpresa", in.p_IdEmpresa);
					paramsPorta.put("IdUsuario", in.p_IdUsuario);
					paramsPorta.put("IdModulo", in.p_IdModulo);
					paramsPorta.put("PropositoPortafolio", fila.IdPropositoPortafolio);
					paramsPorta.put("IdCliente", fila.IdCliente);
					paramsPorta.put("IdPerfilComercial", fila.IdPerfilComercialBaseOrigen);
					paramsPorta.put("CodigoPortafolio", "BF");
					paramsPorta.put("CodigoRegimen", "BF");
					paramsPorta.put("NombrePlan", fila.NombrePlan);
					paramsPorta.put("CodigoAgrupador", fila.CodigoAgrupador);	
						
					//Salida HashMap
					HashMap<String,Class>  paramsPortaOut = new HashMap<String,Class>();
					paramsPortaOut.put("p_out_RefId", Long.class);
					paramsPortaOut.put("p_out_AdvertenciasContador", Long.class);
					paramsPortaOut.put("p_out_ErroresContador", Long.class);
					paramsPortaOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
					paramsPortaOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
					
					HashMap<String,Object> resultSP = sql.call(con, SQL_CALL_CrearPortafolio, paramsPorta, paramsPortaOut);

					if(((Long) resultSP.get("p_out_ErroresContador")).equals(new Long(0))){
						fila.IdPortafolioBonificacion = (Long)resultSP.get("p_out_RefId");
						//Se updatea codigoPortafolio a BF
						UpdatePortafolio UpdPort = new UpdatePortafolio();
						List<UpdatePortafolio> ListUpdPort = new ArrayList<UpdatePortafolio>();
						UpdPort.IdPortafolio= fila.IdPortafolioBonificacion;
						ListUpdPort.add(UpdPort);
						sql.batch(con,SQL_UPDATE_Portafolio,ListUpdPort,UpdatePortafolio.class);
						MensajeSalida msg = new MensajeSalida();
						msg.id = fila.IdInterno;
						msg.tipo = "INFORMACION";
						msg.codigo = "CREACION_PORTAFOLIO_BF_EXITOSO";
						msg.descripcion = "CREACION_PORTAFOLIO_BF_EXITOSO";
						salida.P_OUT_MENSAJES.add(msg);
						
					}else{
						for(ErrorDetalleCall errorDetalle : (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle")){
							/*
							msg = new MensajeSalida();
							msg.id = IdDetalleTraspaso.get(contador);
							msg.tipo = "ERROR";
							msg.codigo = errorDetalle.Codigo;
							msg.descripcion = errorDetalle.Descripcion;
							salida.P_OUT_MENSAJES.add(msg);*/
						}
						for(AdvertenciaDetalleCall advertenciaDetalle : (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle")){	
							/*msg = new MensajeSalida();
							msg.id = IdDetalleTraspaso.get(contador);
							msg.tipo = "ADVERTENCIA";
							msg.codigo = advertenciaDetalle.Codigo;
							msg.descripcion = advertenciaDetalle.Descripcion;
							salida.P_OUT_MENSAJES.add(msg);*/
						}
					}
					if(fila.IdPortafolioBonificacion != 0)
					{/*
					    msg = new MensajeSalida();
						msg.id = IdDetalleTraspaso.get(contador);
						msg.tipo = "OK";
						msg.codigo = "CREACION_PORTAFOLIO_OK";
						msg.descripcion = "Creacion de portafolio exitosa";
						salida.P_OUT_MENSAJES.add(msg);*/
					}

				}

				if(resultadoCustodia.size()==0){
					MensajeSalida msg = new MensajeSalida();
					msg.id = fila.IdInterno;
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "CLIENTE_NO_TIENE_CUSTODIA";
					msg.descripcion = "Cliente no tiene custodia";
					salida.P_OUT_MENSAJES.add(msg);
					
				}else {

					for(int i = resultadoCustodia.size()-1; i >= 0; i --){
						if(resultadoCustodia.get(i).IdMonedaInstrReajuste.equals("USD")){
							resultadoCustodia.remove(i);
						}
					}
				
				}
				//***************************//
				//  ORDENES RV Y FFMM        //
				//***************************//
				for(Custodia filaresultadoCustodia : resultadoCustodia){
					HashMap<String,Object> params = new HashMap<String,Object>();
					params.clear();
					params.put("IdEmpresa",  in.p_IdEmpresa);
					params.put("IdSerieFondo",  filaresultadoCustodia.Nemotecnico);
					params.put("IdRegimenTributario", "BF");
					
					List<RegimenSerieDeFondo> regimenesSeries = sql.query(con, SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class);
					HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
					
					RegimenSerieDeFondo r = (RegimenSerieDeFondo) hseriesRegimen.get(filaresultadoCustodia.Nemotecnico + "_" +"BF");
					
					if (r!= null) {
						
						if (filaresultadoCustodia.IdTipoInstrumento.equals("FFM")){
							BigDecimal montoApagar = filaresultadoCustodia.MontoPPPCustodia;
							BigDecimal montoApagar2 = montoApagar.multiply(BigDecimal.valueOf(100L));
							BigDecimal montoApagar4 = montoApagar2.divide(TotalAPagar,RoundingMode.HALF_DOWN);
							
							BigDecimal montoCalc1 = fila.MontoCalculado;
							BigDecimal montoCalc2 = montoCalc1.multiply(montoApagar4);
							BigDecimal montoApagar3 = montoCalc2.divide(BigDecimal.valueOf(100L),RoundingMode.HALF_DOWN);
							
							HashMap<String,Object> ParamsSelectMonedaFondo = new HashMap<String,Object>();
							ParamsSelectMonedaFondo.put("IdSerieFondo", filaresultadoCustodia.Nemotecnico);
							List<IdMonedaFondo> _IdMonedaFondo = sql.query(con, SQL_Moneda_Fondo, ParamsSelectMonedaFondo, IdMonedaFondo.class);
	
							filaresultadoCustodia.IdMonedaInstrReajuste = _IdMonedaFondo.get(0).IdMoneda;
							InSolicitudesAporteRescate AporteRescateFFMM = new InSolicitudesAporteRescate();  
	
							contadorFFMM++;
							AporteRescateFFMM.IdInterno = contadorFFMM; //filaDistribucionContrato.IdDistribucionInversion;
							AporteRescateFFMM.IdCliente=fila.IdCliente;
							AporteRescateFFMM.IdPortafolio=fila.IdPortafolioBonificacion;
							AporteRescateFFMM.TipoSolicitud="APO";
							AporteRescateFFMM.IdSerieFondo=filaresultadoCustodia.Nemotecnico;
							AporteRescateFFMM.CalculoPorMontoCuota="M";
							AporteRescateFFMM.EsPorMontoLiquido="N";
							AporteRescateFFMM.Monto=montoApagar3;
							AporteRescateFFMM.NumeroCuotas=BigDecimal.valueOf(0); //BigDecimal.valueOf(11000);
							AporteRescateFFMM.IdMoneda=filaresultadoCustodia.IdMonedaInstrReajuste;
							AporteRescateFFMM.IdMedioDePago="CIN";
							AporteRescateFFMM.IdPreferenciaPagoPortafolio=fila.IdPortafolioBonificacion;
							AporteRescateFFMM.Visado="N";
							AporteRescateFFMM.SolicitudBatch="N";
							AporteRescateFFMM.OrigenOperacion="M";
							AporteRescateFFMM.FechaOperacion=FechaSistema.toString();
							AporteRescateFFMM.IdRegimenTributario="BF";
							AporteRescateFFMM.Glosa=fila.IdBonificacion.toString();
							AporteRescateFFMM.IndicadorMLT="N";
							AporteRescateFFMM.IdSerieFondoDestino="";
							AporteRescateFFMM.IdEmisorDestino="";
							AporteRescateFFMM.IdAutorizadoOrigen=0L;
							AporteRescateFFMM.Porcentaje=BigDecimal.valueOf(0);
							AporteRescateFFMM.FolioContrato=0L;
							AporteRescateFFMM.IngresoAutomatico = "S";
							log.write("DISTRIBUCION PARA EL FONDO : "+filaresultadoCustodia.Nemotecnico);
							
							entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(AporteRescateFFMM);
							
						}
						else {
	
							if(filaresultadoCustodia.MontoPPPCustodia.compareTo(BigDecimal.ZERO)!=0) {

								BigDecimal montoApagar = filaresultadoCustodia.MontoPPPCustodia;
								BigDecimal montoApagar2 = montoApagar.multiply(BigDecimal.valueOf(100L));
								BigDecimal montoApagar4 = montoApagar2.divide(TotalAPagar,RoundingMode.HALF_UP);
								
								BigDecimal montoCalc1 = fila.MontoCalculado;
								BigDecimal montoCalc2 = montoCalc1.multiply(montoApagar4);
								BigDecimal montoApagar3 = montoCalc2.divide(BigDecimal.valueOf(100L),RoundingMode.HALF_UP);
								

								CrearOrdenes.ObjOrdenesEntrada Orden = new CrearOrdenes.ObjOrdenesEntrada();

								List<Instrumento> ObjInstrumento = new ArrayList<Instrumento>();
								Instrumento instru = new Instrumento();
								
								instru.IdEmpresa = in.p_IdEmpresa;
								instru.IdInstrumento = filaresultadoCustodia.IdInstrumento;
								instru.IdBolsa = "BCS";
								instru.Fecha = FechaSistema;
								instru.IdCondicionDeLiquidacion = "T0";
								ObjInstrumento.add(instru);
							
								ConsultarInstrumentoPrecioBolsa consultarInstrumentoPrecioBolsa = new ConsultarInstrumentoPrecioBolsa();
								
								ConsultarInstrumentoPrecioBolsa_Entrada entradaConsultarInstrumentoPrecioBolsa = new ConsultarInstrumentoPrecioBolsa_Entrada();
								entradaConsultarInstrumentoPrecioBolsa.p_IdEmpresa = in.p_IdEmpresa;
								entradaConsultarInstrumentoPrecioBolsa.p_IdUsuario = in.p_IdUsuario;
								entradaConsultarInstrumentoPrecioBolsa.p_Instrumentos = ObjInstrumento;
								
								
								ConsultarInstrumentoPrecioBolsa_Salida resultadoInstrumento = consultarInstrumentoPrecioBolsa.logica(con, entradaConsultarInstrumentoPrecioBolsa );

								CalcularMontoYCobrosOrden.Orden_Entrada CalculoMontoOrdenes = new CalcularMontoYCobrosOrden.Orden_Entrada();
								for(DatosInstrumentosPrecioBolsa_Salida filaResultadoInstrumento: resultadoInstrumento.P_OUT_INSTRUMENTO){
									CalculoMontoOrdenes.PrecioOrden = filaResultadoInstrumento.PrecioCierre;
								}
								
								CalculoMontoOrdenes.IdInterno = contadorOrden;
								CalculoMontoOrdenes.CantidadOrden=BigDecimal.ZERO;
								CalculoMontoOrdenes.EsFungible="N";
								CalculoMontoOrdenes.IdBolsa="BCS";
								CalculoMontoOrdenes.IdCorredorCorresponsal=null;
								CalculoMontoOrdenes.IdInstrumento=filaresultadoCustodia.IdInstrumento;
								CalculoMontoOrdenes.IdInstrumentoSubyacente=0L;
								CalculoMontoOrdenes.IdMonedaLiquidacionOrden=filaresultadoCustodia.IdMonedaInstrReajuste;
								CalculoMontoOrdenes.IdPortafolio=fila.IdPortafolioBonificacion;
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
								CalculoMontoOrdenes.MontoTotal=montoApagar3;
								CalculoMontoOrdenes.EsPorMonto="S";
								CalculoMontoOrdenes.TasaReporte=BigDecimal.ZERO;
								CalculoMontoOrdenes.TasaReportado=BigDecimal.ZERO;
								CalculoMontoOrdenes.PlazoReporte=0L;
								CalculoMontoOrdenes.BaseTasa=0L;
								ObjCalculoMontoOrden.add(CalculoMontoOrdenes);
								Orden.FechaRecepcion = "0001-01-01 00:00:00";
								Orden.FechaVigencia = "2018-05-09";
								Orden.FechaEmision = "0001-01-01";
								Orden.FechaOperacion = "0001-01-01";
								Orden.IdCondicionDeLiquidacion = "T0"; 
								Orden.IdTipoOperacion = "COMACN";
								Orden.IdTipoVigencia = "SH";
								Orden.Glosa =fila.IdBonificacion.toString();
								Orden.Cantidad = BigDecimal.valueOf(0L);
								Orden.MontoNeto = montoApagar3;
								Orden.ValorRescate = BigDecimal.valueOf(0L);
								Orden.IdBoveda = "DCV";
								Orden.IdFormaCustodia = "NOM";
								Orden.IdPorcentajeVisibilidad = 1L;
								Orden.PrecioLimite = BigDecimal.valueOf(10000L);
								Orden.EsFacturaInmediata = "N";
								Orden.IdTipoPrecio = "M";
								Orden.IdPortafolio = fila.IdPortafolioBonificacion;
								Orden.IdTipoCanalIngreso = "AGE";
								Orden.IdTipoReajuste = "";
								Orden.IdEmisor = filaresultadoCustodia.Nemotecnico;
								Orden.IdBolsa = "BCS";
								Orden.EspecificaBolsa = "S";
								Orden.IdMonedaLiquidacionOrden = "CLP";
								Orden.TipoCambio = null;
								Orden.IdInstrumento = filaresultadoCustodia.IdInstrumento;
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
								Orden.IdPreferenciaPagoPortafolio = fila.IdPortafolioBonificacion;
								Orden.BaseTasa = 0L;
								Orden.TipoInteres = "";
								Orden.IdMecanismoCaptacion = "OM";
								
								ObjOrden.add(Orden);
			
								contadorOrden++;
							}
							
						}
					}
				}
				
				Recaudacion paramRecaudacion = new Recaudacion();
				paramRecaudacion.IdInterno = contadorId;
				paramRecaudacion.IdRecaudacion = 0L;
				paramRecaudacion.IdEmpresa = in.p_IdEmpresa;
				paramRecaudacion.CodigoArchivo = "";
				paramRecaudacion.FolioCartola = 0L;
				paramRecaudacion.LineaCartola = 0L;
				paramRecaudacion.Sucursal = "0"; 
				paramRecaudacion.FechaDeposito = FechaSistema;
				paramRecaudacion.IdCuentaBanco = IdCtaBco;
				paramRecaudacion.CuentaCorriente = "";
				paramRecaudacion.MontoDeposito = fila.MontoBonificacionAfiliadoNeto;
				paramRecaudacion.ConceptoRecaudacion = "";
				paramRecaudacion.Origen = "BONIF";
				paramRecaudacion.Observacion = "";
				paramRecaudacion.NumeroDocumento = 0L;
				paramRecaudacion.IdTipoDeposito = "TRA";
				paramRecaudacion.IdBanco = IdBco;
				paramRecaudacion.IdRegimenTributario = 0L;
				paramRecaudacion.IdCarga = 0L;
				paramRecaudacion.IdEstado = "ING";
				ListParamsRecaudacion.add(paramRecaudacion);
		
				CrearRecaudacionCliente.Cliente paramCliente = new CrearRecaudacionCliente.Cliente();
				
				paramCliente.IdInterno = contadorId;
				paramCliente.IdRecaudacionCliente = 0L;
				paramCliente.IdRecaudacion = 0L;
				paramCliente.IdRecaudacionEmpleador = 0L;
				paramCliente.IdEmpresa = in.p_IdEmpresa;
				paramCliente.IdCliente = fila.IdCliente;
				paramCliente.FechaIngreso = FechaSistema;
				paramCliente.TipoAfiliado = "IND";
				paramCliente.IdRegimenTriburario = "BF";
				paramCliente.MontoDepositoConvenido = BigDecimal.valueOf(0L);
				paramCliente.MontoDepositoAPV = fila.MontoBonificacionAfiliadoNeto;
				paramCliente.Origen = "BONIF";
				paramCliente.IdEstado = "VIG";
				ListParamsCliente.add(paramCliente);
		
				sumaMontosDiferencia = sumaMontosDiferencia.add(fila.MontoDiferencia);
					
		}

		//Si no hay recaudacion, significa que no hay bonificacion para nadie.
		if(ListParamsRecaudacion.size()==0) return salida;
		
		try {
			//***************************//
			//CREA RECAUDACION DEPOSITO  //
			//***************************//
			/*borrar consultar custodia
			 */

			CrearRecaudacionDeposito.ParametrosEntradaServicio ObjinRec = new CrearRecaudacionDeposito.ParametrosEntradaServicio();		
			ObjinRec.p_IdEmpresa = in.p_IdEmpresa;
			ObjinRec.p_IdUsuario = in.p_IdUsuario;
			ObjinRec.p_IdModulo = "APV"; 
			ObjinRec.p_Tipo = "I";
			ObjinRec.p_Origen = "BONIF";
			ObjinRec.p_Parametros = ListParamsRecaudacion;
			
			CrearRecaudacionDeposito.ParametrosSalidaServicio ObjOutDep = CrearRecaudacionDeposito.logica(con, ObjinRec);
			//salida.P_OUT_MENSAJES.addAll(ObjOutDep.P_OUT_MENSAJES);
			
			salida.P_OUT_ERRORESCONTADOR += ObjOutDep.P_OUT_ERRORESCONTADOR;

			for(MensajeSalida fila :ObjOutDep.P_OUT_MENSAJES){
				if(!fila.codigo.equals("UI_CREACION_DEPOSITO_OK")){ 

					MensajeSalida msgREC = new MensajeSalida();
					msgREC.id = fila.id;
					msgREC.tipo = fila.tipo;
					msgREC.codigo = fila.codigo;
					msgREC.descripcion = fila.descripcion;
					salida.P_OUT_MENSAJES.add(msgREC);	
				}
				
			}
			System.out.println(ObjOutDep.toString());

			for(int idRec = 0; idRec < ObjOutDep.P_OUT_RESULTADO.size(); idRec ++){
				for(int idCli = 0; idCli < ListParamsCliente.size(); idCli ++){
					if(ListParamsCliente.get(idCli).IdInterno.equals(ObjOutDep.P_OUT_RESULTADO.get(idRec).IdInterno)){
						ListParamsCliente.get(idCli).IdRecaudacion = ObjOutDep.P_OUT_RESULTADO.get(idRec).IdRecaudacion;						
					}
				}
			}
			try { 
				//***************************//
				//CREA RECAUDACION CLIENTE   //
				//***************************//
				CrearRecaudacionCliente.ParametrosEntradaServicio ObjinCli = new CrearRecaudacionCliente.ParametrosEntradaServicio();		
				ObjinCli.p_IdEmpresa = in.p_IdEmpresa;
				ObjinCli.p_IdUsuario = in.p_IdUsuario;
				ObjinCli.p_IdModulo = "APV";
				ObjinCli.p_Tipo = "I";
				ObjinCli.p_Origen = "CARGA";
				ObjinCli.p_Parametros = ListParamsCliente;

				CrearRecaudacionCliente.ParametrosSalidaServicio ObjOutCli = CrearRecaudacionCliente.logica(con, ObjinCli);
				//salida.P_OUT_MENSAJES.addAll(ObjOutCli.P_OUT_MENSAJES);
				System.out.println(ObjOutCli.toString());
				List<CrearCargoAbono.ObjCargoAbonoAGenerar> paramsCargoAbono = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
				for(int idRec = 0; idRec < ObjOutDep.P_OUT_RESULTADO.size(); idRec ++){
					for(int idCli = 0; idCli < ListParamsCliente.size(); idCli ++){
						if(ListParamsCliente.get(idCli).IdInterno.equals(ObjOutDep.P_OUT_RESULTADO.get(idRec).IdInterno)){
							CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvAbono = new CrearCargoAbono.ObjCargoAbonoAGenerar();			 //CargoAbono
							CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvCargo = new CrearCargoAbono.ObjCargoAbonoAGenerar();			 //CargoAbono


							//***************************//
							//   Params CTA INV          //
							//***************************//
							//se crea cargo o abono para mover la cuenta inversion
							CtaInvAbono.NumeroCargoAbono = contadorCtaInv;
							CtaInvAbono.IdPortafolio = ListaBonificacion.get(idCli).IdPortafolioBonificacion;
							CtaInvAbono.IdMoneda = "CLP";
							CtaInvAbono.IdTipoMovimiento = "MOVABN";
							CtaInvAbono.IdConcepto = "ABOBON";
							CtaInvAbono.Descripcion = "APV";
							CtaInvAbono.IdCondicionDeLiquidacion = "T0";
							CtaInvAbono.FechaCondicionDeLiquidacion = FechaSistema.toString();
							CtaInvAbono.Monto = ListaBonificacion.get(idCli).MontoCalculado;
							CtaInvAbono.IdEjecutivo = 0L;
							CtaInvAbono.IdSucursal = 0L;
							CtaInvAbono.IdTipoProducto = "";
							CtaInvAbono.IdTipoOperacion = "";
							CtaInvAbono.IdOperacion = ListaBonificacion.get(idCli).IdBonificacion;
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

							contadorCtaInv++;
							CtaInvCargo.NumeroCargoAbono = contadorCtaInv;
							CtaInvCargo.IdPortafolio = IdPortafolioCorredora;
							CtaInvCargo.IdMoneda = "CLP";
							CtaInvCargo.IdTipoMovimiento = "MOVCAR";
							CtaInvCargo.IdConcepto = "CARBON";
							CtaInvCargo.Descripcion = "APV";
							CtaInvCargo.IdCondicionDeLiquidacion = "T0";
							CtaInvCargo.FechaCondicionDeLiquidacion = FechaSistema.toString();
							CtaInvCargo.Monto = ListaBonificacion.get(idCli).MontoCalculado;
							CtaInvCargo.IdEjecutivo = 0L;
							CtaInvCargo.IdSucursal = 0L;
							CtaInvCargo.IdTipoProducto = "";
							CtaInvCargo.IdTipoOperacion = "";
							CtaInvCargo.IdOperacion = ListaBonificacion.get(idCli).IdBonificacion;
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
							contadorCtaInv++;
							paramsCargoAbono.add(CtaInvCargo);


							BonificacionUpdate Bon_update = new BonificacionUpdate();
							
							Bon_update.IdBonificacion   	= ListaBonificacion.get(idCli).IdBonificacion;
							Bon_update.IdEmpresa			= in.p_IdEmpresa;
							Bon_update.IdEstado				= "EJE";
							ListBon_update.add(Bon_update);
						}
					}
				}

			    //CUENTA INVERSION
				    
					CrearCargoAbono.CrearCargoAbonoEntrada paramsCuentaInversionCargoAbono_Entrada = new CrearCargoAbono.CrearCargoAbonoEntrada();

					paramsCuentaInversionCargoAbono_Entrada.p_IdEmpresa = in.p_IdEmpresa;
					paramsCuentaInversionCargoAbono_Entrada.p_IdUsuario = in.p_IdUsuario;
					paramsCuentaInversionCargoAbono_Entrada.p_IdModulo = in.p_IdModulo;
					paramsCuentaInversionCargoAbono_Entrada.p_simularOperacion = "N";
					paramsCuentaInversionCargoAbono_Entrada.p_UsaReglasNegocio = "S";
					paramsCuentaInversionCargoAbono_Entrada.p_FechaSistema = FechaSistema.toString();
					paramsCuentaInversionCargoAbono_Entrada.p_CargoAbonoAGenerar = paramsCargoAbono;
					paramsCuentaInversionCargoAbono_Entrada.p_EsIngresoManual = "N";
					
					CrearCargoAbono ProcesoCrearCargoAbono = new CrearCargoAbono();
					CrearCargoAbono.CrearCargoAbonoSalida OutCuentaInversion = ProcesoCrearCargoAbono.logica(con, paramsCuentaInversionCargoAbono_Entrada); 
					salida.P_OUT_ERRORESCONTADOR += OutCuentaInversion.P_OUT_ERRORESCONTADOR;
					

	 				System.out.println(paramsCargoAbono.toString());
					for(MensajeSalida filaResultadoCtaInv : OutCuentaInversion.P_OUT_MENSAJES){
						for(int i = ListaBonificacion.size()-1; i >= 0; i --){
							for(int j = paramsCargoAbono.size()-1; j >= 0; j --){
								if(paramsCargoAbono.get(j).IdOperacion.toString().equals(ListaBonificacion.get(i).IdBonificacion.toString()) &&
										paramsCargoAbono.get(j).NumeroCargoAbono.toString().equals(filaResultadoCtaInv.id.toString())){

											MensajeSalida msg = new MensajeSalida();
											msg.id = ListaBonificacion.get(i).IdInterno;
											msg.tipo = filaResultadoCtaInv.tipo;
											msg.codigo = filaResultadoCtaInv.codigo;
											msg.descripcion = filaResultadoCtaInv.descripcion;
								}
								
							}
						}
					}
					HashMap<String,Object> paramsFFMMIn = new HashMap<>();

					HashMap<String,Object> paramsSeriesFFMMIn = new HashMap<>();
					HashMap<String,Class> paramsSeriesFFMMOut = new HashMap<>();

					/*ORDENES*/
					if (ObjOrden.size()>0){
						
						//CalculoMontoyCobroOrden
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

			 			try {
							CrearOrdenes.CrearOrdenesSalida OutOrdenes = procesoOrdenes.logica(con, paramsOrdenesEntrada);
							salida.P_OUT_MENSAJES.addAll(OutOrdenes.P_OUT_MENSAJES);
							salida.P_OUT_ERRORESCONTADOR += OutOrdenes.P_OUT_ERRORESCONTADOR;
							
							/*
							for(MensajeSalida filaResultadoFFMM : OutOrdenes.P_OUT_MENSAJES){
								log.write("RESULTADO FFMM : "+filaResultadoFFMM.IdSolicitudAporteRescate);
								MensajeSalida msg1 = new MensajeSalida();
								msg1.id = filaResultadoFFMM.IdInterno;
								msg1.tipo = "INFORMACION";
								msg1.codigo = "INFORMACION";
								msg1.descripcion = "Se crea aporte FFMM No. "+filaResultadoFFMM.IdSolicitudAporteRescate;
								salida.P_OUT_MENSAJES.add(msg1);	
								
							}*/
			 				System.out.println(OutOrdenes.toString());
			 			}
			 			catch(Exception er) {
							er.printStackTrace();	
							//con.rollback();
						}

					}
					if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size()>0){

					//if (true==false){
						

							try {
								entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = in.p_IdEmpresa;
								entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = in.p_IdUsuario;
								entradaCrearSolicitudAporteRescateNormal.p_IdModulo = in.p_IdModulo;
								entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = "N";
								entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = "S";
								entradaCrearSolicitudAporteRescateNormal.p_IdOrigenProceso = "BONIF";
								InSolicitudesAporteRescate AporteRescateFFMM = new InSolicitudesAporteRescate();
								CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
								SalidaCrearSolicitudAporteRescateNormal rpt = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
								
								log.write("FIN APORTE");

								System.out.println("FIN APORTE");


								//RESULTADOFFM
								List<ResultadoSolicitud> resultadoFFMM = rpt.P_OUT_RESULTADO;

								for(ResultadoSolicitud filaResultadoFFMM : resultadoFFMM){
									log.write("RESULTADO FFMM : "+filaResultadoFFMM.IdSolicitudAporteRescate);
								}
								
								salida.P_OUT_ERRORESCONTADOR += rpt.P_OUT_ERRORESCONTADOR;

								for(MensajeSalida mje : rpt.P_OUT_MENSAJES){
									for(int i = ListaBonificacion.size()-1; i >= 0; i --){
										for(int j = entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size()-1; j >= 0; j --){
											if(entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.get(j).Glosa.toString().equals(ListaBonificacion.get(i).IdBonificacion.toString())/* &&
													entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.get(j).IdInterno.toString().equals(mje.id.toString())*/){

														MensajeSalida msg = new MensajeSalida();
														msg.id = ListaBonificacion.get(i).IdInterno;
														msg.tipo = mje.tipo;
														msg.codigo = mje.codigo;
														msg.descripcion = mje.descripcion;
														salida.P_OUT_MENSAJES.add(msg);	
											}
											
										}
									}
								}
							}
							catch (Exception er1) //para validar errores de llamada ffmm
							{
								er1.printStackTrace();
								try 
								{
									// Elimina tablas temporales locales
									SQL.exec(con, "DROP TABLE #TablaApvAporteRescate");			

									 
								} 
								catch (Exception er) 
								{
								}
							}
							finally 
							{
								try 
								{
									// Elimina tablas temporales locales
				 					SQL.exec(con, "DROP TABLE #TablaApvAporteRescate");				
									 
								} 
								catch (Exception er) 
								{
								}

							}

						}

					
					int[] resUpdate = sql.batch(con,SQL_UPDATE_CAMBIO_ESTADO,ListBon_update,BonificacionUpdate.class);
					
			} catch (Exception er) {
					er.printStackTrace();
					//con.rollback();
					MensajeSalida msg1 = new MensajeSalida();
					msg1.id = (long) contador;
					msg1.tipo = "ERROR";
					msg1.codigo = "ERROR_CREAR_CLIENTE_APV";
					msg1.descripcion = "Error al crear cliente";
					salida.P_OUT_MENSAJES.add(msg1);	
				}
			

		} catch (Exception er) {
			er.printStackTrace();
			con.rollback();
			MensajeSalida msg1 = new MensajeSalida();
			msg1.id = (long) contador;
			msg1.tipo = "ERROR";
			msg1.codigo = "ERROR_CREAR_RECAUDACION_APV";
			msg1.descripcion = "Error al crear Recaudacion";
			salida.P_OUT_MENSAJES.add(msg1);	
		}

/*
		con.commit();
		if(sumaMontosDiferencia.compareTo(BigDecimal.ONE) >= 0){
		    TraspasoCuentasBancarias procesoTrasCtaBca = new TraspasoCuentasBancarias();
			TraspasoCuentasBancarias.ParametrosEntradaServicio paramsTrasCtaBca_Entrada = new TraspasoCuentasBancarias.ParametrosEntradaServicio();
			paramsTrasCtaBca_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsTrasCtaBca_Entrada.p_IdUsuario = in.p_IdUsuario;
		    paramsTrasCtaBca_Entrada.p_IdModulo = in.p_IdModulo;
		    paramsTrasCtaBca_Entrada.p_Opcion = "DBF";
		    paramsTrasCtaBca_Entrada.p_Fecha = FechaSistema.toString();
		    paramsTrasCtaBca_Entrada.p_Monto = sumaMontosDiferencia;		    

			TraspasoCuentasBancarias.ParametrosSalidaServicio OutTrasCtaBca_Entrada = procesoTrasCtaBca.logica(con, paramsTrasCtaBca_Entrada); 
		    con.commit();
			//bonificacionADevolver
			DevolucionBF_EntidadRecaudadora procesoDevolver = new DevolucionBF_EntidadRecaudadora();
			DevolucionBF_EntidadRecaudadora.ParametrosEntradaServicio paramsDevolver_Entrada = new DevolucionBF_EntidadRecaudadora.ParametrosEntradaServicio();
			paramsDevolver_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsDevolver_Entrada.p_IdUsuario = in.p_IdUsuario;
			paramsDevolver_Entrada.p_IdModulo = in.p_IdModulo;
			paramsDevolver_Entrada.p_Parametros = bonificacionADevolver;    

			DevolucionBF_EntidadRecaudadora.ParametrosSalidaServicio OutTrasCtaBca_Entrada = procesoDevolver.logica(con, paramsDevolver_Entrada);
			
		}
				/*
		MensajeSalida msg= new MensajeSalida();
		msg.id = contador;
		msg.tipo = MensajeSalida.MENSAJE_OK;
		msg.codigo = "OK";
		msg.descripcion = "Proceso ejecutado correctamente";
		salida.P_OUT_MENSAJES.add(msg);*/
		return salida;
	}
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getSURAConnection()){ // .getQAConnection()) {
			try {
				EjecutarBonificacion demo = new EjecutarBonificacion();	

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":150,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdBonificacion\":68}]}";		
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdBonificacion\":11}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
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